import http from './http'

export interface StockSummaryRow {
  warehouseId: number
  warehouseName: string
  productId: number
  productName: string
  sku: string
  batchId: number
  batchNo: string
  quantity: number
  totalValue: number
}

export interface SalesSummaryRow {
  period: string
  productId: number
  productName: string
  customerId: number
  customerName: string
  quantity: number
  amount: number
}

export interface ProfitSummaryRow {
  period: string
  salesAmount: number
  costAmount: number
  profit: number
}

export interface LowStockRow {
  warehouseId: number
  warehouseName: string
  productId: number
  productName: string
  sku: string
  currentQty: number
  minStock: number
}

export function fetchStockReport(warehouseId?: number) {
  return http.get<StockSummaryRow[]>('/reports/stock', {
    params: warehouseId ? { warehouseId } : {},
  })
}

export function fetchSalesReport(from?: string, to?: string) {
  return http.get<SalesSummaryRow[]>('/reports/sales', { params: { from, to } })
}

export function fetchProfitReport(from?: string, to?: string) {
  return http.get<ProfitSummaryRow[]>('/reports/profit', { params: { from, to } })
}

export function fetchLowStock(warehouseId?: number) {
  return http.get<LowStockRow[]>('/reports/low-stock', {
    params: warehouseId ? { warehouseId } : {},
  })
}
