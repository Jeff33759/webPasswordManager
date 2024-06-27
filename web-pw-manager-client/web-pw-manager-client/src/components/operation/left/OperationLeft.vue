<script setup>
import {computed, inject, onMounted, ref, watch} from "vue";
import {useStore} from "vuex";
import AddTagPopover from "@/components/operation/left/tag/AddTagPopover.vue";
import DeleteTagIcon from "@/components/operation/left/tag/DeleteTagIcon.vue";
import EditTagIcon from "@/components/operation/left/tag/EditTagIcon.vue";
import {useRouter} from 'vue-router';
import {showAlertMessage} from "@/js/MessageBoxUtil.js";
import {ElMessage} from "element-plus";
import Avatar from "@/components/operation/Avatar.vue";
import CategoryClassOneIcon from "./category-icon/ClassOne.vue";
import CategoryClassTwoIcon from "./category-icon/ClassTwo.vue";

const {callGetApi} = inject("myApiSender");
const apiConst = inject("apiConst");

const vuexStore = useStore(); //獲取Vuex實例
const categoriesList = computed(() => vuexStore.getters.categories);
const tagsList = computed(() => vuexStore.getters.tags);
const passwordsList = computed(() => vuexStore.getters.passwords);
const accessToken = computed(() => vuexStore.getters.accessToken);
const hasClickedAddPwButton = computed(() => vuexStore.getters.hasClickedAddPwButton);
const activeLeftMenuItem = computed(() => vuexStore.getters.activeLeftMenuItem);
const router = useRouter();

const handleLeftSelect = (index) => {

  vuexStore.dispatch("setActiveLeftMenuItem", index);

  const parts = index.split('-');

  if (parts.length === 1) { //all
    let copyPasswords = Object.assign([], passwordsList.value); //為了讓點擊all的時候，資料也跟著變動，所以複製了個陣列
    vuexStore.dispatch("setSelectedPasswordsArr", copyPasswords);

    router.push(`/operation`);
    return;
  }

  const prefix = parts[0];
  const idToFind = parseInt(parts[1]);

  switch (prefix) {
    case "category":
      const sameCategoryPasswords = passwordsList.value.filter(password => password.categoryId === idToFind);
      vuexStore.dispatch("setSelectedPasswordsArr", sameCategoryPasswords);
      router.push(`/operation`);
      break;
    case "tag":

      const headers = {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${accessToken.value}`
      };

      callGetApi(
          apiConst.serverAddr,
          `/api/passwords/${idToFind}`,
          null,
          headers,
          (response) => {
            const myData = response.data.data;

            vuexStore.dispatch("setSelectedPasswordsArr", myData.passwordsList);
            router.push(`/operation`);
          },
          (error) => {
            ElMessage.error("發生錯誤，請重試。");
            console.error(error);
          }
      );


      break;
    default:
      router.push({path: '/'});

      showAlertMessage(
          "發生無法預期的錯誤，請重新登入。",
          null,
          null,
          () => {
          }
      );
  }

}

watch(hasClickedAddPwButton, (newValue) => {

  if(newValue) {
    // 以下取消left menu的選中狀態

    vuexStore.dispatch("resetOperationMenuItemIsActive");

    vuexStore.dispatch("setHasClickedAddPwButton", false);
  }

});

</script>

<template>
  <el-menu :default-active="activeLeftMenuItem" id="opeLeftMenu" class="my-menu" @select="handleLeftSelect">
    <el-menu-item index="all">
      <span>列出全部</span>
    </el-menu-item>
    <el-sub-menu index="category">
      <template #title>
        <span>分類</span>
      </template>
      <el-menu-item v-for="category in categoriesList" :key="category.id" :index="'category-' + category.id"
                    class="text-overflow-hidden">
        <div class="menu-item-category-content full-height">
          <div class="icon-container full-height">
            <CategoryClassOneIcon v-if="category.id === 1" />
            <CategoryClassTwoIcon v-else-if="category.id === 2" />
          </div>
          <div class="text-overflow-hidden" :title="category.name">
            {{ category.name }}
          </div>
        </div>

      </el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="tag">
      <template #title>
        <span>標籤</span>
      </template>
      <el-menu-item v-for="tag in tagsList" :key="tag.id" :index="'tag-' + tag.id">
        <div class="menu-item-content full-width">
          <div class="text-overflow-hidden" :title="tag.name">
            {{ tag.name }}
          </div>
          <div>
            <EditTagIcon :tagId=tag.id :tagName=tag.name />
            <DeleteTagIcon :tagId=tag.id />
          </div>
        </div>
      </el-menu-item>
      <addTagPopover/>
    </el-sub-menu>
  </el-menu>


</template>

<style scoped>


.menu-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.menu-item-category-content {
  display: flex;
  align-items: center;
}

.icon-container {
  margin-right: 10px;
  max-width: 25px;
  display: flex;
}
</style>