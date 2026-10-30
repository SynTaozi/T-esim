export type CountryLocale = 'zh' | 'en'

/** ISO 国家代码 → 中文展示名（仅中文 locale 使用） */
const ISO_TO_ZH: Record<string, string> = {
  JP: '日本', KR: '韩国', CN: '中国', HK: '香港', TW: '台湾', MO: '澳门',
  SG: '新加坡', TH: '泰国', MY: '马来西亚', IN: '印度', ID: '印度尼西亚', PH: '菲律宾', VN: '越南',
  GB: '英国', UK: '英国', DE: '德国', FR: '法国', IT: '意大利', ES: '西班牙', NL: '荷兰',
  BE: '比利时', SE: '瑞典', NO: '挪威', FI: '芬兰', PL: '波兰', CZ: '捷克', CH: '瑞士',
  AT: '奥地利', PT: '葡萄牙', IE: '爱尔兰', DK: '丹麦', GR: '希腊',
  US: '美国', CA: '加拿大', BR: '巴西', MX: '墨西哥', AR: '阿根廷', CL: '智利', PE: '秘鲁', CO: '哥伦比亚',
  AU: '澳大利亚', NZ: '新西兰', FJ: '斐济',
  ZA: '南非', EG: '埃及', NG: '尼日利亚', KE: '肯尼亚',
  AE: '阿联酋', SA: '沙特阿拉伯', IL: '以色列', TR: '土耳其', RU: '俄罗斯',
  GLOBAL: '全球'
}

const ZH_TO_ISO: Record<string, string> = Object.fromEntries(
  Object.entries(ISO_TO_ZH).map(([code, zh]) => [zh, code])
)

/** 解析为 ISO 代码（兼容 ISO / 中文 / 英文存库） */
export function resolveCountryCode(codeOrName: string): string | null {
  const trimmed = (codeOrName || '').trim()
  if (!trimmed) return null
  const upper = trimmed.toUpperCase()
  if (ISO_TO_ZH[upper]) return upper
  if (ZH_TO_ISO[trimmed]) return ZH_TO_ISO[trimmed]
  if (upper.length >= 2 && upper.length <= 3 && /^[A-Z]{2,3}$/.test(upper)) return upper
  return null
}

/**
 * 单个国家展示：
 * - 英文：原样显示 ISO 代码（如 HK）
 * - 中文：ISO 转中文（如 香港）
 */
export function formatCountryForDisplay(codeOrName: string, locale: CountryLocale = 'zh'): string {
  const trimmed = (codeOrName || '').trim()
  if (!trimmed) return ''
  const code = resolveCountryCode(trimmed)
  if (locale === 'en') {
    if (code) return code
    const upper = trimmed.toUpperCase()
    if (upper.length >= 2 && upper.length <= 3 && /^[A-Z]{2,3}$/.test(upper)) return upper
    return trimmed
  }
  if (code && ISO_TO_ZH[code]) return ISO_TO_ZH[code]
  return trimmed
}

/** 国家列表 → 指定语言展示（去重、保序） */
export function formatCountriesForDisplay(
  raw: string[] | undefined | null,
  locale: CountryLocale = 'zh'
): string[] {
  const result: string[] = []
  const seen = new Set<string>()
  for (const item of raw || []) {
    const name = formatCountryForDisplay(item, locale)
    if (name && !seen.has(name)) {
      seen.add(name)
      result.push(name)
    }
  }
  return result
}

/** 国家列表 → 逗号分隔展示字符串 */
export function formatCountriesText(raw: string[] | undefined | null, locale: CountryLocale = 'zh'): string {
  return formatCountriesForDisplay(raw, locale).join(', ')
}

const ISO_TO_FLAG: Record<string, string> = {
  US: '🇺🇸', CA: '🇨🇦', MX: '🇲🇽', GB: '🇬🇧', UK: '🇬🇧', DE: '🇩🇪', FR: '🇫🇷',
  IT: '🇮🇹', ES: '🇪🇸', NL: '🇳🇱', CH: '🇨🇭', JP: '🇯🇵', KR: '🇰🇷', SG: '🇸🇬',
  AU: '🇦🇺', NZ: '🇳🇿', CN: '🇨🇳', HK: '🇭🇰', TW: '🇹🇼', TH: '🇹🇭', MY: '🇲🇾',
  ID: '🇮🇩', PH: '🇵🇭', IN: '🇮🇳', BR: '🇧🇷', AR: '🇦🇷', RU: '🇷🇺', AE: '🇦🇪',
  ZA: '🇿🇦', GLOBAL: '🌍'
}

/** 根据 ISO / 中英文名取国旗 emoji */
export function getCountryFlagEmoji(codeOrName: string): string {
  const code = resolveCountryCode(codeOrName)
  if (code && ISO_TO_FLAG[code]) return ISO_TO_FLAG[code]
  return '🌐'
}
