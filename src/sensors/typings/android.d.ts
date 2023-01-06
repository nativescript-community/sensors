

declare namespace com {
    export namespace nativescript {
        export namespace sensors {
            export class SensorManager extends java.lang.Object implements globalAndroid.hardware.SensorEventListener {
                public static class: java.lang.Class<sensors.SensorManager>;
                public static POSSIBLE_MOTION_SENSORS: java.util.List<string>;
                public static ACCURACY_UNCALIBRATED: number;
                public static STANDARD_GRAVITY: number;
                public isSensorRegistered(param0: number): boolean;
                public getDeviceSensors(): java.util.List<globalAndroid.hardware.Sensor>;
                public removeListenerForSensor(param0: string, param1: sensors.SensorManager.SensorManagerEventListener): void;
                public startSensor(param0: number, param1: number, param2: number): globalAndroid.hardware.Sensor;
                public stopSensor(param0: number): void;
                public onSensorChanged(param0: globalAndroid.hardware.SensorEvent): void;
                public removeEventListener(param0: string, param1: sensors.SensorManager.SensorManagerEventListener): boolean;
                public onAccuracyChanged(param0: globalAndroid.hardware.Sensor, param1: number): void;
                public addEventListener(param0: string, param1: sensors.SensorManager.SensorManagerEventListener): void;
                public hasSensor(param0: string): boolean;
                public fireEvent(param0: string, param1: java.util.HashMap<any, any>): void;
                public getActualSensors(param0: string): java.util.List<java.lang.Integer>;
                public addListenerForSensor(param0: string, param1: sensors.SensorManager.SensorManagerEventListener, param2: number, param3: number): boolean;
                public flush(): boolean;
                public hasEvenListener(param0: string): boolean;
                public setHeadingFilter( value: number);
                public constructor(param0: globalAndroid.content.Context);
            }
            export namespace SensorManager {
                export class SensorManagerEventListener extends java.lang.Object {
                    public static class: java.lang.Class<sensors.SensorManager.SensorManagerEventListener>;
                    /**
					 * Constructs a new instance of the com.nativescript.sensors.SensorManager$SensorManagerEventListener interface with the provided implementation. An empty constructor exists calling super() when extending the interface class.
					 */
                    public constructor(implementation?: {
                        onEventData(param0: java.util.HashMap<any, any>, event: string): void;
                    });
                    public onEventData(param0: java.util.HashMap<any, any>, event: string): void;
                }
            }
        }
    }
}

//Generics information:

