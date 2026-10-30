import type { Router } from 'vue-router'
import { paymentApi } from '@/api/payment'

const STRIPE_METHODS = new Set(['WECHAT', 'ALIPAY', 'BANK_CARD'])

export function isStripePaymentMethod(method: string | undefined | null): boolean {
  if (!method) return false
  return STRIPE_METHODS.has(method.toUpperCase())
}

export function isAntomAlipayMethod(method: string | undefined | null): boolean {
  if (!method) return false
  return method.toUpperCase() === 'ANTOM_ALIPAY'
}

/**
 * 创建 Antom Alipay 收银台并同页跳转（与 Stripe ALIPAY 独立）。
 */
export async function redirectToAntomCheckout(options: {
  orderRef: string | number
  email?: string
}): Promise<void> {
  const res: any = await paymentApi.createAntomCheckout({
    orderNo: String(options.orderRef),
    email: options.email
  })
  const url = res?.url || res?.checkoutUrl || res?.data?.url || res?.data?.checkoutUrl
  if (url && String(url).startsWith('http')) {
    window.location.href = String(url)
    return
  }
  throw new Error(res?.message || '创建 Antom 支付失败')
}

function methodPath(method: string): string {
  switch (method.toUpperCase()) {
    case 'WECHAT':
      return 'wechat'
    case 'ALIPAY':
      return 'alipay'
    case 'BANK_CARD':
      return 'bankcard'
    default:
      return 'bankcard'
  }
}

/**
 * 创建 Stripe 收银台并同页跳转（微信经 mocdocs，支付宝/银行卡本站）；失败时落到中转页。
 */
export async function redirectToStripeCheckout(options: {
  orderRef: string | number
  paymentMethod: string
  email?: string
  checkoutUrl?: string | null
  router?: Router
}): Promise<void> {
  const method = options.paymentMethod.toUpperCase()
  if (!STRIPE_METHODS.has(method)) {
    throw new Error(`不支持的 Stripe 支付方式: ${method}`)
  }

  if (options.checkoutUrl && String(options.checkoutUrl).startsWith('http')) {
    window.location.href = String(options.checkoutUrl)
    return
  }

  try {
    const res: any = await paymentApi.createStripeCheckoutSession({
      orderNo: String(options.orderRef),
      paymentMethod: method,
      email: options.email
    })
    const url = res?.url || res?.checkoutUrl || res?.data?.url || res?.data?.checkoutUrl
    if (url && String(url).startsWith('http')) {
      window.location.href = String(url)
      return
    }
    throw new Error(res?.message || '创建支付会话失败')
  } catch (e) {
    // 失败时进入中转页统一提示；微信不把后端/网络技术错误抛给调用方
    if (options.router) {
      await options.router.push({
        path: `/checkout/${methodPath(method)}/${options.orderRef}`
      })
      return
    }
    if (method === 'WECHAT') {
      throw new Error('LAUNCH_PAY_FAILED')
    }
    throw e
  }
}

/**
 * 按支付方式同页跳转：USDT / 余额走本地页；微信/支付宝/银行卡走 Stripe 收银台。
 */
export async function navigateByPaymentMethod(options: {
  paymentMethod: string
  orderId: string | number
  orderNo?: string
  email?: string
  checkoutUrl?: string | null
  router: Router
}): Promise<void> {
  const method = String(options.paymentMethod || '').toUpperCase()
  if (method === 'USDT') {
    await options.router.push(`/pay/${options.orderId}`)
    return
  }
  if (method === 'BALANCE') {
    await options.router.push(`/checkout/balance/${options.orderId}`)
    return
  }
  if (isAntomAlipayMethod(method)) {
    await redirectToAntomCheckout({
      orderRef: options.orderNo || options.orderId,
      email: options.email
    })
    return
  }
  if (isStripePaymentMethod(method)) {
    await redirectToStripeCheckout({
      orderRef: options.orderNo || options.orderId,
      paymentMethod: method,
      email: options.email,
      checkoutUrl: options.checkoutUrl,
      router: options.router
    })
    return
  }
  await options.router.push(`/pay/${options.orderId}`)
}
