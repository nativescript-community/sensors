# Change Log

All notable changes to this project will be documented in this file.
See [Conventional Commits](https://conventionalcommits.org) for commit guidelines.

# [1.4.0](https://github.com/nativescript-community/sensors/compare/v1.3.10...v1.4.0) (2023-08-11)


### Bug Fixes

* **android:** native listener is now working ([3b46c6b](https://github.com/nativescript-community/sensors/commit/3b46c6ba93979caaac40037fbe1fc0e9ffc12c5e))
* **ios:** bearing events were fired as barometer ([c5d5946](https://github.com/nativescript-community/sensors/commit/c5d59469fcd49b19ee03ddcd87eb24174375dba5))


### Features

* **android:** support `magnetometer_raw`, `accelerometer_raw` and `gyroscope_raw` ([2434797](https://github.com/nativescript-community/sensors/commit/2434797afe5abcf24efc4d12091daea78a79d2f7))





## [1.3.10](https://github.com/nativescript-community/sensors/compare/v1.3.9...v1.3.10) (2023-08-10)


### Bug Fixes

* removed `useCurrentThreadLooper` property and added `useSeparatedThread` instead. Current thread looper is now the default on android ([f4f5985](https://github.com/nativescript-community/sensors/commit/f4f59852eaf0f360b48893c5928d4587056d7c21))





## [1.3.9](https://github.com/nativescript-community/sensors/compare/v1.3.8...v1.3.9) (2023-08-09)


### Bug Fixes

* **android:** we now use the current thread so it will work with workers. Also we use JSONObject to communicate between JS/Java now so it should be faster ([e18796e](https://github.com/nativescript-community/sensors/commit/e18796e442c0b371f2d94a1009be939a3987ae04))





## [1.3.8](https://github.com/nativescript-community/sensors/compare/v1.3.7...v1.3.8) (2023-07-31)


### Bug Fixes

* **android:** only start thread when needed. Allow to set thread priority/name ([e96b1de](https://github.com/nativescript-community/sensors/commit/e96b1dec78a148f1a9d3389a66a6e0390307545c))





## [1.3.7](https://github.com/nativescript-community/sensors/compare/v1.3.6...v1.3.7) (2023-07-22)


### Bug Fixes

* **android:** broken build ([b565dcc](https://github.com/nativescript-community/sensors/commit/b565dcc49f20f11df7d8634f4f9759afb0e2c92a))





## [1.3.6](https://github.com/nativescript-community/sensors/compare/v1.3.5...v1.3.6) (2023-07-18)


### Bug Fixes

* events timestamp in float milliseconds to allow micro/nano precision ([a9c0bbe](https://github.com/nativescript-community/sensors/commit/a9c0bbe3b24d3a0000bdcae924514ec6bea2b2e0))





## [1.3.5](https://github.com/nativescript-community/sensors/compare/v1.3.4...v1.3.5) (2023-01-31)


### Bug Fixes

* import fix ([fbfe2f9](https://github.com/nativescript-community/sensors/commit/fbfe2f9e680460e88cdb2e6edcbe13e9a6ea4d96))





## [1.3.4](https://github.com/nativescript-community/sensors/compare/v1.3.3...v1.3.4) (2023-01-31)


### Bug Fixes

* issue preventing @nativescript/core tree shaking ([b973178](https://github.com/nativescript-community/sensors/commit/b973178f274c94519d31cdab6f25dae8ecd34777))





## [1.3.3](https://github.com/nativescript-community/sensors/compare/v1.3.2...v1.3.3) (2023-01-23)


### Bug Fixes

* **android:** improved native-api-usage ([5469064](https://github.com/nativescript-community/sensors/commit/546906488ce787b2d38fa3007db794406a653177))





## [1.3.2](https://github.com/Akylas/nativescript-sensors/compare/v1.3.1...v1.3.2) (2022-12-13)

**Note:** Version bump only for package @nativescript-community/sensors





## [1.3.1](https://github.com/Akylas/nativescript-sensors/compare/v1.3.0...v1.3.1) (2022-12-01)


### Bug Fixes

* import from @nativescript/core ([109aab2](https://github.com/Akylas/nativescript-sensors/commit/109aab229eba0606e1851fd567ce9e0aa153d7f9))





# [1.3.0](https://github.com/Akylas/nativescript-sensors/compare/v1.2.1...v1.3.0) (2022-11-29)


### Features

* **android:** native-api-usage ([8d8d492](https://github.com/Akylas/nativescript-sensors/commit/8d8d492ca0e5b8c75bb83e17d07875bd007cfccd))





## [1.2.1](https://github.com/Akylas/nativescript-sensors/compare/v1.2.0...v1.2.1) (2021-03-20)


### Bug Fixes

* **android:** hasSensor fix ([42ae574](https://github.com/Akylas/nativescript-sensors/commit/42ae574ea067240002144c587448d89c8170917d))





# [1.2.0](https://github.com/Akylas/nativescript-sensors/compare/v1.1.1...v1.2.0) (2021-02-24)


### Features

* heading. sensor ([af1fc3e](https://github.com/Akylas/nativescript-sensors/commit/af1fc3e111d9eeea652af0aae00063d9380d0842))





## [1.1.1](https://github.com/Akylas/nativescript-sensors/compare/v1.1.0...v1.1.1) (2020-11-02)

**Note:** Version bump only for package @nativescript-community/sensors





# [1.1.0](https://github.com/Akylas/nativescript-sensors/compare/v1.0.2...v1.1.0) (2020-09-06)


### Features

* N7 and new plugin name ([24f3189](https://github.com/Akylas/nativescript-sensors/commit/24f3189fcafb7be7b262888de1afc5a97decfa58))





## [1.0.2](https://github.com/nativescript-community/sensors/compare/v1.0.1...v1.0.2) (2020-05-30)


### Bug Fixes

* typings fix ([9a24139](https://github.com/nativescript-community/sensors/commit/9a24139f8cbdc4ca3e93d254f7785cb9929eeb56))





## [1.0.1](https://github.com/nativescript-community/sensors/compare/v1.0.0...v1.0.1) (2020-05-28)


### Bug Fixes

* esm and more ([c2b53f7](https://github.com/nativescript-community/sensors/commit/c2b53f782416c1ae0cc92741811b6b7e770de721))





# [1.0.0](https://github.com/nativescript-community/sensors/compare/v0.0.14...v1.0.0) (2019-12-16)


### Bug Fixes

* updated perms, migration to [@nativescript](https://github.com/nativescript) ([796806a](https://github.com/nativescript-community/sensors/commit/796806ac8cdd27d5b9ea6de07a3df71f79a8b41e))





## [0.0.14](https://github.com/nativescript-community/sensors/compare/v0.0.13...v0.0.14) (2019-09-30)


### Bug Fixes

* error fix when no data in onDeviceMotion ([72a7700](https://github.com/nativescript-community/sensors/commit/72a7700))





## [0.0.13](https://github.com/nativescript-community/sensors/compare/v0.0.12...v0.0.13) (2019-09-21)


### Bug Fixes

* some android fixes ([94228b8](https://github.com/nativescript-community/sensors/commit/94228b8))





## [0.0.12](https://github.com/nativescript-community/sensors/compare/v0.0.11...v0.0.12) (2019-09-18)


### Bug Fixes

* a lot of improvements ([550eaff](https://github.com/nativescript-community/sensors/commit/550eaff))





## [0.0.11](https://github.com/nativescript-community/sensors/compare/v0.0.10...v0.0.11) (2019-08-15)


### Bug Fixes

* cleanup logs ([3ea1df5](https://github.com/nativescript-community/sensors/commit/3ea1df5))





## [0.0.10](https://github.com/nativescript-community/sensors/compare/v0.0.9...v0.0.10) (2019-08-15)

**Note:** Version bump only for package @nativescript-community/sensors





## 0.0.9 (2019-08-15)


### Bug Fixes

* everything working ([1c5e40b](https://github.com/nativescript-community/sensors/commit/1c5e40b))





## [0.0.8](https://github.com/nativescript-community/sensors/compare/v0.0.7...v0.0.8) (2019-08-11)


### Bug Fixes

* signature ([6ed89bf](https://github.com/nativescript-community/sensors/commit/6ed89bf))
* throw error is view has no parent page ([e7f67a4](https://github.com/nativescript-community/sensors/commit/e7f67a4))





## [0.0.7](https://github.com/nativescript-community/sensors/compare/v0.0.6...v0.0.7) (2019-07-24)

**Note:** Version bump only for package @nativescript-community/sensors





## [0.0.6](https://github.com/nativescript-community/sensors/compare/v0.0.5...v0.0.6) (2019-07-19)

**Note:** Version bump only for package @nativescript-community/sensors





## [0.0.5](https://github.com/nativescript-community/sensors/compare/v0.0.4...v0.0.5) (2019-07-09)


### Bug Fixes

* remove wrong dependencies ([26946c2](https://github.com/nativescript-community/sensors/commit/26946c2))





## [0.0.4](https://github.com/nativescript-community/sensors/compare/v0.0.3...v0.0.4) (2019-06-12)


### Bug Fixes

* android one registry per page ([f57fccf](https://github.com/nativescript-community/sensors/commit/f57fccf))
* check for null ([0f0d83d](https://github.com/nativescript-community/sensors/commit/0f0d83d))





## [0.0.3](https://github.com/nativescript-community/sensors/compare/v0.0.2...v0.0.3) (2019-06-12)

**Note:** Version bump only for package @nativescript-community/sensors





## [0.0.2](https://github.com/nativescript-community/sensors/compare/v0.0.1...v0.0.2) (2019-06-12)

**Note:** Version bump only for package @nativescript-community/sensors





## 0.0.1 (2019-06-11)

**Note:** Version bump only for package @nativescript-community/sensors
