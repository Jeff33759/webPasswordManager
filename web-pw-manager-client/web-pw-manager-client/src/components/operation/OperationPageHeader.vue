<script setup>
import {computed, ref, inject, reactive} from 'vue';
import {useStore} from "vuex"; // 使用computed屬性確保數據響應性
import Avatar from "@/components/operation/Avatar.vue";
import {ElMessage} from "element-plus";
import {useRouter} from 'vue-router';
import {showAlertMessage, showNotifyMessage} from "@/js/MessageBoxUtil.js";

const router = useRouter();
const {showSuccessMessage} = inject("messageBoxUtil");
const apiConst = inject("apiConst");
const {callPutApi, callPostApi} = inject("myApiSender");
const vuexStore = useStore(); //獲取Vuex實例
const accessToken = computed(() => vuexStore.getters.accessToken);
const refreshToken = computed(() => vuexStore.getters.refreshToken);
const userInfo = computed(() => vuexStore.getters.userInfo); //使用computed，當userInfo被更動，此組件才會跟著更新。
const {getMFATypeByNum, getMFATypeMapping, getMFATypeNumByName} = inject("MFATypeUtil");

const isUserEditionMode = ref(false);
const userEditDialogVisible = ref(false);
const isUserEditLoading = ref(false);
const userEditForm = ref(null);
const userInfoPageData = reactive({
  userName: userInfo.value.userName,
  email: userInfo.value.email,
  mfaTypeName: getMFATypeByNum(userInfo.value.mfaType),
  mfaTypeNum: userInfo.value.mfaType,
  isActivated: userInfo.value.activated
});

const validateMFATypeNameExist = (rule, mfaTypeName, callback) => {
  const valid = Object.values(getMFATypeMapping()).includes(mfaTypeName);

  if (!valid) {
    callback(new Error('二階段驗證選取錯誤。'));
    return;
  }

  callback();
};

const userEditRules = {
  userName: [
    {min: 1, max: 20, required: true, message: '用戶名限制1~20個字元。', trigger: 'blur'}
  ],
  mfaType: [
    {required: true, validator: validateMFATypeNameExist, message: '二階段驗證選取錯誤。', trigger: 'change'}
  ]
};

const resetUserInfoPageData = () => {
  userInfoPageData.userName = userInfo.value.userName;
  userInfoPageData.email = userInfo.value.email;
  userInfoPageData.mfaTypeName = getMFATypeByNum(userInfo.value.mfaType);
}


const openUserDialog = () => {
  isUserEditionMode.value = false;
  resetUserInfoPageData();
}

const submitUserEdit = () => {

  userEditForm.value.validate((valid) => {

    const selectedMFATypeNum = getMFATypeNumByName(userInfoPageData.mfaTypeName);

    if (!valid) {
      ElMessage.error('參數錯誤。');
      return false;
    }

    isUserEditLoading.value = true;

    const headers = {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${accessToken.value}`
    };

    const body = {
      "userName": userInfoPageData.userName,
      "mfaType": selectedMFATypeNum
    };

    callPutApi(
        apiConst.serverAddr,
        "/api/user",
        null,
        headers,
        body,
        (response) => {
          const myData = response.data.data;

          vuexStore.dispatch("setAndParseOnlyAccessToken", myData.accessToken);

          resetUserInfoPageData();
          isUserEditLoading.value = false;

          showSuccessMessage(`用戶資料修改成功。`, null, null, () => {
            userEditDialogVisible.value = false;
          });
        },
        (error) => {
          isUserEditLoading.value = false;
          ElMessage.error(error.response.data.msg);
        });
  });

}

const editMainPasswordDialogVisible = ref(false);
const isEditMainPasswordLoading = ref(false);
const editMainPasswordForm = ref(null);
const editMainPasswordPageData = reactive({
  password: null,
  newPassword: null
});

const openEditMainPasswordDialog = () => {
  isEditMainPasswordLoading.value = false;
  resetEditMainPasswordData();
}

const resetEditMainPasswordData = () => {
  editMainPasswordPageData.password = null;
  editMainPasswordPageData.newPassword = null;
}

const editMainPasswordRules = {
  password: [
    {min: 6, max: 20, required: true, message: '原密碼限制6~20個字元。', trigger: 'blur'}
  ],
  newPassword: [
    {min: 6, max: 20, required: true, message: '新密碼限制6~20個字元。', trigger: 'blur'}
  ]
};

const submitEditMainPassword = () => {

  editMainPasswordForm.value.validate((valid) => {

    if (!valid) {
      ElMessage.error('參數錯誤。');
      return false;
    }

    isEditMainPasswordLoading.value = true;

    const headers = {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${accessToken.value}`
    };

    const body = {
      "password": editMainPasswordPageData.password,
      "newPassword": editMainPasswordPageData.newPassword
    };

    callPutApi(
        apiConst.serverAddr,
        "/api/user/password",
        null,
        headers,
        body,
        (response) => {
          const myData = response.data.data;

          isEditMainPasswordLoading.value = false;

          showSuccessMessage(`登入密碼修改成功。`, null, null, () => {
            editMainPasswordDialogVisible.value = false;
          });
        },
        (error) => {
          ElMessage.error(error.response.data.msg);
          isEditMainPasswordLoading.value = false;
        });
  });

}


const logout = () => {
  router.push({path: '/'});
  ElMessage.success("登出成功。")
}


const renewAT = () => {

  if (!refreshToken.value) {
    console.log("Cancel the renew accessToken task.");
    return;
  }

  console.log("Start renew accessToken.");

  const headers = {
    "Content-Type": "application/json"
  }

  const body = {
    "refreshToken": refreshToken.value,
    "accessToken": accessToken.value
  }

  callPostApi(
      apiConst.serverAddr,
      "/api/auth/renew/access-token",
      null,
      headers,
      body,
      (response) => {
        const myData = response.data.data;

        vuexStore.dispatch("setAndParseOnlyAccessToken", myData.accessToken); //儲存Token到vuex讓各元件共用。

        prepareNextRenewATTask();
      },
      (error) => {

        if (error.response?.status === 401) {

          router.push({path: '/'});

          showNotifyMessage(
              "登入逾期，請重新登入。",
              null,
              null,
              () => {
              }
          );

          return;
        }

        console.error(error);

        router.push({path: '/'});

        showAlertMessage(
            "發生無法預期的錯誤，請重新登入。",
            null,
            null,
            () => {
            }
        );
      }
  );
}


const prepareNextRenewATTask = () => {

  if (!accessToken.value || !refreshToken.value || !userInfo.value) {
    console.error(`accessToken: ${accessToken.value}, refreshToken: ${refreshToken.value}, userInfo: ${userInfo.value}`);

    router.push({path: '/'});

    showAlertMessage(
        "發生錯誤，請重新登入。",
        null,
        null,
        () => {
        }
    );

    return;
  }

  const atExpMilli = userInfo.value.exp * 1000;
  const nowMilli = Date.now();
  const bufferMilli = 30 * 1000; // 過期前30秒刷新AT
  const delayMilli = (atExpMilli - nowMilli - bufferMilli);

  vuexStore.dispatch("cancelAllRenewATTimeoutIfPresent"); //開發途中有時會出現無窮遞迴，可能是IDE按下儲存後Vue在同一頁刷新所致，用這個來檢查並避免

  const renewATTimeoutId = setTimeout(() => {
    renewAT();
  }, delayMilli);

  vuexStore.dispatch("setRenewATTimeoutId", renewATTimeoutId); //以便登出時可以取消，讓用戶同個瀏覽器先後登入不同帳號時也不會報錯

  console.log(`The next renewAT time is ${nowMilli + delayMilli <= nowMilli ? new Date(nowMilli).toLocaleString() : new Date(nowMilli + delayMilli).toLocaleString()}.`);
}

prepareNextRenewATTask();

</script>

<template>
  <el-row justify="center" align="middle" class="full-height">
    <el-col :span="16" class="full-height text-align-center"
            style="border-right: solid var(--el-color-info-light-5);padding-right: 5px;">
      <el-row justify="center" class="full-height align-items">
        <el-col :span="19" style="font-size: 2rem;">
          歡迎使用本系統
        </el-col>
        <el-col :span="3" class="full-height">
          <el-row align="middle" class="full-height">
            <el-col :span="24">
              <el-button @click="editMainPasswordDialogVisible = true" class="text-right full-width no-padding"
                         style="font-size: 1rem;">
                更改密碼
              </el-button>
            </el-col>
            <el-col :span="24">
              <el-button @click="logout" class="text-right full-width no-padding" style="font-size: 1.5rem;">
                登出
              </el-button>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </el-col>

    <el-col :span="8" class="full-height">
      <el-row align="middle" class="full-height">
        <el-col :span="6" @click="userEditDialogVisible = true" class="cursor-pointer full-height">
          <el-tooltip class="box-item" effect="dark" content="點擊查看用戶詳情" placement="top">
            <Avatar/>
          </el-tooltip>
        </el-col>
        <el-col v-if="userInfo" :span="17" :offset="1" class="full-height">
          <el-scrollbar>
            <p>用戶名：{{ userInfo.userName }}</p>
            <p>郵箱：{{ userInfo.email }}</p>
            <p>二階段身份驗證：{{ getMFATypeByNum(userInfo.mfaType) }}</p>
          </el-scrollbar>
        </el-col>
      </el-row>
    </el-col>
  </el-row>

  <el-dialog @open="openUserDialog" v-model="userEditDialogVisible" title="用戶資訊" width="30%">
    <el-row :justify="'end'" style="margin-bottom: 2rem;">
      <el-col :span="8">
        <el-button @click="isUserEditionMode = !isUserEditionMode" class="text-right full-width"
                   style="font-size: 1.5rem">
          {{ isUserEditionMode ? "取消" : "編輯" }}
        </el-button>
      </el-col>
    </el-row>

    <el-form :model="userInfoPageData" :rules="userEditRules" ref="userEditForm" hide-required-asterisk
             class="my-user-edit-form-label-size" style="margin-top: 10px">
      <el-form-item size="large" label="用戶名" prop="userName">
        <el-input v-model="userInfoPageData.userName" :disabled="!isUserEditionMode"></el-input>
      </el-form-item>
      <el-form-item size="large" label="郵箱" prop="email">
        <el-input v-model="userInfoPageData.email" :disabled="true"></el-input>
      </el-form-item>

      <el-form-item size="large" label="二階段驗證" prop="mfaTypeName" hide-required-asterisk>
        <el-select v-model="userInfoPageData.mfaTypeName" :disabled="!isUserEditionMode">
          <el-option
              v-for="(mfaTypeName) in getMFATypeMapping()"
              :label="mfaTypeName"
              :value="mfaTypeName">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item size="large" label="郵箱激活" prop="email">
        <el-switch v-model="userInfoPageData.isActivated" :disabled="true"></el-switch>
        <span style="margin-left: 2px;font-size: 1.5rem">{{ userInfoPageData.isActivated ? "是" : "否" }}</span>
      </el-form-item>
    </el-form>

    <el-row :justify="'center'" style="border-top: solid var(--el-color-info-light-5); padding-top: 5px">
      <el-col :span="12">
        <el-button style="font-size: 1.5rem" size="large" type="primary" :loading="isUserEditLoading"
                   @click="submitUserEdit" class="full-width"
                   :class="{ invisible: !isUserEditionMode}">送出
        </el-button>
      </el-col>
    </el-row>
  </el-dialog>


  <el-dialog @open="openEditMainPasswordDialog" v-model="editMainPasswordDialogVisible" title="更改登入密碼"
             width="30%">
    <el-form :model="editMainPasswordPageData" :rules="editMainPasswordRules" ref="editMainPasswordForm"
             class="my-edit-main-password-form-label-size" style="margin-top: 10px">
      <el-form-item size="large" label="原密碼" prop="password">
        <el-input v-model="editMainPasswordPageData.password"></el-input>
      </el-form-item>
      <el-form-item size="large" label="新密碼" prop="newPassword">
        <el-input v-model="editMainPasswordPageData.newPassword"></el-input>
      </el-form-item>
    </el-form>
    <el-row :justify="'center'" style="border-top: solid var(--el-color-info-light-5); padding-top: 5px">
      <el-col :span="12">
        <el-button style="font-size: 1.5rem" size="large" type="primary" :loading="isEditMainPasswordLoading"
                   @click="submitEditMainPassword" class="full-width">送出
        </el-button>
      </el-col>
    </el-row>
  </el-dialog>
</template>

<style scoped>

</style>