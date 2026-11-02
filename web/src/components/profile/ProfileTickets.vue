<template>
  <div class="tickets-wrap">
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>{{ $t('common.loading') }}</p>
    </div>

    <div v-else-if="tickets.length === 0" class="empty-card">
      <div class="empty-icon">
        <span class="material-symbols-outlined">confirmation_number</span>
      </div>
      <h3>{{ $t('profile.noTickets') }}</h3>
      <p>{{ $t('tickets.quickSubmitTip') }}</p>
      <button class="pill-blue" type="button" @click="$emit('submit-ticket')">
        {{ $t('profile.submitFirst') }}
      </button>
    </div>

    <template v-else>
      <section class="strip">
        <div class="strip-item">
          <div>
            <span>{{ $t('tickets.totalTickets') }}</span>
            <strong>{{ ticketsTotal }}</strong>
          </div>
          <i class="dot blue"></i>
        </div>
        <div class="strip-item">
          <div>
            <span>{{ $t('tickets.pending') }}</span>
            <strong>{{ ticketStats.pending }}</strong>
          </div>
          <i class="dot warn"></i>
        </div>
        <div class="strip-item">
          <div>
            <span>{{ $t('tickets.solvedLabel') }}</span>
            <strong>{{ ticketStats.resolved }}</strong>
          </div>
          <i class="dot ok"></i>
        </div>
      </section>

      <section class="list-card">
        <div class="list-card-h">
          <h2>{{ $t('tickets.currentInquiries') }}</h2>
          <button class="pill-blue" type="button" @click="$emit('submit-ticket')">
            {{ $t('profile.submitTicket') }}
          </button>
        </div>

        <article
          v-for="ticket in tickets"
          :key="ticket.id"
          class="ticket-row"
          @click="$emit('view-ticket', ticket)"
        >
          <div class="t-icon">
            <span class="material-symbols-outlined">
              {{ ticket.status === 'CLOSED' ? 'inventory_2' : 'confirmation_number' }}
            </span>
          </div>
          <div class="t-body">
            <p class="t-title">
              {{ ticket.subject }}
              <span class="badge" :class="getBadgeClass(ticket.status)">{{ getStatusText(ticket.status) }}</span>
            </p>
            <p class="t-meta">
              #{{ ticket.id }} · {{ formatDate(ticket.createdAt) }} · {{ getPriorityText(ticket.priority) }}
            </p>
          </div>
          <span class="material-symbols-outlined t-chevron">chevron_right</span>
        </article>
      </section>

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
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

.tickets-wrap {
  --blue: #0071e3;
  --blue-soft: rgba(0, 113, 227, 0.08);
  --text: #1d1d1f;
  --muted: #6e6e73;
  --line: #d2d2d7;
  --fill: #f5f5f7;
  --ok: #34c759;
  --warn: #ff9500;
  --shadow: 0 1px 2px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(0, 0, 0, 0.04);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.strip {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.strip-item {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 14px;
  box-shadow: var(--shadow);
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.strip-item span {
  display: block;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 2px;
}

.strip-item strong {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--line);
}
.dot.blue { background: var(--blue); }
.dot.warn { background: var(--warn); }
.dot.ok { background: var(--ok); }

.list-card {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 16px 18px;
}

.list-card-h {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.list-card-h h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.ticket-row {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr) 20px;
  gap: 12px;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  cursor: pointer;
}

.ticket-row:last-child {
  border-bottom: 0;
}

.ticket-row:hover .t-title {
  color: var(--blue);
}

.t-icon {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: var(--fill);
  color: var(--blue);
  display: grid;
  place-items: center;
}

.t-icon .material-symbols-outlined {
  font-size: 18px;
}

.t-title {
  margin: 0 0 3px;
  font-size: 14px;
  font-weight: 600;
}

.t-meta {
  margin: 0;
  color: var(--muted);
  font-size: 12px;
}

.t-chevron {
  color: var(--line);
  font-size: 20px;
}

.badge {
  margin-left: 6px;
  font-size: 11px;
  font-weight: 700;
}

.badge--open,
.badge--pending { color: var(--warn); }
.badge--info,
.badge--solved { color: var(--ok); }
.badge--default { color: var(--muted); }

.pill-blue {
  height: 30px;
  padding: 0 14px;
  border: 0;
  border-radius: 980px;
  background: var(--blue);
  color: #fff;
  font: 600 12px inherit;
  cursor: pointer;
}

.empty-card,
.loading-state {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 48px 20px;
  text-align: center;
  color: var(--muted);
}

.empty-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  border-radius: 16px;
  background: var(--fill);
  color: var(--blue);
  display: grid;
  place-items: center;
}

.empty-card h3 {
  margin: 0 0 6px;
  color: var(--text);
  font-size: 16px;
}

.empty-card p {
  margin: 0 0 16px;
  font-size: 13px;
}

.loading-spinner {
  width: 28px;
  height: 28px;
  margin: 0 auto 12px;
  border: 2px solid rgba(0, 113, 227, 0.15);
  border-top-color: var(--blue);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
}

@media (max-width: 720px) {
  .strip { grid-template-columns: 1fr; }
}
</style>
