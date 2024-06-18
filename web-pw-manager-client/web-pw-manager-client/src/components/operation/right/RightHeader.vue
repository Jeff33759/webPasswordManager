<script setup>
import {useStore} from "vuex";
import {computed, inject} from "vue";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";

const router = useRouter();
const {showConfirmMessage} = inject("messageBoxUtil");

const apiConst = inject("apiConst");
const {callDeleteApi} = inject("myApiSender");
const accessToken = computed(() => vuexStore.getters.accessToken);

const vuexStore = useStore(); //獲取Vuex實例
const selectedPasswordId = computed(() => vuexStore.getters.selectedPasswordId);
const selectedPasswordsArr = computed(() => vuexStore.getters.selectedPasswordsArr);
const passwords = computed(() => vuexStore.getters.passwords);

const clickAddPassword = () => {
  vuexStore.dispatch("setSelectedPasswordsArr", -1);
  vuexStore.dispatch("setHasClickedAddPwButton", true);

  resetPwTitleKeyWord();
}

const resetPwTitleKeyWord = () => {
  vuexStore.dispatch("setPWTitleKeyWord", "");

  const pwTitleKeyWordSearchBarElement = document.getElementById("pwTitleKeyWordSearchBar");
  if (pwTitleKeyWordSearchBarElement) {
    pwTitleKeyWordSearchBarElement.value = '';
  }
}

const clickDeletePassword = () => {

  showConfirmMessage(
      "確認要刪除密碼嗎?",
      "",
      null,
      null,
      () => {
        const headers = {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${accessToken.value}`
        };

        callDeleteApi(
            apiConst.serverAddr,
            `/api/password/${selectedPasswordId.value}`,
            null,
            headers,
            (response) => {
              let copyPasswords = Object.assign([], passwords.value);
              copyPasswords = copyPasswords.filter(password => password.id !== selectedPasswordId.value);
              vuexStore.dispatch("setPasswords", copyPasswords); //刷新vuex

              let copySelectedPasswordsArrPasswords = Object.assign([], selectedPasswordsArr.value);
              copySelectedPasswordsArrPasswords = copySelectedPasswordsArrPasswords.filter(selectedPassword => selectedPassword.id !== selectedPasswordId.value);
              vuexStore.dispatch("setSelectedPasswordsArr", copySelectedPasswordsArrPasswords); //刷新vuex

              router.push(`/operation`);
              ElMessage.success("刪除密碼成功");
            },
            (error) => {
              console.error(error);
              ElMessage.error(error.response.data.msg);
            }
        );
      },
      () => {

      }
  );

}
</script>

<template>
  <el-row justify="space-between"
          style="margin-bottom: 5px;padding: 5px;border-bottom: solid var(--el-color-info-light-5)">
    <el-col :span="14">
      <el-button @click="clickAddPassword" class="text-right" style="font-size: 1.2rem;">
        新增
      </el-button>
    </el-col>
    <el-col :span="4">
      <el-button @click="clickDeletePassword" :disabled="!selectedPasswordId" class="text-right full-width"
                 style="font-size: 1.2rem;">
        刪除
      </el-button>
    </el-col>

  </el-row>
</template>

<style scoped>

</style>