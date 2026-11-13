import api from './index'

export interface ProductPricingTier {
  id: number
  productId?: number
  months: number
  pricePerMonth: number
  totalPrice: number
  salePrice?: number
  physicalSalePrice?: number
  price?: number
  label: string | null
  sortOrder: number
  isActive: number
  isDefault?: boolean
}

export const pricingTierApi = {
  getActiveByProduct: (productId: number) =>
    api.get(`/products/${productId}/pricing-tiers`)
}
