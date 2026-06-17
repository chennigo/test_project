<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchLowStock, type LowStockRow } from '../../api/report'
import { useAccessibleWarehouses } from '../../composables/useAccessibleWarehouses'

const loading = ref(false)
const rows = ref<LowStockRow[]>([])
const warehouseId = ref<number | null>(null)

const { warehouses, load: loadWarehouses } = useAccessibleWarehouses()

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchLowStock(warehouseId.value ?? undefined)
    rows.value = data
  } catch {
    ElMessage.error('加载库存预警失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadWarehouses()
  await loadData()
})
</script>

<template>
  <div class="alerts">
    <div class="page-card">
      <h2 class="page-title">库存预警</h2>
      <p class="page-subtitle">低于最低库存的商品将在此列出</p>

      <div class="filters">
        <el-select v-model="warehouseId" clearable placeholder="全部仓库" style="width: 200px" @change="loadData">
          <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadData">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="warehouseName" label="仓库" min-width="100" />
        <el-table-column prop="productName" label="商品" min-width="120" />
        <el-table-column prop="sku" label="SKU" width="120" />
        <el-table-column prop="currentQty" label="当前库存" width="100" align="right">
          <template #default="{ row }">
            <span class="qty-low">{{ row.currentQty }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="100" align="right" />
      </el-table>

      <el-empty v-if="!loading && rows.length === 0" description="暂无预警商品" />
    </div>
  </div>
</template>

<style scoped>
.alerts {
  max-width: 900px;
}

.filters {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.qty-low {
  color: var(--color-danger);
  font-weight: 600;
}
</style>
