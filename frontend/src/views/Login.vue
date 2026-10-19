<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">小蓝鲸优选</div>
      <div class="brand-sub">管理后台</div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" prefix-icon="User" autocomplete="username" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>
        <el-button class="login-button" type="primary" size="large" :loading="loading" @click="submit">
          登 录
        </el-button>
      </el-form>
      <div class="login-tip">演示账号：admin / admin123</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { login } from '../api';
import { setLogin } from '../store/admin';

const router = useRouter();
const route = useRoute();
const formRef = ref();
const loading = ref(false);
const form = reactive({ username: 'admin', password: 'admin123' });

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

async function submit() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    const data = await login({ username: form.username, password: form.password });
    setLogin(data);
    ElMessage.success('登录成功');
    router.push(route.query.redirect || '/workbench');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #001529 0%, #062a4d 55%, #0b3d70 100%);
}
.login-card {
  width: 380px;
  padding: 40px 40px 28px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.24);
}
.brand {
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 2px;
  color: #001529;
  text-align: center;
}
.brand-sub {
  margin: 6px 0 28px;
  font-size: 13px;
  color: #909399;
  text-align: center;
  letter-spacing: 4px;
}
.login-button {
  width: 100%;
  margin-top: 4px;
}
.login-tip {
  margin-top: 18px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
