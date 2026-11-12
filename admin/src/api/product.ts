import api, { type ApiResult, type PageResult } from './index'

export const productApi = {
  page: (params: Record<string, unknown>) =>
    api.get('/admin/products', { params }) as Promise<ApiResult<PageResult<any>>>,
  get: (id: number) => api.get(`/admin/products/${id}`) as Promise<ApiResult<any>>,
  create: (data: unknown) => api.post('/admin/products', data) as Promise<ApiResult<any>>,
  update: (id: number, data: unknown) => api.put(`/admin/products/${id}`, data) as Promise<ApiResult<any>>,
  remove: (id: number) => api.delete(`/admin/products/${id}`) as Promise<ApiResult<void>>,
  batchDelete: (ids: number[]) => api.delete('/admin/products/batch', { data: ids }) as Promise<ApiResult<void>>,
  updateStatus: (id: number, status: string) =>
    api.patch(`/admin/products/${id}/status`, null, { params: { status } }) as Promise<ApiResult<void>>,
  batchStatus: (ids: number[], status: string) =>
    api.put('/admin/products/batch-status', { ids, status }) as Promise<ApiResult<void>>,
  batchSubcategory: (ids: number[], categoryId: number) =>
    api.put('/admin/products/batch-subcategory', { ids, categoryId }) as Promise<ApiResult<void>>,
  sortOrder: (items: { id: number; sortOrder: number }[]) =>
    api.put('/admin/products/sort-order', items) as Promise<ApiResult<void>>,
  listSkus: (productId: number) =>
    api.get(`/admin/products/${productId}/pricing-tiers`) as Promise<ApiResult<any[]>>,
  saveSkus: (productId: number, tiers: unknown[]) =>
    api.post(`/admin/products/${productId}/pricing-tiers/batch`, tiers) as Promise<ApiResult<any[]>>
}
