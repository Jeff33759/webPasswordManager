<script setup>
import Cover from './Cover.vue'
</script>

<template>
  <el-row justify="center">
    <el-col :span="7" style="margin-bottom: 1rem;margin-top: 1rem;">
      <Cover fit="contain"/>
    </el-col>
  </el-row>
  <el-row justify="space-around" style="height: 3rem">
    <el-col :span="8" class="full-width">
      <router-link to="/register" >
        <el-button type="primary" class="full-width full-height font-size-button-footer">註冊</el-button>
      </router-link>
    </el-col>
    <el-col :span="8" class="full-width">
      <router-link to="/login">
        <el-button type="primary" class="full-width full-height font-size-button-footer">登入</el-button>
      </router-link>
    </el-col>
  </el-row>
</template>

<style scoped>

.font-size-button-footer {
  font-size: 2rem;
}

</style>