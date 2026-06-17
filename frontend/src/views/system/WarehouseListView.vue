<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createWarehouse,
  listWarehouses,
  updateWarehouse,
  type Warehouse,
} from '../../api/warehouse'

const loading = ref(false)
const warehouses = ref<Warehouse[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  name: '',
  address: '',
  status: 'ACTIVE',
})

async function loadData() {
  loading.value = true
  try {
    const { data } = await listWarehouses()
    warehouses.value = data
  } catch {
    ElMessage.error('加载仓库列表失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.name = ''
  form.address = ''
  form.status = 'ACTIVE'
  dialogVisible.value = true
}

function openEdit(row: Warehouse) {
  editingId.value = row.id
  form.name = row.name
  form.address = row.address ?? ''
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入仓库名称')
    return
  }

  try {
    if (editingId.value) {
      await updateWarehouse(editingId.value, {
        name: form.name,
        address: form.address,
        status: form.status,
      })
      ElMessage.success('仓库已更新')
    } else {
      await createWarehouse({ name: form.name, address: form.address })
      ElMessage.success('仓库已创建')
    }
    dialogVisible.value = false
    await loadData()
  } catch {
    ElMessage.error('保存失败')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <div class="page-card">
      <div class="page-header">
        <div>
          <h2 class="page-title">仓库管理</h2>
          <p class="page-subtitle">维护仓库档案与状态</p>
        </div>
        <el-button type="primary" @click="openCreate">新建仓库</el-button>
      </div>

      <el-table v-loading="loading" :data="warehouses" stripe>
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '启用' : row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑仓库' : '新建仓库'"
      width="480px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>
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
</style>
