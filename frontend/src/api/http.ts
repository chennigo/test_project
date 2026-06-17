import axios from 'axios'

const STORAGE_KEY = 'systelm_auth'

function getStoredToken(): string {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return ''
    return (JSON.parse(raw) as { token?: string }).token ?? ''
  } catch {
    return ''
  }
}

const http = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

http.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(STORAGE_KEY)
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default http
