import api from './index'

export interface SiteDisplayConfig {
  loginBannerUrl?: string
  registerBannerUrl?: string
  homeHeroImageUrl?: string
  defaultShippingFee?: number
}

export const siteApi = {
  getConfig: () => api.get('/admin/site-display'),
  updateConfig: (data: SiteDisplayConfig) => api.put('/admin/site-display', data)
}
