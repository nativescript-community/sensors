import { ad } from 'tns-core-modules/utils/utils';
import lazy from 'tns-core-modules/utils/lazy';
let sensorManager: com.nativescript.sensors.SensorManager = null;

export const SENSORS = [
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
    'gravity'
] as const;
export type SensorsTuple = typeof SENSORS; // readonly ['hearts', 'diamonds', 'spades', 'clubs']
export type SensorType = SensorsTuple[number]; // union type

function getSensorManager() {
    if (sensorManager == null) {
        const context: android.content.Context = ad.getApplicationContext();
        sensorManager = new com.nativescript.sensors.SensorManager(context, false);
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
//
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
        } else {
            result[key] = value;
        }
    }

    // map.keySet().forEach(k => {

    // });
    return result;
}
type SensorListener = {
    [key in SensorType]?: {
        jsListeners: Function[];
        androidListeners: com.nativescript.sensors.SensorManager.SensorManagerEventListener[];
    }
};
const listeners: SensorListener = {};
export function startListeningForSensor(sensor: SensorType, listener: Function, updateInterval: number, maxReportLatency = 0) {
    if (!isSensorAvailable(sensor)) {
        throw new Error(`sensor not available ${sensor}`);
    }
    // let newSensorDelay: number;
    // if (updateInterval < 20) newSensorDelay = android.hardware.SensorManager.SENSOR_DELAY_FASTEST;
    // else if (updateInterval < 70) newSensorDelay = android.hardware.SensorManager.SENSOR_DELAY_GAME;
    // else if (updateInterval < 200) newSensorDelay = android.hardware.SensorManager.SENSOR_DELAY_UI;
    // else newSensorDelay = android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

    const androidListener = new com.nativescript.sensors.SensorManager.SensorManagerEventListener({
        onEventData(data, event) {
            listener(androidHashMapToJson(data), event);
        }
    });
    console.log('startListeningForSensor', sensor, updateInterval, maxReportLatency, androidListener);
    const result = getSensorManager().addListenerForSensor(sensor, androidListener, updateInterval, maxReportLatency);
    if (result) {
        listeners[sensor] = listeners[sensor] || { jsListeners: [], androidListeners: [] };

        listeners[sensor].jsListeners.push(listener);
        listeners[sensor].androidListeners.push(androidListener);
    }
    return Promise.resolve(result);
}

export function stopListeningForSensor(sensor: SensorType, listener: Function) {
    if (sensor && listeners[sensor]) {
        const index = listeners[sensor].jsListeners.indexOf(listener);
        if (index !== -1) {
            const androidListener = listeners[sensor].androidListeners[index];
            console.log('stopSensor', sensor, androidListener);
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
}

export function getAllavailableSensors() {
    return SENSORS.filter(hasSensor);
}

export function flush() {
    return getSensorManager().flush();
}
