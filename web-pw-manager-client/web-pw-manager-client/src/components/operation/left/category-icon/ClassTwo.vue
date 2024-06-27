<script setup>
import { ref } from 'vue';
import avatarLogo from './class-two.svg';
const imageSrc = ref(avatarLogo);
</script>

<template>
  <el-image :src="imageSrc" class="full-width">
  </el-image>
</template>

<style scoped>

</style>