import { apiClient } from './client'
import {
  AdminDailyTrendResponse,
  AdminHourlyActivityResponse,
  AdminLanguageDailyResponse,
  AdminLanguageStatResponse,
  AdminOverviewResponse,
  AdminProblemDifficultyHistoryResponse,
  AdminProblemDifficultyResponse,
  AdminProblemStatResponse,
  AdminProblemVerdictResponse,
  AdminSubmissionForecastResponse,
  AdminUserStatResponse,
  AdminVerdictDailyResponse,
  AdminVerdictStatResponse,
  UserDailyTrendResponse,
  UserLanguageDailyResponse,
  UserLanguageStatResponse,
  UserOverviewResponse,
  UserProblemStatResponse,
  UserRecentSubmissionResponse,
  UserVerdictDailyResponse,
  UserVerdictStatResponse,
} from '../types/api'

export async function getUserOverview() {
  const response = await apiClient.get<UserOverviewResponse>('/analytics/user/overview')
  return response.data
}

export async function getUserDaily(days: number) {
  const response = await apiClient.get<UserDailyTrendResponse[]>('/analytics/user/daily', {
    params: { days },
  })
  return response.data
}

export async function getUserLanguage() {
  const response = await apiClient.get<UserLanguageStatResponse[]>('/analytics/user/language')
  return response.data
}

export async function getUserLanguageDaily(days: number) {
  const response = await apiClient.get<UserLanguageDailyResponse[]>('/analytics/user/language/daily', {
    params: { days },
  })
  return response.data
}

export async function getUserVerdict() {
  const response = await apiClient.get<UserVerdictStatResponse[]>('/analytics/user/verdict')
  return response.data
}

export async function getUserVerdictDaily(days: number) {
  const response = await apiClient.get<UserVerdictDailyResponse[]>('/analytics/user/verdict/daily', {
    params: { days },
  })
  return response.data
}

export async function getUserProblems(limit: number) {
  const response = await apiClient.get<UserProblemStatResponse[]>('/analytics/user/problems', {
    params: { limit },
  })
  return response.data
}

export async function getUserRecent(limit: number) {
  const response = await apiClient.get<UserRecentSubmissionResponse[]>('/analytics/user/recent', {
    params: { limit },
  })
  return response.data
}

export async function getAdminOverview(date?: string) {
  const response = await apiClient.get<AdminOverviewResponse>('/analytics/admin/overview', {
    params: date ? { date } : undefined,
  })
  return response.data
}

export async function getAdminDaily(days: number) {
  const response = await apiClient.get<AdminDailyTrendResponse[]>('/analytics/admin/daily', {
    params: { days },
  })
  return response.data
}

export async function getAdminLanguage(date?: string) {
  const response = await apiClient.get<AdminLanguageStatResponse[]>('/analytics/admin/language', {
    params: date ? { date } : undefined,
  })
  return response.data
}

export async function getAdminLanguageDaily(days: number) {
  const response = await apiClient.get<AdminLanguageDailyResponse[]>('/analytics/admin/language/daily', {
    params: { days },
  })
  return response.data
}

export async function getAdminVerdict(date?: string) {
  const response = await apiClient.get<AdminVerdictStatResponse[]>('/analytics/admin/verdict', {
    params: date ? { date } : undefined,
  })
  return response.data
}

export async function getAdminVerdictDaily(days: number) {
  const response = await apiClient.get<AdminVerdictDailyResponse[]>('/analytics/admin/verdict/daily', {
    params: { days },
  })
  return response.data
}

export async function getAdminHourly(date?: string) {
  const response = await apiClient.get<AdminHourlyActivityResponse[]>('/analytics/admin/hourly', {
    params: date ? { date } : undefined,
  })
  return response.data
}

export async function getAdminProblemVerdict(problemId: string | number, date?: string) {
  const response = await apiClient.get<AdminProblemVerdictResponse[]>(
    `/analytics/admin/problems/${problemId}/verdict`,
    {
      params: date ? { date } : undefined,
    },
  )
  return response.data
}

export async function getAdminTopProblems(date?: string, limit = 20) {
  const response = await apiClient.get<AdminProblemStatResponse[]>('/analytics/admin/problems/top', {
    params: { date, limit },
  })
  return response.data
}

export async function getAdminTopUsers(date?: string, limit = 20) {
  const response = await apiClient.get<AdminUserStatResponse[]>('/analytics/admin/users/top', {
    params: { date, limit },
  })
  return response.data
}

export async function getAdminSubmissionForecast(forecastDate?: string) {
  const response = await apiClient.get<AdminSubmissionForecastResponse[]>('/analytics/admin/forecast/submissions', {
    params: forecastDate ? { forecastDate } : undefined,
  })
  return response.data
}

export async function getAdminProblemDifficulty(date?: string, limit = 50, label?: string) {
  const response = await apiClient.get<AdminProblemDifficultyResponse[]>('/analytics/admin/problems/difficulty', {
    params: {
      date,
      limit,
      label,
    },
  })
  return response.data
}

export async function getAdminProblemDifficultyHistory(problemId: string | number, days = 30) {
  const response = await apiClient.get<AdminProblemDifficultyHistoryResponse[]>(
    `/analytics/admin/problems/${problemId}/difficulty/history`,
    {
      params: { days },
    },
  )
  return response.data
}
