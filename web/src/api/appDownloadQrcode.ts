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
}

function unwrap<T>(res: any): T {
  if (res && typeof res === 'object' && 'data' in res && (res.code === 0 || res.code === 200)) {
    return res.data as T
  }
  return res as T
}

export const appDownloadQrcodeApi = {
  listEnabled: async () => unwrap<AppDownloadQrcode[]>(await api.get('/app-download-qrcodes'))
}
