<script setup>
import {ref, defineProps, inject, computed} from 'vue';
import {Edit} from "@element-plus/icons-vue";
import {useStore} from "vuex";
import {ElMessage, ElPopover} from "element-plus"; // 使用computed屬性確保數據響應性


const vuexStore = useStore(); //獲取Vuex實例
const accessToken = computed(() => vuexStore.getters.accessToken);
const tags = computed(() => vuexStore.getters.tags);
const {callPutApi} = inject("myApiSender");
const apiConst = inject("apiConst");

const props = defineProps({
  tagId: Number,
  tagName: String
});

const editTagForm = ref(null);
const isEditTagPopoverVisible = ref(false);
const isEditTagLoading = ref(false);
const editTagFormData = ref({
  tagName: props.tagName
})

const closeEditTagPopover = () => {
  isEditTagPopoverVisible.value = false;
}

const resetDataAndOpenEditTagPopover = (event) => {
  event.stopPropagation(); // 防止事件冒泡

  editTagFormData.value.tagName = props.tagName;
  isEditTagLoading.value = false;
  isEditTagPopoverVisible.value = true;
}

const editTagRules = {
  tagName: [
    {required: true, message: '新標籤名不得為空', trigger: 'blur'},
    {max: 20, message: '標籤名限制20個字以內。', trigger: 'blur'},
  ]
};

const submitEditTagForm = () => {
  editTagForm.value.validate((valid) => {
    if(!valid) {
      ElMessage.error('參數錯誤。');
      return false;
    }

    isEditTagLoading.value = true;

    const headers = {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${accessToken.value}`
    };

    const body = {
      "tagName": editTagFormData.value.tagName
    };

    callPutApi(
        apiConst.serverAddr,
        `/api/tag/${props.tagId}`,
        null,
        headers,
        body,
        (response) => {
          const myData = response.data.data;

          let copyTags = Object.assign([], tags.value);
          copyTags = copyTags.filter(tag => tag.id !== props.tagId);
          copyTags.push({"id": myData.tagId, "name":myData.tagName});
          vuexStore.dispatch("setTags", copyTags); //刷新vuex
          vuexStore.dispatch("setSelectedPasswordsArr", []);
          vuexStore.dispatch("resetOperationMenuItemIsActive");

          closeEditTagPopover();

          ElMessage.success("編輯標籤成功");
        },
        (error) => {
          isEditTagLoading.value = false;
          console.error(error);
          ElMessage.error(error.response.data.msg);
        }
    );
  });
};

</script>

<template>
  <el-popover placement="right" :width="300" :visible="isEditTagPopoverVisible">
    <template #reference>
      <el-icon @click="($event) => resetDataAndOpenEditTagPopover($event)" class="hover-light">
        <edit />
      </el-icon>
    </template>

    <el-form :model="editTagFormData" :rules="editTagRules" ref="editTagForm">
      <el-form-item label="標籤名" prop="tagName">
        <el-input v-model="editTagFormData.tagName"></el-input>
      </el-form-item>
      <el-row justify="space-around">
        <el-col :span="10">
          <el-button @click="closeEditTagPopover" :loading="isEditTagLoading" class="full-width">取消</el-button>
        </el-col>
        <el-col :span="10">
          <el-button type="primary" @click="submitEditTagForm" :loading="isEditTagLoading" class="full-width">確定</el-button>
        </el-col>
      </el-row>
    </el-form>
  </el-popover>




</template>

<style scoped>

</style>