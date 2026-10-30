import api from './index'

export interface ProductCategory {
  id: number
  name: string
  nameEn?: string
  code?: string
  sortOrder?: number
  status?: number
  showOnFront?: boolean
}

function unwrap<T>(res: any): T {
  if (res && typeof res === 'object' && 'data' in res && (res.code === 0 || res.code === 200)) {
    return res.data as T
  }
  return res as T
}

export const productCategoryApi = {
  listForStorefront: async () => unwrap<ProductCategory[]>(await api.get('/product-categories'))
}
