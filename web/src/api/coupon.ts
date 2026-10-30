import api from './index'

export function validateCoupon(code: string, orderAmount: number, productId?: number) {
  return api.post('/coupons/validate', {
    code,
    orderAmount,
    productId: productId || undefined
  })
}
