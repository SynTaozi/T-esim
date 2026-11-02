import type { Router } from 'vue-router'
import { paymentApi } from '@/api/payment'

const STRIPE_METHODS = new Set(['WECHAT', 'ALIPAY', 'BANK_CARD'])

export function isStripePaymentMethod(method: string | undefined | null): boolean {
  if (!method) return false
  return STRIPE_METHODS.has(method.toUpperCase())
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
 * 创建 Stripe 收银台并同页跳转；失败时落到中转页。
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
    const url = res?.data?.url || res?.data?.checkoutUrl
      || res?.url || res?.checkoutUrl
    if (url && String(url).startsWith('http')) {
      window.location.href = String(url)
      return
    }
    // stub / relative redirect: fall through to method route
    if (url && options.router) {
      await options.router.push(String(url))
      return
    }
    throw new Error(res?.message || '创建支付会话失败')
  } catch (e) {
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
 * 按支付方式同页跳转：USDT 走本地页；微信/支付宝/银行卡走 Stripe。
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
    const query = options.orderNo ? { orderNo: String(options.orderNo) } : undefined
    await options.router.push({ path: `/pay/${options.orderId}`, query })
    return
  }
  if (method === 'BALANCE') {
    await options.router.push(`/checkout/balance/${options.orderId}`)
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
