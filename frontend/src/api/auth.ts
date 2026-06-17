import http from './http'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  displayName: string
  role: string
  warehouseIds: number[]
}

export function login(data: LoginRequest) {
  return http.post<LoginResponse>('/auth/login', data)
}
