import api from './index'

export interface AdminAccount {
  id: number
  username: string
  email: string
  role: string
}

export interface AdminAccountUpdatePayload {
  currentPassword: string
  username?: string
  newPassword?: string
}

export const accountApi = {
  getAccount: () => api.get('/admin/account'),
  updateAccount: (data: AdminAccountUpdatePayload) => api.put('/admin/account', data)
}
