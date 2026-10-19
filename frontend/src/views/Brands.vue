<template>
  <div class="panel">
    <el-form :inline="true" class="filter-form" @submit.prevent>
      <el-form-item label="品牌名称">
        <el-input
          v-model="query.name"
          placeholder="请输入品牌名称"
          clearable
          style="width: 260px"
          @keyup.enter="search"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增品牌</el-button>
      <el-button type="success" @click="onDownloadTemplate">下载导入模板</el-button>
      <el-button @click="importState.visible = true">批量导入</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="Logo" width="90" align="center">
        <template #default="{ row }">
          <el-image v-if="isImage(row.logo)" :src="row.logo" fit="contain" style="width: 40px; height: 40px" />
          <span v-else-if="row.logo" class="logo-box">{{ row.logo }}</span>
          <span v-else class="logo-box">{{ (row.name || '').charAt(0) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="品牌名称" width="150" />
      <el-table-column label="首字母" width="90">
        <template #default="{ row }">
          <el-tag effect="light" size="small">{{ row.firstLetter }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="260" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="状态" width="85">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
            {{ row.status === 1 ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="165" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <template v-if="row.editable">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
          <span v-else class="muted">仅创建者可操作</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        :pager-count="7"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.mode === 'create' ? '新增品牌' : '编辑品牌'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="品牌名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入品牌名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="首字母">
          <el-input v-model="form.firstLetter" placeholder="留空则自动取名称首字符" maxlength="1" style="width: 120px" />
        </el-form-item>
        <el-form-item label="Logo">
          <el-input v-model="form.logo" placeholder="图片地址或 emoji，可留空" />
        </el-form-item>
        <el-form-item label="品牌描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入品牌描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :precision="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importState.visible" title="批量导入品牌" width="540px" @closed="resetImport">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".csv"
        :on-change="onFileChange"
        :on-exceed="onFileExceed"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将 CSV 文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">请先下载导入模板，按模板填写后上传（UTF-8 编码 CSV，最大 5MB）。</div>
        </template>
      </el-upload>
      <div v-if="importState.result" class="import-result">
        <div>导入完成：成功 {{ importState.result.success }} 条，失败 {{ importState.result.failed }} 条</div>
        <div v-for="error in importState.result.errors" :key="error" class="error-line">{{ error }}</div>
      </div>
      <template #footer>
        <el-button @click="importState.visible = false">关闭</el-button>
        <el-button type="primary" :loading="importState.loading" @click="submitImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createBrand,
  downloadTemplate,
  fetchBrandPage,
  importCsv,
  removeBrand,
  updateBrand,
} from '../api';

const rows = ref([]);
const total = ref(0);
const page = ref(1);
const size = 10;
const loading = ref(false);
const saving = ref(false);
const query = reactive({ name: '' });

const emptyForm = () => ({ id: null, name: '', logo: '', firstLetter: '', description: '', sort: 0, status: 1 });
const dialog = reactive({ visible: false, mode: 'create' });
const form = reactive(emptyForm());
const formRef = ref();
const uploadRef = ref();
const importState = reactive({ visible: false, file: null, loading: false, result: null });

const rules = {
  name: [{ required: true, message: '请输入品牌名称', trigger: 'blur' }],
};

const isImage = (logo) => !!logo && (logo.startsWith('http') || logo.startsWith('/'));

async function load() {
  loading.value = true;
  try {
    const data = await fetchBrandPage({ page: page.value, size, name: query.name || undefined });
    rows.value = data.list;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

function search() {
  page.value = 1;
  load();
}

function reset() {
  query.name = '';
  page.value = 1;
  load();
}

function openCreate() {
  Object.assign(form, emptyForm());
  dialog.mode = 'create';
  dialog.visible = true;
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    logo: row.logo || '',
    firstLetter: row.firstLetter || '',
    description: row.description || '',
    sort: row.sort,
    status: row.status,
  });
  dialog.mode = 'edit';
  dialog.visible = true;
}

function resetForm() {
  Object.assign(form, emptyForm());
  formRef.value?.clearValidate();
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    const payload = {
      name: form.name,
      logo: form.logo,
      firstLetter: form.firstLetter,
      description: form.description,
      sort: form.sort,
      status: form.status,
    };
    if (dialog.mode === 'create') {
      await createBrand(payload);
      ElMessage.success('品牌已新增');
    } else {
      await updateBrand(form.id, payload);
      ElMessage.success('品牌已保存');
    }
    dialog.visible = false;
    load();
  } finally {
    saving.value = false;
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定要删除品牌「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    });
  } catch (e) {
    return;
  }
  await removeBrand(row.id);
  ElMessage.success('品牌已删除');
  if (rows.value.length === 1 && page.value > 1) page.value -= 1;
  load();
}

function onDownloadTemplate() {
  downloadTemplate('brands', '品牌导入模板.csv');
}

function onFileChange(uploadFile) {
  importState.file = uploadFile.raw;
  importState.result = null;
}

function onFileExceed(files) {
  uploadRef.value.clearFiles();
  uploadRef.value.handleStart(files[0]);
}

async function submitImport() {
  if (!importState.file) {
    ElMessage.warning('请先选择要导入的 CSV 文件');
    return;
  }
  importState.loading = true;
  try {
    importState.result = await importCsv('brands', importState.file);
    ElMessage.success(`导入完成，成功 ${importState.result.success} 条`);
    page.value = 1;
    load();
  } finally {
    importState.loading = false;
  }
}

function resetImport() {
  importState.file = null;
  importState.result = null;
  uploadRef.value?.clearFiles();
}

onMounted(load);
</script>
