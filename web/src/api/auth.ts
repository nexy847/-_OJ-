import { apiClient } from './client'
import { CurrentUser, LoginResponse, RegisterPayload, UpdateCurrentUserPayload, UserResponse } from '../types/api'

export async function login(username: string, password: string) {
  const response = await apiClient.post<LoginResponse>('/auth/login', { username, password })
  return response.data
}

export async function register(payload: RegisterPayload) {
  const response = await apiClient.post<UserResponse>('/users', payload)
  return response.data
}

export async function getCurrentUser() {
  const response = await apiClient.get<CurrentUser>('/auth/me')
  return response.data
}

export async function updateCurrentUser(payload: UpdateCurrentUserPayload) {
  const response = await apiClient.put<UserResponse>('/users/me', payload)
  return response.data
}
