import http from './http'

export interface Product {
  id: number
  name: string
  sku: string
  categoryId?: number | null
  costPrice: number
  salePrice: number
  unit: string
  minStock: number
  status: string
}

export interface ProductBatch {
  id: number
  productId: number
  batchNo: string
  productionDate?: string | null
}

export interface ProductCategory {
  id: number
  name: string
  parentId?: number | null
}

export interface CreateProductPayload {
  name: string
  sku: string
  costPrice?: number
  salePrice?: number
  unit?: string
  minStock?: number
}

export interface UpdateProductPayload {
  name?: string
  sku?: string
  categoryId?: number | null
  costPrice?: number
  salePrice?: number
  unit?: string
  minStock?: number
  status?: string
}

export interface CreateBatchPayload {
  batchNo: string
  productionDate?: string
}

export function listProducts() {
  return http.get<Product[]>('/products')
}

export function createProduct(data: CreateProductPayload) {
  return http.post<Product>('/products', data)
}

export function updateProduct(id: number, data: UpdateProductPayload) {
  return http.put<Product>(`/products/${id}`, data)
}

export function listProductBatches(productId: number) {
  return http.get<ProductBatch[]>(`/products/${productId}/batches`)
}

export function createProductBatch(productId: number, data: CreateBatchPayload) {
  return http.post<ProductBatch>(`/products/${productId}/batches`, data)
}

export function listCategories() {
  return http.get<ProductCategory[]>('/product-categories')
}
