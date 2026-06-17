<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listWarehouses } from '../../api/warehouse'
import { createUser, listUsers, updateUser, type UpdateUserPayload, type User } from '../../api/user'

const loading = ref(false)
const users = ref<User[]>([])
const allWarehouses = ref<{ id: number; name: string }[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const roleOptions = [
  { label: '超级管理员', value: 'admin' },
  { label: '仓管/销售', value: 'warehouse_sales' },
  { label: '财务/统计', value: 'finance' },
]

const form = reactive({
  username: '',
  password: '',
  displayName: '',
  roleName: 'warehouse_sales',
  warehouseIds: [] as number[],
  status: 'ACTIVE',
})

const isCreate = computed(() => editingId.value === null)

async function loadData() {
  loading.value = true
  try {
    const [usersRes, warehousesRes] = await Promise.all([listUsers(), listWarehouses()])
    users.value = usersRes.data
    allWarehouses.value = warehousesRes.data.map((w) => ({ id: w.id, name: w.name }))
  } catch {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function warehouseLabel(ids: number[]) {
  if (!ids.length) return '—'
  return ids
    .map((id) => allWarehouses.value.find((w) => w.id === id)?.name ?? `#${id}`)
    .join('、')
}

function roleLabel(roleName: string) {
  return roleOptions.find((r) => r.value === roleName)?.label ?? roleName
}

function openCreate() {
  editingId.value = null
  form.username = ''
  form.password = ''
  form.displayName = ''
  form.roleName = 'warehouse_sales'
  form.warehouseIds = []
  form.status = 'ACTIVE'
  dialogVisible.value = true
}

function openEdit(row: User) {
  editingId.value = row.id
  form.username = row.username
  form.password = ''
  form.displayName = row.displayName
  form.roleName = row.roleName
  form.warehouseIds = [...row.warehouseIds]
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.displayName.trim()) {
    ElMessage.warning('请输入显示名称')
    return
  }
  if (isCreate.value && !form.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (isCreate.value && !form.password) {
    ElMessage.warning('请输入密码')
    return
  }

  try {
    if (isCreate.value) {
      await createUser({
        username: form.username,
        password: form.password,
        displayName: form.displayName,
        roleName: form.roleName,
        warehouseIds: form.warehouseIds,
      })
      ElMessage.success('用户已创建')
    } else {
      const payload: UpdateUserPayload = {
        displayName: form.displayName,
        roleName: form.roleName,
        warehouseIds: form.warehouseIds,
        status: form.status,
      }
      if (form.password) payload.password = form.password
      await updateUser(editingId.value!, payload)
      ElMessage.success('用户已更新')
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
          <h2 class="page-title">用户管理</h2>
          <p class="page-subtitle">分配角色与仓库权限</p>
        </div>
        <el-button type="primary" @click="openCreate">新建用户</el-button>
      </div>

      <el-table v-loading="loading" :data="users" stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="displayName" label="显示名称" width="140" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">{{ roleLabel(row.roleName) }}</template>
        </el-table-column>
        <el-table-column label="仓库" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ warehouseLabel(row.warehouseIds) }}</template>
        </el-table-column>
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
      :title="isCreate ? '新建用户' : '编辑用户'"
      width="520px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item v-if="isCreate" label="用户名" required>
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item v-else label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="显示名称" required>
          <el-input v-model="form.displayName" />
        </el-form-item>
        <el-form-item :label="isCreate ? '密码' : '新密码（留空不修改）'" :required="isCreate">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleName" style="width: 100%">
            <el-option
              v-for="opt in roleOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分配仓库">
          <el-select v-model="form.warehouseIds" multiple style="width: 100%" placeholder="选择仓库">
            <el-option
              v-for="w in allWarehouses"
              :key="w.id"
              :label="w.name"
              :value="w.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isCreate" label="状态">
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
