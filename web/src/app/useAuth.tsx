import { createContext, ReactNode, useContext, useEffect, useMemo, useState } from 'react'
import { getCurrentUser } from '../api/auth'
import { clearStoredToken, getStoredToken, setStoredToken } from '../api/client'
import { CurrentUser } from '../types/api'

type AuthContextValue = {
  ready: boolean
  token: string | null
  user: CurrentUser | null
  isAdmin: boolean
  login: (token: string) => Promise<CurrentUser>
  logout: () => void
  refresh: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

type Props = {
  children: ReactNode
}

export function AuthProvider({ children }: Props) {
  const [token, setToken] = useState<string | null>(() => getStoredToken())
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [ready, setReady] = useState(false)

  useEffect(() => {
    let cancelled = false

    const bootstrap = async () => {
      if (!token) {
        setReady(true)
        return
      }

      try {
        const me = await getCurrentUser()
        if (!cancelled) {
          setUser(me)
        }
      } catch {
        clearStoredToken()
        if (!cancelled) {
          setToken(null)
          setUser(null)
        }
      } finally {
        if (!cancelled) {
          setReady(true)
        }
      }
    }

    void bootstrap()

    return () => {
      cancelled = true
    }
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      ready,
      token,
      user,
      isAdmin: user?.role === 'ADMIN',
      login: async (nextToken: string) => {
        setStoredToken(nextToken)
        try {
          const me = await getCurrentUser()
          setToken(nextToken)
          setUser(me)
          setReady(true)
          return me
        } catch (error) {
          clearStoredToken()
          setToken(null)
          setUser(null)
          setReady(true)
          throw error
        }
      },
      logout: () => {
        clearStoredToken()
        setToken(null)
        setUser(null)
        setReady(true)
      },
      refresh: async () => {
        if (!getStoredToken()) {
          setToken(null)
          setUser(null)
          setReady(true)
          return
        }
        const me = await getCurrentUser()
        setToken(getStoredToken())
        setUser(me)
        setReady(true)
      },
    }),
    [ready, token, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }
  return context
}
