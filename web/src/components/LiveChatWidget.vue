<template>
  <div class="live-chat" v-show="!hiddenOnAuth">
    <transition name="chat-pop">
      <div v-if="open" class="panel" role="dialog" aria-label="在线咨询">
        <header class="panel-head">
          <div class="panel-head-main">
            <el-icon class="head-icon"><Headset /></el-icon>
            <div>
              <strong>在线咨询</strong>
              <p>周一至周五 09:00–18:30</p>
            </div>
          </div>
          <button type="button" class="icon-btn" @click="open = false" aria-label="关闭">
            <el-icon><Close /></el-icon>
          </button>
        </header>

        <div v-if="showHoursTip" class="hours-tip">
          <div class="hours-tip-title">客服在线时间说明</div>
          <p>
            人工客服仅在<strong>周一至周五 上午 9:00 – 晚上 18:30</strong>在线。
            非工作时间也可留言，我们会在上班后回复。若长时间未收到回复，请提交工单以便跟进。
          </p>
          <div class="hours-tip-acts">
            <router-link class="tip-link" to="/ticket/new" @click="open = false">提交工单</router-link>
            <button type="button" class="tip-ok" @click="dismissHoursTip">我知道了</button>
          </div>
        </div>

        <div ref="bodyRef" class="panel-body">
          <div v-if="bootError" class="sys">{{ bootError }}</div>
          <div v-else-if="loading" class="sys">连接中…</div>
          <template v-else>
            <div class="sys soft">
              你好，有什么可以帮你的？不在线时也可留言；急事先走工单。
            </div>
            <div
              v-for="m in messages"
              :key="m.id"
              class="row"
              :class="{ me: m.senderType === 'VISITOR' }"
            >
              <div class="bubble">{{ m.content }}</div>
              <div class="time">{{ m.createdAt }}</div>
            </div>
            <div v-if="sessionClosed" class="closed-banner">
              <p>本轮咨询已结束。如需继续，请开始新对话。</p>
              <button type="button" class="new-chat-btn" :disabled="loading" @click="startNewChat">
                {{ loading ? '开启中…' : '开始新对话' }}
              </button>
            </div>
          </template>
        </div>

        <footer v-if="!sessionClosed" class="panel-foot">
          <input
            v-model="draft"
            type="text"
            maxlength="1000"
            placeholder="输入消息…"
            :disabled="sending || !session || loading"
            @keydown.enter.prevent="send"
          />
          <button type="button" class="send" :disabled="sending || !draft.trim()" @click="send">
            发送
          </button>
        </footer>
        <footer v-else class="panel-foot closed-foot">
          <button type="button" class="send full" :disabled="loading" @click="startNewChat">
            {{ loading ? '开启中…' : '开始新对话' }}
          </button>
        </footer>
      </div>
    </transition>

    <button type="button" class="fab" :class="{ open }" @click="toggle" aria-label="打开在线咨询">
      <span v-if="!open && unread > 0" class="badge">{{ unread > 9 ? '9+' : unread }}</span>
      <el-icon :size="26">
        <Close v-if="open" />
        <ChatDotRound v-else />
      </el-icon>
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Close, Headset } from '@element-plus/icons-vue'
import { chatApi, chatWsUrl, type ChatMessage, type ChatSession } from '@/api/chat'
import { useAuthStore } from '@/stores/auth'

const VISITOR_KEY = 'tesim_chat_visitor_id'
const TOKEN_KEY = 'tesim_chat_session_token'
const SESSION_KEY = 'tesim_chat_session_id'
const HOURS_TIP_KEY = 'tesim_chat_hours_tip_seen'

const route = useRoute()
const authStore = useAuthStore()

const open = ref(false)
const loading = ref(false)
const sending = ref(false)
const bootError = ref('')
const draft = ref('')
const session = ref<ChatSession | null>(null)
const messages = ref<ChatMessage[]>([])
const unread = ref(0)
const bodyRef = ref<HTMLElement | null>(null)
const showHoursTip = ref(false)
const sessionClosed = computed(() => String(session.value?.status || '').toUpperCase() === 'CLOSED')

function dismissHoursTip() {
  showHoursTip.value = false
  localStorage.setItem(HOURS_TIP_KEY, '1')
}

function markSessionClosed() {
  if (session.value) {
    session.value = { ...session.value, status: 'CLOSED' }
  }
  scrollBottom()
}

async function startNewChat() {
  cleanupWs(true)
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(SESSION_KEY)
  session.value = null
  messages.value = []
  draft.value = ''
  unread.value = 0
  await bootstrap()
}

let ws: WebSocket | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let pingTimer: ReturnType<typeof setInterval> | null = null
let reconnectAttempt = 0
let intentionalClose = false

const hiddenOnAuth = computed(() => {
  const p = route.path || ''
  return p.startsWith('/login') || p.startsWith('/register') || p.startsWith('/forgot-password')
})

function ensureVisitorId(): string {
  let id = localStorage.getItem(VISITOR_KEY)
  if (!id) {
    id = (crypto.randomUUID?.() || `v_${Date.now()}_${Math.random().toString(16).slice(2)}`).replace(/-/g, '')
    localStorage.setItem(VISITOR_KEY, id)
  }
  return id
}

function scrollBottom() {
  nextTick(() => {
    const el = bodyRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function upsertMessage(msg: ChatMessage) {
  if (!msg?.id) return
  const idx = messages.value.findIndex((m) => m.id === msg.id)
  if (idx >= 0) messages.value[idx] = msg
  else messages.value.push(msg)
  messages.value.sort((a, b) => a.id - b.id)
  if (msg.senderType === 'ADMIN' && !open.value) {
    unread.value += 1
  }
  scrollBottom()
}

async function reloadMessages() {
  if (!session.value) return
  const hist: any = await chatApi.messages(session.value.id, session.value.accessToken)
  messages.value = hist?.data || []
  scrollBottom()
}

async function bootstrap() {
  loading.value = true
  bootError.value = ''
  try {
    const prevId = session.value?.id
    const visitorId = ensureVisitorId()
    // openOrResume：有 OPEN 则复用；若上一通已关闭则自动新建
    const res: any = await chatApi.openSession({
      visitorId,
      memberId: authStore.user?.id
    })
    if (res?.code && res.code !== 200) {
      throw new Error(res.message || '无法开启咨询')
    }
    const createdNew = prevId != null && res.data?.id !== prevId
    session.value = res.data
    localStorage.setItem(TOKEN_KEY, res.data.accessToken)
    localStorage.setItem(SESSION_KEY, String(res.data.id))
    unread.value = res.data.unreadVisitor || 0

    await reloadMessages()
    if (createdNew && !messages.value.length) {
      ElMessage.success('已开启新对话')
    }
    connectWs()
  } catch (e: any) {
    bootError.value = e?.message || '连接失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function connectWs() {
  if (!session.value?.accessToken) return
  intentionalClose = false
  cleanupWs(false)
  ws = new WebSocket(chatWsUrl(session.value.accessToken))
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
        if (data.session?.status) {
          session.value = { ...(session.value || data.session), ...data.session }
        }
        if (open.value && session.value && !sessionClosed.value) {
          chatApi.markRead(session.value.id, session.value.accessToken).catch(() => {})
          unread.value = 0
        }
      } else if (data.type === 'SESSION' && data.session) {
        session.value = { ...(session.value || data.session), ...data.session }
        if (String(data.session.status || '').toUpperCase() === 'CLOSED') {
          markSessionClosed()
        }
      } else if (data.type === 'ERROR' && data.error) {
        const err = String(data.error)
        if (err.includes('已关闭') || err.includes('重新发起')) {
          markSessionClosed()
        }
        ElMessage.warning(err)
      }
    } catch {
      // ignore
    }
  }
  ws.onclose = () => {
    cleanupWs(false)
    if (!intentionalClose && open.value) {
      const delay = Math.min(10000, 1000 * Math.pow(2, reconnectAttempt++))
      reconnectTimer = setTimeout(connectWs, delay)
    }
  }
  ws.onerror = () => {
    // onclose will handle reconnect
  }
}

function cleanupWs(closeIntent: boolean) {
  if (closeIntent) intentionalClose = true
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

async function toggle() {
  open.value = !open.value
  if (open.value) {
    if (localStorage.getItem(HOURS_TIP_KEY) !== '1') {
      showHoursTip.value = true
    }
    // 面板内若已是 CLOSED，保留结束提示，由用户点「开始新对话」
    if (sessionClosed.value) {
      scrollBottom()
      return
    }
    // 打开时 openOrResume：有 OPEN 续聊；本地 token 对应已关闭则后端会新建
    await bootstrap()
    if (session.value && !sessionClosed.value) {
      await chatApi.markRead(session.value.id, session.value.accessToken).catch(() => {})
      unread.value = 0
    }
  }
  // 关闭面板不断开 WS，便于未读角标；离开页面时再断
}

async function send() {
  const text = draft.value.trim()
  if (!text || !session.value || sending.value || sessionClosed.value) return
  sending.value = true
  try {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'CHAT', content: text }))
      draft.value = ''
    } else {
      const res: any = await chatApi.send(session.value.id, session.value.accessToken, text)
      if (res?.code && res.code !== 200) {
        throw new Error(res.message || '发送失败')
      }
      if (res.data) upsertMessage(res.data)
      draft.value = ''
      connectWs()
    }
  } catch (e: any) {
    const msg = e?.message || '发送失败'
    if (String(msg).includes('已关闭') || String(msg).includes('重新发起')) {
      markSessionClosed()
    }
    ElMessage.error(msg)
  } finally {
    sending.value = false
  }
}

watch(hiddenOnAuth, (h) => {
  if (h) {
    open.value = false
    cleanupWs(true)
  }
})

onMounted(() => {
  const token = localStorage.getItem(TOKEN_KEY)
  const sid = localStorage.getItem(SESSION_KEY)
  if (token && sid) {
    session.value = {
      id: Number(sid),
      accessToken: token,
      sessionNo: '',
      visitorId: ensureVisitorId(),
      // 真实状态由 WS SESSION 回推校正（可能已 CLOSED）
      status: 'OPEN',
      unreadVisitor: 0
    }
    // 面板关闭时也保持长连接，客服回复/关聊可更新未读与状态
    connectWs()
  }
})

onBeforeUnmount(() => cleanupWs(true))
</script>

<style scoped>
.live-chat {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 1200;
  font-family: Inter, system-ui, sans-serif;
}
.fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 0;
  background: #003ec7;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 12px 28px rgba(0, 62, 199, 0.35);
  position: relative;
  display: grid;
  place-items: center;
}
.fab.open {
  background: #111827;
}
.panel-head-main {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.head-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.18);
  display: grid;
  place-items: center;
  font-size: 18px;
  flex: 0 0 auto;
}
.hours-tip {
  margin: 0;
  padding: 12px 14px;
  background: #eff6ff;
  border-bottom: 1px solid #dbeafe;
  color: #1e3a8a;
}
.hours-tip-title {
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 6px;
}
.hours-tip p {
  margin: 0;
  font-size: 12px;
  line-height: 1.55;
  color: #334155;
}
.hours-tip-acts {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.tip-link {
  font-size: 12px;
  color: #003ec7;
  font-weight: 600;
  text-decoration: none;
}
.tip-link:hover {
  text-decoration: underline;
}
.tip-ok {
  border: 0;
  border-radius: 8px;
  height: 30px;
  padding: 0 12px;
  background: #003ec7;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.sys.soft {
  background: #fff;
  border: 1px dashed #e2e8f0;
  border-radius: 10px;
  padding: 8px 10px;
}
.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  display: grid;
  place-items: center;
}
.panel {
  position: absolute;
  right: 0;
  bottom: 72px;
  width: min(360px, calc(100vw - 24px));
  height: 520px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #003ec7, #2563eb);
  color: #fff;
}
.panel-head strong {
  display: block;
  font-size: 15px;
}
.panel-head p {
  margin: 4px 0 0;
  font-size: 12px;
  opacity: 0.9;
}
.icon-btn {
  border: 0;
  background: transparent;
  color: #fff;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
}
.panel-body {
  flex: 1;
  overflow: auto;
  padding: 14px;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.sys {
  align-self: center;
  max-width: 90%;
  text-align: center;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
  padding: 8px 10px;
}
.row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 85%;
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
  line-height: 1.5;
  color: #111827;
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
  margin-top: 3px;
  font-size: 11px;
  color: #94a3b8;
}
.panel-foot {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #e5e7eb;
  background: #fff;
}
.panel-foot input {
  height: 40px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 0 12px;
  font: inherit;
  outline: none;
}
.panel-foot input:focus {
  border-color: #003ec7;
}
.send {
  height: 40px;
  padding: 0 14px;
  border: 0;
  border-radius: 12px;
  background: #003ec7;
  color: #fff;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}
.send:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.send.full {
  width: 100%;
}
.closed-foot {
  grid-template-columns: 1fr;
}
.closed-banner {
  margin-top: 8px;
  padding: 12px;
  border-radius: 12px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  text-align: center;
}
.closed-banner p {
  margin: 0 0 10px;
  font-size: 13px;
  color: #9a3412;
  line-height: 1.5;
}
.new-chat-btn {
  border: 0;
  border-radius: 10px;
  height: 36px;
  padding: 0 14px;
  background: #003ec7;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}
.chat-pop-enter-active,
.chat-pop-leave-active {
  transition: 0.18s ease;
}
.chat-pop-enter-from,
.chat-pop-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.98);
}
</style>
