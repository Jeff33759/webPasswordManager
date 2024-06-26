<script setup>

import {reactive, ref} from "vue";
import {inject} from "vue";
import {ElMessage} from "element-plus";
import {useRouter} from 'vue-router';

const {showSuccessMessage} = inject("messageBoxUtil");
const apiConst = inject("apiConst");
const {callPostApi} = inject("myApiSender");
const registerForm = ref(null);
const router = useRouter();
const isLoading = ref(false);

const registerFormData = reactive({
  email: '',
  password: '',
  userName: ''
});

const rules = {
  email: [
    {required: true, message: '郵箱不得為空', trigger: 'blur'},
    {min: 10, max: 50, message: '郵箱限制10~50個字元。', trigger: 'blur'},
  ],
  password: [
    {required: true, message: '密碼不得為空', trigger: 'blur'},
    {min: 6, max: 20, required: true, message: '密碼限制6~20個字元。', trigger: 'blur'},
  ],
  userName: [
    {required: true, message: '用戶名不得為空', trigger: 'blur'},
    {min: 1, max: 20, message: '用戶名限制1~20個字元。', trigger: 'blur'}
  ]
};


const submitRegisterForm = () => {
  registerForm.value.validate((valid) => {
    if (!valid) {
      ElMessage.error('參數錯誤。');
      return false;
    }

    isLoading.value = true;

    const headers = {"Content-Type": "application/json"};
    const body = {
      "email": registerFormData.email,
      "password": registerFormData.password,
      "userName": registerFormData.userName
    };

    callPostApi(
        apiConst.serverAddr,
        "/api/auth/register",
        null,
        headers,
        body,
        (response) => {
          const myData = response.data.data;

          showSuccessMessage(`用戶${myData.userName}已註冊成功，請至郵箱${myData.email}收取激活信。如未收取，請嘗試登入後重發激活信。`, null, null, () => {
            router.push({path: '/'});
          });
        },
        (error) => {
          isLoading.value = false;
          ElMessage.error(error.response.data.msg);
        }
    );

  });
};

</script>


<template>
  <el-row justify="center" class="not-login-main-padding">
    <el-col :sm="18" :md="12">
      <el-form size="large" :model="registerFormData" :rules="rules" ref="registerForm" label-width="auto" class="my-form-label-size">
        <el-form-item label="郵箱" prop="email">
          <el-input v-model="registerFormData.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="密碼" prop="password">
          <el-input type="password" v-model="registerFormData.password" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="用戶名" prop="userName" @keyup.enter="submitRegisterForm">
          <el-input v-model="registerFormData.userName" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
    </el-col>
  </el-row>
  <el-row justify="center" style="padding: 0 1rem">
    <el-col :sm="12" :md="6">
      <el-form-item>
        <el-button type="primary" class="full-width full-height font-size-component-register" @click="submitRegisterForm" :loading="isLoading" :disabled="isLoading">送出</el-button>
      </el-form-item>
    </el-col>
  </el-row>

</template>


<style scoped>

.font-size-component-register {
  font-size: 2rem;
}

</style>