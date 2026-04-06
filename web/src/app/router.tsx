import { Navigate, Route, Routes } from 'react-router-dom'
import { AdminRoute, GuestRoute, ProtectedRoute } from '../components/RouteGuards'
import { AppLayout } from '../layouts/AppLayout'
import { AdminAnalyticsPage } from '../pages/admin/AdminAnalyticsPage'
import { AdminExportPage } from '../pages/admin/AdminExportPage'
import { AdminPerdictPage } from '../pages/admin/AdminPerdictPage'
import { AdminProblemDifficultyPage } from '../pages/admin/AdminProblemDifficultyPage'
import { AdminProblemCreatePage } from '../pages/admin/AdminProblemCreatePage'
import { AdminProblemDetailPage } from '../pages/admin/AdminProblemDetailPage'
import { AdminProblemListPage } from '../pages/admin/AdminProblemListPage'
import { LoginPage } from '../pages/LoginPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { ProblemDetailPage } from '../pages/ProblemDetailPage'
import { ProblemListPage } from '../pages/ProblemListPage'
import { ProfilePage } from '../pages/ProfilePage'
import { RegisterPage } from '../pages/RegisterPage'
import { SubmissionDetailPage } from '../pages/SubmissionDetailPage'
import { UserAnalyticsPage } from '../pages/UserAnalyticsPage'

export function AppRouter() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <GuestRoute>
            <LoginPage />
          </GuestRoute>
        }
      />
      <Route
        path="/register"
        element={
          <GuestRoute>
            <RegisterPage />
          </GuestRoute>
        }
      />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/problems" replace />} />
        <Route path="problems" element={<ProblemListPage />} />
        <Route path="problems/:id" element={<ProblemDetailPage />} />
        <Route path="submissions/:id" element={<SubmissionDetailPage />} />
        <Route path="analytics/user" element={<UserAnalyticsPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route
          path="admin/problems"
          element={
            <AdminRoute>
              <AdminProblemListPage />
            </AdminRoute>
          }
        />
        <Route
          path="admin/problems/create"
          element={
            <AdminRoute>
              <AdminProblemCreatePage />
            </AdminRoute>
          }
        />
        <Route
          path="admin/problems/difficulty"
          element={
            <AdminRoute>
              <AdminProblemDifficultyPage />
            </AdminRoute>
          }
        />
        <Route
          path="admin/problems/:id"
          element={
            <AdminRoute>
              <AdminProblemDetailPage />
            </AdminRoute>
          }
        />
        <Route
          path="admin/analytics"
          element={
            <AdminRoute>
              <AdminAnalyticsPage />
            </AdminRoute>
          }
        />
        <Route
          path="admin/perdict"
          element={
            <AdminRoute>
              <AdminPerdictPage />
            </AdminRoute>
          }
        />
        <Route
          path="admin/export"
          element={
            <AdminRoute>
              <AdminExportPage />
            </AdminRoute>
          }
        />
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
