import api, { type ApiResult, type PageResult } from './index'

export const ticketApi = {
  page: (params: Record<string, unknown>) =>
    api.get('/admin/tickets', { params }) as Promise<ApiResult<PageResult<any>>>,
  get: (id: number) => api.get(`/admin/tickets/${id}`) as Promise<ApiResult<any>>,
  reply: (id: number, content: string, imageUrls: string[] = []) =>
    api.post(`/admin/tickets/${id}/reply`, { reply: content, content, imageUrls }) as Promise<ApiResult<any>>,
  close: (id: number) => api.post(`/admin/tickets/${id}/close`) as Promise<ApiResult<any>>
}
