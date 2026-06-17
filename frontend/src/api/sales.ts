import http from './http'

export interface SalesOrderItemPayload {
  productId: number
  batchId: number
  quantity: number
  unitPrice: number
}

export interface SalesOrderPayload {
  customerId: number
  warehouseId: number
  items: SalesOrderItemPayload[]
}

export interface SalesOrder {
  id: number
  docNo: string
  customerId: number
  warehouseId: number
  operatorId: number
  totalAmount: number
  status: string
  createdAt: string
}

export function listSalesOrders() {
  return http.get<SalesOrder[]>('/sales-orders')
}

export function createSalesOrder(data: SalesOrderPayload) {
  return http.post<SalesOrder>('/sales-orders', data)
}
