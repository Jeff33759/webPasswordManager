import {createRouter, createWebHistory} from 'vue-router'
import FrontPageHeader from "@/components/frontpage/FrontPageHeader.vue";
import FrontPageBody from "@/components/frontpage/FrontPageBody.vue";
import RegisterHeader from '@/components/register/RegisterHeader.vue'
import RegisterBody from '@/components/register/RegisterBody.vue'
import LoginBody from '@/components/login/LoginBody.vue'
import LoginHeader from '@/components/login/LoginHeader.vue'
import OperationPageHeader from "@/components/operation/OperationPageHeader.vue";
import OperationPageBody from "@/components/operation/OperationPageBody.vue";
import MFAVerificationHeader from "@/components/login/MFAVerificationHeader.vue";
import MFAVerificationBody from "@/components/login/MFAVerificationBody.vue";
import vuexStore from '@/vuexStore/VuexIndex.js';
import {ElMessage} from "element-plus";
import OperationLeft from "@/components/operation/left/OperationLeft.vue";
import OperationMiddle from "@/components/operation/middle/OperationMiddle.vue";
import OperationRight from "@/components/operation/right/OperationRight.vue";

export const router = createRouter({

    history: createWebHistory(),
    routes: [
        {
            path: '/',
            components: {
                header: FrontPageHeader,
                body: FrontPageBody
            }
        },
        {
            path: '/register',
            components: {
                header: RegisterHeader,
                body: RegisterBody
            }
        },
        {
            path: '/login',
            components: {
                header: LoginHeader,
                body: LoginBody
            }
        },
        {
            path: '/mfa-verification',
            components: {
                header: MFAVerificationHeader,
                body: MFAVerificationBody
            }
        },
        {
            path: '/operation',
            components: {
                header: OperationPageHeader,
                body: OperationPageBody
            },
            children: [
                {
                    path: '', // 子路由的預設路徑為空，表示在 '/operation' 路徑下直接渲染 OperationLeft 元件
                    components: {
                        operationLeft: OperationLeft,
                        operationMiddle: OperationMiddle,
                        operationRight: OperationRight
                    }
                },
                {
                    path: '/operation/pw-info/:pwId',
                    components: {
                        operationLeft: OperationLeft,
                        operationMiddle: OperationMiddle,
                        operationRight: OperationRight
                    }
                }
            ]
        }
    ]
});

const publicPaths = ["/", "/login", "/register", "/mfa-verification"];

router.beforeEach((to, from, next) => { // 如果用戶從login進來，那from.path="/login"，to.path=所在頁面，next用來控制導航的進行。
    if (!publicPaths.includes(to.path) && !vuexStore.getters.accessToken) {
        ElMessage.error("發生錯誤，請重新登入。")
        next("/"); //導到首頁
        return;
    }

    if(publicPaths.includes(to.path)) { //防止用戶上一頁再下一頁回操作頁面，造成一些Vuex資料異常
        vuexStore.dispatch("clearAllLoginStates");
    }

    next(); //正常進入頁面
});

