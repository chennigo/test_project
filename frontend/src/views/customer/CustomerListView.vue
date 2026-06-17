<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createCustomer,
  listCustomerOrders,
  listCustomers,
  updateCustomer,
  type Customer,
} from '../../api/customer'
import type { SalesOrder } from '../../api/sales'

const loading = ref(false)
const customers = ref<Customer[]>([])
const search = ref('')
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const editingId = ref<number | null>(null)
const selectedCustomer = ref<Customer | null>(null)
const orders = ref<SalesOrder[]>([])
const ordersLoading = ref(false)

const form = reactive({
  name: '',
  contact: '',
  phone: '',
  address: '',
})

const isCreate = computed(() => editingId.value === null)

const filteredCustomers = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return customers.value
  return customers.value.filter(
    (c) =>
      c.name.toLowerCase().includes(q) ||
      (c.contact ?? '').toLowerCase().includes(q) ||
      (c.phone ?? '').toLowerCase().includes(q),
  )
})

async function loadData() {
  loading.value = true
  try {
    const { data } = await listCustomers()
    customers.value = data
  } catch {
    ElMessage.error('加载客户列表失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.name = ''
  form.contact = ''
  form.phone = ''
  form.address = ''
  dialogVisible.value = true
}

function openEdit(row: Customer) {
  editingId.value = row.id
  form.name = row.name
  form.contact = row.contact ?? ''
  form.phone = row.phone ?? ''
  form.address = row.address ?? ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入客户名称')
    return
  }

  const payload = {
    name: form.name.trim(),
    contact: form.contact.trim() || undefined,
    phone: form.phone.trim() || undefined,
    address: form.address.trim() || undefined,
  }

  try {
    if (isCreate.value) {
      await createCustomer(payload)
      ElMessage.success('客户已创建')
    } else {
      await updateCustomer(editingId.value!, payload)
      ElMessage.success('客户已更新')
    }
    dialogVisible.value = false
    await loadData()
  } catch {
    ElMessage.error('保存失败')
  }
}

async function openOrders(row: Customer) {
  selectedCustomer.value = row
  drawerVisible.value = true
  ordersLoading.value = true
  try {
    const { data } = await listCustomerOrders(row.id)
    orders.value = data
  } catch {
    ElMessage.error('加载订单历史失败')
    orders.value = []
  } finally {
    ordersLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page">
    <div class="page-card">
      <div class="page-header">
        <div>
          <h2 class="page-title">客户档案</h2>
          <p class="page-subtitle">维护客户信息与查看历史订单</p>
        </div>
        <el-button type="primary" @click="openCreate">新建客户</el-button>
      </div>

      <div class="toolbar">
        <el-input v-model="search" placeholder="搜索名称、联系人或电话" clearable style="width: 260px" />
      </div>

      <el-table v-loading="loading" :data="filteredCustomers" stripe>
        <el-table-column prop="name" label="客户名称" min-width="140" />
        <el-table-column prop="contact" label="联系人" width="120">
          <template #default="{ row }">{{ row.contact || '—' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="130">
          <template #default="{ row }">{{ row.phone || '—' }}</template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.address || '—' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openOrders(row)">订单</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isCreate ? '新建客户' : '编辑客户'"
      width="520px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="客户名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contact" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="drawerVisible"
      :title="selectedCustomer ? `${selectedCustomer.name} — 订单历史` : '订单历史'"
      size="520px"
      destroy-on-close
    >
      <el-table v-loading="ordersLoading" :data="orders" stripe size="small">
        <el-table-column prop="docNo" label="单号" min-width="140">
          <template #default="{ row }">
            <span class="doc-no-inline">{{ row.docNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="100" align="right" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="150" />
      </el-table>
      <p v-if="!ordersLoading && !orders.length" class="empty-hint">暂无订单记录</p>
    </el-drawer>
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

.empty-hint {
  margin-top: 1rem;
  color: var(--color-text-muted);
  font-size: 0.9rem;
}
</style>
