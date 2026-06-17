import http from './http'

export interface StockRecord {
  id: number
  warehouseId: number
  productId: number
  batchId: number
  quantity: number
}

export interface StockInItemPayload {
  productId: number
  batchId: number
  quantity: number
}

export interface StockInPayload {
  warehouseId: number
  items: StockInItemPayload[]
  remark?: string
}

export interface StockOutItemPayload {
  productId: number
  batchId: number
  quantity: number
}

export interface StockOutPayload {
  warehouseId: number
  outType: string
  items: StockOutItemPayload[]
  remark?: string
}

export interface StockCheckItemPayload {
  productId: number
  batchId: number
  actualQty: number
}

export interface StockCheckPayload {
  warehouseId: number
  items: StockCheckItemPayload[]
  remark?: string
}

export interface StockDocument {
  id: number
  docNo: string
  warehouseId: number
  remark?: string | null
}

export function listStocks(warehouseId: number, productId?: number) {
  const params: Record<string, number> = { warehouseId }
  if (productId != null) params.productId = productId
  return http.get<StockRecord[]>('/stocks', { params })
}

export function createStockIn(data: StockInPayload) {
  return http.post<StockDocument>('/stock-ins', data)
}

export function createStockOut(data: StockOutPayload) {
  return http.post<StockDocument & { outType?: string }>('/stock-outs', data)
}

export function createStockCheck(data: StockCheckPayload) {
  return http.post<StockDocument>('/stock-checks', data)
}
