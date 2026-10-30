import api from './index'

export interface PhoneNumber {
  id: number
  name: string
  nameEn?: string
  countryCode: string
  country: string
  countryEn: string
  region: string
  operator: string
  features: string[]
  description: string
  numberType: string
  tips: string[]
  warnings: string[]
  monthlyPrice: number
  maintenancePrice: number
  originalPrice: number
  stock: number
  whatsappTip: string
  status: number
}

export interface Device {
  id: number
  name: string
  nameEn: string
  description: string
  descriptionEn: string
  price: number
  isFree: boolean
  deviceType: string
  shippingFee: number
}

export interface PhoneNumberListResponse {
  numbers: PhoneNumber[]
  total: number
  totalPages: number
  currentPage: number
}

export const phoneNumberApi = {
  getPhoneNumbers: (params?: { 
    region?: string; 
    sortBy?: string; 
    page?: number; 
    size?: number 
  }) => api.get('/phone-numbers', { params }),
  
  getPhoneNumberById: (id: number) => api.get(`/phone-numbers/${id}`),
  
  getRegions: () => api.get('/phone-numbers/regions'),
  
  getDevices: () => api.get('/phone-numbers/devices')
}
