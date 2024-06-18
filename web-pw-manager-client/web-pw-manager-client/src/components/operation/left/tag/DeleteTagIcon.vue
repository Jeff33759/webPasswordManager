<script setup>
import {ref, defineProps, inject, computed} from 'vue';
import {Delete} from "@element-plus/icons-vue";
import {useStore} from "vuex";
import {ElMessage} from "element-plus"; // 使用computed屬性確保數據響應性


const vuexStore = useStore(); //獲取Vuex實例
const accessToken = computed(() => vuexStore.getters.accessToken);
const tags = computed(() => vuexStore.getters.tags);
const {showConfirmMessage} = inject("messageBoxUtil");
const {callDeleteApi} = inject("myApiSender");
const apiConst = inject("apiConst");

const props = defineProps({
  tagId: Number
});

const handleDeleteLeftSelect = (event, tagId) => {
  event.stopPropagation(); // 防止事件冒泡

  showConfirmMessage(
      "確認要刪除標籤嗎?",
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
            `/api/tag/${tagId}`,
            null,
            headers,
            (response) => {
              let copyTags = Object.assign([], tags.value);
              copyTags = copyTags.filter(tag => tag.id !== tagId);
              vuexStore.dispatch("setTags", copyTags); //刷新vuex

              vuexStore.dispatch("setSelectedPasswordsArr", []);
              vuexStore.dispatch("resetOperationMenuItemIsActive");

              ElMessage.success("刪除標籤成功");
            },
            (error) => {
              console.error(error);
              ElMessage.error(error.response.data.msg);
            }
        )

      },
      () => {

      }
  )

}



</script>

<template>
  <el-icon @click="($event) => handleDeleteLeftSelect($event,  props.tagId)" class="hover-light">
    <delete />
  </el-icon>
</template>

<style scoped>

</style>