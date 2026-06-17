<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listProducts, listProductBatches, type Product, type ProductBatch } from '../../api/product'
import { listStocks } from '../../api/stock'
import { createTransfer } from '../../api/transfer'
import { useAccessibleWarehouses } from '../../composables/useAccessibleWarehouses'

interface LineItem {
  productId: number | null
  batchId: number | null
  quantity: number
  available: number
}

const { warehouses, loading: warehousesLoading, load: loadWarehouses } = useAccessibleWarehouses()

const products = ref<Product[]>([])
const batchesMap = ref<Record<number, ProductBatch[]>>({})
const stockMap = ref<Record<string, number>>({})
const submitting = ref(false)

const form = reactive({
  fromWarehouseId: null as number | null,
  toWarehouseId: null as number | null,
  items: [{ productId: null, batchId: null, quantity: 1, available: 0 }] as LineItem[],
})

const toWarehouseOptions = computed(() =>
  warehouses.value.filter((w) => w.id !== form.fromWarehouseId),
)

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

async function onFromWarehouseChange() {
  form.toWarehouseId = null
  if (form.fromWarehouseId) {
    await loadStocksForWarehouse(form.fromWarehouseId)
    for (const row of form.items) {
      updateAvailable(row)
    }
  } else {
    stockMap.value = {}
    for (const row of form.items) {
      row.available = 0
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
  if (!form.fromWarehouseId) {
    ElMessage.warning('请选择调出仓库')
    return
  }
  if (!form.toWarehouseId) {
    ElMessage.warning('请选择调入仓库')
    return
  }
  if (form.fromWarehouseId === form.toWarehouseId) {
    ElMessage.warning('调出与调入仓库不能相同')
    return
  }

  const items = form.items.filter((i) => i.productId && i.batchId && i.quantity > 0)
  if (!items.length) {
    ElMessage.warning('请添加至少一行有效明细')
    return
  }

  for (const item of items) {
    if (item.quantity > item.available) {
      ElMessage.warning('调拨数量不能超过调出仓库可用库存')
      return
    }
  }

  submitting.value = true
  try {
    const { data } = await createTransfer({
      fromWarehouseId: form.fromWarehouseId,
      toWarehouseId: form.toWarehouseId,
      items: items.map((i) => ({
        productId: i.productId!,
        batchId: i.batchId!,
        quantity: i.quantity,
      })),
    })
    ElMessage.success(`调拨成功：${data.docNo}`)
    form.fromWarehouseId = null
    form.toWarehouseId = null
    form.items = [{ productId: null, batchId: null, quantity: 1, available: 0 }]
    stockMap.value = {}
  } catch {
    ElMessage.error('调拨失败')
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
      <h2 class="page-title">仓库调拨</h2>
      <p class="page-subtitle">从调出仓库转移至调入仓库，数量受调出仓库存限制</p>

      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="调出仓库" required>
              <el-select
                v-model="form.fromWarehouseId"
                placeholder="选择调出仓库"
                style="width: 100%"
                :loading="warehousesLoading"
                @change="onFromWarehouseChange"
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
          <el-col :span="12">
            <el-form-item label="调入仓库" required>
              <el-select
                v-model="form.toWarehouseId"
                placeholder="选择调入仓库"
                style="width: 100%"
                :loading="warehousesLoading"
                :disabled="!form.fromWarehouseId"
              >
                <el-option
                  v-for="w in toWarehouseOptions"
                  :key="w.id"
                  :label="w.name"
                  :value="w.id"
                />
              </el-select>
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
              :disabled="!form.fromWarehouseId"
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
            <el-input-number
              v-model="row.quantity"
              :min="0.01"
              :max="row.available || undefined"
              :precision="2"
              style="width: 100%"
            />
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
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认调拨</el-button>
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
