<template>
  <div class="ticket-detail-page">
    <SiteHeader />

    <div class="container main-content">
      <div class="page-header">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          {{ $t('ticketDetail.backList') }}
        </el-button>
      </div>

      <div v-if="ticket" class="chat-card">
        <div class="chat-header">
          <div class="chat-header-main">
            <h1>{{ ticket.subject }}</h1>
            <p>
              {{ ticket.ticketNo }}
              <span v-if="ticket.type"> · {{ getTypeText(ticket.type) }}</span>
              <span> · {{ ticket.createdAt }}</span>
            </p>
          </div>
          <div class="chat-header-tags">
            <el-tag size="small" :type="getPriorityType(ticket.priority)">{{ getPriorityText(ticket.priority) }}</el-tag>
            <el-tag size="small" :type="getStatusType(ticket.status)">{{ getStatusText(ticket.status) }}</el-tag>
          </div>
        </div>

        <div class="chat-body">
          <div
            v-for="m in thread"
            :key="m.key"
            class="chat-row"
            :class="{ me: m.mine }"
          >
            <div class="av" :class="{ mine: m.mine }">
              {{ m.mine ? 'U' : $t('ticketDetail.serviceInitial') }}
            </div>
            <div class="bubble-wrap">
              <div class="bubble" :class="{ mine: m.mine }">
                <p v-if="m.text" class="bubble-text">{{ m.text }}</p>
                <div v-if="m.images.length" class="bubble-images">
                  <el-image
                    v-for="(src, i) in m.images"
                    :key="src + i"
                    :src="src"
                    :preview-src-list="m.images"
                    :initial-index="i"
                    fit="cover"
                    class="bubble-img"
                  />
                </div>
              </div>
              <div class="bubble-time">
                <template v-if="m.mine">{{ $t('ticketDetail.user') }} · </template>
                <template v-else>
                  {{ $t('ticketDetail.customerService') }}{{ m.by ? ` · ${m.by}` : '' }} ·
                </template>
                {{ m.createdAt }}
              </div>
            </div>
          </div>
        </div>

        <div v-if="ticket.status !== 'CLOSED'" class="chat-composer">
          <div class="composer-main">
            <el-input
              v-model="followUpContent"
              type="textarea"
              :rows="3"
              :placeholder="$t('ticketDetail.followUpPlaceholder')"
              maxlength="2000"
              resize="none"
            />
            <div class="composer-upload">
              <el-upload
                v-model:file-list="fileList"
                list-type="picture-card"
                :auto-upload="false"
                :limit="5"
                multiple
                accept="image/jpeg,image/png,image/gif,image/webp"
                :before-upload="beforeUpload"
                :on-exceed="handleExceed"
                :on-preview="handlePreview"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <p class="upload-hint">{{ $t('newTicket.imageUploadHint') }}</p>
            </div>
          </div>
          <el-button type="primary" class="send-btn" :loading="submitting" @click="submitFollowUp">
            {{ $t('ticketDetail.followUpSubmit') }}
          </el-button>
          <el-image-viewer
            v-if="previewVisible"
            :url-list="previewUrls"
            :initial-index="previewIndex"
            @close="previewVisible = false"
          />
        </div>
        <div v-else class="closed-tip">
          {{ $t('ticketDetail.closedTip') }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile, UploadRawFile } from 'element-plus'
import SiteHeader from '@/components/SiteHeader.vue'
import { getTicketById, followUpTicket, uploadTicketImage } from '@/api/ticket'
import { useAuthStore } from '@/stores/auth'
import { navigateToLogin } from '@/utils/authNavigation'
import { splitTicketContent } from '@/utils/ticketContent'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const ticket = ref<any>(null)
const followUpContent = ref('')
const submitting = ref(false)
const fileList = ref<UploadFile[]>([])
const previewVisible = ref(false)
const previewUrls = ref<string[]>([])
const previewIndex = ref(0)

function beforeUpload(rawFile: UploadRawFile): boolean {
  const okType = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(rawFile.type)
  if (!okType) {
    ElMessage.error(t('newTicket.imageTypeError'))
    return false
  }
  if (rawFile.size > 5 * 1024 * 1024) {
    ElMessage.error(t('newTicket.fileSizeError'))
    return false
  }
  return true
}

function handleExceed() {
  ElMessage.warning(t('newTicket.maxFilesHint'))
}

function handlePreview(file: UploadFile) {
  if (file.url) {
    previewUrls.value = [file.url]
  } else if (file.raw) {
    previewUrls.value = [URL.createObjectURL(file.raw)]
  } else {
    return
  }
  previewIndex.value = 0
  previewVisible.value = true
}

type ThreadMsg = {
  key: string
  mine: boolean
  text: string
  images: string[]
  createdAt?: string
  by?: string
}

/** 用户端：自己的消息在右侧；客服在左侧（与 Intercom 用户视角一致：我=用户） */
const thread = computed<ThreadMsg[]>(() => {
  if (!ticket.value) return []
  const list: ThreadMsg[] = []
  const first = splitTicketContent(ticket.value.content)
  if (first.text || first.images.length) {
    list.push({
      key: 'main',
      mine: true,
      text: first.text,
      images: first.images,
      createdAt: ticket.value.createdAt
    })
  }
  for (const msg of ticket.value.replies || []) {
    const parsed = splitTicketContent(msg.content)
    const isAdmin = !!msg.admin || String(msg.senderType || '').toUpperCase() === 'ADMIN'
    list.push({
      key: String(msg.id || `${msg.createdAt}-${list.length}`),
      mine: !isAdmin,
      text: parsed.text,
      images: parsed.images,
      createdAt: msg.createdAt,
      by: msg.repliedBy
    })
  }
  return list
})

function goBack() {
  router.push({ path: '/profile', query: { tab: 'tickets' } })
}

function getTypeText(type: string) {
  const map: any = {
    GENERAL: t('ticketDetail.general'),
    REFUND: t('ticketDetail.refund'),
    TECHNICAL: t('ticketDetail.technical'),
    BILLING: t('ticketDetail.billing'),
    OTHER: t('ticketDetail.other')
  }
  return map[type] || type
}

function getPriorityType(priority: string) {
  const map: any = { LOW: 'info', MEDIUM: '', HIGH: 'warning', URGENT: 'danger' }
  return map[priority] || ''
}

function getPriorityText(priority: string) {
  const map: any = {
    LOW: t('ticketDetail.low'),
    MEDIUM: t('ticketDetail.medium'),
    HIGH: t('ticketDetail.high'),
    URGENT: t('ticketDetail.urgent')
  }
  return map[priority] || priority
}

function getStatusType(status: string) {
  const map: any = { OPEN: 'success', PENDING: 'warning', RESOLVED: 'info', CLOSED: '' }
  return map[status] || ''
}

function getStatusText(status: string) {
  const map: any = {
    OPEN: t('ticketDetail.pending'),
    PENDING: t('ticketDetail.processing'),
    RESOLVED: t('ticketDetail.replied'),
    CLOSED: t('ticketDetail.resolved')
  }
  return map[status] || status
}

function handleAuthError(err: any) {
  if (err.code === 401 || (err.response && err.response.status === 401)) {
    authStore.logout()
    ElMessageBox.alert(t('newTicket.loginExpired'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      type: 'warning'
    }).then(() => {
      navigateToLogin(router, route)
    }).catch(() => {
      navigateToLogin(router, route)
    })
    return true
  }
  return false
}

function loadTicket() {
  const ticketId = Number(route.params.id)
  getTicketById(ticketId)
    .then((res: any) => {
      ticket.value = res.data
    })
    .catch((err: any) => {
      if (!handleAuthError(err)) {
        ElMessage.error(err.message || t('ticketDetail.loadFailed'))
      }
    })
}

async function submitFollowUp() {
  const text = followUpContent.value.trim()
  const hasFiles = fileList.value.some((f) => !!f.raw)
  if (!text && !hasFiles) {
    ElMessage.warning(t('ticketDetail.followUpRequired'))
    return
  }
  submitting.value = true
  try {
    const imageUrls: string[] = []
    for (const f of fileList.value) {
      if (!f.raw) continue
      const up: any = await uploadTicketImage(f.raw)
      const url = up?.data?.url || up?.url
      if (url) imageUrls.push(url)
    }
    const res: any = await followUpTicket(ticket.value.id, text, imageUrls)
    if (res.code !== 200) {
      ElMessage.error(res.message || t('ticketDetail.followUpFailed'))
      return
    }
    ticket.value = res.data
    followUpContent.value = ''
    fileList.value = []
    ElMessage.success(t('ticketDetail.followUpSuccess'))
  } catch (err: any) {
    if (!handleAuthError(err)) {
      ElMessage.error(err.message || t('ticketDetail.followUpFailed'))
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTicket()
})
</script>

<style scoped>
.ticket-detail-page {
  min-height: 100vh;
  background:
    radial-gradient(900px 420px at 50% -120px, rgba(0, 62, 199, 0.1), transparent 60%),
    linear-gradient(180deg, #f3f6fc 0%, #f5f7fb 100%);
}

.main-content {
  padding: 28px 16px 64px;
  max-width: 760px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 14px;
}

.chat-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.06);
  display: flex;
  flex-direction: column;
  min-height: 620px;
}

.chat-header {
  padding: 18px 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.chat-header-main h1 {
  margin: 0;
  font-size: 17px;
  line-height: 1.35;
  color: #111827;
  letter-spacing: -0.01em;
}

.chat-header-main p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 12px;
}

.chat-header-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.chat-body {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: #fafafa;
  overflow: auto;
  max-height: 52vh;
}

.chat-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  max-width: 88%;
}

.chat-row.me {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.av {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  background: #dbeafe;
  color: #1d4ed8;
}

.av.mine {
  background: #111827;
  color: #fff;
}

.bubble-wrap {
  min-width: 0;
}

.bubble {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px 16px 16px 4px;
  padding: 12px 14px;
  font-size: 14px;
  line-height: 1.55;
  color: #111827;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.bubble.mine {
  background: #003ec7;
  color: #fff;
  border-color: transparent;
  border-radius: 16px 16px 4px 16px;
}

.bubble-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.bubble-images {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.bubble-img {
  width: 72px;
  height: 72px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.25);
  cursor: zoom-in;
}

.bubble:not(.mine) .bubble-img {
  border-color: #e5e7eb;
}

.bubble-time {
  margin-top: 4px;
  font-size: 11px;
  color: #9ca3af;
}

.chat-row.me .bubble-time {
  text-align: right;
}

.chat-composer {
  padding: 14px;
  border-top: 1px solid #e5e7eb;
  background: #fff;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: end;
}

.composer-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-composer :deep(.el-textarea__inner) {
  border-radius: 14px;
  min-height: 72px !important;
  box-shadow: none;
}

.composer-upload :deep(.el-upload--picture-card),
.composer-upload :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 72px;
  height: 72px;
}

.upload-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}

.send-btn {
  height: 44px;
  border-radius: 12px;
  font-weight: 700;
  padding: 0 18px;
  align-self: end;
}

.closed-tip {
  margin: 0;
  padding: 14px 18px;
  border-top: 1px solid #e5e7eb;
  color: #6b7280;
  font-size: 13px;
  background: #f9fafb;
}

@media (max-width: 640px) {
  .chat-composer {
    grid-template-columns: 1fr;
  }
  .send-btn {
    width: 100%;
  }
  .chat-row {
    max-width: 94%;
  }
}
</style>
