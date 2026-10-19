<template>
  <div class="panel">
    <el-form :inline="true" class="filter-form" @submit.prevent>
      <el-form-item label="商品名称">
        <el-input
          v-model="query.name"
          placeholder="请输入商品名称"
          clearable
          style="width: 260px"
          @keyup.enter="search"
        />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 180px">
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 180px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="toolbar">
      <el-button type="success" @click="openCreate">新增商品</el-button>
      <el-button type="primary" @click="onDownloadTemplate">下载导入模板</el-button>
      <el-button @click="openImport">批量导入</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="商品" min-width="300" align="center">
        <template #default="{ row }">
          <div class="product-cell">
            <el-image class="product-image" :src="row.image" fit="contain">
              <template #error>
                <div class="product-image cat-image-empty" style="border-style: solid">无图</div>
              </template>
            </el-image>
            <div class="product-name">{{ row.name }}</div>
            <div class="cell-sub">{{ row.description }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="售价" width="80">
        <template #default="{ row }">{{ money(row.price) }}</template>
      </el-table-column>
      <el-table-column label="同行价" width="90">
        <template #default="{ row }">
          <el-tag type="warning" effect="light" size="small">{{ money(row.marketPrice) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="70" />
      <el-table-column prop="sales" label="销量" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="165" />
      <el-table-column label="操作" width="110">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 1" link type="warning" @click="toggleStatus(row, 0)">下架</el-button>
          <el-button v-else link type="success" @click="toggleStatus(row, 1)">上架</el-button>
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
      :title="dialog.mode === 'create' ? '新增商品' : '编辑商品'"
      width="680px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="200" show-word-limit />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-select v-model="form.brandId" placeholder="请选择品牌" clearable style="width: 100%">
                <el-option v-for="item in brands" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="售价" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="同行价">
              <el-input-number v-model="form.marketPrice" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" :precision="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销量">
              <el-input-number v-model="form.sales" :min="0" :precision="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品图片">
          <div class="image-row">
            <el-input v-model="form.image" placeholder="图片地址，例如 /img/products/p1.svg" />
            <el-image v-if="form.image" :src="form.image" fit="contain" class="image-preview" />
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" show-word-limit placeholder="请输入商品卖点描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importState.visible" title="批量导入商品" width="540px" @closed="resetImport">
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
import { ElMessage } from 'element-plus';
import {
  createProduct,
  downloadTemplate,
  fetchBrandAll,
  fetchCategoryAll,
  fetchProductPage,
  importCsv,
  updateProduct,
  updateProductStatus,
} from '../api';

const rows = ref([]);
const total = ref(0);
const page = ref(1);
const size = 10;
const loading = ref(false);
const saving = ref(false);
const categories = ref([]);
const brands = ref([]);

const query = reactive({ name: '', categoryId: null, status: null });

const emptyForm = () => ({
  id: null,
  name: '',
  description: '',
  categoryId: null,
  brandId: null,
  price: 0,
  marketPrice: 0,
  stock: 0,
  sales: 0,
  image: '',
  status: 1,
});

const dialog = reactive({ visible: false, mode: 'create' });
const form = reactive(emptyForm());
const formRef = ref();
const uploadRef = ref();
const importState = reactive({ visible: false, file: null, loading: false, result: null });

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
};

const money = (value) => (value === null || value === undefined ? '-' : Number(value));

async function load() {
  loading.value = true;
  try {
    const data = await fetchProductPage({
      page: page.value,
      size,
      name: query.name || undefined,
      categoryId: query.categoryId ?? undefined,
      status: query.status ?? undefined,
    });
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
  query.categoryId = null;
  query.status = null;
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
    description: row.description || '',
    categoryId: row.categoryId,
    brandId: row.brandId,
    price: Number(row.price),
    marketPrice: Number(row.marketPrice),
    stock: row.stock,
    sales: row.sales,
    image: row.image || '',
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
      description: form.description,
      categoryId: form.categoryId,
      brandId: form.brandId || null,
      price: form.price,
      marketPrice: form.marketPrice,
      stock: form.stock,
      sales: form.sales,
      image: form.image,
      status: form.status,
    };
    if (dialog.mode === 'create') {
      await createProduct(payload);
      ElMessage.success('商品已新增');
    } else {
      await updateProduct(form.id, payload);
      ElMessage.success('商品已保存');
    }
    dialog.visible = false;
    load();
  } finally {
    saving.value = false;
  }
}

async function toggleStatus(row, status) {
  await updateProductStatus(row.id, status);
  ElMessage.success(status === 1 ? '商品已上架' : '商品已下架');
  load();
}

function onDownloadTemplate() {
  downloadTemplate('products', '商品导入模板.csv');
}

function openImport() {
  importState.visible = true;
}

function onFileChange(uploadFile) {
  importState.file = uploadFile.raw;
  importState.result = null;
}

function onFileExceed(files) {
  uploadRef.value.clearFiles();
  const file = files[0];
  uploadRef.value.handleStart(file);
}

async function submitImport() {
  if (!importState.file) {
    ElMessage.warning('请先选择要导入的 CSV 文件');
    return;
  }
  importState.loading = true;
  try {
    importState.result = await importCsv('products', importState.file);
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

onMounted(async () => {
  load();
  const [categoryList, brandList] = await Promise.all([fetchCategoryAll(), fetchBrandAll()]);
  categories.value = categoryList;
  brands.value = brandList;
});
</script>

<style scoped>
.image-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.image-preview {
  width: 56px;
  height: 56px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  flex-shrink: 0;
}
</style>
