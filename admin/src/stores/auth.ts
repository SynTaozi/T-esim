import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi, type AdminSession } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const raw = localStorage.getItem('admin_user')
  const user = ref<AdminSession | null>(raw ? JSON.parse(raw) : null)
  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const res = await authApi.login({ username, password })
    if (res.code !== 200) {
      throw new Error(res.message || '登录失败')
    }
    const data = res.data
    if (!data?.role || data.role !== 'ADMIN') {
      throw new Error('非管理员账号无权登录')
    }
    token.value = data.token
    user.value = data
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_user', JSON.stringify(data))
    return true
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  function updateSession(data: AdminSession) {
    token.value = data.token
    user.value = data
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_user', JSON.stringify(data))
  }

  return { token, user, isLoggedIn, login, logout, updateSession }
})
