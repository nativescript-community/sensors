// require('./ts_helpers');
import Vue from 'nativescript-vue';
import App from './App.vue';
import { knownFolders } from '@nativescript/core/file-system';
import * as application from '@nativescript/core/application';

const currentApp = knownFolders.currentApp();
require('source-map-support').install({
    environment: 'node',
    handleUncaughtExceptions: false,
    retrieveSourceMap(source) {
        const sourceMapPath = source + '.map';
        const appPath = currentApp.path;
        let sourceMapRelativePath = sourceMapPath
            .replace('file:///', '')
            .replace('file://', '')
            .replace(appPath + '/', '');
        if (sourceMapRelativePath.startsWith('app/')) {
            sourceMapRelativePath = sourceMapRelativePath.slice(4);
        }
        return {
            url: sourceMapRelativePath,
            map: currentApp.getFile(sourceMapRelativePath).readTextSync()
        };
    }
});

application.on(application.discardedErrorEvent, args => {
    const error = args.error;
    // const jsError = new Error(error.message);
    // jsError.stack = error.stackTrace;
    console.log(error);
    console.log('[stackTrace test Value]', error.stackTrace);
    console.log('[stack test value]', error.stack);
    // throw jsError;
});

// Error.prepareStackTrace = function prepareStackTrace(error, stack) {
//     console.log('prepareStackTrace test');
// }

import CollectionViewPlugin from 'nativescript-collectionview/vue';
Vue.use(CollectionViewPlugin);

// Prints Vue logs when --env.production is *NOT* set while building
Vue.config.silent = true;
// setShowDebug(true)
// Vue.config.silent = (TNS_ENV === 'production')

new Vue({
    render: h => h('frame', [h(App)])
}).$start();
