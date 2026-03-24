import { apiClient } from './client'
import {
  CreateProblemPayload,
  CreateTestcasePayload,
  ProblemResponse,
  TestcaseResponse,
  UpdateProblemPayload,
} from '../types/api'

export async function listProblems() {
  const response = await apiClient.get<ProblemResponse[]>('/problems')
  return response.data
}

export async function getProblem(id: string | number) {
  const response = await apiClient.get<ProblemResponse>(`/problems/${id}`)
  return response.data
}

export async function createProblem(payload: CreateProblemPayload) {
  const response = await apiClient.post<ProblemResponse>('/problems', payload)
  return response.data
}

export async function updateProblem(id: string | number, payload: UpdateProblemPayload) {
  const response = await apiClient.put<ProblemResponse>(`/problems/${id}`, payload)
  return response.data
}

export async function listProblemTestcases(problemId: string | number) {
  const response = await apiClient.get<TestcaseResponse[]>(`/problems/${problemId}/testcases`)
  return response.data
}

export async function createProblemTestcase(problemId: string | number, payload: CreateTestcasePayload) {
  const response = await apiClient.post<TestcaseResponse>(`/problems/${problemId}/testcases`, payload)
  return response.data
}

export async function updateProblemTestcase(
  problemId: string | number,
  testcaseId: string | number,
  payload: CreateTestcasePayload,
) {
  const response = await apiClient.put<TestcaseResponse>(
    `/problems/${problemId}/testcases/${testcaseId}`,
    payload,
  )
  return response.data
}

export async function deleteProblemTestcase(problemId: string | number, testcaseId: string | number) {
  await apiClient.delete(`/problems/${problemId}/testcases/${testcaseId}`)
}
