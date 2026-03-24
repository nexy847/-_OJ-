import { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../app/useAuth'
import { LoadingScreen } from './LoadingScreen'

type Props = {
  children: ReactNode
}

export function GuestRoute({ children }: Props) {
  const { ready, user, isAdmin } = useAuth()
  if (!ready) {
    return <LoadingScreen />
  }
  if (user) {
    return <Navigate to={isAdmin ? '/admin/analytics' : '/problems'} replace />
  }
  return <>{children}</>
}

export function ProtectedRoute({ children }: Props) {
  const { ready, user } = useAuth()
  const location = useLocation()
  if (!ready) {
    return <LoadingScreen />
  }
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  return <>{children}</>
}

export function AdminRoute({ children }: Props) {
  const { ready, user, isAdmin } = useAuth()
  if (!ready) {
    return <LoadingScreen />
  }
  if (!user) {
    return <Navigate to="/login" replace />
  }
  if (!isAdmin) {
    return <Navigate to="/problems" replace />
  }
  return <>{children}</>
}
