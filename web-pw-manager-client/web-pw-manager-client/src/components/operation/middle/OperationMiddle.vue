<script setup>
import {computed, watch, inject, ref} from "vue";
import {useStore} from "vuex";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";

const router = useRouter();
const apiConst = inject("apiConst");
const {callGetApi} = inject("myApiSender");
const vuexStore = useStore(); //獲取Vuex實例
const selectedPasswordsArr = computed(() => vuexStore.getters.selectedPasswordsArr);
const selectedPasswordId = computed(() => vuexStore.getters.selectedPasswordId);
const accessToken = computed(() => vuexStore.getters.accessToken);

const pwTitleKeyWordThisComponent = computed({ //與Vuex的pwTitleKeyWord雙向綁定。注意ref並沒有指向Vuex的pwTitleKeyWord。
  get() { //當試圖訪問pwTitleKeyWordThisComponent的值時，實際上會去調用getter
    return vuexStore.getters.pwTitleKeyWord;
  },
  set(value) { //當更改pwTitleKeyWordThisComponent的值時(例如Input標籤)，實際上會去調用setter
    vuexStore.dispatch("setPWTitleKeyWord", value);
  }
});

const selectPassword = (passwordId) => {
  vuexStore.dispatch("setSelectedPasswordId", passwordId);

  const headers = {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${accessToken.value}`
  };

  callGetApi(
      apiConst.serverAddr,
      `/api/password/${selectedPasswordId.value}`,
      null,
      headers,
      (response) => {
        const myData = response.data.data;

        vuexStore.dispatch("setSelectedPasswordInfo", myData)
        router.push(`/operation/pw-info/${passwordId}`);

      },
      (error) => {
        ElMessage.error("發生錯誤，請重試。");
        console.error(error);
      }
  );

};

watch(selectedPasswordsArr, (value) => { //監聽selectedPasswordsArr，當有變動就執行以下
  if(Array.isArray(value)) { //查詢出0個密碼或者多個密碼
    vuexStore.dispatch("setSelectedPasswordId", null);
    vuexStore.dispatch("setSelectedPasswordInfo", null);

    return;
  }

  //點擊了新增密碼進來。
  vuexStore.dispatch("setSelectedPasswordId", null);

  const newPasswordInfoTemplate = {
    "id": -1,
    "categoryId": -1,
    "title": "",
    "webUrl": "",
    "password": "",
    "dynamicEntries":{}, //動態欄位，若無會傳空Json
    "remark":"", //備註，若無會傳空字串
    "tagId": 0 //所屬標籤ID，若無會傳0
  };

  vuexStore.dispatch("setSelectedPasswordInfo", newPasswordInfoTemplate);
});

const filterPasswordsByPWTitleKeyWord = computed(() => {
  return pwTitleKeyWordThisComponent.value === '' ?
      selectedPasswordsArr.value : selectedPasswordsArr.value.filter(password => password.title.includes(pwTitleKeyWordThisComponent.value));
});


</script>

<template>
  <div class="middle-container full-height">
    <el-input id="pwTitleKeyWordSearchBar" v-model="pwTitleKeyWordThisComponent" placeholder="密碼標題搜尋" clearable style="margin-bottom: 5px" />
    <div v-if="(Array.isArray(selectedPasswordsArr) && selectedPasswordsArr.length === 0) || selectedPasswordsArr === -1" class="vertical-text full-width text-align-center">
      請選取左側菜單，或者點擊右側新增密碼。
    </div>
    <div v-if="Array.isArray(selectedPasswordsArr)" v-for="password in filterPasswordsByPWTitleKeyWord" @click="selectPassword(password.id)" :class="{ 'selected': password.id === selectedPasswordId }" class="password-info-ele full-width">
      {{ password.title }}
    </div>
  </div>

</template>

<style scoped>

.middle-container {
  border-right: solid var(--el-color-info-light-5);
  border-left: solid var(--el-color-info-light-5);
  background-color: #FFFFFF;
}

.password-info-ele {
  padding: 1rem 0;
  font-size: 1.2rem;
  border-bottom: solid var(--el-border-color);
  text-align: center;
  background-color: var(--el-color-info-light-9);
}

.password-info-ele:hover {
  background-color: var(--el-color-info-light-7);
  cursor: pointer;
}

.password-info-ele.selected {
  background-color: var(--el-color-info-light-5);
}

.vertical-text {
  writing-mode: vertical-rl; /* 文字從右到左豎排 */
  font-size: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>