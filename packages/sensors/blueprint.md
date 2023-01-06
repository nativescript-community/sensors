{{ load:../../tools/readme/edit-warning.md }}
{{ template:title }}
{{ template:badges }}
{{ template:description }}

| <img src="https://raw.githubusercontent.com/nativescript-community/gps/master/images/demo-ios.gif" height="500" /> | <img src="https://raw.githubusercontent.com/nativescript-community/gps/master/images/demo-android.gif" height="500" /> |
| --- | ----------- |
| iOS Demo | Android Demo |

{{ template:toc }}

## Installation
Run the following command from the root of your project:

`ns plugin add {{ pkg.name }}`

## Usage

Here is a simple example. You can find more in the doc [here](https://nativescript-community.github.io/sensors)

```typescript
import { startListeningForSensor, stopListeningForSensor } from '@nativescript-community/sensors';
function onSensor(sensorData, sensorId: string) {

}
startListeningForSensor(sensor, this.onSensor);

stopListeningForSensor(sensor, this.onSensor);
```

### Examples:

- [Basic](demo-snippets/vue/Basic.vue)
  - A basic sliding drawer.
{{ load:../../tools/readme/demos-and-development.md }}
{{ load:../../tools/readme/questions.md }}