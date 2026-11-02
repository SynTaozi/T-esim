import api, { type ApiResult } from './index'

export const dashboardApi = {
  stats: () => api.get('/admin/dashboard/stats') as Promise<ApiResult<any>>,
  overview: () => api.get('/admin/dashboard/overview') as Promise<ApiResult<any>>
}
