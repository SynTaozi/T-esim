import api, { type ApiResult, type PageResult } from './index'

export const emailAccountApi = {
  list: () => api.get('/admin/email-accounts') as Promise<ApiResult<any[]>>,
  save: (data: any) =>
    data.id
      ? (api.put(`/admin/email-accounts/${data.id}`, data) as Promise<ApiResult<any>>)
      : (api.post('/admin/email-accounts', data) as Promise<ApiResult<any>>),
  remove: (id: number) => api.delete(`/admin/email-accounts/${id}`) as Promise<ApiResult<void>>,
  test: (id: number) => api.post(`/admin/email-accounts/${id}/test`) as Promise<ApiResult<any>>,
  listEmails: (id: number, params?: Record<string, unknown>) =>
    api.get(`/admin/email-accounts/${id}/emails`, { params }) as Promise<ApiResult<PageResult<any>>>,
  send: (id: number, data: unknown) =>
    api.post(`/admin/email-accounts/${id}/emails/send`, data) as Promise<ApiResult<any>>
}

export const emailTemplateApi = {
  list: () => api.get('/admin/email-templates') as Promise<ApiResult<any[]>>,
  get: (id: number) => api.get(`/admin/email-templates/${id}`) as Promise<ApiResult<any>>,
  update: (id: number, data: unknown) => api.put(`/admin/email-templates/${id}`, data) as Promise<ApiResult<any>>,
  setEnabled: (id: number, enabled: boolean) =>
    api.put(`/admin/email-templates/${id}/enabled`, { enabled }) as Promise<ApiResult<any>>,
  preview: (id: number) => api.get(`/admin/email-templates/${id}/preview`) as Promise<ApiResult<any>>
}

export const emailSendLogApi = {
  page: (params?: { status?: string; bizType?: string; toEmail?: string; page?: number; size?: number }) =>
    api.get('/admin/email-send-logs', { params }) as Promise<ApiResult<PageResult<any>>>
}
