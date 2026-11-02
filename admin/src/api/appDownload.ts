import api from './index'

export interface AppDownloadQrcode {
  id: number
  platform: string
  name: string
  nameEn?: string
  downloadUrl: string
  qrImageUrl?: string
  sortOrder?: number
  status?: number
  remark?: string
}

export const appDownloadApi = {
  listAll: () => api.get('/admin/app-download-qrcodes'),
  getById: (id: number) => api.get(`/admin/app-download-qrcodes/${id}`),
  create: (data: Partial<AppDownloadQrcode>) => api.post('/admin/app-download-qrcodes', data),
  update: (id: number, data: Partial<AppDownloadQrcode>) =>
    api.put(`/admin/app-download-qrcodes/${id}`, data),
  remove: (id: number) => api.delete(`/admin/app-download-qrcodes/${id}`)
}
