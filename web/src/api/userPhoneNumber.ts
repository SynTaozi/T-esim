import request from './index'

export interface UserPhoneNumber {
  id?: number
  phoneNumber: string
  countryCode: string
  country: string
  carrier?: string
  numberType?: string
  features?: string
  purchasePrice: number
  renewalPrice: number
  expiryDate?: string
  validityDays?: number
  status: string
  notes?: string
  createdAt?: string
  updatedAt?: string
}

export interface PhoneNumberStats {
  totalCount: number
  activeCount: number
  expiredCount: number
  totalSpent: number
  expiringCount: number
  expiringNumbers: UserPhoneNumber[]
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const userPhoneNumberApi = {
  // 获取用户电话套餐列表
  getList(params?: { page?: number; size?: number }) {
    return request.get<any>('/user/phone-numbers', { params })
  },

  // 获取用户电话套餐统计
  getStats() {
    return request.get<any>('/user/phone-numbers/stats')
  },

  // 获取电话套餐详情
  getById(id: number) {
    return request.get<any>(`/user/phone-numbers/${id}`)
  },

  // 续费电话套餐
  renew(id: number, amount?: number) {
    return request.post<any>(`/user/phone-numbers/${id}/renew`, null, {
      params: amount !== undefined ? { amount } : {}
    })
  },

  // 取消电话套餐
  cancel(id: number) {
    return request.post<any>(`/user/phone-numbers/${id}/cancel`)
  }
}
