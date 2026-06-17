import http from './http'
import type { SalesOrder } from './sales'

export interface Customer {
  id: number
  name: string
  contact?: string | null
  phone?: string | null
  address?: string | null
  createdAt: string
}

export interface CreateCustomerPayload {
  name: string
  contact?: string
  phone?: string
  address?: string
}

export interface UpdateCustomerPayload {
  name: string
  contact?: string
  phone?: string
  address?: string
}

export function listCustomers() {
  return http.get<Customer[]>('/customers')
}

export function createCustomer(data: CreateCustomerPayload) {
  return http.post<Customer>('/customers', data)
}

export function updateCustomer(id: number, data: UpdateCustomerPayload) {
  return http.put<Customer>(`/customers/${id}`, data)
}

export function listCustomerOrders(id: number) {
  return http.get<SalesOrder[]>(`/customers/${id}/orders`)
}
