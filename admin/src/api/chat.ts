import api, { type ApiResult, type PageResult } from './index'

export type ChatMessage = {
  id: number
  sessionId: number
  senderType: string
  content: string
  createdAt?: string
}

export type ChatSession = {
  id: number
  sessionNo: string
  visitorId: string
  memberId?: number
  nickname?: string
  email?: string
  status: string
  lastMessageAt?: string
  lastPreview?: string
  unreadAdmin?: number
  unreadVisitor?: number
  createdAt?: string
}

export const adminChatApi = {
  page: (params: Record<string, unknown>) =>
    api.get('/admin/chat/sessions', { params }) as Promise<ApiResult<PageResult<ChatSession>>>,
  get: (id: number) => api.get(`/admin/chat/sessions/${id}`) as Promise<ApiResult<ChatSession>>,
  messages: (id: number) =>
    api.get(`/admin/chat/sessions/${id}/messages`) as Promise<ApiResult<ChatMessage[]>>,
  reply: (id: number, content: string) =>
    api.post(`/admin/chat/sessions/${id}/messages`, { content }) as Promise<ApiResult<ChatMessage>>,
  close: (id: number) => api.post(`/admin/chat/sessions/${id}/close`) as Promise<ApiResult<ChatSession>>,
  read: (id: number) => api.post(`/admin/chat/sessions/${id}/read`) as Promise<ApiResult<void>>
}

export function adminChatWsUrl(token: string): string {
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${proto}//${window.location.host}/api/ws/admin/chat?token=${encodeURIComponent(token)}`
}
