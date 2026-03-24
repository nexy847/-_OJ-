import { apiClient } from './client'

export async function exportHdfs() {
  const response = await apiClient.post<string>('/admin/export-hdfs')
  return response.data
}
