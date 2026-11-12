<template>
  <div class="live-chat-page">
    <PageHeader title="在线咨询" subtitle="实时文字客服 · 离线留言可稍后回复" />
    <div class="chat-workbench page-card">
      <aside class="list">
        <div class="list-head">
          <el-select v-model="status" style="width: 120px" @change="reload">
            <el-option label="进行中" value="OPEN" />
            <el-option label="已关闭" value="CLOSED" />
            <el-option label="全部" value="ALL" />
          </el-select>
          <el-button link type="primary" @click="reload">刷新</el-button>
        </div>
        <div v-loading="loading" class="list-body">
          <button
            v-for="s in rows"
            :key="s.id"
            type="button"
            class="session-item"
            :class="{ on: activeId === s.id }"
            @click="openSession(s)"
          >
            <div class="s-top">
              <strong>{{ displayName(s) }}</strong>
              <el-badge v-if="s.unreadAdmin" :value="s.unreadAdmin" />
            </div>
            <div class="s-preview">{{ s.lastPreview || '（暂无消息）' }}</div>
            <div class="s-meta">{{ s.lastMessageAt || s.createdAt }} · {{ s.status }}</div>
          </button>
          <el-empty v-if="!loading && !rows.length" description="暂无会话" :image-size="64" />
        </div>
      </aside>

      <section class="room">
        <template v-if="active">
          <header class="room-head">
            <div>
              <h3>{{ displayName(active) }}</h3>
              <p>
                {{ active.sessionNo }}
                <span v-if="active.email"> · {{ active.email }}</span>
                <span v-if="active.memberId"> · 会员 #{{ active.memberId }}</span>
              </p>
            </div>
            <div class="room-acts">
              <el-tag size="small" :type="active.status === 'OPEN' ? 'success' : 'info'">{{ active.status }}</el-tag>
              <el-button
                v-if="active.status === 'OPEN'"
                size="small"
                @click="closeSession"
              >关闭会话</el-button>
            </div>
          </header>

          <div ref="bodyRef" class="room-body">
            <div
              v-for="m in messages"
              :key="m.id"
              class="row"
              :class="{ me: m.senderType === 'ADMIN' }"
            >
              <div class="bubble">{{ m.content }}</div>
              <div class="time">
                {{ m.senderType === 'ADMIN' ? '客服' : '访客' }}
                <span v-if="m.createdAt"> · {{ m.createdAt }}</span>
              </div>
            </div>
          </div>

          <footer v-if="active.status === 'OPEN'" class="room-foot">
            <el-input
              v-model="draft"
              type="textarea"
              :rows="3"
              maxlength="1000"
              resize="none"
              placeholder="输入回复（仅文字）"
              @keydown.enter.exact.prevent="send"
            />
            <el-button type="primary" :loading="sending" :disabled="!draft.trim()" @click="send">
              发送
            </el-button>
          </footer>
          <el-alert
            v-else
            type="info"
            :closable="false"
            title="会话已关闭"
            class="closed"
          />
        </template>
        <div v-else class="empty-room">
          选择左侧会话开始回复
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { adminChatApi, adminChatWsUrl, type ChatMessage, type ChatSession } from '@/api/chat'

const loading = ref(false)
const sending = ref(false)
const status = ref('OPEN')
const rows = ref<ChatSession[]>([])
const activeId = ref<number | null>(null)
const active = ref<ChatSession | null>(null)
const messages = ref<ChatMessage[]>([])
const draft = ref('')
const bodyRef = ref<HTMLElement | null>(null)

let ws: WebSocket | null = null
let pingTimer: ReturnType<typeof setInterval> | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let reconnectAttempt = 0

function displayName(s: ChatSession) {
  return s.nickname || s.email || `访客 ${String(s.visitorId || '').slice(0, 6)}`
}

function scrollBottom() {
  nextTick(() => {
    const el = bodyRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

async function reload() {
  loading.value = true
  try {
    const res = await adminChatApi.page({ status: status.value, page: 0, size: 50 })
    rows.value = res.data?.content || []
    if (activeId.value) {
      const still = rows.value.find((r) => r.id === activeId.value)
      if (still) active.value = still
    }
  } finally {
    loading.value = false
  }
}

async function openSession(s: ChatSession) {
  activeId.value = s.id
  active.value = s
  draft.value = ''
  const res = await adminChatApi.messages(s.id)
  messages.value = res.data || []
  s.unreadAdmin = 0
  scrollBottom()
}

function upsertMessage(msg: ChatMessage) {
  if (!msg?.id) return
  if (activeId.value === msg.sessionId) {
    if (!messages.value.some((m) => m.id === msg.id)) {
      messages.value.push(msg)
      messages.value.sort((a, b) => a.id - b.id)
      scrollBottom()
    }
  }
  const row = rows.value.find((r) => r.id === msg.sessionId)
  if (row) {
    row.lastPreview = msg.content
    row.lastMessageAt = msg.createdAt
    if (msg.senderType === 'VISITOR' && activeId.value !== msg.sessionId) {
      row.unreadAdmin = (row.unreadAdmin || 0) + 1
    }
  } else if (msg.senderType === 'VISITOR') {
    reload()
  }
}

function applySessionUpdate(s: ChatSession) {
  const idx = rows.value.findIndex((r) => r.id === s.id)
  if (idx >= 0) {
    rows.value[idx] = { ...rows.value[idx], ...s }
    if (status.value === 'OPEN' && String(s.status || '').toUpperCase() === 'CLOSED') {
      rows.value.splice(idx, 1)
    }
  } else if (status.value === 'OPEN' || status.value === 'ALL') {
    if (!(status.value === 'OPEN' && String(s.status || '').toUpperCase() === 'CLOSED')) {
      rows.value.unshift(s)
    }
  }
  if (activeId.value === s.id) {
    active.value = { ...(active.value || s), ...s }
  }
}

async function send() {
  if (!active.value || !draft.value.trim() || sending.value) return
  const text = draft.value.trim()
  sending.value = true
  try {
    const res = await adminChatApi.reply(active.value.id, text)
    if (res.data) upsertMessage(res.data)
    draft.value = ''
  } catch (e: any) {
    ElMessage.error(e?.message || '发送失败')
  } finally {
    sending.value = false
  }
}

async function closeSession() {
  if (!active.value) return
  await ElMessageBox.confirm('确认关闭该咨询会话？', '关闭会话', { type: 'warning' })
  await adminChatApi.close(active.value.id)
  ElMessage.success('已关闭')
  await reload()
  if (activeId.value) {
    const s = rows.value.find((r) => r.id === activeId.value)
    if (s) await openSession(s)
    else {
      active.value = { ...active.value, status: 'CLOSED' }
    }
  }
}

function connectWs() {
  const token = localStorage.getItem('admin_token')
  if (!token) return
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
      const data = JSON.parse(ev.data)
      if (data.type === 'MESSAGE' && data.message) {
        upsertMessage(data.message)
        if (data.session) {
          applySessionUpdate(data.session)
        }
      } else if (data.type === 'SESSION' && data.session) {
        applySessionUpdate(data.session)
      }
    } catch {
      // ignore
    }
  }
  ws.onclose = () => {
    cleanupWs()
    const delay = Math.min(10000, 1000 * Math.pow(2, reconnectAttempt++))
    reconnectTimer = setTimeout(connectWs, delay)
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
    try { ws.close() } catch { /* ignore */ }
    ws = null
  }
}

onMounted(async () => {
  await reload()
  connectWs()
})

onBeforeUnmount(cleanupWs)
</script>

<style scoped>
.live-chat-page {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.live-chat-page :deep(.page-header) {
  flex: 0 0 auto;
  margin-bottom: 12px;
}
.chat-workbench {
  flex: 1 1 auto;
  min-height: 0;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  grid-template-rows: minmax(0, 1fr);
  padding: 0 !important;
  overflow: hidden;
}
.list {
  border-right: 1px solid var(--admin-border, #e5e7eb);
  display: flex;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
  height: 100%;
  overflow: hidden;
}
.list-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid var(--admin-border, #e5e7eb);
  flex: 0 0 auto;
}
.list-body {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
}
.session-item {
  width: 100%;
  text-align: left;
  border: 0;
  border-bottom: 1px solid #f1f5f9;
  background: #fff;
  padding: 12px;
  cursor: pointer;
}
.session-item:hover,
.session-item.on {
  background: #f8fafc;
}
.session-item.on {
  box-shadow: inset 3px 0 0 #003ec7;
}
.s-top {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
}
.s-preview {
  margin-top: 4px;
  font-size: 13px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.s-meta {
  margin-top: 4px;
  font-size: 11px;
  color: #94a3b8;
}
.room {
  display: flex;
  flex-direction: column;
  min-height: 0;
  min-width: 0;
  height: 100%;
  overflow: hidden;
}
.room-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--admin-border, #e5e7eb);
  flex: 0 0 auto;
}
.room-head h3 {
  margin: 0 0 4px;
  font-size: 16px;
}
.room-head p {
  margin: 0;
  color: #64748b;
  font-size: 12px;
}
.room-acts {
  display: flex;
  gap: 8px;
  align-items: center;
}
.room-body {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 75%;
}
.row.me {
  align-self: flex-end;
  align-items: flex-end;
}
.bubble {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px 14px 14px 4px;
  padding: 10px 12px;
  font-size: 14px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}
.row.me .bubble {
  background: #003ec7;
  color: #fff;
  border-color: transparent;
  border-radius: 14px 14px 4px 14px;
}
.time {
  margin-top: 4px;
  font-size: 11px;
  color: #94a3b8;
}
.room-foot {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: end;
  padding: 12px 16px;
  border-top: 1px solid var(--admin-border, #e5e7eb);
  flex: 0 0 auto;
}
.closed {
  margin: 12px 16px;
}
.empty-room {
  flex: 1;
  display: grid;
  place-items: center;
  color: #94a3b8;
}
@media (max-width: 900px) {
  .chat-workbench {
    grid-template-columns: 1fr;
  }
}
</style>
