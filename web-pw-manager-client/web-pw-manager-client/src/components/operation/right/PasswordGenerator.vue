<script setup>
import {ref, computed} from 'vue';

const form = ref({
  length: 12,
  includeNumbers: true,
  includeSymbols: true,
  password: ''
});

const generatePassword = () => {
  const length = form.value.length;
  const baseCharset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  const numbers = "0123456789";
  const symbols = "!@#$%^&*()_+~`|}{[]:;?><,./-=";

  let useCharacters = baseCharset;

  if (form.value.includeNumbers) {
    useCharacters += numbers;
  }

  if (form.value.includeSymbols) {
    useCharacters += symbols;
  }

  let output = "";

  for (let i = 0; i < length; i++) {
    output += useCharacters.charAt(Math.floor(Math.random() * useCharacters.length)); //從useCharacters隨機拿一個出來
  }

  form.value.password = output;
};

const calculatePasswordStrength = (password) => {
  let strength = 0;

  if (password.length >= 8) {
    strength += 1;
  }

  if (/[A-Z]/.test(password)) {
    strength += 1;
  }

  if (/[a-z]/.test(password)) {
    strength += 1;
  }

  if (/[0-9]/.test(password)) {
    strength += 1;
  }

  if (/[^A-Za-z0-9]/.test(password)) {
    strength += 1;
  }

  return strength;
};

const passwordStrength = computed(() => calculatePasswordStrength(form.value.password));
const passwordStrengthStatus = computed(() => {

  if (passwordStrength.value < 2) {
    return 'exception';
  }

  if (passwordStrength.value < 3) {
    return 'warning';
  }

  return 'success';
});

</script>


<template>
  <div class="password-generator">
    <h2>密碼生成器</h2>
    <el-form :model="form" label-width="auto">
      <el-form-item label="密碼長度">
        <el-slider v-model="form.length" :min="6" :max="20" show-input></el-slider>
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="form.includeNumbers">包含數字</el-checkbox>
        <el-checkbox v-model="form.includeSymbols">包含符號</el-checkbox>
      </el-form-item>
      <el-row justify="center">
        <el-col :span="12">
          <el-form-item>
            <el-button type="primary" @click="generatePassword" class="full-width">生成</el-button>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="生成的密碼">
        <el-input v-model="form.password" readonly></el-input>
      </el-form-item>
      <el-form-item label="密碼強度">
        <el-progress :percentage="passwordStrength * 25" :status="passwordStrengthStatus"></el-progress>
      </el-form-item>
    </el-form>
  </div>
</template>


<style scoped>
.password-generator {
  padding: 1rem;
  border: solid var(--el-color-info-light-5);
  border-radius: 5px;
}

.password-generator h2 {
  text-align: center;
}
</style>
