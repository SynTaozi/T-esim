type CouponPayload = {
  messageKey?: string | null
  messageParam?: string | null
  message?: string | null
} | null

/** 能识别则返回本地化文案，否则 null（避免把非优惠码错误误译成「优惠码无效」） */
export function resolveCouponTipOrNull(
  t: (key: string, values?: Record<string, unknown>) => string,
  payload?: CouponPayload
): string | null {
  const key = payload?.messageKey
  if (key) {
    return t(`checkout.${key}`, {
      amount: payload?.messageParam ?? '',
      min: payload?.messageParam ?? ''
    })
  }

  const msg = (payload?.message || '').trim()
  if (!msg) return null

  const legacyMap: Array<[RegExp | string, string, ((m: string) => Record<string, string>)?]> = [
    ['请输入优惠码', 'couponEmpty'],
    ['订单金额无效', 'couponInvalidAmount'],
    ['优惠码不存在', 'couponNotFound'],
    ['优惠码已停用', 'couponDisabled'],
    ['优惠码已被使用', 'couponUsed'],
    ['优惠码不可用', 'couponUnavailable'],
    ['优惠码尚未生效', 'couponNotStarted'],
    ['优惠码已过期', 'couponExpired'],
    ['优惠码可用', 'couponAvailable'],
    [/不适用于/, 'couponCategoryMismatch'],
    [/仅适用于指定产品分类/, 'couponCategoryMismatch'],
    [/优惠门槛/, 'couponMinAmount', (m) => {
      const match = m.match(/([\d.]+)\s*$/)
      return { min: match?.[1] || '', amount: match?.[1] || '' }
    }]
  ]

  for (const [pattern, i18nKey, paramsFn] of legacyMap) {
    const hit = typeof pattern === 'string' ? msg.includes(pattern) : pattern.test(msg)
    if (hit) {
      const params = paramsFn ? paramsFn(msg) : {}
      return t(`checkout.${i18nKey}`, params)
    }
  }

  return null
}

/** 优惠码校验结果 → 本地化提示（优先 messageKey） */
export function resolveCouponTip(
  t: (key: string, values?: Record<string, unknown>) => string,
  payload?: CouponPayload
): string {
  return resolveCouponTipOrNull(t, payload) || t('checkout.couponInvalid')
}
