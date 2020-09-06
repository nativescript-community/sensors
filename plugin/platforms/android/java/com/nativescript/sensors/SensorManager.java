package com.nativescript.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SensorManager implements SensorEventListener {

    private static final String PROPERTY_X = "x";
    private static final String PROPERTY_Y = "y";
    private static final String PROPERTY_Z = "z";
    private static final String PROPERTY_ACCURACY = "accuracy";
    private static final String PROPERTY_YAW = "yaw";
    private static final String PROPERTY_PITCH = "pitch";
    private static final String PROPERTY_ROLL = "roll";
    private static final String PROPERTY_GRAVITY = "gravity";
    private static final String PROPERTY_TIMESTAMP = "timestamp";
    private static final String PROPERTY_USER = "user";
    private static final String PROPERTY_RAW = "raw";


    private static final String EVENT_LINEAR_ACCELERATION = "linearAcceleration";
    private static final String EVENT_ACC = "accelerometer";
    private static final String EVENT_GYRO = "gyroscope";
    private static final String EVENT_ORIENTATION = "orientation";
    private static final String EVENT_MAG = "magnetometer";
    private static final String EVENT_MOTION = "motion";
    private static final String EVENT_ROTATION = "rotation";
    private static final String EVENT_PRESSURE = "barometer";
    private static final String EVENT_PROXIMITY = "proximity";
    private static final String EVENT_STATIONARY_DETECT = "stationary";
    private static final String EVENT_STEP_COUNTER = "stepCounter";
    private static final String EVENT_STEP_DETECTOR = "stepDetector";
    private static final String EVENT_TEMPERATURE = "temperature";
    private static final String EVENT_HEART_BEAT = "heartBeat";
    private static final String EVENT_HEART_RATE = "heartRate";
    private static final String EVENT_HUMIDITY = "humidity";
    private static final String EVENT_LIGHT = "light";
    private static final String EVENT_GRAVITY = "gravity";


    public static List<String> POSSIBLE_MOTION_SENSORS = Arrays.asList(EVENT_ACC,
            EVENT_GYRO, EVENT_ORIENTATION, EVENT_MAG, EVENT_ROTATION);

    public static final int ACCURACY_UNCALIBRATED = android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;
    public static final float STANDARD_GRAVITY = android.hardware.SensorManager.STANDARD_GRAVITY;


    public static Boolean isSimulatorCache = null;

    public boolean isSimulator() {
        if (isSimulatorCache  == null) {
            isSimulatorCache =
                    Build.FINGERPRINT.startsWith("generic") ||
                            Build.FINGERPRINT.startsWith("unknown") ||
                            (Build.MODEL.contains("google_sdk")) ||
                            Build.MODEL.contains("Emulator") ||
                            Build.MODEL.contains("Android SDK built for x86") ||
                            (Build.MANUFACTURER.contains("Genymotion")) ||
                            (Build.MANUFACTURER.contains("Genymotion")) ||
                            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                            (Build.PRODUCT.contains("sdk"));
        }
        return isSimulatorCache;
    }

    private static final Object valuesLock = new Object();

    private static String TAG = "SensorManager";

    public interface SensorManagerEventListener {
        void onEventData(HashMap data, String event);

    }

    private Handler mSensorHandler;
    private android.hardware.SensorManager mSensorManager;

    private HashMap<Integer, Integer> mRegisteredSensors = new HashMap<>();
    private List<String> motionSensors = new ArrayList<>(
            POSSIBLE_MOTION_SENSORS);
    private int currentMagnetometerAccuracy = ACCURACY_UNCALIBRATED;
    private float currentPressureAltitude = -1;
    private boolean computeRotationMatrix = true;

    private float[] gravity = new float[3];

    private final int matrix_size = 16;
    private float[] R = new float[matrix_size];
    private float[] outR = new float[matrix_size];
    private float[] I = new float[matrix_size];
    private float[] values = new float[3];
    private boolean isReady = false;

    private float[] mRotationMatrix;
    private float[] temporaryQuaternion;
    private float[] mOrientation;
    private float[] filteredAcc = new float[3];
    private float[] filteredMag = new float[3];

    private long startSteps = 0;
    private long steperStartTime = 0;

    private int motionRealNbSensors = motionSensors.size() + 1;

    private HashMap<Integer, float[]> mCurrentValues = new HashMap<Integer, float[]>();
    private long lastTimeStamp = -1;


    private HashMap<String, List<SensorManagerEventListener>> mListeners = new HashMap<>();

    /*
     * Constructor. Since the class does not extend Application or Activity, we need
     * to context of the running process/app.
     */
    public SensorManager(Context context) {
        // Get the SensorManager
        mSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // Setup our background thread for sensors
        HandlerThread mSensorThread = new HandlerThread("Nativescript Sensosr Thread", Process.THREAD_PRIORITY_BACKGROUND);
        mSensorThread.start();
        mSensorHandler = new Handler(mSensorThread.getLooper()); // Blocks until looper is prepared
        // Log.d(TAG, "SensorManager HandlerThread: " + mSensorThread);
    }


    protected void fireEvent(String event, HashMap data) {
        List<SensorManagerEventListener> listeners = mListeners.get(event);
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onEventData(data, event);
            }
        }
    }

    protected boolean hasEvenListener(String event) {
        List<SensorManagerEventListener> listeners = mListeners.get(event);
        return (listeners != null && listeners.size() > 0);
    }

    protected void addEventListener(String event, SensorManagerEventListener listener) {
        List<SensorManagerEventListener> listeners = mListeners.get(event);
        if (listeners == null) {
            listeners = new ArrayList<SensorManagerEventListener>();
            mListeners.put(event, listeners);
        }
        listeners.add(listener);
    }

    protected boolean removeEventListener(String event, SensorManagerEventListener listener) {
        List<SensorManagerEventListener> listeners = mListeners.get(event);
        if (listeners != null) {
            final boolean wasIn = listeners.remove(listener);
            if (listeners.size() == 0) {
                mListeners.remove(event);
            }
            return wasIn;
        }
        return false;

    }

//    public void setListener(SensorManagerListener mListener) {
//        this.mListener = mListener;
//        Log.d(TAG, "Listener: " + this.mListener);
//    }

//    public static boolean supportsFIFO(Sensor sensor) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            return sensor.getFifoMaxEventCount() != 0;
//        }
//        return false;
//    }

    // @Override
    // public void onSensorChanged(SensorEvent event) {
    //     if (mListener != null) {

    //         long newSensorEventTimestamp = (new Date()).getTime()
    //                 + (event.timestamp - System.nanoTime()) / 1000000L;/


    //         LiteDataBag liteDataBag = null;
    //         HeavyDataBag heavyDataBag = null;
    //         HashMap<String, Float> sensorData = new HashMap<>();

    //         int sensorType = event.sensor.getType();
    //         // depending on the the sensor type set the result to return in the listener
    //         if (sensorType == Sensor.TYPE_ACCELEROMETER
    //                 || sensorType == Sensor.TYPE_LINEAR_ACCELERATION
    //                 || sensorType == Sensor.TYPE_GYROSCOPE
    //                 || sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
    //             sensorData.put("x", event.values[0]);
    //             sensorData.put("y", event.values[1]);
    //             sensorData.put("z", event.values[2]);
    //         } else if (sensorType == Sensor.TYPE_GRAVITY) {
    //             // gravity values are inversed!
    //             sensorData.put("x", -event.values[0]);
    //             sensorData.put("y", -event.values[1]);
    //             sensorData.put("z", -event.values[2]);
    //         } else if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
    //             sensorData.put("x", event.values[0]);
    //             sensorData.put("y", event.values[1]);
    //             sensorData.put("z", event.values[2]);
    //             sensorData.put("cos", event.values[3]);
    //             sensorData.put("heading_accuracy", event.values[4]);
    //         } else if (sensorType == Sensor.TYPE_GAME_ROTATION_VECTOR) {
    //             sensorData.put("x", event.values[0]);
    //             sensorData.put("y", event.values[1]);
    //             sensorData.put("z", event.values[2]);
    //             sensorData.put("cos", event.values[3]);
    //         } else if (sensorType == Sensor.TYPE_STATIONARY_DETECT) {
    //             sensorData.put("stationary", event.values[0]);
    //         } else if (sensorType == Sensor.TYPE_PROXIMITY) {
    //             sensorData.put("proximity", event.values[0]);
    //         } else if (sensorType == Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT) {
    //             sensorData.put("state", event.values[0]);
    //         } else if (sensorType == Sensor.TYPE_HEART_RATE) {
    //             sensorData.put("heart_rate", event.values[0]);
    //         } else if (sensorType == Sensor.TYPE_HEART_BEAT) {
    //             sensorData.put("confidence", event.values[0]);
    //         } else if (sensorType == Sensor.TYPE_LIGHT) {
    //             sensorData.put("light_level", event.values[0]);
    //         } else if (sensorType == Sensor.TYPE_PRESSURE) {
    //             sensorData.put("pressure", event.values[0]);
    //         } else if (sensorType == Sensor.TYPE_RELATIVE_HUMIDITY) {
    //             sensorData.put("humidity", event.values[0]); // Relative ambient air humidity in percent
    //         } else if (sensorType == Sensor.TYPE_AMBIENT_TEMPERATURE) {
    //             sensorData.put("temp", event.values[0]); // ambient (room) temperature in degree Celsius
    //         } else if (sensorType == Sensor.TYPE_POSE_6DOF) {
    //             sensorData.put("x", event.values[0]);
    //             sensorData.put("y", event.values[1]);
    //             sensorData.put("z", event.values[2]);
    //             sensorData.put("cos", event.values[3]);
    //             sensorData.put("x_translation", event.values[4]);
    //             sensorData.put("y_translation", event.values[5]);
    //             sensorData.put("z_translation", event.values[6]);
    //             sensorData.put("x_rotation", event.values[7]);
    //             sensorData.put("y_rotation", event.values[8]);
    //             sensorData.put("z_rotation", event.values[9]);
    //             sensorData.put("cos_rotation", event.values[10]);
    //             sensorData.put("x_delta_translation", event.values[11]);
    //             sensorData.put("y_delta_translation", event.values[12]);
    //             sensorData.put("z_delta_translation", event.values[13]);
    //             sensorData.put("sequence_number", event.values[14]);
    //         }

    //         // if (useLiteDataBagForSensorData) {
    //             liteDataBag = new LiteDataBag();
    //             liteDataBag.s = sensorType;
    //             liteDataBag.ts = event.timestamp;
    //             liteDataBag.d = sensorData;
    //         // } else {
    //         //     heavyDataBag = new HeavyDataBag();
    //         //     heavyDataBag.sensor = event.sensor.getStringType();
    //         //     heavyDataBag.timestamp = event.timestamp;
    //         //     heavyDataBag.data = sensorData;
    //         // }

    //         Gson gson = new Gson();
    //         String result = useLiteDataBagForSensorData
    //                 ? gson.toJson(liteDataBag)
    //                 : gson.toJson(heavyDataBag);

    //         // send the data to the SensorManagerListener
    //         mListener.onSensorChanged(result);
    //     }
    // }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            currentMagnetometerAccuracy = accuracy;
        // send the values to the SensorManagerListener
//        mListener.onAccuracyChanged(sensor, accuracy);

    }

    protected List<Integer> getActualSensors(String sensorType) {
        List<Integer> result = new ArrayList<>();
        switch (sensorType) {
            case EVENT_LINEAR_ACCELERATION:
                result.add(Sensor.TYPE_LINEAR_ACCELERATION);
                result.add(Sensor.TYPE_GRAVITY);
                break;
            case EVENT_ACC:
                result.add(Sensor.TYPE_ACCELEROMETER);
                break;
            case EVENT_GYRO:
                result.add(Sensor.TYPE_GYROSCOPE);
                break;
            case EVENT_MAG:
                result.add(Sensor.TYPE_MAGNETIC_FIELD);
                break;
            case EVENT_ROTATION:
                result.add(Sensor.TYPE_ROTATION_VECTOR);
                break;
            case EVENT_PRESSURE:
                result.add(
                        Sensor.TYPE_PRESSURE);
                break;
            case EVENT_PROXIMITY:
                result.add(
                        Sensor.TYPE_PROXIMITY);
                break;
            case EVENT_HEART_BEAT:
                result.add(
                        Sensor.TYPE_HEART_BEAT);
                break;
            case EVENT_HEART_RATE:
                result.add(
                        Sensor.TYPE_HEART_RATE);
                break;
            case EVENT_STEP_DETECTOR:
                result.add(
                        Sensor.TYPE_STEP_DETECTOR);
                break;
            case EVENT_STEP_COUNTER:
                result.add(
                        Sensor.TYPE_STEP_COUNTER);
                break;
            case EVENT_TEMPERATURE:
                result.add(
                        Sensor.TYPE_AMBIENT_TEMPERATURE);
                break;
            case EVENT_HUMIDITY:
                result.add(
                        Sensor.TYPE_RELATIVE_HUMIDITY);
                break;
            case EVENT_STATIONARY_DETECT:
                result.add(
                        Sensor.TYPE_STATIONARY_DETECT);
                break;
            case EVENT_ORIENTATION:
                result.add(Sensor.TYPE_ORIENTATION);
                break;
            case EVENT_LIGHT:
                result.add(Sensor.TYPE_LIGHT);
                break;
            case EVENT_GRAVITY:
                result.add(Sensor.TYPE_GRAVITY);
                break;
            case EVENT_MOTION:
                if (motionSensors.contains(EVENT_MAG)) {
                    result.add(Sensor.TYPE_MAGNETIC_FIELD);
                }

                if (motionSensors.contains(EVENT_GYRO)) {
                    result.add(Sensor.TYPE_GYROSCOPE);
                }
                if (motionSensors.contains(EVENT_ORIENTATION)) {
                    result.add(Sensor.TYPE_ORIENTATION);
                }
                if (motionSensors.contains(EVENT_ACC)) {
                    result.add(Sensor.TYPE_LINEAR_ACCELERATION);
                    result.add(Sensor.TYPE_GRAVITY);
                }
                if (motionSensors.contains(EVENT_ROTATION)) {
                    result.add(Sensor.TYPE_ROTATION_VECTOR);
                    result.add(Sensor.TYPE_MAGNETIC_FIELD);

                }
                break;
        }
        Log.d(TAG, "getActualSensors "  + sensorType + ": " + result);
        return result;
    }

    public boolean addListenerForSensor(String sensorType, SensorManagerEventListener listener, int sensorDelay, int maxReportLatencyUs) {
        final List<Integer> sensors = getActualSensors(sensorType);
        addEventListener(sensorType, listener);
        boolean result = true;
        for (Integer s : sensors) {
            result = result && startSensor(s, sensorDelay, maxReportLatencyUs) != null;
        }
        if (!result) {
            for (Integer s : sensors) {
                stopSensor(s);
            }
        }
        return result;
    }

    public void removeListenerForSensor(String sensorType, SensorManagerEventListener listener) {
        final boolean wasIn = removeEventListener(sensorType, listener);
        if (wasIn) {
            final List<Integer> sensors = getActualSensors(sensorType);
            for (Integer s : sensors) {
                stopSensor(s);
            }
        }
    }

    protected boolean isSensorRegistered(int sensor) {
        return mRegisteredSensors.containsKey(sensor)  &&  mRegisteredSensors.get(sensor) > 0;
    }

    public boolean hasSensor(String sensorType) {
        final List<Integer> sensors = getActualSensors(sensorType);
        for (Integer s : sensors) {
            Sensor defaultSensor = mSensorManager.getDefaultSensor(s);
            if (defaultSensor != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the sensor with the provided sensor reporting delay.
     *
     * @param sensor      [int] - The sensor type.
     * @param sensorDelayMS [int] - The sensory reporting delay in MS.
     * @param maxReportLatencyMs [int] - The sensory max report latency in MS.
     * @return [boolean] - Return true if sensor is registered with the listener.
     */
    protected Sensor startSensor(int sensor, int sensorDelayMS, int maxReportLatencyMs) {

//        final boolean isStarted = mRegisteredSensors.contains(sensor);
//        if (isStarted) {
//            return;
//        }
        if (mSensorManager == null) { // BOO NO SENSOR MANAGER
            Log.d(TAG, "SensorManager does not have the SensorManager. Will return null since no sensor can be registered.");
            return null;
        }

        // here we have the SensorManager so try to get the sensor requested
        Sensor defaultSensor = mSensorManager.getDefaultSensor(sensor);
        // if we have the sensor then register the listener
        if (defaultSensor != null) {
            if (sensor == Sensor.TYPE_STEP_COUNTER) {
                startSteps = 0;
                steperStartTime = (new Date()).getTime();
            }
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Log.d(TAG, "startSensor " + sensor + ", " + eventProperty(sensor)+ ", " + defaultSensor.getFifoMaxEventCount() + ", " + sensorDelayMS + ", " + maxReportLatencyMs + ", " + defaultSensor);
            // } else {
            Log.d(TAG, "startSensor " + sensor + ", " + eventProperty(sensor) + ", " + sensorDelayMS + ", " + maxReportLatencyMs + ", " + defaultSensor);
            // }

            // calling multiple register on the same sensor will fail
            boolean didRegister = mRegisteredSensors.containsKey(sensor);
            if (!didRegister) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    didRegister = mSensorManager.registerListener(this, defaultSensor, sensorDelayMS * 1000, maxReportLatencyMs * 1000, mSensorHandler);
                } else {
                    didRegister = mSensorManager.registerListener(this, defaultSensor, sensorDelayMS * 1000, mSensorHandler);
                }
            }

            if (didRegister) {
                if (!mRegisteredSensors.containsKey(sensor)) {
                    mRegisteredSensors.put(sensor, 1);
                }
                return defaultSensor;  // YAY SENSOR REGISTERED
            } else {
                // Log.d(TAG, "SensorManager failed to registerListener for type " + sensor);
                return null;
            }
        } else {
            // Log.d(TAG, "SensorManager unable to get the default sensor for type: " + sensor);
            return null; // BOO SENSOR NOT REGISTERED
        }
    }

    /**
     * Stops the sensor by unregistering.
     *
     * @param sensor [int] - The sensor to unregister.
     */
    protected void stopSensor(int sensor) {
        boolean shouldReallyStop = false;
        if (mRegisteredSensors.containsKey(sensor)) {
            final int currentCount = mRegisteredSensors.get(sensor);
            if (currentCount == 1) {
                shouldReallyStop = true;
                mRegisteredSensors.remove(sensor);
            } else {
                mRegisteredSensors.put(sensor, currentCount - 1);
            }
        }
        if (shouldReallyStop) {
            Sensor defaultSensor = mSensorManager.getDefaultSensor(sensor);
            // Log.d(TAG, "stopSensor " + sensor + ", " + eventProperty(sensor) + ", " + defaultSensor);

            if (defaultSensor != null) {
                mSensorManager.unregisterListener(this, defaultSensor);
            }
        }

    }

    public List<Sensor> getDeviceSensors() {
        if (mSensorManager != null) {
            return mSensorManager.getSensorList(Sensor.TYPE_ALL);
        } else {
            return null;
        }
    }

    /**
     * Flushes the FIFO of all the sensors registered for this listener.
     *
     * @return boolean
     */
    public boolean flush() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            return this.mSensorManager.flush(this);
        } else {
            Log.w(TAG, "No sensor listener set, so unable to flush the data.");
            return false;
        }
    }

    private HashMap eventToMap(int type, float[] values, Integer eventAccuracy) {
        HashMap sensordata = new HashMap();
        if (eventAccuracy != null) {
            sensordata.put(PROPERTY_ACCURACY, eventAccuracy);
        }

        switch (type) {
            case Sensor.TYPE_ACCELEROMETER: {
                float x = values[0];
                float y = values[1];
                float z = values[2];
                // float[] gravs = mCurrentValues.get(Sensor.TYPE_GRAVITY);
                // final float alpha = (float) 0.8;
                // x /= -STANDARD_GRAVITY;
                // y /= -STANDARD_GRAVITY;
                // z /= -STANDARD_GRAVITY;

                // gravity[0] = alpha * gravity[0] + (1 - alpha) * x;
                // gravity[1] = alpha * gravity[1] + (1 - alpha) * y;
                // gravity[2] = alpha * gravity[2] + (1 - alpha) * z;

                sensordata.put(PROPERTY_X, x);
                sensordata.put(PROPERTY_Y, y);
                sensordata.put(PROPERTY_Z, z);
                // HashMap useracc = new HashMap();
                // useracc.put(PROPERTY_X, x - gravity[0]);
                // useracc.put(PROPERTY_Y, y - gravity[1]);
                // useracc.put(PROPERTY_Z, z - gravity[2]);
                // HashMap gacc = new HashMap();
                // gacc.put(PROPERTY_X, gravity[0]);
                // gacc.put(PROPERTY_Y, gravity[1]);
                // gacc.put(PROPERTY_Z, gravity[2]);

                // HashMap raw = new HashMap();
                // raw.put(PROPERTY_X, values[0]);
                // raw.put(PROPERTY_Y, values[1]);
                // raw.put(PROPERTY_Z, values[2]);
                // sensordata.put(PROPERTY_RAW, raw);
                // sensordata.put(PROPERTY_USER, useracc);
                // sensordata.put(PROPERTY_GRAVITY, gacc);
                break;
            }
            case Sensor.TYPE_LINEAR_ACCELERATION: {
                if (!mCurrentValues.containsKey(Sensor.TYPE_GRAVITY)) {
                    return sensordata;
                }
                float x = values[0];
                float y = values[1];
                float z = values[2];
                // float[] gravs = mCurrentValues.get(Sensor.TYPE_GRAVITY);
                // final float alpha = (float) 0.8;
                x /= STANDARD_GRAVITY;
                y /= STANDARD_GRAVITY;
                z /= STANDARD_GRAVITY;

                float[] gravity = mCurrentValues.get(Sensor.TYPE_GRAVITY);
                float gx = gravity[0] / STANDARD_GRAVITY;
                float gy = gravity[1] / STANDARD_GRAVITY;
                float gz = gravity[2] / STANDARD_GRAVITY;

                sensordata.put(PROPERTY_X, x + gx);
                sensordata.put(PROPERTY_Y, y + gy);
                sensordata.put(PROPERTY_Z, z + gz);
                HashMap useracc = new HashMap();
                useracc.put(PROPERTY_X, x);
                useracc.put(PROPERTY_Y, y);
                useracc.put(PROPERTY_Z, z);
                HashMap gacc = new HashMap();
                gacc.put(PROPERTY_X, gx);
                gacc.put(PROPERTY_Y, gy);
                gacc.put(PROPERTY_Z, gz);
                HashMap raw = new HashMap();
                raw.put(PROPERTY_X, values[0]);
                raw.put(PROPERTY_Y, values[1]);
                raw.put(PROPERTY_Z, values[2]);
                sensordata.put(PROPERTY_RAW, raw);
                sensordata.put(PROPERTY_USER, useracc);
                sensordata.put(PROPERTY_GRAVITY, gacc);
                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD: {
                float x = values[0];
                float y = values[1];
                float z = values[2];
                sensordata.put(PROPERTY_X, x);
                sensordata.put(PROPERTY_Y, y);
                sensordata.put(PROPERTY_Z, z);
                sensordata.put(PROPERTY_ACCURACY, currentMagnetometerAccuracy);
                break;
            }
            case Sensor.TYPE_ORIENTATION: {
                float x = values[0];
                float y = values[1];
                float z = values[2];
                sensordata.put(PROPERTY_YAW, x);
                sensordata.put(PROPERTY_PITCH, y);
                sensordata.put(PROPERTY_ROLL, z);
                break;
            }
            case Sensor.TYPE_GYROSCOPE: {
                float x = values[0];
                float y = values[1];
                float z = values[2];
                sensordata.put(PROPERTY_X, x);
                sensordata.put(PROPERTY_Y, y);
                sensordata.put(PROPERTY_Z, z);
                break;
            }
            case Sensor.TYPE_GRAVITY: {
                sensordata.put(PROPERTY_X, -values[0]);
                sensordata.put(PROPERTY_Y, -values[1]);
                sensordata.put(PROPERTY_Z, -values[2]);
                break;
            }
            case Sensor.TYPE_STEP_COUNTER: {
                if (startSteps == 0) {
                  startSteps = (long)values[0];
                }
                sensordata.put("steps", values[0] - startSteps);
                sensordata.put("startDate", steperStartTime);
                sensordata.put("endDate", (new Date()).getTime());
                break;
            }
            case Sensor.TYPE_PROXIMITY: {
                sensordata.put("proximity", values[0]);
                break;
            }
            case Sensor.TYPE_HEART_RATE: {
                sensordata.put("heartRate", values[0]);
                break;
            }
            case Sensor.TYPE_HEART_BEAT: {
                sensordata.put("hearBeat", values[0]);
                break;
            }
            case Sensor.TYPE_LIGHT: {
                sensordata.put("level", values[0]);
                break;
            }
            case Sensor.TYPE_RELATIVE_HUMIDITY: {
                sensordata.put("humidity", values[0]); // Relative ambient air humidity in percent
                break;
            }
            case Sensor.TYPE_AMBIENT_TEMPERATURE: {
                sensordata.put("temperature", values[0]); // ambient (room) temperature in degree Celsius
                break;
            }
            case Sensor.TYPE_STATIONARY_DETECT: {
                sensordata.put("stationary", values[0]); 
                break;
            }
            case Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT: {
                sensordata.put("state", values[0]); 
                break;
            }
            case Sensor.TYPE_PRESSURE: {
                float currentPressure = values[0];
                float altitudeDifference = 0;
                float newAltitude = android.hardware.SensorManager.getAltitude(android.hardware.SensorManager.PRESSURE_STANDARD_ATMOSPHERE, currentPressure);
                if (currentPressureAltitude != 1) {
                    altitudeDifference = newAltitude - currentPressureAltitude;
                }
                currentPressureAltitude = newAltitude;
                sensordata.put("pressure", values[0]);
                sensordata.put("relativeAltitude", altitudeDifference);
                break;
            }
            case Sensor.TYPE_ROTATION_VECTOR:
            case Sensor.TYPE_GAME_ROTATION_VECTOR: {
                if (temporaryQuaternion == null) {
                    temporaryQuaternion = new float[4];
                }
                android.hardware.SensorManager.getQuaternionFromVector(temporaryQuaternion, values);
                sensordata.put("quaternion", temporaryQuaternion);
                sensordata.put(PROPERTY_ACCURACY, currentMagnetometerAccuracy);
                if (computeRotationMatrix) {
                    if (mRotationMatrix == null) {
                        mRotationMatrix = new float[16];
                    }
                    android.hardware.SensorManager.getRotationMatrixFromVector(mRotationMatrix,
                            values);
                    sensordata.put("rotationMatrix", mRotationMatrix);
                    if (mOrientation == null) {
                        mOrientation = new float[3];
                    }
                    android.hardware.SensorManager.getOrientation(mRotationMatrix, mOrientation);
                    sensordata.put("orientation", mOrientation);
                }

            }
            default:
                break;
        }
        return sensordata;
    }


    private String eventProperty(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                return EVENT_ACC;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return EVENT_LINEAR_ACCELERATION;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
            case Sensor.TYPE_ROTATION_VECTOR:
                return EVENT_ROTATION;
            case Sensor.TYPE_MAGNETIC_FIELD:
                return EVENT_MAG;
            case Sensor.TYPE_GYROSCOPE:
                return EVENT_GYRO;
            case Sensor.TYPE_ORIENTATION:
                return EVENT_ORIENTATION;
            case Sensor.TYPE_PRESSURE:
                return EVENT_PRESSURE;
            case Sensor.TYPE_PROXIMITY:
                return EVENT_PROXIMITY;
            case Sensor.TYPE_STATIONARY_DETECT:
                return EVENT_STATIONARY_DETECT;
            case Sensor.TYPE_STEP_COUNTER:
                return EVENT_STEP_COUNTER;
            case Sensor.TYPE_STEP_DETECTOR:
                return EVENT_STEP_DETECTOR;
            case Sensor.TYPE_HEART_BEAT:
                return EVENT_HEART_BEAT;
            case Sensor.TYPE_HEART_RATE:
                return EVENT_HEART_RATE;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return EVENT_TEMPERATURE;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return EVENT_HUMIDITY;
            case Sensor.TYPE_LIGHT:
                return EVENT_LIGHT;
            case Sensor.TYPE_GRAVITY:
                return EVENT_GRAVITY;
            default:
                return null;
        }
    }

    private long sensorTimeReference = 0l;
    private long myTimeReference = 0l;
    public void onSensorChanged(SensorEvent event) {
        synchronized (valuesLock) {
            final int sensorType = event.sensor.getType();
            final String type = eventProperty(sensorType);
            if(sensorTimeReference == 0l && myTimeReference == 0l) {
                sensorTimeReference = event.timestamp;
                myTimeReference = System.currentTimeMillis();
            }
            // set event timestamp to current time in milliseconds
            event.timestamp = myTimeReference +
                    Math.round((event.timestamp - sensorTimeReference) / 1000000.0);
            long newSensorEventTimestamp = event.timestamp;// MICRO
        //    Log.d(TAG, "onSensorChanged " + sensorType + ", " + type);
            if (sensorType == Sensor.TYPE_GRAVITY
                        && isSensorRegistered(Sensor.TYPE_LINEAR_ACCELERATION)) {
                    if (!mCurrentValues.containsKey(Sensor.TYPE_GRAVITY))
                    {
                        // gravity values are inversed!
                        float[] gravs = event.values.clone();
                        gravs[0] *= -1;
                        gravs[1] *= -1;
                        gravs[2] *= -1;
                        mCurrentValues.put(sensorType, gravs);
                    }
                } 
            // SECONDS
            if (mListeners.containsKey(type)) {

                HashMap sensordata = null;

                if (sensorType == Sensor.TYPE_LINEAR_ACCELERATION
                        && isSensorRegistered(Sensor.TYPE_GRAVITY)) {
                    if (!mCurrentValues.containsKey(Sensor.TYPE_GRAVITY)) {
                        return;
                    }
                    sensordata = eventToMap(sensorType,
                            event.values, event.accuracy);
                    mCurrentValues.remove(Sensor.TYPE_GRAVITY);
                } else {
                    sensordata = eventToMap(sensorType,
                            event.values, event.accuracy);
                }

                if (sensordata != null) {
                    sensordata.put(PROPERTY_TIMESTAMP,
                            newSensorEventTimestamp);
                    fireEvent(type, sensordata);
                }
                return;
            }
            if (hasEvenListener(EVENT_MOTION)) {
//                Log.d(TAG, "Motion go event " + sensorType + ", " + mCurrentValues.containsKey(sensorType));
                if (mCurrentValues.containsKey(sensorType))
                    return;

                float[] values = event.values.clone();
                if (sensorType == Sensor.TYPE_GRAVITY) {
                    // gravity values are inversed!
                    values[0] *= -1;
                    values[1] *= -1;
                    values[2] *= -1;
                }
                mCurrentValues.put(sensorType, values);

                if (mCurrentValues.size() != motionRealNbSensors)
                    return;

//                long deltaTime = (newSensorEventTimestamp - lastTimeStamp); // MILLI
                // SECONDS
//                if (lastTimeStamp != -1 && deltaTime < updateInterval) {
//                    mCurrentValues.clear();
//                    return;
//                }

                HashMap data = new HashMap();
                data.put(PROPERTY_TIMESTAMP, newSensorEventTimestamp);
                data.put(PROPERTY_ACCURACY, event.accuracy);

                HashMap sensordata;

                // ACCELEROMETER
                if (mCurrentValues.get(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
                    sensordata = eventToMap(Sensor.TYPE_LINEAR_ACCELERATION, mCurrentValues.get(Sensor.TYPE_LINEAR_ACCELERATION), null);
                    data.put(EVENT_ACC, sensordata);
                }

                // GYROSCOPE
                if (mCurrentValues.get(Sensor.TYPE_GYROSCOPE) != null) {
//                    sensordata = eventToMap(Sensor.TYPE_GYROSCOPE, mCurrentValues.get(Sensor.TYPE_GYROSCOPE), null);
                    data.put(EVENT_GYRO, mCurrentValues.get(Sensor.TYPE_GYROSCOPE));
                }

                // ORIENTATION
                if (mCurrentValues.get(Sensor.TYPE_ORIENTATION) != null) {
//                    sensordata = eventToMap(Sensor.TYPE_ORIENTATION,
//                            mCurrentValues.get(Sensor.TYPE_ORIENTATION), null);
                    data.put(EVENT_ORIENTATION, mCurrentValues.get(Sensor.TYPE_ORIENTATION));
                }

                // MAGNETIC_FIELD
                if (mCurrentValues.get(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                    sensordata = eventToMap(Sensor.TYPE_MAGNETIC_FIELD,
                            mCurrentValues.get(Sensor.TYPE_MAGNETIC_FIELD), null);
                    data.put(EVENT_MAG, sensordata);
                }


                // GRAVITY
                if (mCurrentValues.get(Sensor.TYPE_GRAVITY) != null) {
//                    sensordata = eventToMap(Sensor.TYPE_MAGNETIC_FIELD,
//                            mCurrentValues.get(Sensor.TYPE_MAGNETIC_FIELD), null);
                    data.put(EVENT_MAG, mCurrentValues.get(Sensor.TYPE_GRAVITY));
                }

                if (mCurrentValues.get(Sensor.TYPE_ROTATION_VECTOR) != null) {
                    float[] quat = mCurrentValues.get(Sensor.TYPE_ROTATION_VECTOR);
                    data.put("quaternion", quat);
                    if (computeRotationMatrix) {
                        if (mRotationMatrix == null) {
                            mRotationMatrix = new float[16];
                        }
                        android.hardware.SensorManager.getRotationMatrixFromVector(mRotationMatrix, quat);
                        data.put("rotationMatrix", mRotationMatrix);
                        if (mOrientation == null) {
                            mOrientation = new float[3];
                        }
                        android.hardware.SensorManager.getOrientation(mRotationMatrix, mOrientation);
                        data.put("orientation", mOrientation);
                    }

                }
                lastTimeStamp = newSensorEventTimestamp;
                mCurrentValues.clear();
                fireEvent(EVENT_MOTION, data);
            }
        }
    }

}