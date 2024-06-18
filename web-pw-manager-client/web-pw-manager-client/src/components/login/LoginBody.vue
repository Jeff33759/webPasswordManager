<script setup>
import {inject, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {useRouter} from 'vue-router';
import {useStore} from 'vuex';


const {showSuccessMessage} = inject("messageBoxUtil");
const apiConst = inject("apiConst");
const {callPostApi} = inject("myApiSender");
const loginForm = ref(null);
const router = useRouter();
const vuexStore = useStore(); //獲取Vuex實例
const isLoading = ref(false);

const loginFormData = reactive({
  email: '',
  password: ''
});

const rules = {
  email: [
    {required: true, message: '郵箱不得為空', trigger: 'blur'},
    {min: 10, max: 50, message: '郵箱限制10~50個字元。', trigger: 'blur'},
  ],
  password: [
    {required: true, message: '密碼不得為空', trigger: 'blur'},
    {min: 6, max: 20, message: '密碼限制6~20個字元。', trigger: 'blur'},
  ]
};



const submitLoginForm = () => {
  loginForm.value.validate((valid) => {
    if(!valid) {
      ElMessage.error('參數錯誤。');
      return false;
    }

    isLoading.value = true;

    const headers = {"Content-Type": "application/json"};
    const body = {
      "email": loginFormData.email,
      "password": loginFormData.password,
    };

    callPostApi(
        apiConst.serverAddr,
        "/api/auth/login",
        null,
        headers,
        body,
        (response) => {
          const statusCode = response.status;

          if(statusCode === 202) { //有設置MFA
            vuexStore.dispatch("setEmailForMfaBeforeMFAVerification", loginFormData.email); // 設置email給MFA驗證的元件使用

            showSuccessMessage(response.data.msg, null, null, () => {
              router.push({path: '/mfa-verification'});
            });

            return;
          }

          const myData = response.data.data;
          vuexStore.dispatch("setAndParseAllToken", myData); //儲存Token到vuex讓各元件共用。

          showSuccessMessage(`登入成功。`, null, null, () => {
            router.push({path: '/operation'});
          });
        },
        (error) => {
          isLoading.value = false;
          console.error(error);
          ElMessage.error(error.response.data.msg);
        }
    );
  });
};

</script>

<template>
  <el-row justify="center" class="not-login-main-padding">
    <el-col :sm="18" :md="12">
      <el-form  size="large" :model="loginFormData" :rules="rules" ref="loginForm" class="my-form-label-size">
        <el-form-item label="郵箱" prop="email">
          <el-input v-model="loginFormData.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="密碼" prop="password">
          <el-input type="password" v-model="loginFormData.password" autocomplete="off" @keyup.enter="submitLoginForm"></el-input>
        </el-form-item>
      </el-form>
    </el-col>
  </el-row>
  <el-row justify="center" style="padding: 0 1rem">
    <el-col :sm="12" :md="6">
      <el-form-item>
        <el-button type="primary" class="full-width full-height font-size-component-login" @click="submitLoginForm" :loading="isLoading" :disabled="isLoading">送出</el-button>
      </el-form-item>
    </el-col>
  </el-row>
</template>

<style scoped>
.font-size-component-login {
  font-size: 2rem;
}

</style>