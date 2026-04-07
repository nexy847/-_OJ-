import { apiClient } from './client'
import { AdminSubmissionDetailResponse, AdminSubmissionListItemResponse } from '../types/api'

export async function getAdminSubmissions() {
  const response = await apiClient.get<AdminSubmissionListItemResponse[]>('/admin/submissions')
  return response.data
}

export async function getAdminSubmissionDetail(id: string | number) {
  const response = await apiClient.get<AdminSubmissionDetailResponse>(`/admin/submissions/${id}`)
  return response.data
}
