import axios from 'axios';
import { ElMessage } from 'element-plus';
import { adminStore, clearLogin } from '../store/admin';

const http = axios.create({
  baseURL: '/admin',
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  if (adminStore.token) {
    config.headers.Authorization = `Bearer ${adminStore.token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    // 文件下载（模板）直接返回原始响应
    if (response.config.responseType === 'blob') {
      return response;
    }
    const body = response.data;
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) {
        return body.data;
      }
      ElMessage.error(body.message || '请求失败');
      return Promise.reject(new Error(body.message || '请求失败'));
    }
    return body;
  },
  (error) => {
    const status = error.response?.status;
    const message = error.response?.data?.message;
    if (status === 401) {
      clearLogin();
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
      ElMessage.error(message || '登录状态已失效，请重新登录');
    } else {
      ElMessage.error(message || error.message || '网络异常，请稍后重试');
    }
    return Promise.reject(error);
  },
);

export default http;
