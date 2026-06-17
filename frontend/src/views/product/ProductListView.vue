<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import {
  createProduct,
  createProductBatch,
  listProductBatches,
  listProducts,
  updateProduct,
  type Product,
  type ProductBatch,
} from '../../api/product'

const auth = useAuthStore()
const canEdit = computed(() => auth.role === 'admin')

const loading = ref(false)
const products = ref<Product[]>([])
const search = ref('')
const dialogVisible = ref(false)
const detailVisible = ref(false)
const editingId = ref<number | null>(null)
const detailProduct = ref<Product | null>(null)
const batches = ref<ProductBatch[]>([])
const batchesLoading = ref(false)

const form = reactive({
  name: '',
  sku: '',
  costPrice: 0,
  salePrice: 0,
  unit: '件',
  minStock: 0,
  status: 'ACTIVE',
})

const batchForm = reactive({
  batchNo: '',
  productionDate: '',
})

const filteredProducts = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return products.value
  return products.value.filter(
    (p) => p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q),
  )
})

async function loadData() {
  loading.value = true
  try {
    const { data } = await listProducts()
    products.value = data
  } catch {
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.name = ''
  form.sku = ''
  form.costPrice = 0
  form.salePrice = 0
  form.unit = '件'
  form.minStock = 0
  form.status = 'ACTIVE'
  dialogVisible.value = true
}

function openEdit(row: Product) {
  editingId.value = row.id
  form.name = row.name
  form.sku = row.sku
  form.costPrice = row.costPrice
  form.salePrice = row.salePrice
  form.unit = row.unit
  form.minStock = row.minStock
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name.trim() || !form.sku.trim()) {
    ElMessage.warning('请填写商品名称和 SKU')
    return
  }

  try {
    if (editingId.value) {
      await updateProduct(editingId.value, {
        name: form.name,
        sku: form.sku,
        costPrice: form.costPrice,
        salePrice: form.salePrice,
        unit: form.unit,
        minStock: form.minStock,
        status: form.status,
      })
      ElMessage.success('商品已更新')
    } else {
      await createProduct({
        name: form.name,
        sku: form.sku,
        costPrice: form.costPrice,
        salePrice: form.salePrice,
        unit: form.unit,
        minStock: form.minStock,
      })
      ElMessage.success('商品已创建')
    }
    dialogVisible.value = false
    await loadData()
  } catch {
    ElMessage.error('保存失败')
  }
}

async function openDetail(row: Product) {
  detailProduct.value = row
  batchForm.batchNo = ''
  batchForm.productionDate = ''
  detailVisible.value = true
  await loadBatches(row.id)
}

async function loadBatches(productId: number) {
  batchesLoading.value = true
  try {
    const { data } = await listProductBatches(productId)
    batches.value = data
  } catch {
    ElMessage.error('加载批次失败')
  } finally {
    batchesLoading.value = false
  }
}

async function handleCreateBatch() {
  if (!detailProduct.value) return
  if (!batchForm.batchNo.trim()) {
    ElMessage.warning('请输入批次号')
    return
  }

  try {
    await createProductBatch(detailProduct.value.id, {
      batchNo: batchForm.batchNo,
      productionDate: batchForm.productionDate || undefined,
    })
    ElMessage.success('批次已创建')
    batchForm.batchNo = ''
    batchForm.productionDate = ''
    await loadBatches(detailProduct.value.id)
  } catch {
    ElMessage.error('创建批次失败')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <div class="page-card">
      <div class="page-header">
        <div>
          <h2 class="page-title">商品管理</h2>
          <p class="page-subtitle">维护商品档案与批次</p>
        </div>
        <el-button v-if="canEdit" type="primary" @click="openCreate">新建商品</el-button>
      </div>

      <div class="toolbar">
        <el-input v-model="search" placeholder="搜索名称或 SKU" clearable style="width: 240px" />
      </div>

      <el-table v-loading="loading" :data="filteredProducts" stripe>
        <el-table-column prop="sku" label="SKU" width="120">
          <template #default="{ row }">
            <span class="doc-no-inline">{{ row.sku }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="costPrice" label="成本价" width="100" />
        <el-table-column prop="salePrice" label="售价" width="100" />
        <el-table-column prop="minStock" label="最低库存" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '启用' : row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="canEdit" link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑商品' : '新建商品'"
      width="520px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="SKU" required>
          <el-input v-model="form.sku" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="成本价">
              <el-input-number v-model="form.costPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="售价">
              <el-input-number v-model="form.salePrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="单位">
              <el-input v-model="form.unit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低库存">
              <el-input-number v-model="form.minStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="editingId" label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailVisible"
      :title="detailProduct ? `商品详情 — ${detailProduct.name}` : '商品详情'"
      width="640px"
      destroy-on-close
    >
      <template v-if="detailProduct">
        <div class="detail-meta">
          <span class="doc-no-inline">{{ detailProduct.sku }}</span>
          <span>{{ detailProduct.unit }} · 成本 {{ detailProduct.costPrice }} · 售价 {{ detailProduct.salePrice }}</span>
        </div>

        <h4 class="section-title">批次列表</h4>
        <el-table v-loading="batchesLoading" :data="batches" size="small" stripe>
          <el-table-column prop="batchNo" label="批次号">
            <template #default="{ row }">
              <span class="doc-no-inline">{{ row.batchNo }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="productionDate" label="生产日期" />
        </el-table>

        <div v-if="canEdit" class="batch-form">
          <h4 class="section-title">新建批次</h4>
          <el-form label-position="top" inline>
            <el-form-item label="批次号">
              <el-input v-model="batchForm.batchNo" placeholder="批次号" />
            </el-form-item>
            <el-form-item label="生产日期">
              <el-date-picker
                v-model="batchForm.productionDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleCreateBatch">添加批次</el-button>
            </el-form-item>
          </el-form>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  max-width: 1100px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.page-header .page-subtitle {
  margin-bottom: 0;
}

.toolbar {
  margin-bottom: 1rem;
}

.section-title {
  font-family: var(--font-display);
  font-size: 0.95rem;
  margin: 1rem 0 0.5rem;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.batch-form {
  margin-top: 1rem;
  padding-top: 0.5rem;
  border-top: 1px solid var(--color-border);
}
</style>
