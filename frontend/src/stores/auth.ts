import { defineStore } from 'pinia'
import http from '../api/http'

const STORAGE_KEY = 'systelm_auth'

export type UserRole = 'admin' | 'warehouse_sales' | 'finance' | ''

interface AuthState {
  token: string
  username: string
  displayName: string
  role: UserRole
  warehouseIds: number[]
}

interface LoginResponse {
  token: string
  username: string
  displayName: string
  role: string
  warehouseIds: number[]
}

function loadPersisted(): Partial<AuthState> {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return {}
    return JSON.parse(raw) as Partial<AuthState>
  } catch {
    return {}
  }
}

function persist(state: AuthState) {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      token: state.token,
      username: state.username,
      displayName: state.displayName,
      role: state.role,
      warehouseIds: state.warehouseIds,
    }),
  )
}

function clearPersisted() {
  localStorage.removeItem(STORAGE_KEY)
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: '',
    username: '',
    displayName: '',
    role: '',
    warehouseIds: [],
    ...loadPersisted(),
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    roleLabel: (state) => {
      const labels: Record<string, string> = {
        admin: '超级管理员',
        warehouse_sales: '仓管/销售',
        finance: '财务/统计',
      }
      return labels[state.role] ?? state.role
    },
  },

  actions: {
    async login(username: string, password: string) {
      const { data } = await http.post<LoginResponse>('/auth/login', {
        username,
        password,
      })

      this.token = data.token
      this.username = data.username
      this.displayName = data.displayName
      this.role = data.role as UserRole
      this.warehouseIds = data.warehouseIds ?? []
      persist(this.$state)
    },

    logout() {
      this.token = ''
      this.username = ''
      this.displayName = ''
      this.role = ''
      this.warehouseIds = []
      clearPersisted()
    },
  },
})
