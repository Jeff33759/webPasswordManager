<script setup>
import { ref } from 'vue';
import frontPageImage from './frontPage.png';
const imageSrc = ref(frontPageImage);
</script>

<template>
  <el-image :src="imageSrc" class="full-width">
  </el-image>
</template>

<style scoped>

</style>