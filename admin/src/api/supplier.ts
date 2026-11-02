import api, { type ApiResult } from './index'

export const supplierApi = {
  listConfigs: () => api.get('/admin/talk2all/configs') as Promise<ApiResult<any[]>>,
  saveConfig: (data: any) =>
    data.id
      ? (api.put(`/admin/talk2all/configs/${data.id}`, data) as Promise<ApiResult<any>>)
      : (api.post('/admin/talk2all/configs', data) as Promise<ApiResult<any>>),
  removeConfig: (id: number) => api.delete(`/admin/talk2all/configs/${id}`) as Promise<ApiResult<void>>,
  test: (id: number) => api.post(`/admin/talk2all/configs/${id}/test`) as Promise<ApiResult<any>>,
  switchEnv: (id: number, environment: string) =>
    api.put(`/admin/talk2all/configs/${id}/environment`, { environment }) as Promise<ApiResult<any>>,
  listMappings: (params?: Record<string, unknown>) =>
    api.get('/admin/talk2all/mappings', { params }) as Promise<ApiResult<any[]>>,
  saveMapping: (data: any) =>
    data.id
      ? (api.put(`/admin/talk2all/mappings/${data.id}`, data) as Promise<ApiResult<any>>)
      : (api.post('/admin/talk2all/mappings', data) as Promise<ApiResult<any>>),
  removeMapping: (id: number) => api.delete(`/admin/talk2all/mappings/${id}`) as Promise<ApiResult<void>>,
  batchRemoveMappings: (ids: number[]) =>
    api.delete('/admin/talk2all/mappings/batch', { data: ids }) as Promise<ApiResult<void>>,
  syncPreview: (configId: number) =>
    api.get(`/admin/talk2all/configs/${configId}/terminal-plans/sync-preview`) as Promise<ApiResult<any>>,
  syncApply: (data: unknown) => api.post('/admin/talk2all/terminal-plans/sync-apply', data) as Promise<ApiResult<any>>,
  bulkPreview: (configId: number) =>
    api.get(`/admin/talk2all/configs/${configId}/mappings/bulk-preview`) as Promise<ApiResult<any>>,
  bulkApply: (data: unknown) => api.post('/admin/talk2all/mappings/bulk-apply', data) as Promise<ApiResult<any>>
}
