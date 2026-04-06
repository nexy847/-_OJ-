export type Role = 'ADMIN' | 'USER'

export type CurrentUser = {
  id: number
  username: string
  role: Role
}

export type LoginResponse = {
  token: string
  expiresAt: string
}

export type RegisterPayload = {
  username: string
  password: string
}

export type UserResponse = {
  id: number
  username: string
  createdAt: string
}

export type UpdateCurrentUserPayload = {
  username: string
  currentPassword: string
  newPassword?: string
}

export type TestcaseResponse = {
  id: number
  inputPath: string
  outputPath: string
  weight: number
}

export type ProblemResponse = {
  id: number
  title: string
  description: string
  timeLimitMs: number
  memoryLimitMb: number
  createdAt: string
  testcases: TestcaseResponse[]
}

export type CreateProblemPayload = {
  title: string
  description: string
  timeLimitMs: number
  memoryLimitMb: number
  testcases: {
    inputPath: string
    outputPath: string
    weight: number
  }[]
  testcaseContents?: CreateTestcasePayload[]
}

export type UpdateProblemPayload = {
  title: string
  description: string
  timeLimitMs: number
  memoryLimitMb: number
}

export type CreateTestcasePayload = {
  inputContent: string
  outputContent: string
  weight: number
}

export type Language = 'C' | 'CPP' | 'JAVA' | 'PYTHON'

export type SubmissionResponse = {
  id: number
  userId: number
  problemId: number
  language: Language
  status: string
  verdict: string
  createdAt: string
  updatedAt: string
}

export type CreateSubmissionPayload = {
  problemId: number
  language: Language
  code: string
}

export type JudgeResultResponse = {
  submissionId: number
  verdict: string
  timeMs: number
  memoryKb: number
  compileError: string | null
  runtimeError: string | null
  message: string | null
  createdAt: string
}

export type UserOverviewResponse = {
  userId: number
  total: number
  accepted: number
  acRate: number
  avgTimeMs: number
  avgMemoryKb: number
}

export type UserDailyTrendResponse = {
  date: string
  total: number
  accepted: number
  acRate: number
  avgTimeMs: number
  avgMemoryKb: number
}

export type UserLanguageStatResponse = {
  language: string
  total: number
  accepted: number
  acRate: number
}

export type UserLanguageDailyResponse = {
  date: string
  language: string
  total: number
  accepted: number
  acRate: number
}

export type UserVerdictStatResponse = {
  verdict: string
  total: number
}

export type UserVerdictDailyResponse = {
  date: string
  verdict: string
  total: number
}

export type UserProblemStatResponse = {
  problemId: number
  problemTitle: string | null
  total: number
  accepted: number
  acRate: number
  avgTimeMs: number
  avgMemoryKb: number
}

export type UserRecentSubmissionResponse = {
  submissionId: number
  problemId: number
  problemTitle: string | null
  language: string
  verdict: string
  timeMs: number
  memoryKb: number
  createdAt: string
}

export type AdminOverviewResponse = {
  date: string
  total: number
  accepted: number
  acRate: number
  avgTimeMs: number
  avgMemoryKb: number
  activeUsers: number
}

export type AdminDailyTrendResponse = {
  date: string
  total: number
  accepted: number
  acRate: number
  avgTimeMs: number
  avgMemoryKb: number
}

export type AdminLanguageStatResponse = {
  language: string
  total: number
  accepted: number
  acRate: number
}

export type AdminLanguageDailyResponse = {
  date: string
  language: string
  total: number
  accepted: number
  acRate: number
  avgTimeMs: number
  avgMemoryKb: number
}

export type AdminVerdictStatResponse = {
  verdict: string
  total: number
}

export type AdminVerdictDailyResponse = {
  date: string
  verdict: string
  total: number
}

export type AdminProblemStatResponse = {
  problemId: number
  problemTitle: string | null
  total: number
  accepted: number
  acRate: number
  avgTimeMs: number
  avgMemoryKb: number
}

export type AdminUserStatResponse = {
  userId: number
  username: string | null
  total: number
  accepted: number
  acRate: number
}

export type AdminHourlyActivityResponse = {
  date: string
  hourOfDay: number
  total: number
  accepted: number
  acRate: number
  activeUsers: number
}

export type AdminProblemVerdictResponse = {
  date: string
  problemId: number
  verdict: string
  total: number
}

export type AdminSubmissionForecastResponse = {
  forecastDate: string
  targetDate: string
  predictedSubmissions: number
  modelName: string
}

export type AdminProblemDifficultyResponse = {
  date: string
  problemId: number
  problemTitle: string | null
  totalSubmissions: number
  acceptedSubmissions: number
  acRate: number
  avgTimeMs: number
  avgMemoryKb: number
  avgAttemptsPerUser: number
  waRate: number
  reRate: number
  tleRate: number
  difficultyScore: number
  difficultyLabel: string
  modelName: string
}

export type AdminProblemDifficultyHistoryResponse = {
  date: string
  difficultyScore: number
  difficultyLabel: string
  acRate: number
  avgTimeMs: number
  avgAttemptsPerUser: number
}
