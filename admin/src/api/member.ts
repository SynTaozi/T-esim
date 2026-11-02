import api, { type ApiResult, type PageResult } from './index'

export const memberApi = {
  page: (params: Record<string, unknown>) =>
    api.get('/admin/users', { params }) as Promise<ApiResult<PageResult<any>>>,
  get: (id: number) => api.get(`/admin/users/${id}`) as Promise<ApiResult<any>>,
  updateStatus: (id: number, status: string) =>
    api.put(`/admin/users/${id}/status`, { status }) as Promise<ApiResult<void>>
}
