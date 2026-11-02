import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

/** Login returns { token, user }; profile returns MemberVO. Normalize both. */
function normalizeUser(raw: any) {
  if (!raw || typeof raw !== 'object') return null
  if (raw.user && typeof raw.user === 'object' && (raw.token != null || raw.user.id != null || raw.user.email != null)) {
    return raw.user
  }
  return raw
}

function readStoredUser() {
  try {
    const raw = localStorage.getItem('user')
    if (!raw) return null
    const normalized = normalizeUser(JSON.parse(raw))
    if (normalized && raw) {
      // migrate nested AuthTokenVO accidentally stored as user
      localStorage.setItem('user', JSON.stringify(normalized))
    }
    return normalized
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<any>(readStoredUser())

  const isLoggedIn = computed(() => !!token.value)

  /** Email used for order fulfillment / Stripe receipt */
  const email = computed(() => {
    const u = user.value
    return String(u?.email || u?.user?.email || '').trim()
  })

  function persistSession(nextToken: string, nextUser: any) {
    token.value = nextToken
    user.value = normalizeUser(nextUser)
    localStorage.setItem('token', nextToken)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  async function login(username: string, password: string) {
    try {
      const res: any = await authApi.login({ username, password })
      if (res.code === 200) {
        persistSession(res.data.token, res.data.user || res.data)
        return true
      }
      throw new Error(res.message || '登录失败')
    } catch (error: any) {
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message)
      }
      throw error
    }
  }

  async function register(data: { username: string; password: string; email: string; verificationCode: string; phone?: string; referralCode?: string }) {
    try {
      const res: any = await authApi.register(data)
      if (res.code === 200) {
        persistSession(res.data.token, res.data.user || res.data)
        return true
      }
      throw new Error(res.message || '注册失败')
    } catch (error: any) {
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message)
      }
      throw error
    }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function fetchProfile() {
    try {
      const res: any = await authApi.getProfile()
      if (res.code === 200) {
        user.value = normalizeUser(res.data)
        localStorage.setItem('user', JSON.stringify(user.value))
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }

  return {
    token,
    user,
    email,
    isLoggedIn,
    login,
    register,
    logout,
    fetchProfile
  }
})
