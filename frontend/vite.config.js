import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// 开发时把 /admin 请求代理到 Spring Boot，生产环境由后端直接托管 dist 静态文件
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/admin': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    chunkSizeWarningLimit: 1500,
  },
});
