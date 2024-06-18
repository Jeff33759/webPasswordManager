<script setup>
import { ref } from 'vue';
import avatarLogo from './avatar.svg';
const imageSrc = ref(avatarLogo);
</script>

<template>
  <el-image :src="imageSrc" class="full-height">
  </el-image>
</template>

<style scoped>

</style>