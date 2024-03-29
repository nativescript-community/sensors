<template>
    <Page>
        <ActionBar title="Sensors Demo" />
        <GridLayout>
            <ScrollView>
                <StackLayout>
                    <Label :text="'availableSensors: ' + sensors" textWrap />
                    <GridLayout v-for="sensor in sensors" :key="sensor" rows="auto,auto,*" columns="*,auto" height="150" margin="10" backgroundColor="lightgray">
                        <Label :text="sensor" textTransform="uppercase" :color="sensorStarted(sensor) ? 'green' : 'red'" />
                        <Label col="2" :text="sensorTimeStamp(sensor)" horizontalAlignment="right" fontSize="10" />
                        <Label row="1" :text="sensorAccuracy(sensor)" fontSize="10" />
                        <Label row="2" :text="sensorDataText(sensor)" textWrap fontSize="10" verticalAlignment="top" />
                        <button
                            rowSpan="3"
                            col="1"
                            ref="button"
                            horizontalAlignment="right"
                            verticalAlignment="middle"
                            :text="sensorStarted(sensor) ? 'stop' : 'start'"
                            @tap="startStopSensor(sensor)"
                        />
                    </GridLayout>
                    <GridLayout v-if="hasBarometer" rows="auto,auto,*" columns="*,auto" height="150" margin="10" backgroundColor="lightgray">
                        <Label text="Altitude" textTransform="uppercase" :color="sensorStarted('barometer') ? 'green' : 'red'" />
                        <Label col="2" :text="sensorTimeStamp('barometer')" horizontalAlignment="right" fontSize="10" />
                        <Label row="1" :text="sensorAccuracy('barometer')" fontSize="10" />
                        <Label row="2" v-show="currentAltitude" :text="'currentAltitude: ' + currentAltitude" textWrap fontSize="10" verticalAlignment="top" />
                        <StackLayout rowSpan="3" col="1" horizontalAlignment="right" verticalAlignment="middle">
                            <button ref="button" :text="sensorStarted('barometer') ? 'stop' : 'start'" @tap="startStopSensor('barometer')" />
                            <button ref="button" text="reference" @tap="getNearestAirportPressure()" />
                        </StackLayout>
                    </GridLayout>
                </StackLayout>
            </ScrollView>
        </GridLayout>
    </Page>
</template>

<script lang="ts">
import Vue from 'vue';
import Component from 'vue-class-component';
import { CoreTypes, View, alert, confirm } from '@nativescript/core';
const dateFormat = require('dateformat');
import { SensorsTuple } from '../../src/sensors';
import { GPS, GeoLocation, Options as GeolocationOptions, setMockEnabled } from '@nativescript-community/gps';
let geolocation: GPS;
import * as sensors from '@nativescript-community/sensors';
export default class Basic extends Vue {
    listeningForMotion = false;
    sensors: sensors.SensorType[] = [];
    hasBarometer = false;
    airportPressure = 1013.5;
    currentAltitude = null;
    mounted() {
        console.log('mounted');
        this.sensors = sensors.getAllavailableSensors();
        this.hasBarometer = this.sensors.indexOf('barometer') !== -1;
        // this.sensors.forEach(s=>{
        //     this.sensorsData[s] = null;
        //     this.sensorsState[s] = false;
        // })
        console.log('sensors', JSON.stringify(this.sensors));
    }
    sensorsData: {
        [k in sensors.SensorType]?: any;
    } = sensors.SENSORS.reduce((accumulator, currentValue) => {
        accumulator[currentValue] = null;
        return accumulator;
    }, {});
    sensorsState: {
        [k in sensors.SensorType]?: boolean;
    } = sensors.SENSORS.reduce((accumulator, currentValue) => {
        accumulator[currentValue] = false;
        return accumulator;
    }, {});

    get sensorDataText() {
        return (sensor) => {
            const data = this.sensorsData[sensor];
            if (!data) {
                return 'no data';
            } else {
                const { accuracy, timestamp, ...dataWithoutTsAndAccuracy } = data;
                return JSON.stringify(dataWithoutTsAndAccuracy);
            }
        };
    }
    get sensorAccuracy() {
        return (sensor) => {
            const data = this.sensorsData[sensor];
            if (data && data.accuracy) {
                return `accuracy: ${data.accuracy}`;
            } else {
                return '';
            }
        };
    }
    get sensorTimeStamp() {
        return (sensor) => {
            const data = this.sensorsData[sensor];
            if (data && data.timestamp) {
                return dateFormat(new Date(data.timestamp), 'HH:mm:ss sss');
            } else {
                return '';
            }
        };
    }

    get sensorStarted() {
        return (sensor) => {
            return !!this.sensorsState[sensor];
        };
    }

    onSensor(data, sensor: string) {
        this.sensorsData[sensor] = data;
        if (sensor === 'barometer' && this.airportPressure != null) {
            // we can compute altitude
            this.currentAltitude = sensors.getAltitude(data.pressure, this.airportPressure);
        }
    }
    async enableLocation() {
        if (!geolocation) {
            geolocation = new GPS();
        }
        try {
            const r = await geolocation.isAuthorized();
            if (!r) {
                await geolocation.authorize(false);
            }

            if (!geolocation.isEnabled()) {
                return null;
            } else {
                return confirm({
                    // title: localize('stop_session'),
                    message: 'gps_not_enabled',
                    okButtonText: 'settings',
                    cancelButtonText: 'cancel'
                }).then((result) => {
                    if (!!result) {
                        return geolocation.openGPSSettings();
                    }
                    return Promise.reject();
                });
            }
        } catch (err) {
            if (err && /denied/i.test(err.message)) {
                confirm({
                    // title: localize('stop_session'),
                    message: 'gps_not_authorized',
                    okButtonText: 'settings',
                    cancelButtonText: 'cancel'
                }).then((result) => {
                    if (result) {
                        geolocation.openGPSSettings().catch(() => {});
                    }
                });
                return Promise.reject(undefined);
            } else {
                return Promise.reject(err);
            }
        }
    }
    getNearestAirportPressure() {
        return this.enableLocation().then(() => {
            geolocation
                .getCurrentLocation({ desiredAccuracy: CoreTypes.Accuracy.high, maximumAge: 120000 })
                .then((r) => sensors.getAirportPressureAtLocation('YOURAPIKEY', r.latitude, r.longitude))
                .then((r) => {
                    this.airportPressure = r.pressure;
                    alert(`found nearest airport pressure ${r.name} with pressure:${r.pressure} hPa`);
                })
                .catch((err) => {
                    alert(`could not find nearest airport pressure: ${err}`);
                });
        });
    }
    startStopSensor(sensor: sensors.SensorType) {
        console.log('startStopSensor', sensor, this.sensorStarted(sensor));
        if (this.sensorStarted(sensor)) {
            sensors.stopListeningForSensor(sensor, this.onSensor);
            this.sensorsState[sensor] = false;
        } else {
            sensors
                .startListeningForSensor(sensor, this.onSensor, 100)
                .then((r) => {
                    this.sensorsState[sensor] = r[0];
                })
                .catch((err) => alert(err));
        }
    }
}
</script>

<style scoped lang="scss">
ActionBar {
    background-color: #42b883;
    color: white;
}
Button {
    background-color: #42b883;
    color: white;
}
</style>
