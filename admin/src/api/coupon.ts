import api from './index'

export interface Coupon {
  id: number
  code: string
  discountAmount: number
  useType?: 'ONCE' | 'UNLIMITED'
  categoryId?: number | null
  status?: 'ACTIVE' | 'EXHAUSTED' | 'DISABLED'
  minOrderAmount?: number
  startsAt?: string | null
  expiresAt?: string | null
  remark?: string
  usedCount?: number
}

export const couponApi = {
  list: () => api.get('/admin/coupons'),
  create: (data: Partial<Coupon>) => api.post('/admin/coupons', data),
  update: (id: number, data: Partial<Coupon>) => api.put(`/admin/coupons/${id}`, data),
  disable: (id: number) => api.put(`/admin/coupons/${id}/disable`),
  usages: (params?: { couponId?: number; page?: number; size?: number }) =>
    api.get('/admin/coupons/usages', { params })
}
