<template>
  <div>
    <el-card shadow="never" class="block">
      <template #header>
        <span>经营概览</span>
        <span class="card-extra">数据实时统计</span>
      </template>
      <el-row :gutter="16">
        <el-col v-for="item in cards" :key="item.key" :span="6">
          <div class="stat-card" :style="{ background: item.color }">
            <div class="stat-label">{{ item.label }}</div>
            <div class="stat-value">{{ item.value }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <template #header>欢迎使用小蓝鲸优选管理后台</template>
      <p>当前登录：{{ adminStore.adminName || 'admin' }}（{{ role }}）</p>
      <p class="doc-line">
        接口文档：
        <el-link type="primary" href="/doc.html" target="_blank">http://localhost:8080/doc.html</el-link>
      </p>
      <el-divider />
      <div class="quick">
        <el-button type="primary" @click="router.push('/products')">商品管理</el-button>
        <el-button type="success" @click="onReserved('订单管理')">订单管理</el-button>
        <el-button @click="onReserved('同行商家审核')">同行商家审核</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { fetchDashboardStats } from '../api';
import { adminStore, roleText } from '../store/admin';

const router = useRouter();
const role = computed(() => roleText(adminStore.role));
const stats = ref({});

const definitions = [
  { label: '今日订单数', key: 'todayOrderCount', color: '#1f6feb' },
  { label: '今日实付金额(元)', key: 'todayPayAmount', color: '#07c160' },
  { label: '待付款订单', key: 'pendingPay', color: '#e6a23c' },
  { label: '待发货订单', key: 'pendingShip', color: '#f56c6c' },
  { label: '总订单数', key: 'totalOrderCount', color: '#409eff' },
  { label: '总用户数', key: 'totalUserCount', color: '#67c23a' },
  { label: '待审核商家', key: 'merchantPending', color: '#9b59b6' },
];

const format = (key, value) => {
  if (value === undefined || value === null) return '-';
  if (key === 'todayPayAmount') return Number(value).toFixed(2);
  return Number(value).toLocaleString('zh-CN');
};

const cards = computed(() =>
  definitions.map((item) => ({ ...item, value: format(item.key, stats.value[item.key]) })),
);

function onReserved(name) {
  ElMessage.info(`${name}模块为预留功能，本次复刻只实现了工作台与商品管理下的三个页面`);
}

onMounted(async () => {
  try {
    stats.value = await fetchDashboardStats();
  } catch (e) {
    // 拦截器已提示错误，卡片保持 "-"
  }
});
</script>

<style scoped>
.block {
  margin-bottom: 16px;
}
.card-extra {
  float: right;
  font-size: 12px;
  color: #909399;
}
.stat-card {
  color: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
}
.stat-label {
  font-size: 13px;
  opacity: 0.9;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  margin-top: 6px;
  word-break: break-all;
}
.doc-line {
  margin-top: 12px;
  color: #909399;
}
.quick {
  display: flex;
  gap: 12px;
}
</style>
