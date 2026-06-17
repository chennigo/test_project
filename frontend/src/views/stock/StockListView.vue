<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listProducts, listProductBatches, type Product, type ProductBatch } from '../../api/product'
import { listStocks, type StockRecord } from '../../api/stock'
import { useAccessibleWarehouses } from '../../composables/useAccessibleWarehouses'

interface StockRow extends StockRecord {
  productName: string
  productSku: string
  batchNo: string
}

const { warehouses, loading: warehousesLoading, load: loadWarehouses } = useAccessibleWarehouses()

const products = ref<Product[]>([])
const batchesMap = ref<Record<number, ProductBatch[]>>({})
const stocks = ref<StockRow[]>([])
const loading = ref(false)
const warehouseId = ref<number | null>(null)
const productId = ref<number | null>(null)

const productOptions = computed(() =>
  products.value.filter((p) => p.status === 'ACTIVE'),
)

function productName(id: number) {
  return products.value.find((p) => p.id === id)?.name ?? `#${id}`
}

function productSku(id: number) {
  return products.value.find((p) => p.id === id)?.sku ?? ''
}

function batchNo(productIdVal: number, batchId: number) {
  const batches = batchesMap.value[productIdVal] ?? []
  return batches.find((b) => b.id === batchId)?.batchNo ?? `#${batchId}`
}

async function loadProducts() {
  const { data } = await listProducts()
  products.value = data
}

async function ensureBatches(productIdVal: number) {
  if (batchesMap.value[productIdVal]) return
  const { data } = await listProductBatches(productIdVal)
  batchesMap.value[productIdVal] = data
}

async function loadStocks() {
  if (!warehouseId.value) {
    stocks.value = []
    return
  }

  loading.value = true
  try {
    const { data } = await listStocks(
      warehouseId.value,
      productId.value ?? undefined,
    )

    const productIds = [...new Set(data.map((s) => s.productId))]
    await Promise.all(productIds.map((id) => ensureBatches(id)))

    stocks.value = data.map((s) => ({
      ...s,
      productName: productName(s.productId),
      productSku: productSku(s.productId),
      batchNo: batchNo(s.productId, s.batchId),
    }))
  } catch {
    ElMessage.error('加载库存失败')
    stocks.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadStocks()
}

function handleReset() {
  productId.value = null
  loadStocks()
}

onMounted(async () => {
  await loadWarehouses()
  await loadProducts()
})
</script>

<template>
  <div class="page">
    <div class="page-card">
      <h2 class="page-title">库存查询</h2>
      <p class="page-subtitle">按仓库与商品筛选当前库存</p>

      <div class="toolbar">
        <el-select
          v-model="warehouseId"
          placeholder="选择仓库"
          style="width: 200px"
          :loading="warehousesLoading"
          @change="loadStocks"
        >
          <el-option
            v-for="w in warehouses"
            :key="w.id"
            :label="w.name"
            :value="w.id"
          />
        </el-select>
        <el-select
          v-model="productId"
          placeholder="全部商品"
          clearable
          filterable
          style="width: 220px"
          :disabled="!warehouseId"
        >
          <el-option
            v-for="p in productOptions"
            :key="p.id"
            :label="`${p.name} (${p.sku})`"
            :value="p.id"
          />
        </el-select>
        <el-button type="primary" :disabled="!warehouseId" @click="handleSearch">查询</el-button>
        <el-button :disabled="!warehouseId" @click="handleReset">重置商品</el-button>
      </div>

      <el-table v-loading="loading" :data="stocks" stripe empty-text="请选择仓库后查询">
        <el-table-column prop="productSku" label="SKU" width="120">
          <template #default="{ row }">
            <span class="doc-no-inline">{{ row.productSku }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品" min-width="140" />
        <el-table-column prop="batchNo" label="批次" width="130">
          <template #default="{ row }">
            <span class="doc-no-inline">{{ row.batchNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 960px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: 1rem;
}
</style>
