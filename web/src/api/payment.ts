import api from './index'

// USDT 支付订单接口
export interface UsdtPaymentOrder {
  paymentId: string
  orderNo: string
  amount: number
  usdtAmount: number
  usdtAddress: string
  status: string
  expireTime: number
  transactionId?: string
  paidTime?: number
}

// 创建 USDT 支付订单请求
export interface CreateUsdtPaymentRequest {
  orderNo: string
  amount: number
  productName: string
  remark?: string
}

// USDT 支付回调请求
export interface UsdtCallbackRequest {
  paymentId: string
  transactionId: string
}

export const paymentApi = {
  /**
   * 创建 USDT 支付订单
   * @param data 订单信息
   * @returns USDT 支付订单信息
   */
  createUsdtPayment: (data: CreateUsdtPaymentRequest) => api.post('/payment/usdt/create', data),

  /**
   * USDT 支付回调
   * @param data 回调信息
   */
  usdtCallback: (data: UsdtCallbackRequest) => api.post('/payment/usdt/callback', data),

  /**
   * 查询支付状态（通过 paymentId）
   * @param paymentId 支付流水号
   */
  queryPayment: (paymentId: string) => api.get(`/payment/usdt/query/${paymentId}`),

  /**
   * 查询支付状态（通过订单号）
   * @param orderNo 业务订单号
   */
  queryPaymentByOrderNo: (orderNo: string) => api.get(`/payment/usdt/query-by-order/${orderNo}`),

  /**
   * 获取 USDT 收款地址
   */
  getUsdtAddress: () => api.get('/payment/usdt/address'),

  /**
   * 模拟支付成功（测试用）
   * @param paymentId 支付流水号
   */
  simulatePay: (paymentId: string) => api.post('/payment/usdt/simulate-pay', { paymentId }),

  /**
   * 获取待执行的延迟回调任务数量（调试用）
   */
  getPendingTasks: () => api.get('/payment/usdt/debug/pending-tasks'),

  /**
   * 创建 Stripe Checkout Session（微信 / 支付宝 / 银行卡）
   */
  createStripeCheckoutSession: (data: {
    orderNo?: string
    orderId?: string | number
    paymentMethod: 'WECHAT' | 'ALIPAY' | 'BANK_CARD' | string
    email?: string
  }) => api.post('/payment/stripe/checkout-session', data),

  /**
   * 创建 Antom Alipay 收银台（ANTOM_ALIPAY，不替代 ALIPAY）
   */
  createAntomCheckout: (data: {
    orderNo?: string
    orderId?: string | number
    email?: string
  }) => api.post('/payment/antom/checkout', data),

  /**
   * 支付成功页：用 Stripe session_id 主动确认并落库
   */
  confirmStripeSession: (sessionId: string) =>
    api.get('/payment/stripe/confirm', { params: { session_id: sessionId } })
}
