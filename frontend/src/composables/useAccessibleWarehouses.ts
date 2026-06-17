import { ref } from 'vue'
import { listWarehouses, type Warehouse } from '../api/warehouse'
import { useAuthStore } from '../stores/auth'

export function useAccessibleWarehouses() {
  const auth = useAuthStore()
  const warehouses = ref<Warehouse[]>([])
  const loading = ref(false)

  async function load() {
    loading.value = true
    try {
      if (auth.role === 'admin') {
        const { data } = await listWarehouses()
        warehouses.value = data
      } else {
        const allowed = new Set(auth.warehouseIds)
        try {
          const { data } = await listWarehouses()
          warehouses.value = data.filter((w) => allowed.has(w.id))
        } catch {
          warehouses.value = auth.warehouseIds.map((id) => ({
            id,
            name: `仓库 #${id}`,
            address: '',
            status: 'ACTIVE',
          }))
        }
      }
    } finally {
      loading.value = false
    }
  }

  return { warehouses, loading, load }
}
