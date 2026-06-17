import http from './http'

export interface Warehouse {
  id: number
  name: string
  address?: string | null
  status: string
}

export interface CreateWarehousePayload {
  name: string
  address?: string
}

export interface UpdateWarehousePayload {
  name?: string
  address?: string
  status?: string
}

export function listWarehouses() {
  return http.get<Warehouse[]>('/warehouses')
}

export function createWarehouse(data: CreateWarehousePayload) {
  return http.post<Warehouse>('/warehouses', data)
}

export function updateWarehouse(id: number, data: UpdateWarehousePayload) {
  return http.put<Warehouse>(`/warehouses/${id}`, data)
}
