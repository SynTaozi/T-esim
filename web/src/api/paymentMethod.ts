import api from './index'

export const paymentMethodApi = {
  getEnabled: () => api.get('/payment-methods/enabled')
}
