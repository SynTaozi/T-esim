import api from './index'
import type { SiteDisplayConfig } from '@/constants/siteDisplayDefaults'

export const siteDisplayApi = {
  getConfig: () => api.get<{ code: number; data: SiteDisplayConfig }>('/public/site-display')
}
