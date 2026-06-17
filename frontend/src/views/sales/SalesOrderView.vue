<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listCustomers, type Customer } from '../../api/customer'
import { listProducts, listProductBatches, type Product, type ProductBatch } from '../../api/product'
import { createSalesOrder, listSalesOrders, type SalesOrder } from '../../api/sales'
import { listStocks } from '../../api/stock'
import { useAccessibleWarehouses } from '../../composables/useAccessibleWarehouses'

interface LineItem {
  productId: number | null
  batchId: number | null
  quantity: number
  unitPrice: number
  available: number
}

const { warehouses, loading: warehousesLoading, load: loadWarehouses } = useAccessibleWarehouses()

const customers = ref<Customer[]>([])
const products = ref<Product[]>([])
const batchesMap = ref<Record<number, ProductBatch[]>>({})
const stockMap = ref<Record<string, number>>({})
const orders = ref<SalesOrder[]>([])
const ordersLoading = ref(false)
const submitting = ref(false)

const form = reactive({
  customerId: null as number | null,
  warehouseId: null as number | null,
  items: [{ productId: null, batchId: null, quantity: 1, unitPrice: 0, available: 0 }] as LineItem[],
})

const orderTotal = computed(() =>
  form.items.reduce((sum, row) => {
    if (!row.productId || !row.batchId || row.quantity <= 0) return sum
    return sum + row.quantity * row.unitPrice
  }, 0),
)

function customerName(id: number) {
  return customers.value.find((c) => c.id === id)?.name ?? `#${id}`
}

function warehouseName(id: number) {
  return warehouses.value.find((w) => w.id === id)?.name ?? `#${id}`
}

async function loadCustomers() {
  const { data } = await listCustomers()
  customers.value = data
}

async function loadProducts() {
  const { data } = await listProducts()
  products.value = data.filter((p) => p.status === 'ACTIVE')
}

async function loadOrders() {
  ordersLoading.value = true
  try {
    const { data } = await listSalesOrders()
    orders.value = data
  } catch {
    ElMessage.error('加载销售单列表失败')
  } finally {
    ordersLoading.value = false
  }
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
    const product = products.value.find((p) => p.id === row.productId)
    if (product && !row.unitPrice) {
      row.unitPrice = product.salePrice
    }
  }
}

function onBatchChange(row: LineItem) {
  updateAvailable(row)
}

function addLine() {
  form.items.push({ productId: null, batchId: null, quantity: 1, unitPrice: 0, available: 0 })
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
  if (!form.customerId) {
    ElMessage.warning('请选择客户')
    return
  }
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
      ElMessage.warning('销售数量不能超过可用库存')
      return
    }
    if (item.unitPrice < 0) {
      ElMessage.warning('单价不能为负数')
      return
    }
  }

  submitting.value = true
  try {
    const { data } = await createSalesOrder({
      customerId: form.customerId,
      warehouseId: form.warehouseId,
      items: items.map((i) => ({
        productId: i.productId!,
        batchId: i.batchId!,
        quantity: i.quantity,
        unitPrice: i.unitPrice,
      })),
    })
    ElMessage.success(`开单成功：${data.docNo}`)
    form.customerId = null
    form.warehouseId = null
    form.items = [{ productId: null, batchId: null, quantity: 1, unitPrice: 0, available: 0 }]
    stockMap.value = {}
    await loadOrders()
  } catch {
    ElMessage.error('开单失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadWarehouses(), loadCustomers(), loadProducts(), loadOrders()])
})
</script>

<template>
  <div class="page">
    <div class="page-card">
      <h2 class="page-title">销售开单</h2>
      <p class="page-subtitle">选择客户与仓库，录入销售明细</p>

      <el-form label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户" required>
              <el-select v-model="form.customerId" placeholder="选择客户" filterable style="width: 100%">
                <el-option
                  v-for="c in customers"
                  :key="c.id"
                  :label="c.name"
                  :value="c.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
        </el-row>
      </el-form>

      <div class="lines-header">
        <h3 class="section-title">明细行</h3>
        <el-button size="small" @click="addLine">添加行</el-button>
      </div>

      <el-table :data="form.items" stripe>
        <el-table-column label="商品" min-width="150">
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
        <el-table-column label="批次" min-width="110">
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
        <el-table-column label="可用" width="70" align="right">
          <template #default="{ row }">{{ row.available }}</template>
        </el-table-column>
        <el-table-column label="数量" width="110">
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
        <el-table-column label="单价" width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.unitPrice" :min="0" :precision="2" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="90" align="right">
          <template #default="{ row }">
            {{ row.productId && row.batchId ? (row.quantity * row.unitPrice).toFixed(2) : '—' }}
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

      <div class="total-row">
        <span class="total-label">合计</span>
        <span class="total-amount">{{ orderTotal.toFixed(2) }}</span>
      </div>

      <div class="actions">
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认开单</el-button>
      </div>
    </div>

    <div class="page-card list-card">
      <h3 class="section-title">历史销售单</h3>
      <el-table v-loading="ordersLoading" :data="orders" stripe>
        <el-table-column prop="docNo" label="单号" min-width="140">
          <template #default="{ row }">
            <span class="doc-no-inline">{{ row.docNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户" min-width="120">
          <template #default="{ row }">{{ customerName(row.customerId) }}</template>
        </el-table-column>
        <el-table-column label="仓库" min-width="120">
          <template #default="{ row }">{{ warehouseName(row.warehouseId) }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="100" align="right" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 1100px;
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

.total-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border);
}

.total-label {
  font-weight: 500;
  color: var(--color-text-muted);
}

.total-amount {
  font-family: var(--font-mono);
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--color-primary);
}

.actions {
  margin-top: 1.25rem;
}

.list-card {
  margin-top: 1.25rem;
}

.list-card .section-title {
  margin-bottom: 0.75rem;
}
</style>
