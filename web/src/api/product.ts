import api from './index'

export interface Product {
  id: number
  name: string
  nameEn?: string
  description: string
  price: number
  applicableRegions: string  // JSON string: ["Asia","Europe"]
  countries: string         // JSON string: ["日本","韩国"]
  dataLimit: string
  validityDays: number
  stock: number
  imageUrl: string
  status: number
  productType?: number  // 0-用完限速, 1-周期高速
  itemType?: number    // 关联 product_categories.id；0=未分类
  categoryName?: string
  categoryNameEn?: string
  categoryCode?: string
  subCategoryId?: number | null
  subCategoryName?: string
  subCategoryNameEn?: string
  subCategoryCode?: string
  features?: string    // JSON数组: 产品特性（中文）
  featuresEn?: string  // JSON数组: 产品特性（英文）
  plan_type?: number   // 0-固定期限, 1-每日流量, 2-体验包
  sms_support?: number // 0-不支持短信, 1-支持短信
  data_support?: number // 0-不支持流量, 1-支持流量
  voice_support?: number // 0-不支持电话, 1-支持电话
  topup_type?: number  // 0-不可续费, 1-支持续费, 2-支持续费+每日重置
  planType?: number    // 同 plan_type（后端驼峰字段）
  smsSupport?: number  // 同 sms_support（后端驼峰字段）
  dataSupport?: number // 同 data_support
  voiceSupport?: number // 同 voice_support
  topupType?: number   // 同 topup_type（后端驼峰字段）
  importantLabels?: string
  importantLabelsEn?: string
  isPhysicalCard?: number  // 0-eSIM卡, 1-实体卡
  displayImageUrl?: string  // 产品展示大图
  sortOrder?: number  // 自定义排序序号
}

export const productApi = {
  getProducts: (region?: string, page?: number, size?: number, itemType?: number, sortBy?: string, subCategoryId?: number | null) => {
    const params: any = {}
    if (region) params.region = region
    if (page !== undefined) params.page = page
    if (size !== undefined) params.size = size
    if (itemType !== undefined && itemType !== 0) params.itemType = itemType
    if (sortBy && sortBy !== 'default') params.sortBy = sortBy
    if (subCategoryId !== undefined && subCategoryId !== null && subCategoryId > 0) params.subCategoryId = subCategoryId
    return api.get('/products', { params })
  },
  searchProducts: (keyword: string, page?: number, size?: number, itemType?: number, sortBy?: string, subCategoryId?: number | null) => {
    const params: any = { keyword }
    if (page !== undefined) params.page = page
    if (size !== undefined) params.size = size
    if (itemType !== undefined && itemType !== 0) params.itemType = itemType
    if (sortBy && sortBy !== 'default') params.sortBy = sortBy
    if (subCategoryId !== undefined && subCategoryId !== null && subCategoryId > 0) params.subCategoryId = subCategoryId
    return api.get('/products/search', { params })
  },
  getFeaturedProducts: (size?: number) => {
    const params: any = {}
    if (size !== undefined) params.size = size
    return api.get('/products/featured', { params })
  },
  getRegions: () => api.get('/products/regions'),
  getRegionCounts: () => api.get('/products/region-counts'),
  getProductById: (id: number) => api.get(`/products/${id}`)
}
