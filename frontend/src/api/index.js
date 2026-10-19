import http from './http';

/* 登录认证 */
export const login = (payload) => http.post('/auth/login', payload);
export const logout = () => http.post('/auth/logout');
export const fetchMe = () => http.get('/auth/me');

/* 工作台 */
export const fetchDashboardStats = () => http.get('/dashboard/stats');

/* 商品 */
export const fetchProductPage = (params) => http.get('/products', { params });
export const createProduct = (payload) => http.post('/products', payload);
export const updateProduct = (id, payload) => http.put(`/products/${id}`, payload);
export const updateProductStatus = (id, status) => http.patch(`/products/${id}/status`, { status });
export const removeProduct = (id) => http.delete(`/products/${id}`);

/* 分类 */
export const fetchCategoryPage = (params) => http.get('/categories', { params });
export const fetchCategoryAll = () => http.get('/categories/all');
export const createCategory = (payload) => http.post('/categories', payload);
export const updateCategory = (id, payload) => http.put(`/categories/${id}`, payload);
export const updateCategoryStatus = (id, status) => http.patch(`/categories/${id}/status`, { status });
export const removeCategory = (id) => http.delete(`/categories/${id}`);

/* 品牌 */
export const fetchBrandPage = (params) => http.get('/brands', { params });
export const fetchBrandAll = () => http.get('/brands/all');
export const createBrand = (payload) => http.post('/brands', payload);
export const updateBrand = (id, payload) => http.put(`/brands/${id}`, payload);
export const updateBrandStatus = (id, status) => http.patch(`/brands/${id}/status`, { status });
export const removeBrand = (id) => http.delete(`/brands/${id}`);

/* 模板下载 & 批量导入：kind = products | brands */
export async function downloadTemplate(kind, filename) {
  const response = await http.get(`/${kind}/template`, { responseType: 'blob' });
  const url = window.URL.createObjectURL(new Blob([response.data], { type: 'text/csv;charset=utf-8' }));
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

export function importCsv(kind, file) {
  const form = new FormData();
  form.append('file', file);
  return http.post(`/${kind}/import`, form, { headers: { 'Content-Type': 'multipart/form-data' } });
}
