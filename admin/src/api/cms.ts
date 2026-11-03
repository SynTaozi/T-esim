import api, { type ApiResult, type PageResult } from './index'

export const faqApi = {
  listCategories: () => api.get('/admin/faq/categories') as Promise<ApiResult<any[]>>,
  saveCategory: (data: any) =>
    data.id
      ? (api.put(`/admin/faq/categories/${data.id}`, data) as Promise<ApiResult<any>>)
      : (api.post('/admin/faq/categories', data) as Promise<ApiResult<any>>),
  removeCategory: (id: number) => api.delete(`/admin/faq/categories/${id}`) as Promise<ApiResult<void>>,
  list: (params?: Record<string, unknown>) => api.get('/admin/faq', { params }) as Promise<ApiResult<any[]>>,
  save: (data: any) =>
    data.id ? (api.put(`/admin/faq/${data.id}`, data) as Promise<ApiResult<any>>) : (api.post('/admin/faq', data) as Promise<ApiResult<any>>),
  remove: (id: number) => api.delete(`/admin/faq/${id}`) as Promise<ApiResult<void>>
}

export const tutorialApi = {
  listCategories: () => api.get('/admin/tutorials/categories') as Promise<ApiResult<any[]>>,
  saveCategory: (data: any) =>
    data.id
      ? (api.put(`/admin/tutorials/categories/${data.id}`, data) as Promise<ApiResult<any>>)
      : (api.post('/admin/tutorials/categories', data) as Promise<ApiResult<any>>),
  removeCategory: (id: number) => api.delete(`/admin/tutorials/categories/${id}`) as Promise<ApiResult<void>>,
  list: (params?: Record<string, unknown>) => api.get('/admin/tutorials', { params }) as Promise<ApiResult<any[]>>,
  save: (data: any) =>
    data.id
      ? (api.put(`/admin/tutorials/${data.id}`, data) as Promise<ApiResult<any>>)
      : (api.post('/admin/tutorials', data) as Promise<ApiResult<any>>),
  remove: (id: number) => api.delete(`/admin/tutorials/${id}`) as Promise<ApiResult<void>>
}

export const appDownloadApi = {
  list: () => api.get('/admin/app-download-qrcodes') as Promise<ApiResult<any[]>>,
  save: (data: any) =>
    data.id
      ? (api.put(`/admin/app-download-qrcodes/${data.id}`, data) as Promise<ApiResult<any>>)
      : (api.post('/admin/app-download-qrcodes', data) as Promise<ApiResult<any>>),
  remove: (id: number) => api.delete(`/admin/app-download-qrcodes/${id}`) as Promise<ApiResult<void>>
}

export const paymentMethodApi = {
  list: () => api.get('/admin/payment-methods') as Promise<ApiResult<any[]>>,
  create: (data: unknown) => api.post('/admin/payment-methods', data) as Promise<ApiResult<any>>,
  update: (id: number, data: unknown) => api.put(`/admin/payment-methods/${id}`, data) as Promise<ApiResult<any>>,
  updateEnabled: (id: number, enabled: boolean) =>
    api.put(`/admin/payment-methods/${id}/enabled`, { enabled }) as Promise<ApiResult<void>>,
  remove: (id: number) => api.delete(`/admin/payment-methods/${id}`) as Promise<ApiResult<void>>
}

export const couponApi = {
  page: (params?: Record<string, unknown>) =>
    api.get('/admin/coupons', { params }) as Promise<ApiResult<PageResult<any>>>,
  save: (data: any) =>
    data.id ? (api.put(`/admin/coupons/${data.id}`, data) as Promise<ApiResult<any>>) : (api.post('/admin/coupons', data) as Promise<ApiResult<any>>),
  disable: (id: number) => api.put(`/admin/coupons/${id}/disable`) as Promise<ApiResult<void>>,
  usages: (params?: Record<string, unknown>) =>
    api.get('/admin/coupons/usages', { params }) as Promise<ApiResult<PageResult<any>>>
}

export const siteApi = {
  get: () => api.get('/admin/site-display') as Promise<ApiResult<any>>,
  save: (data: unknown) => api.put('/admin/site-display', data) as Promise<ApiResult<any>>
}

export const themeApi = {
  get: () => api.get('/admin/system/theme') as Promise<ApiResult<any>>,
  save: (data: unknown) => api.post('/admin/system/theme', data) as Promise<ApiResult<any>>
}
