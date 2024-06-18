<script setup>
import {computed, inject} from "vue";
import {showAlertMessage} from "@/js/MessageBoxUtil.js";
import {useStore} from "vuex"; // 使用computed屬性確保數據響應性
import {useRouter} from 'vue-router';

const router = useRouter();
const vuexStore = useStore(); //獲取Vuex實例
const apiConst = inject("apiConst");
const {callGetApi} = inject("myApiSender");
const accessToken = computed(() => vuexStore.getters.accessToken);


const handleLoadNecessaryDataError = (error) => {
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

const loadAllNecessaryData = () => {
  loadAllCategories();
  loadAllTags();
  loadAllPasswords();
}

const loadAllCategories = () => {

  const headers = {
    "Authorization": `Bearer ${accessToken.value}`
  }

  callGetApi(
      apiConst.serverAddr,
      "/api/categories",
      null,
      headers,
      (response) => {
        const myData = response.data.data;

        vuexStore.dispatch("setCategories", myData.categoriesList);

        console.log("Load categories successfully.")
      },
      (error) => {
        handleLoadNecessaryDataError(error);
      }
  )
}

const loadAllTags = () => {

  const headers = {
    "Authorization": `Bearer ${accessToken.value}`
  }

  callGetApi(
      apiConst.serverAddr,
      "/api/tags",
      null,
      headers,
      (response) => {
        const myData = response.data.data;

        vuexStore.dispatch("setTags", myData.tagsList);

        console.log("Load all tags successfully.")
      },
      (error) => {
        handleLoadNecessaryDataError(error);
      }
  )

}

const loadAllPasswords = () => {

  const headers = {
    "Authorization": `Bearer ${accessToken.value}`
  }

  callGetApi(
      apiConst.serverAddr,
      "/api/passwords",
      null,
      headers,
      (response) => {
        const myData = response.data.data;

        vuexStore.dispatch("setPasswords", myData.passwordsList);

        console.log("Load all passwords successfully.")
      },
      (error) => {
        handleLoadNecessaryDataError(error);
      }
  )

}

loadAllNecessaryData();


</script>

<template>
  <el-row class="full-height">
    <el-col :span="5">
      <router-view name="operationLeft"/>
    </el-col>
    <el-col :span="4">
      <router-view name="operationMiddle"/>
    </el-col>
    <el-col :span="15">
      <router-view name="operationRight"/>
    </el-col>
  </el-row>
</template>

<style scoped>

</style>