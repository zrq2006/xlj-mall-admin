import { createRouter, createWebHistory } from 'vue-router';
import AdminLayout from '../layout/AdminLayout.vue';

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/workbench',
    children: [
      {
        path: 'workbench',
        name: 'workbench',
        component: () => import('../views/Workbench.vue'),
        meta: { title: '工作台' },
      },
      {
        path: 'products',
        name: 'products',
        component: () => import('../views/Products.vue'),
        meta: { title: '商品列表' },
      },
      {
        path: 'categories',
        name: 'categories',
        component: () => import('../views/Categories.vue'),
        meta: { title: '商品分类' },
      },
      {
        path: 'brands',
        name: 'brands',
        component: () => import('../views/Brands.vue'),
        meta: { title: '品牌管理' },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/workbench' },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const token = localStorage.getItem('xlj_token');
  if (!to.meta.public && !token) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }
  if (to.path === '/login' && token) {
    return { path: '/workbench' };
  }
  return true;
});

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 小蓝鲸优选管理后台` : '小蓝鲸优选 · 管理后台';
});

export default router;
