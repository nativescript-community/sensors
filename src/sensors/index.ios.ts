export const SENSORS = [
    'linearAcceleration',
    'magnetometer',
    'accelerometer',
    'gyroscope',
    'rotation',
    'orientation',
    'motion',
    'barometer',
    'stepCounter',
    'floorCounter',
    'distance',
    'pace',
    'cadence',
    'heading'
] as const;
export type SensorsTuple = typeof SENSORS; // readonly ['hearts', 'diamonds', 'spades', 'clubs']
export type SensorType = SensorsTuple[number]; // union type
export * from './index.common';

import { request } from '@nativescript-community/perms';
import { Trace } from '@nativescript/core';
import { CLog, CLogTypes } from './index.common';

let headingDelegate: LocationHeadingListenerImpl;
let headingManager: CLLocationManager;
@NativeClass
class LocationHeadingListenerImpl extends NSObject implements CLLocationManagerDelegate {
    public static ObjCProtocols = [CLLocationManagerDelegate];
    public static new() {
        const listener = LocationHeadingListenerImpl.new();
        return listener;
    }
    public locationManagerDidUpdateHeading?(manager: CLLocationManager, newHeading: CLHeading) {
        if (Trace.isEnabled()) {
            CLog(CLogTypes.info, `locationManagerDidUpdateHeading(${newHeading})`);
        }
        const event = {
            timestamp: newHeading.timestamp,
            heading: newHeading.magneticHeading,
            accuracy: newHeading.headingAccuracy,
            trueHeading: newHeading.trueHeading,
            x: newHeading.x,
            y: newHeading.y,
            z: newHeading.z
        };
        fireEvent('barometer', event);
    }

    public locationManagerShouldDisplayHeadingCalibration(manager: CLLocationManager) {
        return false;
    }
}

const uptime = NSProcessInfo.processInfo.systemUptime;
// Now since 1970
const nowTimeIntervalSince1970 = NSDate.date().timeIntervalSince1970;
// Voila our offset
const bootTimestamp = nowTimeIntervalSince1970 - uptime;

const STANDARD_GRAVITY = 9.80665;

let motionQueue: NSOperationQueue;
let motionManager: CMMotionManager;
function getMotionManager() {
    if (!motionManager) {
        motionManager = CMMotionManager.new();
    }
    return motionManager;
}
let altitudeManager: CMAltimeter;
function getAltitudeManager() {
    if (!altitudeManager) {
        altitudeManager = CMAltimeter.new();
    }
    return altitudeManager;
}
let pedometer: CMPedometer;
function getPedometer() {
    if (!pedometer) {
        pedometer = CMPedometer.new();
    }
    return pedometer;
}

export const ACCURACY_HIGH = CMMagneticFieldCalibrationAccuracy.High;
export const ACCURACY_MEDIUM = CMMagneticFieldCalibrationAccuracy.Medium;
export const ACCURACY_LOW = CMMagneticFieldCalibrationAccuracy.Low;
export const ACCURACY_UNCALIBRATED = CMMagneticFieldCalibrationAccuracy.Uncalibrated;

export function isSensorAvailable(sensor: SensorType) {
    switch (sensor) {
        case 'accelerometer':
            return getMotionManager().accelerometerAvailable;
        case 'barometer':
            return CMAltimeter.isRelativeAltitudeAvailable();
        case 'orientation':
        case 'motion':
        case 'rotation':
            return getMotionManager().deviceMotionAvailable;
        case 'magnetometer':
            return getMotionManager().magnetometerAvailable;
        case 'gyroscope':
            return getMotionManager().gyroAvailable;
        case 'stepCounter':
            return CMPedometer.isStepCountingAvailable();
        case 'heading':
            return CLLocationManager.headingAvailable();
    }
    return false;
}

type SensorListener = { [key in SensorType]?: Function[] };
const listeners: SensorListener = {};

function fireEvent(event: string, data: Object) {
    listeners[event].forEach((l) => l(data, event));
}

function onDeviceMotion(data: CMDeviceMotion, error: NSError) {
    if (!data) {
        return; // for now. Need error handling
    }
    const currentAttitude = data.attitude;
    const realTimestamp = Math.round((bootTimestamp + data.timestamp) * 1000);
    if (listeners['orientation']) {
        const event = {
            accuracy: data.magneticField.accuracy,
            timestamp: realTimestamp,
            yaw: currentAttitude.yaw,
            pitch: currentAttitude.pitch,
            roll: currentAttitude.roll
        };
        fireEvent('orientation', event);
    }
    if (listeners['rotation']) {
        const quat = currentAttitude.quaternion;
        const rotation = currentAttitude.rotationMatrix;
        const event = {
            timestamp: realTimestamp,
            accuracy: data.magneticField.accuracy,
            quaternion: [quat.x, quat.y, quat.z, quat.w],
            rotationMatrix: [rotation.m11, rotation.m12, rotation.m12, 0, rotation.m21, rotation.m22, rotation.m23, 0, rotation.m31, rotation.m32, rotation.m33, 0, 0, 0, 0, 1],
            orientation: [currentAttitude.yaw, currentAttitude.pitch, currentAttitude.roll]
        };
        fireEvent('rotation', event);
    }
    if (listeners['motion']) {
        const quat = currentAttitude.quaternion;
        const rotation = currentAttitude.rotationMatrix;
        const event = {
            timestamp: realTimestamp,
            accelerometer: {
                gravity: {
                    x: data.gravity.x,
                    y: data.gravity.y,
                    z: data.gravity.z
                },
                user: {
                    x: data.userAcceleration.x,
                    y: data.userAcceleration.y,
                    z: data.userAcceleration.z
                },
                x: data.gravity.x + data.userAcceleration.x,
                y: data.gravity.y + data.userAcceleration.y,
                z: data.gravity.z + data.userAcceleration.z
            },
            accuracy: data.magneticField.accuracy,
            orientation: [currentAttitude.yaw, currentAttitude.pitch, currentAttitude.roll],
            magnetometer: [data.magneticField.field.x, data.magneticField.field.y, data.magneticField.field.z],
            quaternion: [quat.x, quat.y, quat.z, quat.w],
            rotationMatrix: [rotation.m11, rotation.m12, rotation.m12, 0, rotation.m21, rotation.m22, rotation.m23, 0, rotation.m31, rotation.m32, rotation.m33, 0, 0, 0, 0, 1],
            gyroscope: [data.rotationRate.x, data.rotationRate.y, data.rotationRate.z]
        };
        fireEvent('motion', event);
    }
    if (listeners['linearAcceleration']) {
        // const quat = currentAttitude.quaternion;
        // const rotation = currentAttitude.rotationMatrix;
        const event = {
            timestamp: realTimestamp,
            accelerometer: {
                gravity: {
                    x: data.gravity.x,
                    y: data.gravity.y,
                    z: data.gravity.z
                },
                user: {
                    x: data.userAcceleration.x,
                    y: data.userAcceleration.y,
                    z: data.userAcceleration.z
                },
                raw: {
                    x: data.userAcceleration.x * STANDARD_GRAVITY,
                    y: data.userAcceleration.y * STANDARD_GRAVITY,
                    z: data.userAcceleration.z * STANDARD_GRAVITY
                },
                x: data.gravity.x + data.userAcceleration.x,
                y: data.gravity.y + data.userAcceleration.y,
                z: data.gravity.z + data.userAcceleration.z
            },
            accuracy: data.magneticField.accuracy
        };
        fireEvent('linearAcceleration', event);
    }
}
function onDeviceAccelerometer(data: CMAccelerometerData, error: NSError) {
    if (listeners['accelerometer']) {
        const realTimestamp = Math.round((bootTimestamp + data.timestamp) * 1000);
        const event = {
            timestamp: realTimestamp,
            x: data.acceleration.x,
            y: data.acceleration.y,
            z: data.acceleration.z
            // raw: {
            //     x: data.acceleration.x * STANDARD_GRAVITY,
            //     y: data.acceleration.y * STANDARD_GRAVITY,
            //     z: data.acceleration.z * STANDARD_GRAVITY
            // },
        };
        fireEvent('accelerometer', event);
    }
}
function onDeviceMagnetometer(data: CMMagnetometerData, error: NSError) {
    if (listeners['magnometer']) {
        const realTimestamp = Math.round((bootTimestamp + data.timestamp) * 1000);
        const event = {
            timestamp: realTimestamp,
            x: data.magneticField.x,
            y: data.magneticField.y,
            z: data.magneticField.z
        };
        fireEvent('magnometer', event);
    }
}
function onDeviceAltitude(data: CMAltitudeData, error: NSError) {
    if (listeners['barometer']) {
        const realTimestamp = Math.round((bootTimestamp + data.timestamp) * 1000);
        const event = {
            timestamp: realTimestamp,
            relativeAltitude: data.relativeAltitude,
            pressure: data.pressure
        };
        fireEvent('barometer', event);
    }
}
function onDeviceGyro(data: CMGyroData, error: NSError) {
    if (listeners['gyroscope']) {
        const realTimestamp = (bootTimestamp + data.timestamp) * 1000;
        const event = {
            timestamp: realTimestamp,
            x: data.rotationRate.x,
            y: data.rotationRate.y,
            z: data.rotationRate.z
        };
        fireEvent('gyroscope', event);
    }
}

function onPedometer(data: CMPedometerData, error: NSError) {
    if (listeners['stepCounter']) {
        const event = {
            startDate: data.startDate,
            endDate: data.endDate,
            steps: data.numberOfSteps
        };
        fireEvent('stepCounter', event);
    }
}

export function startListeningForSensor(
    sensors: SensorType | SensorType[],
    listener: Function,
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
            if (!motionQueue) {
                motionQueue = NSOperationQueue.new();
            }
            switch (sensor) {
                case 'accelerometer': {
                    const motionManager = getMotionManager();
                    motionManager.accelerometerUpdateInterval = updateInterval / 1000;
                    if (!motionManager.accelerometerActive) {
                        motionManager.startAccelerometerUpdatesToQueueWithHandler(motionQueue, onDeviceAccelerometer);
                    }
                    break;
                }
                case 'heading': {
                    return request('location').then((r) => {
                        if (r[0] === 'authorized') {
                            if (!headingManager) {
                                headingManager = new CLLocationManager();
                            }
                            if (!headingDelegate) {
                                headingDelegate = LocationHeadingListenerImpl.new();
                            }
                            headingManager.delegate = headingDelegate;

                            if (options?.headingTrueNorth === true) {
                                headingManager.distanceFilter = options?.headingDistanceFilter || 1000;
                                headingManager.desiredAccuracy = options?.headingDistanceAccuracy || kCLLocationAccuracyKilometer;
                                headingManager.startUpdatingLocation();
                            }
                            headingManager.headingFilter = options?.headingFilter || 5;
                            headingManager.startUpdatingHeading();
                            return true;
                        }
                        return false;
                    });
                }
                case 'barometer': {
                    return request('motion').then((r) => {
                        if (r[0] === 'authorized') {
                            const altitudeManager = getAltitudeManager();
                            // altitudeManager.update = updateInterval / 1000;
                            altitudeManager.startRelativeAltitudeUpdatesToQueueWithHandler(motionQueue, onDeviceAltitude);
                            return true;
                        }
                        return false;
                    });
                }
                case 'orientation':
                case 'motion':
                case 'rotation': {
                    const motionManager = getMotionManager();
                    motionManager.deviceMotionUpdateInterval = updateInterval / 1000;
                    motionManager.showsDeviceMovementDisplay = true;
                    motionManager.startDeviceMotionUpdatesUsingReferenceFrameToQueueWithHandler(CMAttitudeReferenceFrame.XArbitraryCorrectedZVertical, motionQueue, onDeviceMotion);
                    break;
                }
                case 'magnetometer': {
                    const motionManager = getMotionManager();
                    motionManager.magnetometerUpdateInterval = updateInterval / 1000;
                    motionManager.startMagnetometerUpdatesToQueueWithHandler(motionQueue, onDeviceMagnetometer);
                    break;
                }
                case 'gyroscope': {
                    const motionManager = getMotionManager();
                    motionManager.gyroUpdateInterval = updateInterval / 1000;
                    motionManager.startGyroUpdatesToQueueWithHandler(motionQueue, onDeviceGyro);
                    break;
                }
                case 'stepCounter': {
                    const pedometer = getPedometer();
                    // pedometer.update = updateInterval / 1000;
                    const fromDate = new Date();
                    pedometer.startPedometerUpdatesFromDateWithHandler(fromDate, onPedometer);
                    break;
                }
                default: {
                    return Promise.resolve(false);
                }
            }
            listeners[sensor] = listeners[sensor] || [];
            listeners[sensor].push(listener);
            return Promise.resolve(true);
        })
    );
}

export function stopListeningForSensor(sensors: SensorType | SensorType[], listener: Function) {
    if (!Array.isArray(sensors)) {
        sensors = [sensors];
    }
    return Promise.all(
        sensors.map((sensor) => {
            if (sensor && listeners[sensor]) {
                const index = listeners[sensor].indexOf(listener);
                if (index !== -1) {
                    listeners[sensor].splice(index, 1);
                }
                if (!listeners[sensor] || listeners[sensor].length === 0) {
                    delete listeners[sensor];
                    switch (sensor) {
                        case 'accelerometer': {
                            if (motionManager.accelerometerActive) {
                                motionManager.stopAccelerometerUpdates();
                            }
                            break;
                        }
                        case 'barometer': {
                            const altitudeManager = getAltitudeManager();
                            altitudeManager.stopRelativeAltitudeUpdates();
                            break;
                        }
                        case 'heading': {
                            if (headingManager) {
                                headingManager.stopUpdatingLocation();
                                headingManager.stopUpdatingHeading();
                            }
                            break;
                        }
                        case 'orientation':
                        case 'motion':
                        case 'rotation': {
                            const motionManager = getMotionManager();
                            if (!listeners['orientation'] && !listeners['motion'] && !listeners['rotation']) {
                                motionManager.stopDeviceMotionUpdates();
                            }
                            break;
                        }
                        case 'magnetometer': {
                            const motionManager = getMotionManager();
                            motionManager.stopMagnetometerUpdates();
                            break;
                        }
                        case 'gyroscope': {
                            const motionManager = getMotionManager();
                            motionManager.startGyroUpdates();
                            break;
                        }
                        case 'stepCounter': {
                            const pedometer = getPedometer();
                            pedometer.stopPedometerUpdates();
                            break;
                        }
                    }
                }
            }
        })
    );
}

export function getAllavailableSensors() {
    return SENSORS.filter(isSensorAvailable);
}
export function flush() {
    // NOOP
}
