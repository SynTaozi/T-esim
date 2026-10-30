import api from './index'

export interface OrderItem {
  id: number
  productId: number
  productName: string
  nameEn?: string  // 英文名称
  nameZh?: string  // 当前产品中文名（兼容历史英文快照）
  price: number
  quantity: number
  subtotal: number
  itemType?: string
  dataLimit?: string
  validityDays?: number
  applicableRegions?: string
  countries?: string
  productType?: number  // 0-用完限速, 1-周期高速
}

export interface Order {
  id: number
  orderNo: string
  totalAmount: number
  finalAmount?: number
  status: number
  statusText: string
  email: string
  shippingAddress: string
  remark: string
  paymentMethod: string
  trackingNumber: string
  shippingCarrier: string
  trackingQrCode?: string  // 物流二维码URL
  /** 客户通知（缺货等） */
  customerNoticeSentAt?: string
  customerNoticeSubject?: string
  customerNoticeContent?: string
  orderType: string  // PURCHASE-购买, RENEWAL-续费
  renewalOrderId: number  // 续费关联的原订单ID
  items: OrderItem[]
  createdAt: string
  updatedAt: string
  // USDT 支付字段（下单时同步创建并持久化）
  paymentId?: string
  thirdPartyOrderNo?: string
  usdtAmount?: number
  usdtAddress?: string
  paymentExpireTime?: number
  paymentStatus?: string
  transactionId?: string
}

export interface CreateOrderData {
  email: string
  shippingAddress?: string
  remark?: string
  /** 用户下单备注 */
  customerRemark?: string
  paymentMethod: string
  cartItemIds?: number[]
  productId?: number
  orderType?: string  // PURCHASE-购买, RENEWAL-续费
  renewalOrderId?: number
  phoneNumberId?: number
  periodValue?: string
  periodUnit?: string
  productName?: string
  totalAmount?: number
  language?: string  // 用户语言设置，用于保存多语言产品名称
}

export interface GetMyOrdersParams {
  page?: number
  pageSize?: number
  status?: number
}

export const orderApi = {
  createOrder: (data: CreateOrderData) => api.post('/orders', data),
  getOrders: () => api.get('/orders'),
  getOrdersPaginated: (page: number = 0, size: number = 5) =>
    api.get('/orders/paginated', { params: { page, size } }),
  getMyOrders: (params?: GetMyOrdersParams) =>
    api.get('/orders/my-orders', { params }),
  getOrderById: (id: number) => api.get(`/orders/${id}`),
  getOrderByNo: (orderNo: string) => api.get(`/orders/no/${orderNo}`),
  cancelOrder: (id: number) => api.put(`/orders/${id}/cancel`),
  payOrder: (id: number) => api.put(`/orders/${id}/pay`)
}
