/**
 * 按支付方式 code 返回当前语言文案（不使用后端 DB 里的中文 name）。
 */
export function getPaymentMethodLabel(
  code: string | undefined | null,
  t: (key: string) => string
): string {
  const c = String(code || '').toUpperCase()
  switch (c) {
    case 'WECHAT':
      return t('payment.wechatPay')
    case 'ALIPAY':
    case 'ANTOM_ALIPAY':
      return t('payment.alipay')
    case 'BANK_CARD':
    case 'CARD':
      return t('payment.bankCard')
    case 'USDT':
      return t('payment.usdt')
    case 'BALANCE':
      return t('payment.balancePayment')
    default:
      return code || ''
  }
}

export function getPaymentMethodDesc(
  code: string | undefined | null,
  t: (key: string) => string,
  fallback?: string | null
): string {
  const c = String(code || '').toUpperCase()
  switch (c) {
    case 'USDT':
      return t('payment.trc20Note')
    case 'WECHAT':
      return t('recharge.wechatPaymentDesc')
    case 'ALIPAY':
      return t('recharge.stripeAlipayDesc')
    case 'ANTOM_ALIPAY':
      return t('recharge.antomAlipayDesc')
    case 'BANK_CARD':
    case 'CARD':
      return t('recharge.bankCardDesc')
    case 'BALANCE':
      return t('recharge.balancePaymentDesc')
    default:
      return fallback || ''
  }
}
