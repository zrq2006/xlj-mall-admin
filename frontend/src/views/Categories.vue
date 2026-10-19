<template>
  <div class="panel">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="分类图标" width="110" align="center">
        <template #default="{ row }">
          <span v-if="row.icon" class="emoji-icon">{{ row.icon }}</span>
          <span v-else class="muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="分类图片" width="150">
        <template #default="{ row }">
          <el-image v-if="row.image" :src="row.image" fit="cover" style="width: 80px; height: 60px; border-radius: 4px" />
          <span v-else class="cat-image-empty">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="分类名称" min-width="240" />
      <el-table-column prop="sort" label="排序" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
            {{ row.status === 1 ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
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
      :title="dialog.mode === 'create' ? '新增分类' : '编辑分类'"
      width="520px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="分类图标">
          <el-input v-model="form.icon" placeholder="可填写 emoji，例如 🎁" maxlength="20" />
        </el-form-item>
        <el-form-item label="分类图片">
          <el-input v-model="form.image" placeholder="图片地址，可留空" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :precision="0" controls-position="right" />
          <span class="dialog-tip" style="margin-left: 8px">数值越小越靠前</span>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createCategory,
  fetchCategoryPage,
  removeCategory,
  updateCategory,
} from '../api';

const rows = ref([]);
const total = ref(0);
const page = ref(1);
const size = 10;
const loading = ref(false);
const saving = ref(false);

const emptyForm = () => ({ id: null, name: '', icon: '', image: '', sort: 0, status: 1 });
const dialog = reactive({ visible: false, mode: 'create' });
const form = reactive(emptyForm());
const formRef = ref();

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
};

async function load() {
  loading.value = true;
  try {
    const data = await fetchCategoryPage({ page: page.value, size });
    rows.value = data.list;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
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
    icon: row.icon || '',
    image: row.image || '',
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
      icon: form.icon,
      image: form.image,
      sort: form.sort,
      status: form.status,
    };
    if (dialog.mode === 'create') {
      await createCategory(payload);
      ElMessage.success('分类已新增');
    } else {
      await updateCategory(form.id, payload);
      ElMessage.success('分类已保存');
    }
    dialog.visible = false;
    load();
  } finally {
    saving.value = false;
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定要删除分类「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    });
  } catch (e) {
    return;
  }
  await removeCategory(row.id);
  ElMessage.success('分类已删除');
  if (rows.value.length === 1 && page.value > 1) page.value -= 1;
  load();
}

onMounted(load);
</script>
