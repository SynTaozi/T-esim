import api from './index'

export interface LoginData {
  username: string
  password: string
}

export interface RegisterData {
  username: string
  password: string
  email: string
  phone?: string
  referralCode?: string
  verificationCode: string
}

export const authApi = {
  login: (data: LoginData) => api.post('/auth/login', data),
  register: (data: RegisterData) => api.post('/auth/register', data),
  getProfile: () => api.get('/auth/profile'),
  updateProfile: (data: any) => api.put('/auth/profile', data),
  sendRegisterCode: (email: string) => api.post('/auth/send-register-code', { email }),
  sendResetCode: (email: string) => api.post('/auth/send-reset-code', { email }),
  resetPassword: (data: { email: string; verificationCode: string; newPassword: string }) =>
    api.post('/auth/reset-password', data)
}
