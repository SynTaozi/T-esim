/**
 * 轻量 SEO：更新 document title / meta / Open Graph / canonical。
 * 不引入额外依赖，适合当前 Vite SPA。
 */

const SITE_NAME = 'Tesim'

export interface PageSeoInput {
  title?: string
  description?: string
  path?: string
  image?: string
  type?: 'website' | 'product' | 'article'
  noindex?: boolean
}

function upsertMeta(attr: 'name' | 'property', key: string, content: string) {
  if (!content) return
  const selector = `meta[${attr}="${key}"]`
  let el = document.head.querySelector(selector) as HTMLMetaElement | null
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute(attr, key)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

function upsertLink(rel: string, href: string) {
  if (!href) return
  let el = document.head.querySelector(`link[rel="${rel}"]`) as HTMLLinkElement | null
  if (!el) {
    el = document.createElement('link')
    el.setAttribute('rel', rel)
    document.head.appendChild(el)
  }
  el.setAttribute('href', href)
}

export function absoluteUrl(path = '/'): string {
  const origin = typeof window !== 'undefined' ? window.location.origin : 'https://www.tesim.com'
  if (!path || path === '/') return `${origin}/`
  return path.startsWith('http') ? path : `${origin}${path.startsWith('/') ? '' : '/'}${path}`
}

export function applyPageSeo(input: PageSeoInput): void {
  const title = input.title?.trim() || `${SITE_NAME} - 全球 eSIM 与实体卡`
  const description =
    input.description?.trim() ||
    'Tesim 提供全球 eSIM 与实体 SIM 卡，覆盖多国家地区，即买即用；支持推广分成计划。'
  const url = absoluteUrl(input.path || (typeof window !== 'undefined' ? window.location.pathname : '/'))
  const ogImage = input.image?.startsWith('http')
    ? input.image!
    : absoluteUrl(input.image || '/logo.png')

  document.title = title

  upsertMeta('name', 'description', description)
  upsertMeta('name', 'robots', input.noindex ? 'noindex, nofollow' : 'index, follow')

  upsertMeta('property', 'og:site_name', SITE_NAME)
  upsertMeta('property', 'og:type', input.type || 'website')
  upsertMeta('property', 'og:title', title)
  upsertMeta('property', 'og:description', description)
  upsertMeta('property', 'og:url', url)
  upsertMeta('property', 'og:image', ogImage)

  upsertMeta('name', 'twitter:card', 'summary_large_image')
  upsertMeta('name', 'twitter:title', title)
  upsertMeta('name', 'twitter:description', description)
  upsertMeta('name', 'twitter:image', ogImage)

  upsertLink('canonical', url)
}

/** 路由 meta.seo 约定 */
export type RouteSeoMeta = {
  title?: string
  titleEn?: string
  description?: string
  descriptionEn?: string
  noindex?: boolean
}

export function resolveRouteSeo(
  seo: RouteSeoMeta | undefined,
  locale: string
): PageSeoInput {
  if (!seo) {
    return {}
  }
  const isEn = locale === 'en'
  return {
    title: isEn ? seo.titleEn || seo.title : seo.title || seo.titleEn,
    description: isEn ? seo.descriptionEn || seo.description : seo.description || seo.descriptionEn,
    noindex: seo.noindex
  }
}
