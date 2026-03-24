import axios from 'axios'

const TOKEN_KEY = 'oj_token'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  timeout: 15000,
})

apiClient.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      clearStoredToken()
      if (window.location.pathname !== '/login') {
        window.location.assign('/login')
      }
    }
    return Promise.reject(error)
  },
)

export function getStoredToken() {
  return window.localStorage.getItem(TOKEN_KEY)
}

export function setStoredToken(token: string) {
  window.localStorage.setItem(TOKEN_KEY, token)
}

export function clearStoredToken() {
  window.localStorage.removeItem(TOKEN_KEY)
}

export function extractApiError(error: unknown) {
  if (axios.isAxiosError(error)) {
    const payload = error.response?.data
    if (typeof payload === 'string' && payload.trim()) {
      return payload
    }
    if (typeof payload?.message === 'string' && payload.message.trim()) {
      return payload.message
    }
    return error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return 'Request failed'
}
