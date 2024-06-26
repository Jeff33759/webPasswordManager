<script setup>
import {inject, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {useRouter} from 'vue-router';
import {useStore} from 'vuex';


const {showSuccessMessage} = inject("messageBoxUtil");
const apiConst = inject("apiConst");
const {callPostApi} = inject("myApiSender");
const mfaVerificationForm = ref(null);
const router = useRouter();
const vuexStore = useStore(); //獲取Vuex實例
const isLoading = ref(false);

const mfaVerificationFormData = reactive({
  verificationCode: ''
});

const rules = {
  verificationCode: [
    {required: true, message: '驗證碼不得為空。', trigger: 'blur'},
    {min: 6, max: 6, message: '驗證碼必須為6個字。', trigger: 'blur'}
  ]
};


const submitMFAVerificationForm = () => {
  mfaVerificationForm.value.validate((valid) => {
    if (!valid) {
      ElMessage.error('參數錯誤。');
      return false;
    }

    isLoading.value = true;

    const headers = {"Content-Type": "application/json"};
    const body = {
      "email": vuexStore.getters.emailForMfa,
      "verificationCode": mfaVerificationFormData.verificationCode
    };

    callPostApi(
        apiConst.serverAddr,
        "/api/auth/login/verification",
        null,
        headers,
        body,
        (response) => {
          const myData = response.data.data;
          vuexStore.dispatch("setAndParseAllToken", myData); //儲存Token到vuex讓各元件共用。

          showSuccessMessage(`登入成功。`, null, null, () => {
            router.push({path: '/operation'});
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
      <el-form size="large" :model="mfaVerificationFormData" :rules="rules" ref="mfaVerificationForm" class="my-form-label-size" label-width="auto" @submit.native.prevent> <!-- 當表單只有一個input，就需要這樣去阻止預設的提交事件，不然按下Enter會有很奇怪的現象 -->
        <el-form-item label="驗證碼" prop="verificationCode" @keyup.native.enter="submitMFAVerificationForm"> <!-- 當表單只有一個input，就需要這樣去阻止預設的提交事件，不然按下Enter會有很奇怪的現象 -->
          <el-input v-model="mfaVerificationFormData.verificationCode" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
    </el-col>
  </el-row>
  <el-row justify="center" style="padding: 0 1rem">
    <el-col :sm="12" :md="6">
      <el-form-item>
        <el-button type="primary" class="full-width full-height font-size-component-mfa" @click="submitMFAVerificationForm"
                   :loading="isLoading" :disabled="isLoading">送出
        </el-button>
      </el-form-item>
    </el-col>
  </el-row>
</template>

<style scoped>

.font-size-component-mfa {
  font-size: 2rem;
}

</style>