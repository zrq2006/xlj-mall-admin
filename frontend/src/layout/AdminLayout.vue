<template>
  <el-container class="layout">
    <el-aside width="210px" class="aside">
      <div class="logo">小蓝鲸优选</div>
      <el-menu
        :default-active="activePath"
        background-color="#001529"
        text-color="rgba(255,255,255,0.68)"
        active-text-color="#ffffff"
        @select="onMenuSelect"
      >
        <template v-for="menu in menus" :key="menu.key">
          <el-sub-menu v-if="menu.children" :index="menu.key">
            <template #title>
              <el-icon><Grid /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
              <el-icon><Grid /></el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="menu.path">
            <el-icon><Grid /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">{{ pageTitle }}</div>
        <div class="header-right">
          <el-link class="header-bell" :underline="false" @click="onBell">
            <el-icon :size="18"><Bell /></el-icon>
          </el-link>
          <el-dropdown trigger="click" @command="onCommand">
            <span class="header-user">
              <el-icon><UserFilled /></el-icon>
              <span>{{ adminStore.adminName || 'admin' }}</span>
              <el-tag size="small" type="primary" effect="light" class="role-tag">{{ role }}</el-tag>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { adminStore, clearLogin, roleText } from '../store/admin';
import { logout } from '../api';

const route = useRoute();
const router = useRouter();

const menus = [
  { key: 'workbench', path: '/workbench', name: '工作台' },
  {
    key: 'product',
    name: '商品管理',
    children: [
      { path: '/products', name: '商品列表' },
      { path: '/categories', name: '分类管理' },
      { path: '/brands', name: '品牌管理' },
    ],
  },
  {
    key: 'analysis',
    name: '数据分析',
    children: [
      { path: '/reserved/sales', name: '销售分析' },
      { path: '/reserved/traffic', name: '流量分析' },
    ],
  },
  {
    key: 'marketing',
    name: '营销管理',
    children: [
      { path: '/reserved/coupon', name: '优惠券' },
      { path: '/reserved/seckill', name: '秒杀活动' },
    ],
  },
  {
    key: 'content',
    name: '内容管理',
    children: [
      { path: '/reserved/banner', name: '轮播图' },
      { path: '/reserved/notice', name: '公告管理' },
    ],
  },
  {
    key: 'order',
    name: '订单管理',
    children: [
      { path: '/reserved/order-list', name: '订单列表' },
      { path: '/reserved/after-sale', name: '退款售后' },
    ],
  },
  {
    key: 'merchant',
    name: '商家入驻',
    children: [
      { path: '/reserved/merchant-list', name: '商家列表' },
      { path: '/reserved/merchant-audit', name: '入驻审核' },
    ],
  },
  {
    key: 'profile',
    name: '个人中心',
    children: [
      { path: '/reserved/profile', name: '个人资料' },
      { path: '/reserved/message', name: '消息通知' },
    ],
  },
  {
    key: 'ops',
    name: '运维管理',
    children: [
      { path: '/reserved/config', name: '系统参数' },
      { path: '/reserved/log', name: '操作日志' },
    ],
  },
];

const activePath = computed(() => route.path);
const pageTitle = computed(() => route.meta.title || '工作台');
const role = computed(() => roleText(adminStore.role));

function onMenuSelect(index) {
  if (index.startsWith('/reserved')) {
    ElMessage.info('该模块为预留菜单，本次复刻只实现了工作台与商品管理下的三个页面');
    return;
  }
  if (index !== route.path) {
    router.push(index);
  }
}

function onBell() {
  ElMessage.info('暂无新消息');
}

async function onCommand(command) {
  if (command === 'profile') {
    ElMessage.info('个人资料编辑暂未开放');
    return;
  }
  if (command === 'logout') {
    try {
      await logout();
    } catch (e) {
      // 令牌无状态，退出失败也不影响本地清理
    }
    clearLogin();
    router.push('/login');
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background: #001529;
  overflow-x: hidden;
  overflow-y: auto;
}
.aside::-webkit-scrollbar {
  width: 6px;
}
.aside::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.16);
  border-radius: 3px;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.aside :deep(.el-menu-item),
.aside :deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.85);
}
.aside :deep(.el-menu-item:hover),
.aside :deep(.el-sub-menu__title:hover) {
  background-color: #1f3a5a !important;
  color: #fff !important;
}
.aside :deep(.el-menu-item.is-active) {
  background-color: #1f6feb !important;
  color: #fff !important;
}
.aside :deep(.el-menu .el-menu) {
  background-color: #000c17 !important;
}
.aside :deep(.el-menu .el-menu-item) {
  background-color: #000c17 !important;
  color: rgba(255, 255, 255, 0.92) !important;
  font-size: 13px;
  min-width: 0;
  height: 44px;
  line-height: 44px;
}
.aside :deep(.el-menu .el-menu-item .el-icon) {
  color: rgba(255, 255, 255, 0.92);
}
.aside :deep(.el-menu .el-menu-item:hover) {
  background-color: #1f3a5a !important;
  color: #fff !important;
}
.aside :deep(.el-menu .el-menu-item.is-active) {
  background-color: #1f6feb !important;
  color: #fff !important;
}
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  height: 60px;
  padding: 0 20px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.header-bell {
  display: flex;
  align-items: center;
  color: #606266;
}
.header-user {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #303133;
  outline: none;
}
.role-tag {
  margin-left: 2px;
}
.main {
  padding: 16px;
  background: #f0f2f5;
  overflow-y: auto;
}
</style>
