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

      <div class="ticket-info" v-if="ticket">
        <div class="info-header">
          <div class="info-title">
            <h1>{{ ticket.subject }}</h1>
            <div class="info-tags">
              <el-tag>{{ getTypeText(ticket.type) }}</el-tag>
              <el-tag :type="getPriorityType(ticket.priority)">{{ getPriorityText(ticket.priority) }}</el-tag>
              <el-tag :type="getStatusType(ticket.status)">{{ getStatusText(ticket.status) }}</el-tag>
            </div>
          </div>
          <div class="info-meta">
            <p>{{ $t('ticketDetail.ticketNo') }}：{{ ticket.ticketNo }}</p>
            <p>{{ $t('ticketDetail.createTime') }}：{{ ticket.createdAt }}</p>
          </div>
        </div>

        <div class="ticket-content">
          <div class="content-item user-content">
            <div class="content-header">
              <el-avatar :size="40">{{ ticket.userId || 'U' }}</el-avatar>
              <span class="content-author">{{ $t('ticketDetail.user') }}</span>
              <span class="content-time">{{ ticket.createdAt }}</span>
            </div>
            <div class="content-body">
              {{ ticket.content }}
            </div>
          </div>

          <div
            v-for="(msg, idx) in ticket.replies || []"
            :key="msg.id || idx"
            class="content-item"
            :class="msg.admin ? 'admin-content' : 'user-content'"
          >
            <div class="content-header">
              <el-avatar
                :size="40"
                :class="{ 'admin-avatar': msg.admin }"
              >
                {{ msg.admin ? $t('ticketDetail.serviceInitial') : (ticket.userId || 'U') }}
              </el-avatar>
              <span class="content-author">
                <template v-if="msg.admin">
                  {{ $t('ticketDetail.customerService') }}
                  <span v-if="msg.repliedBy"> {{ msg.repliedBy }}</span>
                </template>
                <template v-else>{{ $t('ticketDetail.user') }}</template>
              </span>
              <span class="content-time">{{ msg.createdAt }}</span>
            </div>
            <div class="content-body">
              {{ msg.content }}
            </div>
          </div>
        </div>

        <div class="follow-up" v-if="ticket.status !== 'CLOSED'">
          <h3>{{ $t('ticketDetail.followUpTitle') }}</h3>
          <el-input
            v-model="followUpContent"
            type="textarea"
            :rows="4"
            :placeholder="$t('ticketDetail.followUpPlaceholder')"
            maxlength="2000"
            show-word-limit
          />
          <div class="follow-up-actions">
            <el-button type="primary" :loading="submitting" @click="submitFollowUp">
              {{ $t('ticketDetail.followUpSubmit') }}
            </el-button>
          </div>
        </div>
        <div class="closed-tip" v-else>
          {{ $t('ticketDetail.closedTip') }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SiteHeader from '@/components/SiteHeader.vue'
import { getTicketById, followUpTicket } from '@/api/ticket'
import { useAuthStore } from '@/stores/auth'
import { navigateToLogin } from '@/utils/authNavigation'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const ticket = ref<any>(null)
const followUpContent = ref('')
const submitting = ref(false)

function goBack() {
  router.push('/profile?ticket=true')
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
      ticket.value = res.ticket
    })
    .catch((err: any) => {
      console.error('Failed to load ticket', err)
      if (!handleAuthError(err)) {
        ElMessage.error(err.message || t('ticketDetail.loadFailed'))
      }
    })
}

async function submitFollowUp() {
  if (!followUpContent.value.trim()) {
    ElMessage.warning(t('ticketDetail.followUpRequired'))
    return
  }
  submitting.value = true
  try {
    const res: any = await followUpTicket(ticket.value.id, followUpContent.value.trim())
    if (res.success === false) {
      ElMessage.error(res.message || t('ticketDetail.followUpFailed'))
      return
    }
    ticket.value = res.ticket
    followUpContent.value = ''
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
  background: var(--bg-primary);
}

.main-content {
  padding: 40px 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header :deep(.el-button) {
  background: var(--bg-hover);
  border-color: var(--border-default);
  color: var(--text-secondary);
}

.page-header :deep(.el-button:hover) {
  background: var(--accent-muted);
  border-color: var(--accent-primary);
  color: var(--accent-primary);
}

.ticket-info {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 32px;
  border: 1px solid var(--border-default);
}

.info-header {
  border-bottom: 1px solid var(--border-subtle);
  padding-bottom: 24px;
  margin-bottom: 24px;
}

.info-title h1 {
  font-size: 24px;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.info-tags {
  display: flex;
  gap: 8px;
}

.info-tags :deep(.el-tag) {
  background: var(--accent-muted);
  border: none;
  color: var(--accent-primary);
}

.info-tags :deep(.el-tag--success) {
  background: rgba(16, 185, 129, 0.15);
  color: #34d399;
}

.info-tags :deep(.el-tag--warning) {
  background: rgba(251, 191, 36, 0.15);
  color: #fbbf24;
}

.info-tags :deep(.el-tag--danger) {
  background: rgba(244, 63, 94, 0.15);
  color: #f87171;
}

.info-tags :deep(.el-tag--info) {
  background: var(--bg-hover);
  color: var(--text-muted);
}

.info-meta {
  margin-top: 12px;
  color: var(--text-muted);
  font-size: 14px;
}

.info-meta p {
  margin: 4px 0;
}

.ticket-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-item {
  padding: 20px;
  border-radius: 10px;
}

.user-content {
  background: var(--bg-hover);
  border: 1px solid var(--border-subtle);
}

.admin-content {
  background: var(--accent-muted);
  border: 1px solid rgba(225, 29, 72, 0.2);
}

.content-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.content-header :deep(.el-avatar) {
  background: var(--accent-primary);
  color: #fff;
}

.content-author {
  font-weight: 600;
  color: var(--text-primary);
}

.admin-avatar {
  background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary)) !important;
}

.content-time {
  color: var(--text-muted);
  font-size: 13px;
  margin-left: auto;
}

.content-body {
  line-height: 1.8;
  color: var(--text-secondary);
  white-space: pre-wrap;
}

.follow-up {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-subtle);
}

.follow-up h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.follow-up-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.closed-tip {
  margin-top: 24px;
  padding: 12px 16px;
  border-radius: 8px;
  background: var(--bg-hover);
  color: var(--text-muted);
  font-size: 14px;
}
</style>
