<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchProfitReport,
  fetchSalesReport,
  fetchStockReport,
  type ProfitSummaryRow,
  type SalesSummaryRow,
  type StockSummaryRow,
} from '../../api/report'
import { useAccessibleWarehouses } from '../../composables/useAccessibleWarehouses'

const activeTab = ref('stock')
const loading = ref(false)
const dateFrom = ref('')
const dateTo = ref('')
const warehouseId = ref<number | null>(null)

const stockRows = ref<StockSummaryRow[]>([])
const salesRows = ref<SalesSummaryRow[]>([])
const profitRows = ref<ProfitSummaryRow[]>([])

const { warehouses, load: loadWarehouses } = useAccessibleWarehouses()

async function loadStock() {
  loading.value = true
  try {
    const { data } = await fetchStockReport(warehouseId.value ?? undefined)
    stockRows.value = data
  } catch {
    ElMessage.error('加载库存报表失败')
  } finally {
    loading.value = false
  }
}

async function loadSales() {
  loading.value = true
  try {
    const { data } = await fetchSalesReport(dateFrom.value || undefined, dateTo.value || undefined)
    salesRows.value = data
  } catch {
    ElMessage.error('加载销售报表失败')
  } finally {
    loading.value = false
  }
}

async function loadProfit() {
  loading.value = true
  try {
    const { data } = await fetchProfitReport(dateFrom.value || undefined, dateTo.value || undefined)
    profitRows.value = data
  } catch {
    ElMessage.error('加载利润报表失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange(name: string | number) {
  if (name === 'stock') loadStock()
  else if (name === 'sales') loadSales()
  else if (name === 'profit') loadProfit()
}

onMounted(async () => {
  await loadWarehouses()
  await loadStock()
})
</script>

<template>
  <div class="reports">
    <div class="page-card">
      <h2 class="page-title">报表中心</h2>
      <p class="page-subtitle">库存、销售与利润分析</p>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="库存报表" name="stock">
          <div class="filters">
            <el-select v-model="warehouseId" clearable placeholder="全部仓库" style="width: 200px" @change="loadStock">
              <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
            </el-select>
            <el-button type="primary" :loading="loading" @click="loadStock">刷新</el-button>
          </div>
          <el-table v-loading="loading" :data="stockRows" stripe>
            <el-table-column prop="warehouseName" label="仓库" min-width="100" />
            <el-table-column prop="productName" label="商品" min-width="120" />
            <el-table-column prop="sku" label="SKU" width="120" />
            <el-table-column prop="batchNo" label="批次" width="120">
              <template #default="{ row }">
                <span class="doc-no-inline">{{ row.batchNo }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="90" align="right" />
            <el-table-column prop="totalValue" label="库存金额" width="110" align="right">
              <template #default="{ row }">¥{{ row.totalValue.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="销售报表" name="sales">
          <div class="filters">
            <el-date-picker v-model="dateFrom" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" />
            <el-date-picker v-model="dateTo" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" />
            <el-button type="primary" :loading="loading" @click="loadSales">查询</el-button>
          </div>
          <el-table v-loading="loading" :data="salesRows" stripe>
            <el-table-column prop="period" label="日期" width="110" />
            <el-table-column prop="productName" label="商品" min-width="120" />
            <el-table-column prop="customerName" label="客户" min-width="120" />
            <el-table-column prop="quantity" label="数量" width="90" align="right" />
            <el-table-column prop="amount" label="金额" width="110" align="right">
              <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="利润分析" name="profit">
          <div class="filters">
            <el-date-picker v-model="dateFrom" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" />
            <el-date-picker v-model="dateTo" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" />
            <el-button type="primary" :loading="loading" @click="loadProfit">查询</el-button>
          </div>
          <el-table v-loading="loading" :data="profitRows" stripe>
            <el-table-column prop="period" label="日期" width="110" />
            <el-table-column prop="salesAmount" label="销售额" align="right">
              <template #default="{ row }">¥{{ row.salesAmount.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="costAmount" label="成本" align="right">
              <template #default="{ row }">¥{{ row.costAmount.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="profit" label="利润" align="right">
              <template #default="{ row }">
                <span :class="{ 'text-profit': row.profit >= 0, 'text-loss': row.profit < 0 }">
                  ¥{{ row.profit.toFixed(2) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.reports {
  max-width: 1100px;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.text-profit {
  color: var(--color-success);
  font-weight: 500;
}

.text-loss {
  color: var(--color-danger);
  font-weight: 500;
}
</style>
