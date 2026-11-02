import api from './index'

export const paymentMethodApi = {
  getEnabled: () => api.get('/admin/payment-methods/enabled'),
  getAll: () => api.get('/admin/payment-methods'),
  updateEnabled: (id: number, enabled: boolean) =>
    api.put(`/admin/payment-methods/${id}/enabled`, { enabled }),
  save: (data: Record<string, unknown>) => api.post('/admin/payment-methods', data),
  delete: (id: number) => api.delete(`/admin/payment-methods/${id}`)
}
