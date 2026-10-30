<template>
  <div class="tickets-page">
    <SiteHeader />

    <!-- Hero Banner -->
    <div class="page-hero">
      <div class="hero-glow"></div>
      <div class="container">
        <div class="hero-inner">
          <div class="hero-icon">
            <svg viewBox="0 0 48 48" fill="none">
              <rect x="4" y="8" width="40" height="32" rx="6" stroke="currentColor" stroke-width="2.5"/>
              <path d="M4 18h40" stroke="currentColor" stroke-width="2.5"/>
              <circle cx="14" cy="13" r="2" fill="currentColor"/>
              <circle cx="24" cy="13" r="2" fill="currentColor"/>
              <path d="M14 28h8M14 34h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <circle cx="36" cy="34" r="8" fill="var(--accent-primary)" opacity="0.15"/>
              <path d="M33 34l2 2 4-4" stroke="var(--accent-primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="hero-text">
            <h1>{{ $t('tickets.title') }}</h1>
            <p>{{ $t('tickets.quickSubmitTip') }}</p>
          </div>
          <el-button type="primary" size="large" class="new-ticket-btn" @click="router.push('/ticket/new')">
            <el-icon><Plus /></el-icon>
            {{ $t('tickets.submitTicket') }}
          </el-button>
        </div>
      </div>
    </div>

    <div class="container main-content">

      <!-- 状态过滤 Tabs -->
      <div class="status-tabs">
        <button
          v-for="tab in statusTabs"
          :key="tab.value"
          :class="['tab-btn', { active: activeStatus === tab.value }]"
          @click="switchStatus(tab.value)"
        >
          <span class="tab-dot" :style="{ background: tab.color }"></span>
          {{ tab.label }}
          <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
        </button>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="state-box">
        <div class="spinner"></div>
        <p>{{ $t('common.loading') }}</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredTickets.length === 0" class="state-box empty">
        <div class="empty-svg">
          <svg viewBox="0 0 100 100" fill="none">
            <circle cx="50" cy="50" r="46" stroke="var(--border-default)" stroke-width="2" stroke-dasharray="6 4"/>
            <rect x="28" y="30" width="44" height="40" rx="6" stroke="var(--text-muted)" stroke-width="2"/>
            <path d="M28 42h44" stroke="var(--text-muted)" stroke-width="2"/>
            <path d="M38 54h10M38 62h16" stroke="var(--text-muted)" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <h3>{{ $t('tickets.noTickets') }}</h3>
        <p>{{ $t('tickets.submitFirst') }}</p>
        <el-button type="primary" @click="router.push('/ticket/new')">
          <el-icon><Plus /></el-icon>
          {{ $t('tickets.submitTicket') }}
        </el-button>
      </div>

      <!-- 工单列表 -->
      <div v-else class="tickets-list">
        <div
          v-for="ticket in filteredTickets"
          :key="ticket.id"
          class="ticket-card"
          @click="viewDetail(ticket)"
        >
          <!-- 左侧状态条 -->
          <div class="card-status-bar" :class="'bar-' + ticket.status.toLowerCase()"></div>

          <div class="card-body">
            <div class="card-top">
              <div class="card-left">
                <span class="ticket-no"># {{ ticket.ticketNo }}</span>
                <span class="type-chip">{{ getTypeText(ticket.type) }}</span>
              </div>
              <div class="card-right">
                <span :class="['status-badge', 'status-' + ticket.status.toLowerCase()]">
                  <span class="status-dot"></span>
                  {{ getStatusText(ticket.status) }}
                </span>
              </div>
            </div>

            <h3 class="ticket-subject">{{ ticket.subject }}</h3>

            <div class="card-meta">
              <span class="meta-item">
                <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5">
                  <circle cx="8" cy="8" r="6.5"/>
                  <path d="M8 5v3.5l2 1.5" stroke-linecap="round"/>
                </svg>
                {{ ticket.createdAt }}
              </span>
              <span :class="['priority-badge', 'pri-' + ticket.priority.toLowerCase()]">
                {{ getPriorityText(ticket.priority) }}
              </span>
            </div>
          </div>

          <div class="card-arrow">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 3l5 5-5 5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination-wrapper">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- Footer -->
    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import { getTickets } from '@/api/ticket'
import { useAuthStore } from '@/stores/auth'
import { navigateToLogin } from '@/utils/authNavigation'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const tickets = ref<any[]>([])
const total = ref(0)
const pageSize = ref(20)
const currentPage = ref(1)
const activeStatus = ref('ALL')

const statusTabs = computed(() => [
  { value: 'ALL',     label: t('tickets.filterAll'),  color: 'var(--accent-primary)',  count: tickets.value.length },
  { value: 'OPEN',    label: t('tickets.pending'),    color: '#22c55e', count: tickets.value.filter(t => t.status === 'OPEN').length },
  { value: 'PENDING', label: t('tickets.processing'), color: '#f59e0b', count: tickets.value.filter(t => t.status === 'PENDING').length },
  { value: 'RESOLVED',label: t('tickets.replied'),   color: '#6b7280', count: tickets.value.filter(t => t.status === 'RESOLVED').length },
  { value: 'CLOSED',  label: t('tickets.resolved'),  color: '#9ca3af', count: tickets.value.filter(t => t.status === 'CLOSED').length },
])

const filteredTickets = computed(() => {
  if (activeStatus.value === 'ALL') return tickets.value
  return tickets.value.filter(t => t.status === activeStatus.value)
})

function switchStatus(val: string) {
  activeStatus.value = val
}

function getTypeText(type: string) {
  const map: any = {
    GENERAL: t('tickets.general'),
    REFUND: t('tickets.refund'),
    TECHNICAL: t('tickets.technical'),
    BILLING: t('tickets.billing'),
    OTHER: t('tickets.other')
  }
  return map[type] || type
}

function getPriorityText(priority: string) {
  const map: any = {
    LOW: t('tickets.low'),
    MEDIUM: t('tickets.medium'),
    HIGH: t('tickets.high'),
    URGENT: t('tickets.urgent')
  }
  return map[priority] || priority
}

function getStatusText(status: string) {
  const map: any = {
    OPEN: t('tickets.pending'),
    PENDING: t('tickets.processing'),
    RESOLVED: t('tickets.replied'),
    CLOSED: t('tickets.resolved')
  }
  return map[status] || status
}

function loadTickets() {
  loading.value = true
  getTickets({ page: currentPage.value - 1, size: pageSize.value })
    .then((res: any) => {
      tickets.value = res.tickets || []
      total.value = res.total || 0
    })
    .catch((err: any) => {
      if (err.code === 401 || (err.response && err.response.status === 401)) {
        authStore.logout()
        ElMessageBox.alert(t('newTicket.loginExpired'), t('common.warning'), {
          confirmButtonText: t('common.confirm'),
          type: 'warning'
        }).then(() => navigateToLogin(router, route)).catch(() => navigateToLogin(router, route))
      } else {
        ElMessage.error(err.message || t('tickets.loadFailed'))
      }
    })
    .finally(() => { loading.value = false })
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadTickets()
}

function viewDetail(row: any) {
  router.push(`/ticket/${row.id}`)
}

onMounted(() => {
  loadTickets()
})
</script>

<style scoped>
/* ===== 页面底色 ===== */
.tickets-page {
  min-height: 100vh;
}

/* ===== Hero ===== */
.page-hero {
  position: relative;
  overflow: hidden;
  background: var(--bg-deep, var(--bg-card));
  border-bottom: 1px solid var(--border-subtle);
  padding: 48px 0 40px;
}

.hero-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 60% 80% at 10% 50%, var(--accent-muted, rgba(99,102,241,0.08)) 0%, transparent 70%);
  pointer-events: none;
}

.hero-inner {
  position: relative;
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.hero-icon {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  background: var(--accent-muted, rgba(99,102,241,0.1));
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-primary);
}

.hero-icon svg {
  width: 36px;
  height: 36px;
}

.hero-text {
  flex: 1;
  min-width: 200px;
}

.hero-text h1 {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.hero-text p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.new-ticket-btn {
  flex-shrink: 0;
}

/* ===== main content ===== */
.main-content {
  padding: 32px var(--site-content-padding-x, 20px) 60px;
  max-width: var(--site-content-max-width, 1300px);
  margin: 0 auto;
}

/* ===== Status Tabs ===== */
.status-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 28px;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border-radius: 20px;
  border: 1px solid var(--border-default);
  background: var(--bg-card);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  border-color: var(--accent-primary);
  color: var(--accent-primary);
}

.tab-btn.active {
  background: var(--accent-primary);
  border-color: var(--accent-primary);
  color: #fff;
}

.tab-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.tab-btn.active .tab-dot {
  background: rgba(255,255,255,0.8) !important;
}

.tab-count {
  background: rgba(255,255,255,0.25);
  border-radius: 10px;
  padding: 1px 7px;
  font-size: 11px;
  font-weight: 600;
}

.tab-btn:not(.active) .tab-count {
  background: var(--bg-hover);
  color: var(--text-muted);
}

/* ===== State Boxes ===== */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  color: var(--text-secondary);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border-default);
  border-top-color: var(--accent-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-svg {
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
}

.state-box.empty h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.state-box.empty p {
  color: var(--text-secondary);
  margin: 0 0 24px;
  font-size: 14px;
}

/* ===== Ticket List ===== */
.tickets-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ticket-card {
  display: flex;
  align-items: stretch;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s, border-color 0.18s;
}

.ticket-card:hover {
  transform: translateY(-2px);
  border-color: var(--accent-primary);
  box-shadow: var(--shadow-md), var(--shadow-glow);
}

/* 左侧状态色条 */
.card-status-bar {
  width: 4px;
  flex-shrink: 0;
}
.bar-open     { background: #22c55e; }
.bar-pending  { background: #f59e0b; }
.bar-resolved { background: #6b7280; }
.bar-closed   { background: #d1d5db; }

.card-body {
  flex: 1;
  padding: 18px 20px;
  min-width: 0;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.card-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ticket-no {
  font-size: 12px;
  font-family: 'SF Mono', 'Fira Mono', monospace;
  color: var(--text-muted);
  background: var(--bg-hover);
  padding: 2px 8px;
  border-radius: 6px;
  white-space: nowrap;
}

.type-chip {
  font-size: 12px;
  color: var(--accent-primary);
  background: var(--accent-muted, rgba(99,102,241,0.08));
  padding: 2px 8px;
  border-radius: 10px;
}

/* 状态徽章 */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 12px;
  white-space: nowrap;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  flex-shrink: 0;
}

.status-open     { color: #16a34a; background: rgba(34,197,94,0.1); }
.status-pending  { color: #d97706; background: rgba(245,158,11,0.1); }
.status-resolved { color: #4b5563; background: rgba(107,114,128,0.1); }
.status-closed   { color: #9ca3af; background: rgba(156,163,175,0.1); }

.ticket-subject {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-muted);
}

.meta-item svg {
  width: 13px;
  height: 13px;
  flex-shrink: 0;
}

/* 优先级徽章 */
.priority-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
}
.pri-low    { color: #6b7280; background: rgba(107,114,128,0.1); }
.pri-medium { color: #2563eb; background: rgba(37,99,235,0.1); }
.pri-high   { color: #d97706; background: rgba(245,158,11,0.1); }
.pri-urgent { color: #dc2626; background: rgba(220,38,38,0.1); }

/* 右侧箭头 */
.card-arrow {
  display: flex;
  align-items: center;
  padding: 0 18px;
  color: var(--text-muted);
  transition: color 0.2s, transform 0.2s;
}

.card-arrow svg {
  width: 16px;
  height: 16px;
}

.ticket-card:hover .card-arrow {
  color: var(--accent-primary);
  transform: translateX(3px);
}

/* ===== 分页 ===== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* ===== Element Plus 覆盖 ===== */
:deep(.el-button--primary) {
  background: var(--accent-primary);
  border-color: var(--accent-primary);
}

:deep(.el-button--primary:hover) {
  background: var(--accent-hover);
  border-color: var(--accent-hover);
  transform: translateY(-1px);
}

:deep(.el-button) {
  background: var(--bg-hover);
  border: 1px solid var(--border-default);
  color: var(--text-primary);
}

:deep(.el-button:hover) {
  background: var(--accent-muted);
  border-color: var(--accent-primary);
  color: var(--accent-primary);
}

:deep(.el-tag) {
  border: none;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: var(--bg-hover);
  --el-pagination-text-color: var(--text-secondary);
  --el-pagination-button-color: var(--text-secondary);
  --el-pagination-hover-color: var(--accent-primary);
}

:deep(.el-pagination .el-pager li) {
  background: var(--bg-hover);
  border: 1px solid var(--border-default);
  color: var(--text-secondary);
}

:deep(.el-pagination .el-pager li:hover) {
  color: var(--accent-primary);
}

:deep(.el-pagination .el-pager li.is-active) {
  background: var(--accent-primary);
  color: #fff;
  border-color: transparent;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  background: var(--bg-hover);
  border: 1px solid var(--border-default);
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .tickets-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
}
</style>
