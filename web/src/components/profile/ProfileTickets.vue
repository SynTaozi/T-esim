<template>
  <div class="tickets-wrap">
    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>{{ $t('common.loading') }}</p>
    </div>

    <!-- Empty state -->
    <div v-else-if="tickets.length === 0" class="empty-state">
      <div class="empty-icon-wrap">
        <span class="material-symbols-outlined">confirmation_number</span>
      </div>
      <h3>{{ $t('profile.noTickets') }}</h3>
      <p>{{ $t('tickets.quickSubmitTip') }}</p>
      <button class="new-ticket-btn" @click="$emit('submit-ticket')">
        <span class="material-symbols-outlined btn-icon">add</span>
        {{ $t('profile.submitFirst') }}
      </button>
    </div>

    <template v-else>
      <!-- Stats Grid -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-top">
            <span class="material-symbols-outlined stat-icon">forum</span>
            <span class="stat-label">{{ $t('tickets.totalTickets') }}</span>
          </div>
          <div class="stat-value">{{ ticketsTotal }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-top">
            <span class="material-symbols-outlined stat-icon stat-icon--pending">pending</span>
            <span class="stat-label">{{ $t('tickets.pending') }}</span>
          </div>
          <div class="stat-value stat-value--pending">{{ ticketStats.pending }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-top">
            <span class="material-symbols-outlined stat-icon stat-icon--solved">check_circle</span>
            <span class="stat-label">{{ $t('tickets.solvedLabel') }}</span>
          </div>
          <div class="stat-value stat-value--solved">{{ ticketStats.resolved }}</div>
        </div>
      </div>

      <!-- Tickets list section -->
      <div class="list-section">
        <div class="list-header">
          <h3 class="list-title">{{ $t('tickets.currentInquiries') }}</h3>
        </div>
        <div class="ticket-list">
          <div
            v-for="ticket in tickets"
            :key="ticket.id"
            class="ticket-item"
            :class="{ 'ticket-item--resolved': ticket.status === 'CLOSED' }"
            @click="$emit('view-ticket', ticket)"
          >
            <div class="ticket-left">
              <div class="ticket-icon-wrap" :class="{ 'ticket-icon-wrap--solved': ticket.status === 'CLOSED' }">
                <span class="material-symbols-outlined">{{ ticket.status === 'CLOSED' ? 'inventory_2' : 'confirmation_number' }}</span>
              </div>
              <div class="ticket-info">
                <div class="ticket-meta">
                  <span class="ticket-no" :class="{ 'ticket-no--solved': ticket.status === 'CLOSED' }">#{{ ticket.id }}</span>
                  <span class="meta-sep"></span>
                  <span class="ticket-category">{{ getPriorityText(ticket.priority) }}</span>
                </div>
                <h4 class="ticket-subject">{{ ticket.subject }}</h4>
              </div>
            </div>
            <div class="ticket-right">
              <span class="ticket-date">{{ formatDate(ticket.createdAt) }}</span>
              <span class="ticket-badge" :class="getBadgeClass(ticket.status)">{{ getStatusText(ticket.status) }}</span>
              <span class="material-symbols-outlined chevron-icon">chevron_right</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrap" v-if="ticketsTotal > pageSize">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="ticketsTotal"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="$emit('page-change', $event)"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { locale } = useI18n({ useScope: 'global' })

defineProps<{
  tickets: any[]
  ticketsTotal: number
  pageSize: number
  currentPage: number
  loading: boolean
  ticketStats: {
    all: number
    pending: number
    resolved: number
  }
  getPriorityType: (priority: string) => string
  getPriorityText: (priority: string) => string
  getStatusType: (status: string) => string
  getStatusText: (status: string) => string
  formatDate: (dateStr: string) => string
}>()

defineEmits<{
  'submit-ticket': []
  'view-ticket': [ticket: any]
  'page-change': [page: number]
}>()

function getBadgeClass(status: string): string {
  const s = (status || '').toUpperCase()
  if (s === 'OPEN' || s === 'PROCESSING') return 'badge--open'
  if (s === 'PENDING') return 'badge--pending'
  if (s === 'RESOLVED') return 'badge--info'
  if (s === 'CLOSED') return 'badge--solved'
  return 'badge--default'
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Hanken+Grotesk:wght@400;500;600;700&display=swap');

.tickets-wrap {
  font-family: 'Hanken Grotesk', system-ui, sans-serif;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ===== HEADER ===== */
.tickets-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.section-title {
  font-size: 32px;
  font-weight: 600;
  color: #1a1b1f;
  letter-spacing: -0.01em;
  margin: 0 0 8px;
  line-height: 1.2;
}

.section-desc {
  font-size: 17px;
  color: #414753;
  margin: 0;
  line-height: 1.5;
}

.new-ticket-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #0059b5;
  color: #fff;
  border: none;
  border-radius: 9999px;
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.02em;
  cursor: pointer;
  transition: background 0.2s, box-shadow 0.2s;
  white-space: nowrap;
  font-family: inherit;
}

.new-ticket-btn:hover {
  background: #00458f;
  box-shadow: 0 4px 12px rgba(0, 89, 181, 0.3);
}

.btn-icon {
  font-size: 18px;
  font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

/* ===== STATS GRID ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
}

.stat-card {
  background: #fff;
  border: 1px solid rgba(193, 198, 214, 0.5);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.stat-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  color: #414753;
}

.stat-icon {
  font-size: 22px;
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

.stat-icon--pending { color: #9b3f00; }
.stat-icon--solved { color: #0059b5; }

.stat-label {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.stat-value {
  font-size: 52px;
  font-weight: 700;
  color: #1a1b1f;
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.stat-value--pending { color: #9b3f00; }

/* ===== LIST SECTION ===== */
.list-section {
  background: #fff;
  border: 1px solid rgba(193, 198, 214, 0.5);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(193, 198, 214, 0.4);
}

.list-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1b1f;
  margin: 0;
}

/* ===== TICKET ITEM ===== */
.ticket-list {
  display: flex;
  flex-direction: column;
}

.ticket-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(193, 198, 214, 0.25);
  cursor: pointer;
  transition: box-shadow 0.2s, background 0.15s;
}

.ticket-item:last-child { border-bottom: none; }

.ticket-item:hover {
  background: #f4f3f8;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.ticket-item--resolved { opacity: 0.8; }

.ticket-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.ticket-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #eeedf3;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #414753;
  transition: background 0.2s, color 0.2s;
}

.ticket-item:hover .ticket-icon-wrap:not(.ticket-icon-wrap--solved) {
  background: rgba(0, 89, 181, 0.08);
  color: #0059b5;
}

.ticket-icon-wrap--solved {
  background: #e3e2e7;
  color: #717785;
}

.ticket-icon-wrap .material-symbols-outlined {
  font-size: 20px;
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

.ticket-info {
  flex: 1;
  min-width: 0;
}

.ticket-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.ticket-no {
  font-size: 12px;
  font-weight: 600;
  color: #0059b5;
  letter-spacing: 0.02em;
}

.ticket-no--solved { color: #717785; }

.meta-sep {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #c1c6d6;
  flex-shrink: 0;
}

.ticket-category {
  font-size: 12px;
  color: #414753;
  font-weight: 500;
}

.ticket-subject {
  font-size: 15px;
  font-weight: 500;
  color: #1a1b1f;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.ticket-item--resolved .ticket-subject { color: #414753; }

/* ===== TICKET RIGHT ===== */
.ticket-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .ticket-item {
    flex-direction: column;
    align-items: flex-start;
  }
  .ticket-right {
    width: 100%;
    justify-content: space-between;
    border-top: 1px solid rgba(193, 198, 214, 0.3);
    padding-top: 12px;
  }
  .chevron-icon { display: none; }
}

.ticket-date {
  font-size: 12px;
  color: #717785;
  white-space: nowrap;
  font-weight: 500;
}

/* Status badges */
.ticket-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.badge--open {
  background: #0071e3;
  color: #fff;
}

.badge--pending {
  background: #ffdbcb;
  color: #341000;
}

.badge--info {
  background: #dbeafe;
  color: #1d4ed8;
}

.badge--solved {
  background: #e3e2e7;
  color: #414753;
}

.badge--default {
  background: #eeedf3;
  color: #414753;
}

.chevron-icon {
  font-size: 20px;
  color: #414753;
  opacity: 0;
  transition: opacity 0.2s, transform 0.2s;
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

.ticket-item:hover .chevron-icon {
  opacity: 1;
  transform: translateX(2px);
}

/* ===== LOADING STATE ===== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 24px;
  gap: 16px;
  color: #414753;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(0, 89, 181, 0.15);
  border-top-color: #0059b5;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== EMPTY STATE ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 80px 24px;
  gap: 16px;
  background: #fff;
  border: 1px solid rgba(193, 198, 214, 0.5);
  border-radius: 12px;
}

.empty-icon-wrap {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #f4f3f8;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #414753;
}

.empty-icon-wrap .material-symbols-outlined {
  font-size: 32px;
  font-variation-settings: 'FILL' 0, 'wght' 300, 'GRAD' 0, 'opsz' 48;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1b1f;
  margin: 0;
}

.empty-state p {
  font-size: 15px;
  color: #414753;
  margin: 0;
  max-width: 360px;
}

/* ===== PAGINATION ===== */
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}
</style>
