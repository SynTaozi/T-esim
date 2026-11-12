/** 与后端 TicketAppServiceImpl.appendImageUrls / 前台约定一致 */
const IMAGE_MARK = '\n\n__IMAGES__\n'

export function splitTicketContent(raw: string | null | undefined): {
  text: string
  images: string[]
} {
  if (!raw) {
    return { text: '', images: [] }
  }
  const idx = raw.indexOf(IMAGE_MARK)
  if (idx < 0) {
    const alt = raw.split('__IMAGES__')
    if (alt.length < 2) {
      return { text: raw, images: [] }
    }
    return {
      text: alt[0].trimEnd(),
      images: alt[1]
        .split(/\r?\n/)
        .map((s) => s.trim())
        .filter((s) => s.startsWith('/'))
    }
  }
  return {
    text: raw.slice(0, idx).trimEnd(),
    images: raw
      .slice(idx + IMAGE_MARK.length)
      .split(/\r?\n/)
      .map((s) => s.trim())
      .filter((s) => s.startsWith('/'))
  }
}
