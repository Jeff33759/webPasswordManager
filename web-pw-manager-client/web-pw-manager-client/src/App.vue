<script setup>

</script>

<template>

  <el-container>
    <el-header id="my-header" class="my-background-color" style="height: 6rem;padding: 0 2px;">
      <router-view name="header"/>
    </el-header>

    <el-main class="my-background-color" style="height: 100vh;padding: 0;">
      <router-view name="body"/>
    </el-main>

  </el-container>

</template>

<style scoped>

#my-header {
  overflow: hidden;
  height: Auto;
  border-bottom: solid var(--el-color-info-light-5);
}


</style>
