/** 待支付订单是否已超时（与 USDT 支付页 / 订单保留时长一致） */
export function isOrderPaymentExpired(order: {
  status?: number
  paymentExpireTime?: number | string | null
  createdAt?: string | number | Date | null
} | null | undefined): boolean {
  if (!order || order.status == null || Number(order.status) !== 0) {
    return false
  }

  const expireMs = Number(order.paymentExpireTime)
  if (Number.isFinite(expireMs) && expireMs > 0) {
    return Date.now() > expireMs
  }

  if (order.createdAt == null || order.createdAt === '') {
    return false
  }
  const createdMs = new Date(order.createdAt).getTime()
  if (!Number.isFinite(createdMs)) {
    return false
  }
  // 与文案「订单将保留24小时」一致；无 paymentExpireTime 时的兜底
  return Date.now() > createdMs + 24 * 60 * 60 * 1000
}
