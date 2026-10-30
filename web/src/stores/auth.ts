import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<any>(localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    try {
      const res: any = await authApi.login({ username, password })
      if (res.code === 200) {
        token.value = res.data.token
        user.value = res.data
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data))
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
        token.value = res.data.token
        user.value = res.data
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data))
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
        user.value = res.data
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    register,
    logout,
    fetchProfile
  }
})
