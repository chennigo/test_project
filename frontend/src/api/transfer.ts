import http from './http'

export interface TransferItemPayload {
  productId: number
  batchId: number
  quantity: number
}

export interface TransferPayload {
  fromWarehouseId: number
  toWarehouseId: number
  items: TransferItemPayload[]
}

export interface Transfer {
  id: number
  docNo: string
  fromWarehouseId: number
  toWarehouseId: number
  operatorId: number
  status: string
  createdAt: string
}

export function createTransfer(data: TransferPayload) {
  return http.post<Transfer>('/transfers', data)
}
