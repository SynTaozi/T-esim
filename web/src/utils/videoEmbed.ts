export type VideoEmbedKind = 'youtube' | 'bilibili' | 'file'

export interface ResolvedVideoEmbed {
  kind: VideoEmbedKind
  /** iframe src for youtube/bilibili; original url for file */
  src: string
}

/**
 * 将媒体 URL 解析为可播放地址：YouTube / B 站用 iframe，其它视为直链文件。
 */
export function resolveVideoEmbed(rawUrl: string): ResolvedVideoEmbed | null {
  const url = (rawUrl || '').trim()
  if (!url) return null

  const youtube = resolveYoutube(url)
  if (youtube) return youtube

  const bilibili = resolveBilibili(url)
  if (bilibili) return bilibili

  // 已知平台但解析失败（如 b23.tv 短链）→ 不走直链，交给页面提示
  if (isKnownPlatform(url)) return null

  return { kind: 'file', src: url }
}

function isKnownPlatform(url: string): boolean {
  try {
    const host = new URL(url).hostname.replace(/^www\./, '').toLowerCase()
    return (
      host === 'youtu.be' ||
      host === 'youtube.com' ||
      host === 'm.youtube.com' ||
      host === 'music.youtube.com' ||
      host === 'bilibili.com' ||
      host === 'm.bilibili.com' ||
      host === 'b23.tv' ||
      host === 'player.bilibili.com'
    )
  } catch {
    return false
  }
}

function resolveYoutube(url: string): ResolvedVideoEmbed | null {
  let id = ''
  let start = 0

  try {
    const u = new URL(url)
    const host = u.hostname.replace(/^www\./, '').toLowerCase()

    if (host === 'youtu.be') {
      id = u.pathname.split('/').filter(Boolean)[0] || ''
    } else if (host === 'youtube.com' || host === 'm.youtube.com' || host === 'music.youtube.com') {
      if (u.pathname.startsWith('/embed/')) {
        id = u.pathname.split('/')[2] || ''
      } else if (u.pathname.startsWith('/shorts/')) {
        id = u.pathname.split('/')[2] || ''
      } else if (u.pathname.startsWith('/live/')) {
        id = u.pathname.split('/')[2] || ''
      } else {
        id = u.searchParams.get('v') || ''
      }
    } else {
      return null
    }

    start = parseYoutubeStart(u.searchParams.get('t') || u.searchParams.get('start') || '')
    // hash like #t=57s
    if (!start && u.hash) {
      const hashT = u.hash.match(/[?&#]t=([^&]+)/)?.[1] || u.hash.replace(/^#t=/, '')
      start = parseYoutubeStart(hashT)
    }
  } catch {
    return null
  }

  if (!id || !/^[\w-]{6,}$/.test(id)) return null

  const params = new URLSearchParams()
  if (start > 0) params.set('start', String(start))
  const qs = params.toString()
  return {
    kind: 'youtube',
    src: `https://www.youtube.com/embed/${id}${qs ? `?${qs}` : ''}`
  }
}

/** 解析 t=57 / t=57s / t=1m30s / t=1h2m3s */
function parseYoutubeStart(raw: string): number {
  if (!raw) return 0
  const t = decodeURIComponent(raw).trim()
  if (/^\d+$/.test(t)) return Number(t)
  const m = t.match(/^(?:(\d+)h)?(?:(\d+)m)?(?:(\d+)s)?$/i)
  if (m) {
    return (Number(m[1] || 0) * 3600) + (Number(m[2] || 0) * 60) + Number(m[3] || 0)
  }
  return 0
}

function resolveBilibili(url: string): ResolvedVideoEmbed | null {
  try {
    const u = new URL(url)
    const host = u.hostname.replace(/^www\./, '').toLowerCase()

    // 已是播放器地址
    if (host === 'player.bilibili.com') {
      return { kind: 'bilibili', src: url.startsWith('http') ? url : `https:${url}` }
    }

    if (host !== 'bilibili.com' && host !== 'm.bilibili.com' && host !== 'b23.tv') {
      return null
    }

    // b23.tv 短链无法在前端展开，提示用户用完整 bilibili.com 链接
    if (host === 'b23.tv') {
      return null
    }

    const path = u.pathname
    const bv = path.match(/\/video\/(BV[\w]+)/i)?.[1]
    const aid = path.match(/\/video\/av(\d+)/i)?.[1]
    const page = Number(u.searchParams.get('p') || '1') || 1

    if (bv) {
      const params = new URLSearchParams({
        bvid: bv,
        page: String(page),
        high_quality: '1',
        danmaku: '0'
      })
      return {
        kind: 'bilibili',
        src: `https://player.bilibili.com/player.html?${params.toString()}`
      }
    }
    if (aid) {
      const params = new URLSearchParams({
        aid,
        page: String(page),
        high_quality: '1',
        danmaku: '0'
      })
      return {
        kind: 'bilibili',
        src: `https://player.bilibili.com/player.html?${params.toString()}`
      }
    }
  } catch {
    return null
  }
  return null
}
