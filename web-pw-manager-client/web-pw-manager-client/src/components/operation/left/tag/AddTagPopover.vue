<script setup>
import {computed, inject, ref} from 'vue'
import {ElMessage, ElPopover} from 'element-plus';
import {useStore} from "vuex"; // 使用computed屬性確保數據響應性

const vuexStore = useStore(); //獲取Vuex實例
const tags = computed(() => vuexStore.getters.tags);
const accessToken = computed(() => vuexStore.getters.accessToken);

const apiConst = inject("apiConst");
const {callPostApi} = inject("myApiSender");


const addTagForm = ref(null);
const isAddTagPopoverVisible = ref(false);
const isAddTagLoading = ref(false);
const addTagFormData = ref({
  tagName: ""
})


const closeAddTagPopover = () => {
  isAddTagPopoverVisible.value = false;
}

const resetDataAndOpenAddTagPopover = () => {
  addTagFormData.value.tagName = "";
  isAddTagLoading.value = false;
  isAddTagPopoverVisible.value = true;
}

const addTagRules = {
  tagName: [
    {required: true, message: '標籤名不得為空', trigger: 'blur'},
    {max: 20, message: '標籤名限制20個字以內。', trigger: 'blur'},
  ]
};

const submitAddTagForm = () => {
  addTagForm.value.validate((valid) => {
    if(!valid) {
      ElMessage.error('參數錯誤。');
      return false;
    }

    isAddTagLoading.value = true;

    const headers = {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${accessToken.value}`
    };

    const body = {
      "tagName": addTagFormData.value.tagName
    };

    callPostApi(
        apiConst.serverAddr,
        "/api/tag",
        null,
        headers,
        body,
        (response) => {
          const myData = response.data.data;

          let copyTags = Object.assign([], tags.value);
          copyTags.push({"id": myData.tagId, "name":myData.tagName});
          vuexStore.dispatch("setTags", copyTags); //刷新vuex

          closeAddTagPopover();

          ElMessage.success("新增標籤成功");
        },
        (error) => {
          isAddTagLoading.value = false;
          console.error(error);
          ElMessage.error(error.response.data.msg);
        }
    );
  });
};

</script>

<template>
  <el-popover placement="right" :width="300" :visible="isAddTagPopoverVisible">
    <template #reference>
      <el-button @click="resetDataAndOpenAddTagPopover" class="full-width" style="font-size: 1.2rem;">
        新增標籤
      </el-button>
    </template>

    <el-form :model="addTagFormData" :rules="addTagRules" ref="addTagForm">
      <el-form-item label="標籤名" prop="tagName">
        <el-input v-model="addTagFormData.tagName"></el-input>
      </el-form-item>
      <el-row justify="space-around">
        <el-col :span="10">
          <el-button @click="closeAddTagPopover" :loading="isAddTagLoading" class="full-width">取消</el-button>
        </el-col>
        <el-col :span="10">
          <el-button type="primary" @click="submitAddTagForm" :loading="isAddTagLoading" class="full-width">確定</el-button>
        </el-col>
      </el-row>
    </el-form>
  </el-popover>
</template>

<style scoped>

</style>