import '@/css/main.css'

import {createApp} from 'vue'
import App from '@/App.vue'
import "element-plus/dist/index.css";
import elementPlus from 'element-plus'
import vuexStore from '@/vuexStore/VuexIndex';
import {router} from '@/router/route.js';
import {apiConst} from "@/js/api/ApiConst.js";
import {showSuccessMessage, showNotifyMessage, showAlertMessage, showConfirmMessage} from "@/js/MessageBoxUtil.js";
import {callPostApi, callPutApi, callGetApi, callDeleteApi} from "@/js/api/MyApiSender.js";
import {getMFATypeByNum, getMFATypeMapping, getMFATypeNumByName} from "@/js/MFATypeMapper.js";

createApp(App)
    //使用插件
    .use(vuexStore)
    .use(elementPlus)
    .use(router)
    //全域依賴注入
    .provide("messageBoxUtil", {showSuccessMessage, showNotifyMessage, showAlertMessage, showConfirmMessage})
    .provide("myApiSender", {callPostApi, callPutApi, callGetApi, callDeleteApi})
    .provide('apiConst', apiConst)
    .provide('MFATypeUtil', {getMFATypeByNum, getMFATypeMapping, getMFATypeNumByName})
    .mount('#app')

