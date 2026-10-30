import type { Router, RouteLocationNormalizedLoaded } from 'vue-router'

/** 登录成功后允许跳转的内部路径（防开放重定向） */
export function resolvePostLoginRedirect(raw: unknown, fallback = '/'): string {
  if (typeof raw !== 'string' || !raw.startsWith('/') || raw.startsWith('//')) {
    return fallback
  }
  if (raw === '/login' || raw === '/register') {
    return fallback
  }
  return raw
}

export function getLoginRedirectTarget(route?: RouteLocationNormalizedLoaded): string | undefined {
  const path = route?.fullPath
  if (!path || path === '/login' || path === '/register') {
    return undefined
  }
  return path
}

/** 跳转登录页，并携带登录成功后返回地址 */
export function navigateToLogin(router: Router, route?: RouteLocationNormalizedLoaded) {
  const redirect = getLoginRedirectTarget(route)
  router.push({
    path: '/login',
    query: redirect ? { redirect } : undefined
  })
}
