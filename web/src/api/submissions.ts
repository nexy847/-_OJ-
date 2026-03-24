import axios from 'axios'
import { apiClient } from './client'
import { CreateSubmissionPayload, JudgeResultResponse, SubmissionResponse } from '../types/api'

export async function createSubmission(payload: CreateSubmissionPayload) {
  const response = await apiClient.post<SubmissionResponse>('/submissions', payload)
  return response.data
}

export async function getSubmission(id: string | number) {
  const response = await apiClient.get<SubmissionResponse>(`/submissions/${id}`)
  return response.data
}

export async function getSubmissionResultOrNull(id: string | number) {
  try {
    const response = await apiClient.get<JudgeResultResponse>(`/submissions/${id}/result`)
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 400) {
      return null
    }
    throw error
  }
}
