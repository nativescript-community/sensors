import { Trace, Utils } from '@nativescript/core';
import lazy from '@nativescript/core/utils/lazy';
import { CLog, CLogTypes } from './index.common';
let sensorManager: com.nativescript.sensors.SensorManager = null;

export * from './index.common';

export const SENSORS = [
    'linearAcceleration',
    'magnetometer',
    'accelerometer',
    'gyroscope',
    'rotation',
    'orientation',
    'motion',
    'barometer',
    'proximity',
    'stationary',
    'stepCounter',
    'stepDetector',
    'temperature',
    'heartBeat',
    'heartRate',
    'humidity',
    'light',
    'gravity',
    'heading'
] as const;
export type SensorsTuple = typeof SENSORS; // readonly ['hearts', 'diamonds', 'spades', 'clubs']
export type SensorType = SensorsTuple[number]; // union type

export function getAltitude(pressure: number, airportPressure: number) {
    return android.hardware.SensorManager.getAltitude(airportPressure, pressure);
}
function getSensorManager() {
    if (sensorManager == null) {
        const context: android.content.Context = Utils.android.getApplicationContext();
        sensorManager = new com.nativescript.sensors.SensorManager(context);
    }
    return sensorManager;
}

function hasSensor(type: SensorType) {
    const sensorManager = getSensorManager();
    if (sensorManager == null) {
        return false;
    }
    return sensorManager.hasSensor(type);
}

export function setThreadPriority(priority: number) {
    const sensorManager = getSensorManager();
    if (sensorManager != null) {
        sensorManager.threadPriority = priority;
    }
}

export function setThreadName(name: string) {
    const sensorManager = getSensorManager();
    if (sensorManager != null) {
        sensorManager.threadName = name;
    }
}
export function setUseSeparatedThread(value: boolean) {
    const sensorManager = getSensorManager();
    if (sensorManager != null) {
        sensorManager.useSeparatedThread = value;
    }
}

export function isSensorAvailable(sensor: SensorType) {
    return hasSensor(sensor);
}

export const ACCURACY_HIGH = lazy(() => android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
export const ACCURACY_MEDIUM = lazy(() => android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
export const ACCURACY_LOW = lazy(() => android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW);
export const ACCURACY_UNCALIBRATED = lazy(() => android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE);

function androidHashMapToJson(map: java.util.HashMap<any, any>) {
    const result = {};
    let value, pair, key;

    const it = map.entrySet().iterator();
    while (it.hasNext()) {
        pair = it.next() as java.util.Map.Entry<any, any>;
        value = pair.getValue();
        key = pair.getKey();
        if (value instanceof java.util.HashMap) {
            result[key] = androidHashMapToJson(value);
        } else if (value instanceof java.lang.Number) {
            result[key] = value.doubleValue();
        } else if (value && value.hasOwnProperty('length')) {
            result[key] = Array.from({ length: value.length }).map((v, i) => value[i]);
        } else {
            result[key] = value;
        }
    }

    return result;
}
type SensorListener = {
    [key in SensorType]?: {
        jsListeners: Function[];
        androidListeners: com.nativescript.sensors.SensorManager.SensorManagerEventListener[];
    };
};
const listeners: SensorListener = {};
export function startListeningForSensor(
    sensors: SensorType | SensorType[],
    listener: Function | com.nativescript.sensors.SensorManager.SensorManagerEventListener,
    updateInterval: number,
    maxReportLatency = 0,
    options?: {
        headingFilter?: number;
        headingTrueNorth?: boolean;
        headingDistanceFilter?: number;
        headingDistanceAccuracy?: number;
    }
) {
    if (!Array.isArray(sensors)) {
        sensors = [sensors];
    }
    return Promise.all(
        sensors.map((sensor) => {
            if (!isSensorAvailable(sensor)) {
                throw new Error(`sensor not available ${sensor}`);
            }
            // let newSensorDelay: number;
            // if (updateInterval < 20) newSensorDelay = android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
            // else if (updateInterval < 70) newSensorDelay = android.hardware.SensorManager.SENSOR_DELAY_GAME;
            // else if (updateInterval < 200) newSensorDelay = android.hardware.SensorManager.SENSOR_DELAY_UI;
            // else newSensorDelay = android.hardware.SensorManager.SENSOR_DELAY_NORMAL;
            let androidListener;
            if (typeof listener === 'object') {
                //native interface
                androidListener = listener;
            } else {
                androidListener = new com.nativescript.sensors.SensorManager.SensorManagerEventListener({
                    onEventData(datastring, data, event) {
                        // console.log('onEventData', data);
                        listener(JSON.parse(datastring), event);
                    }
                });
            }
            if (Trace.isEnabled()) {
                CLog(CLogTypes.info, 'addListenerForSensor', sensor, listener, androidListener, updateInterval, maxReportLatency);
            }
            // console.log('addListenerForSensor', sensor, listener, androidListener, updateInterval, maxReportLatency);
            if (sensor === 'heading') {
                if (options && 'headingFilter' in options) {
                    getSensorManager().setHeadingFilter(options.headingFilter);
                }
            }
            const result = getSensorManager().addListenerForSensor(sensor, androidListener, updateInterval, maxReportLatency);
            if (result) {
                listeners[sensor] = listeners[sensor] || { jsListeners: [], androidListeners: [] };

                listeners[sensor].jsListeners.push(listener as any);
                listeners[sensor].androidListeners.push(androidListener);
                // console.log('stored listener', listener, androidListener, listeners[sensor]);
            }
            return Promise.resolve(result);
        })
    );
}

export function stopListeningForSensor(sensors: SensorType | SensorType[], listener: Function | com.nativescript.sensors.SensorManager.SensorManagerEventListener) {
    if (!Array.isArray(sensors)) {
        sensors = [sensors];
    }
    return Promise.all(
        sensors.map((sensor) => {
            if (Trace.isEnabled()) {
                CLog(CLogTypes.info, 'stopListeningForSensor', sensor, listeners[sensor]);
            }
            if (sensor && listeners[sensor]) {
                const index = listeners[sensor].jsListeners.indexOf(listener as any);
                if (index !== -1) {
                    const androidListener = listeners[sensor].androidListeners[index];
                    if (Trace.isEnabled()) {
                        CLog(CLogTypes.info, 'removeListenerForSensor', sensor, index, androidListener);
                    }
                    if (androidListener) {
                        getSensorManager().removeListenerForSensor(sensor, androidListener);
                    }
                    listeners[sensor].jsListeners.splice(index, 1);
                    listeners[sensor].androidListeners.splice(index, 1);
                    if (!listeners[sensor] || listeners[sensor].jsListeners.length === 0) {
                        delete listeners[sensor];
                    }
                }
            }
        })
    );
}

export function getAllavailableSensors() {
    return SENSORS.filter(hasSensor);
}

export function flush() {
    return getSensorManager().flush();
}

export function estimateMagneticField(lat: number, lon: number, altitude: number, time: number = Date.now()) {
    const result = new android.hardware.GeomagneticField(lat, lon, altitude, time);

    return result as {
        getDeclination(): number;
        getInclination(): number;
        getHorizontalStrength(): number;
        getFieldStrength(): number;
        getX(): number;
        getY(): number;
        getZ(): number;
    };
}
