import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  if ((config.method || 'get').toLowerCase() === 'get') {
    config.params = { ...(config.params || {}), _t: Date.now() }
  }
  return config
})

api.interceptors.response.use(
  (response) => {
    const data = response.data
    // 后端业务失败常仍返回 HTTP 200 + { code != 200 }，必须按失败处理，避免页面误报「保存成功」
    if (data && typeof data === 'object' && typeof data.code === 'number' && data.code !== 200) {
      const message = data.message || '操作失败'
      ElMessage.error(message)
      return Promise.reject(data)
    }
    return data
  },
  (error) => {
    const status = error.response?.status
    const payload = error.response?.data
    const message = payload?.message || error.message || '操作失败'
    ElMessage.error(message)
    // 未带 token / token 失效时后端可能返回 401；无 ADMIN 角色为 403
    if (status === 401 || status === 403) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api

export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
