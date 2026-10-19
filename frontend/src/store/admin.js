import { reactive } from 'vue';

const TOKEN_KEY = 'xlj_token';
const NAME_KEY = 'xlj_admin_name';
const ROLE_KEY = 'xlj_admin_role';

export const adminStore = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  adminName: localStorage.getItem(NAME_KEY) || '',
  role: localStorage.getItem(ROLE_KEY) || '',
});

export function setLogin({ token, adminName, nickname, role }) {
  adminStore.token = token;
  adminStore.adminName = adminName || nickname || 'admin';
  adminStore.role = role || 'operator';
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(NAME_KEY, adminStore.adminName);
  localStorage.setItem(ROLE_KEY, adminStore.role);
}

export function clearLogin() {
  adminStore.token = '';
  adminStore.adminName = '';
  adminStore.role = '';
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(NAME_KEY);
  localStorage.removeItem(ROLE_KEY);
}

export function roleText(role = adminStore.role) {
  return role === 'admin' ? '系统管理员' : '运营人员';
}
