<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listProducts, listProductBatches, type Product, type ProductBatch } from '../../api/product'
import { createStockCheck, listStocks } from '../../api/stock'
import { useAccessibleWarehouses } from '../../composables/useAccessibleWarehouses'

interface CheckRow {
  productId: number
  batchId: number
  productName: string
  batchNo: string
  bookQty: number
  actualQty: number
}

const { warehouses, loading: warehousesLoading, load: loadWarehouses } = useAccessibleWarehouses()

const products = ref<Product[]>([])
const batchesMap = ref<Record<number, ProductBatch[]>>({})
const loading = ref(false)
const submitting = ref(false)
const rows = ref<CheckRow[]>([])

const form = reactive({
  warehouseId: null as number | null,
  remark: '',
})

const diffCount = computed(() =>
  rows.value.filter((r) => r.actualQty !== r.bookQty).length,
)

function productName(id: number) {
  return products.value.find((p) => p.id === id)?.name ?? `#${id}`
}

async function loadProducts() {
  const { data } = await listProducts()
  products.value = data
}

async function loadBatches(productId: number) {
  if (batchesMap.value[productId]) return batchesMap.value[productId]
  const { data } = await listProductBatches(productId)
  batchesMap.value[productId] = data
  return data
}

async function loadStockList() {
  if (!form.warehouseId) {
    ElMessage.warning('请先选择仓库')
    return
  }

  loading.value = true
  try {
    const { data: stocks } = await listStocks(form.warehouseId)
    const enriched: CheckRow[] = []

    for (const stock of stocks) {
      await loadBatches(stock.productId)
      const batch = batchesMap.value[stock.productId]?.find((b) => b.id === stock.batchId)
      enriched.push({
        productId: stock.productId,
        batchId: stock.batchId,
        productName: productName(stock.productId),
        batchNo: batch?.batchNo ?? `#${stock.batchId}`,
        bookQty: stock.quantity,
        actualQty: stock.quantity,
      })
    }

    rows.value = enriched
    if (!enriched.length) {
      ElMessage.info('该仓库暂无账面库存')
    }
  } catch {
    ElMessage.error('加载库存失败')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.warehouseId) {
    ElMessage.warning('请选择仓库')
    return
  }
  if (!rows.value.length) {
    ElMessage.warning('请先加载库存列表')
    return
  }

  submitting.value = true
  try {
    const { data } = await createStockCheck({
      warehouseId: form.warehouseId,
      remark: form.remark || undefined,
      items: rows.value.map((r) => ({
        productId: r.productId,
        batchId: r.batchId,
        actualQty: r.actualQty,
      })),
    })
    ElMessage.success(`盘点成功：${data.docNo}`)
    await loadStockList()
  } catch {
    ElMessage.error('盘点失败')
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
      <h2 class="page-title">盘点单</h2>
      <p class="page-subtitle">加载账面库存并录入实盘数量</p>

      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="10">
            <el-form-item label="仓库" required>
              <el-select
                v-model="form.warehouseId"
                placeholder="选择仓库"
                style="width: 100%"
                :loading="warehousesLoading"
                @change="rows = []"
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
          <el-col :span="10">
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="可选备注" />
            </el-form-item>
          </el-col>
          <el-col :span="4" class="load-col">
            <el-button type="primary" :loading="loading" @click="loadStockList">加载库存</el-button>
          </el-col>
        </el-row>
      </el-form>

      <div v-if="rows.length" class="summary">
        共 {{ rows.length }} 条 · 差异 {{ diffCount }} 条
      </div>

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="productName" label="商品" min-width="140" />
        <el-table-column prop="batchNo" label="批次" width="120">
          <template #default="{ row }">
            <span class="doc-no-inline">{{ row.batchNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bookQty" label="账面数量" width="100" align="right" />
        <el-table-column label="实盘数量" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.actualQty" :min="0" :precision="2" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="差异" width="90" align="right">
          <template #default="{ row }">
            <span :class="{ diff: row.actualQty !== row.bookQty }">
              {{ row.actualQty - row.bookQty }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="rows.length" class="actions">
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认盘点</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 960px;
}

.load-col {
  display: flex;
  align-items: flex-end;
  padding-bottom: 4px;
}

.summary {
  margin-bottom: 0.75rem;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

.diff {
  color: var(--color-accent);
  font-weight: 500;
}

.actions {
  margin-top: 1.25rem;
}
</style>
