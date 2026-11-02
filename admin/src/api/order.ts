import api, { type ApiResult, type PageResult } from './index'

export const orderApi = {
  page: (params: Record<string, unknown>) =>
    api.get('/admin/orders', { params }) as Promise<ApiResult<PageResult<any>>>,
  get: (id: number) => api.get(`/admin/orders/${id}`) as Promise<ApiResult<any>>,
  updateStatus: (id: number, status: string) =>
    api.put(`/admin/orders/${id}`, null, { params: { status } }) as Promise<ApiResult<any>>,
  updateShipping: (id: number, data: unknown) =>
    api.put(`/admin/orders/${id}/shipping`, data) as Promise<ApiResult<any>>,
  resendNotification: (id: number) =>
    api.post(`/admin/orders/${id}/resend-notification`) as Promise<ApiResult<any>>,
  customerNotice: (id: number, data: unknown) =>
    api.post(`/admin/orders/${id}/customer-notice`, data) as Promise<ApiResult<any>>,
  /** 手动履约发货 */
  fulfill: (id: number, data: {
    shippingCarrier?: string
    trackingNumber?: string
    trackingQrCode?: string
    shippingPhone?: string
    shippingRemark?: string
  }) => api.post(`/admin/orders/${id}/fulfill`, data) as Promise<ApiResult<any>>
}
