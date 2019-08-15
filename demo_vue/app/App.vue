<template>
    <Page @navigatedTo="onNavigatedTo">
        <ActionBar title="Sensors Demo" />
        <GridLayout>
            <ScrollView>
                <StackLayout>
                    <Label :text="'availableSensors: ' + sensors" textWrap />
                    <GridLayout v-for="(sensor) in sensors" rows="auto,auto,*" columns="*,auto" height="150" margin="10" backgroundColor="lightgray">
                        <Label :text="sensor" textTransform="uppercase" :color="sensorStarted(sensor) ? 'green':'red'" />
                        <Label col="2" :text="sensorTimeStamp(sensor)" horizontalAlignment="right" fontSize="10" />
                        <Label row="1" :text="sensorAccuracy(sensor)" fontSize="10" />
                        <Label row="2" :text="sensorDataText(sensor)" textWrap fontSize="10" verticalAlignment="top" />
                        <button rowSpan="3" col="1" ref="button" horizontalAlignment="right" verticalAlignment="middle" :text="sensorStarted(sensor) ? 'stop' : 'start'" @tap="startStopSensor(sensor)" />
                    </GridLayout>
                </StackLayout>
            </ScrollView>
        </GridLayout>

    </Page>
</template>

<script lang="ts">
import { GC } from 'utils/utils';
import BaseVueComponent from './BaseVueComponent';
import Component from 'vue-class-component';
import { View } from 'tns-core-modules/ui/page/page';
import { Provide } from 'vue-property-decorator';
import * as sensors from 'nativescript-sensors';
var dateFormat = require('dateformat');
import { SensorsTuple } from '../../src/sensors';

@Component({
    components: {}
})
export default class App extends BaseVueComponent {
    onNavigatedTo() {
        GC();
    }
    listeningForMotion = false;
    sensors: sensors.SensorType[] = [];
    mounted() {
        super.mounted();
        console.log('mounted');
        this.sensors = sensors.getAllavailableSensors();
        // this.sensors.forEach(s=>{
        //     this.sensorsData[s] = null;
        //     this.sensorsState[s] = false;
        // })
        console.log('sensors', JSON.stringify(this.sensors));
    }
    sensorsData: {
        [k in sensors.SensorsTuple]: any;
    } = (sensors.SENSORS as sensors.SensorType[]).reduce((accumulator, currentValue) => {
        accumulator[currentValue] = null;
        return accumulator;
    }, {});
    sensorsState: {
        [k in sensors.SensorsTuple]: boolean;
    } = (sensors.SENSORS as sensors.SensorType[]).reduce((accumulator, currentValue) => {
        accumulator[currentValue] = false;
        return accumulator;
    }, {});

    get sensorDataText() {
        return sensor => {
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
        return sensor => {
            const data = this.sensorsData[sensor];
            if (data && data.accuracy) {
                return `accuracy: ${data.accuracy}`;
            } else {
                return '';
            }
        };
    }
    get sensorTimeStamp() {
        return sensor => {
            const data = this.sensorsData[sensor];
            if (data && data.timestamp) {
                return dateFormat(new Date(data.timestamp), 'HH:mm:ss sss');
            } else {
                return '';
            }
        };
    }

    get sensorStarted() {
        return sensor => {
            return !!this.sensorsState[sensor];
        };
    }

    onSensor(data, sensor: string) {
        this.sensorsData[sensor] = data;
    }

    startStopSensor(sensor: sensors.SensorType) {
        console.log('startStopSensor', sensor, this.sensorStarted(sensor));
        if (this.sensorStarted(sensor)) {
            sensors.stopListeningForSensor(sensor, this.onSensor);
            this.sensorsState[sensor] = false;
        } else {
            sensors
                .startListeningForSensor(sensor, this.onSensor, 100)
                .then(r => {
                    this.sensorsState[sensor] = r;
                })
                .catch(err => alert(err));
        }
    }
}
</script>
