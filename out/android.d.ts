/// <reference path="android-declarations.d.ts"/>

declare module com {
	export module nativescript {
		export module sensors {
			export class BuildConfig extends java.lang.Object {
				public static class: java.lang.Class<com.nativescript.sensors.BuildConfig>;
				public static DEBUG: boolean;
				public static APPLICATION_ID: string;
				public static BUILD_TYPE: string;
				public static FLAVOR: string;
				public static VERSION_CODE: number;
				public static VERSION_NAME: string;
				public constructor();
			}
		}
	}
}

declare module com {
	export module nativescript {
		export module sensors {
			export class SensorManager extends java.lang.Object implements globalAndroid.hardware.SensorEventListener {
				public static class: java.lang.Class<com.nativescript.sensors.SensorManager>;
				public static POSSIBLE_MOTION_SENSORS: java.util.List<string>;
				public static ACCURACY_UNCALIBRATED: number;
				public static STANDARD_GRAVITY: number;
				public isSensorRegistered(param0: number): boolean;
				public getDeviceSensors(): java.util.List<globalAndroid.hardware.Sensor>;
				public removeListenerForSensor(param0: string, param1: com.nativescript.sensors.SensorManager.SensorManagerEventListener): void;
				public startSensor(param0: number, param1: number, param2: number): globalAndroid.hardware.Sensor;
				public stopSensor(param0: number): void;
				public onSensorChanged(param0: globalAndroid.hardware.SensorEvent): void;
				public removeEventListener(param0: string, param1: com.nativescript.sensors.SensorManager.SensorManagerEventListener): boolean;
				public onAccuracyChanged(param0: globalAndroid.hardware.Sensor, param1: number): void;
				public addEventListener(param0: string, param1: com.nativescript.sensors.SensorManager.SensorManagerEventListener): void;
				public hasSensor(param0: string): boolean;
				public fireEvent(param0: string, param1: java.util.HashMap): void;
				public getActualSensors(param0: string): java.util.List<java.lang.Integer>;
				public addListenerForSensor(param0: string, param1: com.nativescript.sensors.SensorManager.SensorManagerEventListener, param2: number, param3: number): void;
				public flush(): boolean;
				public hasEvenListener(param0: string): boolean;
				public constructor(param0: globalAndroid.content.Context, param1: boolean);
			}
			export module SensorManager {
				export class SensorManagerEventListener extends java.lang.Object {
					public static class: java.lang.Class<com.nativescript.sensors.SensorManager.SensorManagerEventListener>;
					/**
					 * Constructs a new instance of the com.nativescript.sensors.SensorManager$SensorManagerEventListener interface with the provided implementation. An empty constructor exists calling super() when extending the interface class.
					 */
					public constructor(implementation: {
						onEventData(param0: java.util.HashMap): void;
					});
					public constructor();
					public onEventData(param0: java.util.HashMap): void;
				}
			}
		}
	}
}

//Generics information:

