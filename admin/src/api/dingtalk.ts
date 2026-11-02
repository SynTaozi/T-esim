import api, { type ApiResult, type PageResult } from './index'

export const dingtalkApi = {
  listConfigs: () => api.get('/admin/dingtalk/configs') as Promise<ApiResult<any[]>>,
  saveConfig: (data: any) =>
    data.id
      ? (api.put(`/admin/dingtalk/configs/${data.id}`, data) as Promise<ApiResult<any>>)
      : (api.post('/admin/dingtalk/configs', data) as Promise<ApiResult<any>>),
  removeConfig: (id: number) => api.delete(`/admin/dingtalk/configs/${id}`) as Promise<ApiResult<void>>,
  test: (id: number) => api.post(`/admin/dingtalk/configs/${id}/test`) as Promise<ApiResult<any>>,
  logs: (params?: Record<string, unknown>) =>
    api.get('/admin/dingtalk/logs', { params }) as Promise<ApiResult<PageResult<any>>>
}
