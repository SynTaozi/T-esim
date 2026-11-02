import api, { type ApiResult } from './index'

export const categoryApi = {
  listRoots: () => api.get('/admin/product-categories', { params: { rootsOnly: true } }) as Promise<ApiResult<any[]>>,
  createRoot: (data: unknown) => api.post('/admin/product-categories', data) as Promise<ApiResult<any>>,
  updateRoot: (id: number, data: unknown) => api.put(`/admin/product-categories/${id}`, data) as Promise<ApiResult<any>>,
  removeRoot: (id: number) => api.delete(`/admin/product-categories/${id}`) as Promise<ApiResult<void>>,
  listChildren: (parentId?: number) =>
    api.get('/admin/product-subcategories', { params: parentId ? { categoryId: parentId, parentId } : {} }) as Promise<ApiResult<any[]>>,
  createChild: (data: unknown) => api.post('/admin/product-subcategories', data) as Promise<ApiResult<any>>,
  updateChild: (id: number, data: unknown) => api.put(`/admin/product-subcategories/${id}`, data) as Promise<ApiResult<any>>,
  removeChild: (id: number) => api.delete(`/admin/product-subcategories/${id}`) as Promise<ApiResult<void>>,
  batchSortChildren: (items: { id: number; sortOrder: number }[]) =>
    api.put('/admin/product-subcategories/batch-sort', items) as Promise<ApiResult<void>>
}
