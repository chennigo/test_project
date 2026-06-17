import http from './http'

export interface User {
  id: number
  username: string
  displayName: string
  roleName: string
  warehouseIds: number[]
  status: string
}

export interface CreateUserPayload {
  username: string
  password: string
  displayName: string
  roleName: string
  warehouseIds: number[]
}

export interface UpdateUserPayload {
  displayName?: string
  roleName?: string
  warehouseIds?: number[]
  status?: string
  password?: string
}

export function listUsers() {
  return http.get<User[]>('/users')
}

export function createUser(data: CreateUserPayload) {
  return http.post<User>('/users', data)
}

export function updateUser(id: number, data: UpdateUserPayload) {
  return http.put<User>(`/users/${id}`, data)
}
