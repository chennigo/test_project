<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listProducts, listProductBatches, type Product, type ProductBatch } from '../../api/product'
import { createStockOut, listStocks } from '../../api/stock'
import { useAccessibleWarehouses } from '../../composables/useAccessibleWarehouses'

interface LineItem {
  productId: number | null
  batchId: number | null
  quantity: number
  available: number
}

const outTypeOptions = [
  { label: '损耗', value: 'LOSS' },
  { label: '样品', value: 'SAMPLE' },
  { label: '其他', value: 'OTHER' },
]

const { warehouses, loading: warehousesLoading, load: loadWarehouses } = useAccessibleWarehouses()

const products = ref<Product[]>([])
const batchesMap = ref<Record<number, ProductBatch[]>>({})
const stockMap = ref<Record<string, number>>({})
const submitting = ref(false)

const form = reactive({
  warehouseId: null as number | null,
  outType: 'OTHER',
  remark: '',
  items: [{ productId: null, batchId: null, quantity: 1, available: 0 }] as LineItem[],
})

async function loadProducts() {
  const { data } = await listProducts()
  products.value = data.filter((p) => p.status === 'ACTIVE')
}

async function loadStocksForWarehouse(warehouseId: number) {
  const { data } = await listStocks(warehouseId)
  stockMap.value = {}
  for (const s of data) {
    stockMap.value[`${s.productId}-${s.batchId}`] = s.quantity
  }
}

async function onWarehouseChange() {
  if (form.warehouseId) {
    await loadStocksForWarehouse(form.warehouseId)
    for (const row of form.items) {
      updateAvailable(row)
    }
  }
}

async function loadBatchesForProduct(productId: number) {
  if (batchesMap.value[productId]) return
  const { data } = await listProductBatches(productId)
  batchesMap.value[productId] = data
}

function updateAvailable(row: LineItem) {
  if (!row.productId || !row.batchId) {
    row.available = 0
    return
  }
  row.available = stockMap.value[`${row.productId}-${row.batchId}`] ?? 0
}

async function onProductChange(row: LineItem) {
  row.batchId = null
  row.available = 0
  if (row.productId) {
    await loadBatchesForProduct(row.productId)
  }
}

function onBatchChange(row: LineItem) {
  updateAvailable(row)
}

function addLine() {
  form.items.push({ productId: null, batchId: null, quantity: 1, available: 0 })
}

function removeLine(index: number) {
  if (form.items.length <= 1) return
  form.items.splice(index, 1)
}

function batchesFor(productId: number | null) {
  if (!productId) return []
  return batchesMap.value[productId] ?? []
}

async function handleSubmit() {
  if (!form.warehouseId) {
    ElMessage.warning('请选择仓库')
    return
  }

  const items = form.items.filter((i) => i.productId && i.batchId && i.quantity > 0)
  if (!items.length) {
    ElMessage.warning('请添加至少一行有效明细')
    return
  }

  for (const item of items) {
    if (item.quantity > item.available) {
      ElMessage.warning('出库数量不能超过可用库存')
      return
    }
  }

  submitting.value = true
  try {
    const { data } = await createStockOut({
      warehouseId: form.warehouseId,
      outType: form.outType,
      remark: form.remark || undefined,
      items: items.map((i) => ({
        productId: i.productId!,
        batchId: i.batchId!,
        quantity: i.quantity,
      })),
    })
    ElMessage.success(`出库成功：${data.docNo}`)
    form.warehouseId = null
    form.outType = 'OTHER'
    form.remark = ''
    form.items = [{ productId: null, batchId: null, quantity: 1, available: 0 }]
    stockMap.value = {}
  } catch {
    ElMessage.error('出库失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadWarehouses()
  await loadProducts()
})
</script>

<template>
  <div class="page">
    <div class="page-card">
      <h2 class="page-title">出库单</h2>
      <p class="page-subtitle">选择仓库并录入出库明细</p>

      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="仓库" required>
              <el-select
                v-model="form.warehouseId"
                placeholder="选择仓库"
                style="width: 100%"
                :loading="warehousesLoading"
                @change="onWarehouseChange"
              >
                <el-option
                  v-for="w in warehouses"
                  :key="w.id"
                  :label="w.name"
                  :value="w.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出库类型">
              <el-select v-model="form.outType" style="width: 100%">
                <el-option
                  v-for="opt in outTypeOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="可选备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="lines-header">
        <h3 class="section-title">明细行</h3>
        <el-button size="small" @click="addLine">添加行</el-button>
      </div>

      <el-table :data="form.items" stripe>
        <el-table-column label="商品" min-width="160">
          <template #default="{ row }">
            <el-select
              v-model="row.productId"
              placeholder="选择商品"
              filterable
              style="width: 100%"
              @change="onProductChange(row)"
            >
              <el-option
                v-for="p in products"
                :key="p.id"
                :label="`${p.name} (${p.sku})`"
                :value="p.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="批次" min-width="120">
          <template #default="{ row }">
            <el-select
              v-model="row.batchId"
              placeholder="选择批次"
              style="width: 100%"
              :disabled="!row.productId"
              @change="onBatchChange(row)"
            >
              <el-option
                v-for="b in batchesFor(row.productId)"
                :key="b.id"
                :label="b.batchNo"
                :value="b.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="可用" width="80" align="right">
          <template #default="{ row }">{{ row.available }}</template>
        </el-table-column>
        <el-table-column label="数量" width="130">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="0.01" :max="row.available || undefined" :precision="2" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="" width="60">
          <template #default="{ $index }">
            <el-button link type="danger" :disabled="form.items.length <= 1" @click="removeLine($index)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="actions">
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认出库</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 960px;
}

.lines-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 1rem 0 0.5rem;
}

.section-title {
  font-family: var(--font-display);
  font-size: 0.95rem;
  margin: 0;
}

.actions {
  margin-top: 1.25rem;
}
</style>
