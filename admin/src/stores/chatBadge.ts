import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { adminChatApi, adminChatWsUrl, type ChatSession } from '@/api/chat'

export const useChatBadgeStore = defineStore('chatBadge', () => {
  const unreadTotal = ref(0)
  const hasUnread = computed(() => unreadTotal.value > 0)

  let ws: WebSocket | null = null
  let pingTimer: ReturnType<typeof setInterval> | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let reconnectAttempt = 0
  let started = false
  let intentionalClose = false

  function applyFromSessions(rows: ChatSession[]) {
    unreadTotal.value = rows.reduce((sum, s) => sum + (s.unreadAdmin || 0), 0)
  }

  async function refresh() {
    try {
      const res = await adminChatApi.page({ status: 'OPEN', page: 0, size: 100 })
      applyFromSessions(res.data?.content || [])
    } catch {
      // ignore transient errors; keep last known badge
    }
  }

  function onPayload(data: any) {
    if (data?.type === 'MESSAGE' && data.message) {
      if (String(data.message.senderType || '').toUpperCase() === 'VISITOR') {
        const n = data.session?.unreadAdmin
        if (typeof n === 'number') {
          // 当前页可能已读清零；以服务端会话字段为准，必要时整体刷新
          if (n > 0) {
            unreadTotal.value = Math.max(unreadTotal.value, 1)
            // 精确总数用列表汇总
            refresh()
          } else {
            refresh()
          }
        } else {
          unreadTotal.value = Math.max(unreadTotal.value, 1)
          refresh()
        }
      } else if (data.session) {
        refresh()
      }
    } else if (data?.type === 'SESSION' && data.session) {
      refresh()
    }
  }

  function cleanupWs() {
    if (pingTimer) {
      clearInterval(pingTimer)
      pingTimer = null
    }
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (ws) {
      try {
        ws.close()
      } catch {
        // ignore
      }
      ws = null
    }
  }

  function connectWs() {
    const token = localStorage.getItem('admin_token')
    if (!token) return
    intentionalClose = false
    cleanupWs()
    ws = new WebSocket(adminChatWsUrl(token))
    ws.onopen = () => {
      reconnectAttempt = 0
      pingTimer = setInterval(() => {
        if (ws && ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify({ type: 'PING' }))
        }
      }, 25000)
    }
    ws.onmessage = (ev) => {
      try {
        onPayload(JSON.parse(ev.data))
      } catch {
        // ignore
      }
    }
    ws.onclose = () => {
      cleanupWs()
      if (!intentionalClose && started) {
        const delay = Math.min(10000, 1000 * Math.pow(2, reconnectAttempt++))
        reconnectTimer = setTimeout(connectWs, delay)
      }
    }
  }

  async function start() {
    if (started) {
      await refresh()
      return
    }
    started = true
    await refresh()
    connectWs()
  }

  function stop() {
    started = false
    intentionalClose = true
    cleanupWs()
    unreadTotal.value = 0
  }

  return {
    unreadTotal,
    hasUnread,
    refresh,
    start,
    stop
  }
})
