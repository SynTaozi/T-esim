import api, { type ApiResult } from './index'

export interface AdminSession {
  token: string
  id: number
  username: string
  email: string
  role: string
  fullName?: string
}

export const authApi = {
  login: (data: { username: string; password: string }) =>
    api.post('/admin/auth/login', data) as Promise<ApiResult<AdminSession>>,
  me: () => api.get('/admin/auth/me') as Promise<ApiResult<AdminSession>>,
  updateAccount: (data: {
    username?: string
    currentPassword: string
    newPassword?: string
  }) => api.put('/admin/account', data) as Promise<ApiResult<AdminSession>>
}
