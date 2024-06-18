<script setup>
import {computed, inject, ref, watchEffect} from 'vue';
import {useStore} from "vuex";
import {ElMessage} from "element-plus";
import PasswordGenerator from "@/components/operation/right/PasswordGenerator.vue";
const apiConst = inject("apiConst");
const {callPostApi, callPutApi} = inject("myApiSender");
const vuexStore = useStore(); //獲取Vuex實例

const selectedPasswordInfo = computed(() => vuexStore.getters.selectedPasswordInfo);
const selectedPasswordId = computed(() => vuexStore.getters.selectedPasswordId);
const selectedPasswordsArr = computed(() => vuexStore.getters.selectedPasswordsArr);
const passwords = computed(() => vuexStore.getters.passwords);
const tags = computed(() => vuexStore.getters.tags);
const categories = computed(() => vuexStore.getters.categories);
const accessToken = computed(() => vuexStore.getters.accessToken);

const passwordGeneratorDialogVisible = ref(false);

const findTagNameById = (tags, tagId) => {

  if(tagId === 0) {
    return null;
  }
  const targetTag = tags.find(tag => tag.id === tagId);
  return targetTag ? targetTag.name : null;
}

const findTagIdByName = (tags, tagName) => {
  if(!tagName) {
    return 0;
  }
  const targetTag = tags.find(tag => tag.name === tagName);
  return targetTag ? targetTag.id : null;
}

const findCategoryNameById = (categories, categoryId) => {
  if(categoryId === -1) {
    return null;
  }

  const targetCategory = categories.find(category => category.id === categoryId);
  return targetCategory ? targetCategory.name : null;
}

const findCategoryIdByName = (categories, categoryName) => {
  if(!categoryName) {
    return -1;
  }
  const targetCategory = categories.find(category => category.name === categoryName);
  return targetCategory ? targetCategory.id : null;
}





const pwForm = ref(null);
const isPWFormLoading = ref(false);




const pwFormData = ref({
  "id": selectedPasswordInfo.value ? selectedPasswordInfo.value.id : -1,
  "categoryName":selectedPasswordInfo.value ? findCategoryNameById(categories.value, selectedPasswordInfo.value.categoryId) : "None",
  "title": selectedPasswordInfo.value ? selectedPasswordInfo.value.title : "",
  "webUrl": selectedPasswordInfo.value ? selectedPasswordInfo.value.webUrl : "",
  "password": selectedPasswordInfo.value ? selectedPasswordInfo.value.password : "",
  "dynamicEntries": selectedPasswordInfo.value ? selectedPasswordInfo.value.dynamicEntries : {},
  "remark": selectedPasswordInfo.value ? selectedPasswordInfo.value.remark : "",
  "tagName": selectedPasswordInfo.value ? findTagNameById(tags.value, selectedPasswordInfo.value.tagId) : "None",
})

// 監聽selectedPasswordInfo、tags、categories的變化，並更新pwFormData
watchEffect(() => {
  pwFormData.value.id = selectedPasswordInfo.value ? selectedPasswordInfo.value.id : -1;
  pwFormData.value.categoryName = selectedPasswordInfo.value ? findCategoryNameById(categories.value, selectedPasswordInfo.value.categoryId) : "None";
  pwFormData.value.title = selectedPasswordInfo.value ? selectedPasswordInfo.value.title : "";
  pwFormData.value.webUrl = selectedPasswordInfo.value ? selectedPasswordInfo.value.webUrl : "";
  pwFormData.value.password = selectedPasswordInfo.value ? selectedPasswordInfo.value.password : "";
  pwFormData.value.dynamicEntries = selectedPasswordInfo.value ? selectedPasswordInfo.value.dynamicEntries : {};
  pwFormData.value.remark = selectedPasswordInfo.value ? selectedPasswordInfo.value.remark : "";
  pwFormData.value.tagName = selectedPasswordInfo.value ? findTagNameById(tags.value, selectedPasswordInfo.value.tagId) : "None";
});


const pwRules = {
  categoryName: [
    {required: true, message: '分類不得為空。', trigger: 'blur'},
  ],
  title: [
    {required: true, message: '密碼標題不得為空。', trigger: 'blur'},
    {max: 50, message: '密碼標題限制50個字以內。', trigger: 'blur'},
  ],
  webUrl: [
    {max: 200, message: '網站連結限制200個字以內。', trigger: 'blur'},
  ],
  password: [
    {required: true, message: '密碼本文不得為空。', trigger: 'blur'},
    {max: 20, message: '密碼本文限制50個字以內。', trigger: 'blur'},
  ],
  remark: [
    {max: 500, message: '備註限制500個字以內。', trigger: 'blur'},
  ],
};

const submitPWForm = () => {
  pwForm.value.validate((valid) => {
    if(!valid) {
      ElMessage.error('參數錯誤。');
      return false;
    }

    isPWFormLoading.value = true;

    if(pwFormData.value.id === -1) {
      callAddPwApi();
      isPWFormLoading.value = false;
      return ;
    }

    callEditPwApi();
    isPWFormLoading.value = false;
  });
};

const callAddPwApi = () => {
  const headers = {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${accessToken.value}`
  };

  const body = {
    "categoryId": findCategoryIdByName(categories.value, pwFormData.value.categoryName),
    "title": pwFormData.value.title,
    "webUrl": pwFormData.value.webUrl,
    "password": pwFormData.value.password,
    "dynamicEntries": pwFormData.value.dynamicEntries, // TODO 動態欄位功能待新增
    "remark": pwFormData.value.remark,
    "tagId": findTagIdByName(tags.value, pwFormData.value.tagName)
  };

  callPostApi(
      apiConst.serverAddr,
      "/api/password",
      null,
      headers,
      body,
      (response) => {
        const myData = response.data.data;

        let copyPasswords = Object.assign([], passwords.value);
        copyPasswords.push({"id": myData.id, "title":myData.title, categoryId: myData.categoryId});
        vuexStore.dispatch("setPasswords", copyPasswords); //刷新vuex

        vuexStore.dispatch("setSelectedPasswordsArr", []);

        ElMessage.success("新增密碼成功");
      },
      (error) => {
        console.error(error);
        ElMessage.error(error.response.data.msg);
      }
  );
}

const callEditPwApi = () => {
  const headers = {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${accessToken.value}`
  };

  const body = {
    "title": pwFormData.value.title,
    "webUrl": pwFormData.value.webUrl,
    "password": pwFormData.value.password,
    "dynamicEntries": pwFormData.value.dynamicEntries, // TODO 動態欄位功能待新增
    "remark": pwFormData.value.remark,
    "tagId": findTagIdByName(tags.value, pwFormData.value.tagName)
  };

  callPutApi(
      apiConst.serverAddr,
      `/api/password/${pwFormData.value.id}`,
      null,
      headers,
      body,
      (response) => {
        const myData = response.data.data;

        //更新密碼列表
        let copyPasswords = Object.assign([], passwords.value);
        const targetPassword= copyPasswords.find(password => password.id === myData.id);
        if(targetPassword) {
          targetPassword.title = myData.title;
        }
        vuexStore.dispatch("setPasswords", copyPasswords); //刷新vuex

        //更新選中密碼列表
        vuexStore.dispatch("setSelectedPasswordsArr", []);
        vuexStore.dispatch("resetOperationMenuItemIsActive");

        ElMessage.success("編輯密碼成功");
      },
      (error) => {
        console.error(error);
        ElMessage.error(error.response.data.msg);
      }
  );
}

</script>

<template>
  <div class="right-body-content" v-if="selectedPasswordInfo">
    <el-form :model="pwFormData" :rules="pwRules" ref="pwForm" class="right-body-form-label-size">
      <el-form-item size="large" label="密碼標題" prop="title">
        <el-input v-model="pwFormData.title"></el-input>
      </el-form-item>
      <el-form-item size="large" label="網站連結" prop="webUrl">
        <el-input v-model="pwFormData.webUrl"></el-input>
      </el-form-item>
      <el-row :align="'top'" :justify="'space-between'">
        <el-col :span="19">
          <el-form-item size="large" label="密碼" prop="password" class="">
            <el-input v-model="pwFormData.password"></el-input>
          </el-form-item>
        </el-col>

        <el-col :span="4">
          <el-button @click="passwordGeneratorDialogVisible = true" type="primary" class="full-width">密碼生成器</el-button>
        </el-col>
      </el-row>

      <el-dialog v-model="passwordGeneratorDialogVisible">
        <PasswordGenerator />
      </el-dialog>



      <el-form-item size="large" label="備註" prop="remark">
        <el-input size="large" type="textarea" v-model="pwFormData.remark" :rows="4"></el-input>
      </el-form-item>
      <el-form-item size="large" label="分類" prop="categoryName">
        <el-select v-model="pwFormData.categoryName" :disabled="selectedPasswordId">
          <el-option
              v-for="(category) in categories"
              :label="category.name"
              :value="category.name">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item size="large" label="標籤" prop="tagName">
        <el-select v-model="pwFormData.tagName">
          <el-option :value="null">
          </el-option>
          <el-option
              v-for="(tag) in tags"
              :label="tag.name"
              :value="tag.name">
          </el-option>
        </el-select>
      </el-form-item>
      <el-row justify="space-around">
        <el-col :span="10">
          <el-button type="primary" @click="submitPWForm" :loading="isPWFormLoading" class="full-width">送出</el-button>
        </el-col>
      </el-row>
    </el-form>
  </div>



</template>

<style scoped>

.right-body-content {
  padding: 2rem;
}


</style>