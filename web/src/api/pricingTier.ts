import api from './index'

export interface ProductPricingTier {
  id: number
  productId: number
  months: number
  pricePerMonth: number
  totalPrice: number
  label: string | null
  sortOrder: number
  isActive: number
}

export const pricingTierApi = {
  getActiveByProduct: (productId: number) =>
    api.get(`/products/${productId}/pricing-tiers`)
}
