/** 待支付订单保留时长（与后端 OrderPaymentExpireService.TTL / payment.expire_at 一致） */
export const ORDER_PAYMENT_EXPIRE_MS = 30 * 60 * 1000

/** 待支付订单是否已超时（与支付页倒计时 / 订单保留时长一致） */
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
  // 无 paymentExpireTime 时按创建时间 + 30 分钟兜底
  return Date.now() > createdMs + ORDER_PAYMENT_EXPIRE_MS
}
