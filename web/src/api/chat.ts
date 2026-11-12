import api from './index'

export type ChatMessage = {
  id: number
  sessionId: number
  senderType: 'VISITOR' | 'ADMIN' | string
  content: string
  createdAt?: string
}

export type ChatSession = {
  id: number
  sessionNo: string
  accessToken: string
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

export const chatApi = {
  openSession: (data: { visitorId: string; nickname?: string; email?: string; memberId?: number }) =>
    api.post('/chat/sessions', data) as Promise<{ code: number; data: ChatSession; message?: string }>,

  messages: (sessionId: number, token: string) =>
    api.get(`/chat/sessions/${sessionId}/messages`, {
      headers: { 'X-Chat-Token': token }
    }) as Promise<{ code: number; data: ChatMessage[] }>,

  send: (sessionId: number, token: string, content: string) =>
    api.post(
      `/chat/sessions/${sessionId}/messages`,
      { content },
      { headers: { 'X-Chat-Token': token } }
    ) as Promise<{ code: number; data: ChatMessage; message?: string }>,

  markRead: (sessionId: number, token: string) =>
    api.post(`/chat/sessions/${sessionId}/read`, null, {
      headers: { 'X-Chat-Token': token }
    }),

  close: (sessionId: number, token: string) =>
    api.post(`/chat/sessions/${sessionId}/close`, null, {
      headers: { 'X-Chat-Token': token }
    })
}

export function chatWsUrl(token: string): string {
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${proto}//${window.location.host}/api/ws/chat?token=${encodeURIComponent(token)}`
}
