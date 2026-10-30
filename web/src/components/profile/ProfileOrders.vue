<template>
  <div class="orders-page">

    <!-- 发货信息：二维码 / 快递服务商+单号，按有无组合显示 -->
    <el-dialog v-model="shipDialogVisible" :title="$t('orders.shipInfo')" width="360px" align-center>
      <div class="ship-dialog-body" v-if="selectedShipOrder">
        <div class="qr-code-container" v-if="hasQr(selectedShipOrder)">
          <EsimQrImage :value="selectedShipOrder.trackingQrCode!" img-class="qr-code-image" :size="260" />
        </div>
        <div class="ship-logistics" v-if="hasLogistics(selectedShipOrder)">
          <div class="ship-logistics-row">
            <span class="ship-logistics-label">{{ $t('orderDetail.shippingCarrier') }}</span>
            <span class="ship-logistics-value">{{ getCarrierName(selectedShipOrder.shippingCarrier) }}</span>
          </div>
          <div class="ship-logistics-row">
            <span class="ship-logistics-label">{{ $t('orderDetail.trackingNumber') }}</span>
            <span class="ship-logistics-value">{{ selectedShipOrder.trackingNumber }}</span>
          </div>
        </div>
      </div>
    </el-dialog>


    <!-- Metrics Grid -->
    <section class="metrics-grid">
      <div class="metric-card">
        <div class="metric-head">
          <h3 class="metric-label">{{ $t('profile.totalOrders') || 'Total Orders' }}</h3>
          <div class="metric-icon metric-icon--default">
            <span class="material-symbols-outlined">receipt_long</span>
          </div>
        </div>
        <p class="metric-value">{{ orderStats.totalCount }}</p>
      </div>
      <div class="metric-card">
        <div class="metric-head">
          <h3 class="metric-label">{{ $t('profile.pendingPayment') || 'Pending Payment' }}</h3>
          <div class="metric-icon metric-icon--tertiary">
            <span class="material-symbols-outlined">pending_actions</span>
          </div>
        </div>
        <p class="metric-value">{{ orderStats.pendingCount }}</p>
      </div>
      <div class="metric-card">
        <div class="metric-head">
          <h3 class="metric-label">{{ $t('profile.paid') || 'Paid / Active' }}</h3>
          <div class="metric-icon metric-icon--primary">
            <span class="material-symbols-outlined">check_circle</span>
          </div>
        </div>
        <p class="metric-value">{{ orderStats.paidCount }}</p>
      </div>
      <div class="metric-card">
        <div class="metric-head">
          <h3 class="metric-label">{{ $t('profile.cumulativeAmount') || 'Total Spent' }}</h3>
          <div class="metric-icon metric-icon--outline">
            <span class="material-symbols-outlined">payments</span>
          </div>
        </div>
        <p class="metric-value metric-value--amount">${{ orderStats.totalAmount.toFixed(2) }}</p>
      </div>
    </section>

    <!-- List Header -->
    <div class="list-header">
      <h2 class="list-title">{{ $t('orders.recentTransactions') }}</h2>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>{{ $t('common.loading') }}</p>
    </div>

    <div class="orders-list" v-else-if="orders && orders.length > 0">
      <article
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        :class="order.status === 0 ? 'order-card--pending' : 'order-card--default'"
      >
        <!-- Main Row -->
        <div class="order-main">
          <div class="order-icon-wrap">
            <span class="material-symbols-outlined order-sim-icon">sim_card</span>
          </div>
          <div class="order-info">
            <div class="order-title-row">
              <h4 class="order-name">{{ getOrderProductName(order) }}</h4>
              <span class="order-badge" :class="`badge-${order.status ?? 0}`">{{ getOrderStatusText(order.status) }}</span>
            </div>
            <p class="order-desc">
              {{ [getOrderRegions(order), getOrderData(order), getOrderDuration(order), getOrderProductType(order)].filter(v => v && v !== '-').join(' · ') || '-' }}
            </p>
            <div class="order-meta">
              <span>{{ order.orderNo || '-' }}</span>
              <span class="meta-dot">•</span>
              <span>{{ order.createdAt ? formatDate(order.createdAt) : '-' }}</span>
              <template v-if="order.paymentMethod">
                <span class="meta-dot">•</span>
                <span>{{ getPaymentText(order.paymentMethod) }}</span>
              </template>
            </div>
          </div>

          <!-- Right: Price + Actions -->
          <div class="order-right">
            <p class="order-price">${{ formatAmount(order.totalAmount) }}</p>
            <div class="order-actions">
              <button
                v-if="order.status === 0"
                class="action-btn action-btn--primary"
                @click="$emit('pay-order', order)"
              >
                {{ $t('orders.payNow') }}
              </button>
              <button
                v-if="order.status === 2 && canShowShipDialog(order)"
                class="action-btn action-btn--outline"
                @click="showShipInfo(order)"
              >
                <span class="material-symbols-outlined qr-icon">{{ hasQr(order) ? 'qr_code_2' : 'local_shipping' }}</span>
                {{ $t('orders.viewShipping') }}
              </button>
              <button
                v-if="order.status === 0"
                class="action-btn action-btn--ghost"
                @click="$emit('cancel-order', order.id)"
              >
                {{ $t('orders.cancelOrder') }}
              </button>
            </div>
          </div>
        </div>

        <!-- Payment Method Selector (status=0) -->
        <div v-if="order.status === 0" class="order-extra order-extra--payment">
          <span class="extra-label">{{ $t('profile.switchPayment') }}：</span>
          <span
            v-for="pm in paymentMethods"
            :key="pm.value"
            class="gateway-option"
            :class="{ active: selectedPayment[order.id] === pm.value }"
            @click="$emit('select-payment', order.id, pm.value)"
          >{{ pm.label }}</span>
          <span v-if="paymentMethods.length === 0" class="gateway-empty">
            {{ $t('payment.noPaymentMethods') || '后台暂未开启可用支付方式' }}
          </span>
        </div>

        <!-- 客户通知（缺货等） -->
        <div v-if="order.customerNoticeSentAt && order.customerNoticeContent" class="order-extra order-extra--notice">
          <div class="notice-tip-head">
            <el-icon><Message /></el-icon>
            <span>{{ $t('profile.customerNoticeTip') }}</span>
          </div>
          <p class="notice-tip-body">{{ order.customerNoticeContent }}</p>
        </div>

        <!-- Status Tips -->
        <div v-if="order.status === 1" class="order-extra order-extra--waiting">
          <div class="waiting-tip-left">
            <el-icon><Clock /></el-icon>
            <span>{{ $t('profile.paidWaitingShip') }}</span>
          </div>
          <span class="waiting-tip-right">{{ $t('profile.phoneCardShipWithin24h') }}</span>
        </div>
        <div v-else-if="order.status === 2" class="order-extra order-extra--shipped">
          <el-icon><Message /></el-icon>
          <span>{{ $t('profile.shippedCheckEmail') }}</span>
        </div>
      </article>
    </div>

    <!-- Empty State -->
    <el-empty v-else :description="$t('orders.noOrders')">
      <router-link to="/products">
        <el-button type="primary">{{ $t('orders.goShopping') }}</el-button>
      </router-link>
    </el-empty>

    <!-- Pagination -->
    <div class="orders-pagination" v-if="!loading && ordersTotal > orderPageSize">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="ordersTotal"
        :page-size="orderPageSize"
        :current-page="orderCurrentPage"
        @current-change="$emit('page-change', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref } from 'vue'
import { Check, Clock, Message } from '@element-plus/icons-vue'
import type { Order } from '@/api/order'
import EsimQrImage from '@/components/EsimQrImage.vue'

const { locale, t } = useI18n({ useScope: 'global' })

const shipDialogVisible = ref(false)
const selectedShipOrder = ref<Order | null>(null)

function hasQr(order: Order | null | undefined): boolean {
  return !!(order?.trackingQrCode && order.trackingQrCode.trim())
}

function hasLogistics(order: Order | null | undefined): boolean {
  const carrier = order?.shippingCarrier?.trim()
  if (!carrier || carrier.toLowerCase() === 'talk2all') return false
  return !!(order?.trackingNumber?.trim())
}

function canShowShipDialog(order: Order): boolean {
  return hasQr(order) || hasLogistics(order)
}

function getCarrierName(carrier: string): string {
  if (!carrier || carrier.toLowerCase() === 'talk2all') return ''
  const map: Record<string, string> = {
    SF: '顺丰速运',
    YTO: '圆通速递',
    ZTO: '中通快递',
    YD: '韵达快递',
    STO: '申通快递',
    JTSD: '极兔速递',
    JD: '京东物流',
    EMS: '邮政EMS',
    DBL: '德邦快递',
    OTHER: '其他'
  }
  return map[carrier] || carrier
}

function showShipInfo(order: Order) {
  selectedShipOrder.value = order
  shipDialogVisible.value = true
}

defineProps<{
  orders: Order[]
  ordersTotal: number
  orderPageSize: number
  orderCurrentPage: number
  loading?: boolean
  orderStats: {
    totalCount: number
    pendingCount: number
    paidCount: number
    totalAmount: number
  }
  selectedPayment: Record<number, string>
  paymentMethods: Array<{ value: string; label: string }>
  getOrderProductName: (order: Order) => string
  getOrderStatusText: (status: number) => string
  getOrderRegions: (order: Order) => string
  getOrderData: (order: Order) => string
  getOrderDuration: (order: Order) => string
  getOrderProductType: (order: Order) => string
  getPaymentText: (method: string) => string
  formatDate: (dateStr: string) => string
  formatAmount: (amount: any) => string
}>()

defineEmits<{
  'pay-order': [order: Order]
  'cancel-order': [id: number]
  'select-payment': [id: number, method: string]
  'page-change': [page: number]
}>()
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Hanken+Grotesk:wght@400;500;600;700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

.orders-page {
  font-family: 'Hanken Grotesk', system-ui, sans-serif;
}

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

/* ===== PAGE HEADER ===== */
.orders-header {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 48px;
}

@media (min-width: 768px) {
  .orders-header {
    flex-direction: row;
    align-items: flex-end;
    justify-content: space-between;
  }
}

.orders-title {
  font-size: 40px;
  font-weight: 600;
  letter-spacing: -0.01em;
  line-height: 1.2;
  color: #1a1b1f;
  margin: 0 0 8px;
}

.orders-subtitle {
  font-size: 17px;
  line-height: 1.5;
  color: #414753;
  margin: 0;
}

.new-order-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: #0059b5;
  color: #ffffff;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.02em;
  text-decoration: none;
  transition: background 0.2s;
  white-space: nowrap;
  flex-shrink: 0;
}

.new-order-btn:hover {
  background: #005cbb;
}

.new-order-btn .material-symbols-outlined {
  font-size: 20px;
}

/* ===== METRICS GRID ===== */
.metrics-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  margin-bottom: 48px;
}

@media (min-width: 640px) {
  .metrics-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (min-width: 1024px) {
  .metrics-grid { grid-template-columns: repeat(4, 1fr); }
}

.metric-card {
  background: #ffffff;
  border: 1px solid rgba(193, 198, 214, 0.4);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.metric-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.metric-label {
  font-size: 12px;
  font-weight: 600;
  color: #414753;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0;
}

.metric-icon {
  width: 32px;
  height: 32px;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.metric-icon .material-symbols-outlined {
  font-size: 18px;
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

.metric-icon--default { background: #eeedf3; color: #0059b5; }
.metric-icon--tertiary { background: #ffdbcb; color: #7a3000; }
.metric-icon--primary { background: #d7e2ff; color: #00458f; }
.metric-icon--outline { background: #eeedf3; color: #414753; }

.metric-value {
  font-size: 48px;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.1;
  color: #1a1b1f;
  margin: 0;
}

.metric-value--amount {
  font-size: 32px;
  margin-top: auto;
}

/* ===== LIST HEADER ===== */
.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(193, 198, 214, 0.3);
}

.list-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1b1f;
  margin: 0;
}

/* ===== ORDERS LIST ===== */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ===== ORDER CARD ===== */
.order-card {
  background: #ffffff;
  border: 1px solid #c1c6d6;
  border-radius: 12px;
  overflow: hidden;
  transition: border-color 0.2s, background 0.2s;
}

.order-card--pending:hover {
  border-color: rgba(0, 89, 181, 0.5);
  background: #f4f3f8;
}

.order-card--default:hover {
  border-color: #717785;
  background: #f4f3f8;
}

/* Main row inside card */
.order-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  align-items: flex-start;
}

@media (min-width: 768px) {
  .order-main {
    flex-direction: row;
    align-items: center;
  }
}

/* SIM icon */
.order-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #eeedf3;
  border: 1px solid #c1c6d6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.order-sim-icon {
  font-size: 24px;
  color: #414753;
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

/* Order info */
.order-info {
  flex: 1;
  min-width: 0;
}

.order-title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 12px;
  margin-bottom: 4px;
}

.order-name {
  font-size: 16px;
  font-weight: 700;
  color: #1a1b1f;
  margin: 0;
  letter-spacing: 0.02em;
}

/* Status badges */
.order-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.badge-0 { background: #ffdbcb; color: #7a3000; border: 1px solid rgba(154, 63, 0, 0.2); }
.badge-1 { background: #d7e2ff; color: #00458f; border: 1px solid rgba(0, 92, 187, 0.2); }
.badge-2 { background: #e9e7ed; color: #1a1b1f; border: 1px solid rgba(193, 198, 214, 0.5); }
.badge-3 { background: #eeedf3; color: #414753; border: 1px solid #c1c6d6; }
.badge-4 { background: #ffdad6; color: #93000a; border: 1px solid rgba(186, 26, 26, 0.2); }

.order-desc {
  font-size: 14px;
  line-height: 1.5;
  color: #414753;
  margin: 0 0 8px;
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #717785;
}

.meta-dot {
  color: #c1c6d6;
}

/* Right: price + actions */
.order-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-top: 1px solid rgba(193, 198, 214, 0.3);
  padding-top: 16px;
  width: 100%;
}

@media (min-width: 768px) {
  .order-right {
    flex-direction: column;
    align-items: flex-end;
    justify-content: center;
    border-top: none;
    padding-top: 0;
    width: auto;
    flex-shrink: 0;
  }
}

.order-price {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.3;
  color: #1a1b1f;
  margin: 0;
}

.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

/* ===== ACTION BUTTONS (matching order.html) ===== */
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.02em;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s, color 0.2s, border-color 0.2s;
  border: 1px solid transparent;
}

.action-btn--primary {
  background: #0059b5;
  color: #ffffff;
  border-color: #0059b5;
}

.action-btn--primary:hover {
  background: rgba(0, 89, 181, 0.9);
}

.action-btn--outline {
  background: transparent;
  color: #0059b5;
  border-color: rgba(0, 89, 181, 0.3);
}

.action-btn--outline:hover {
  background: rgba(0, 89, 181, 0.05);
}

.action-btn--ghost {
  background: transparent;
  color: #717785;
  border-color: rgba(113, 119, 133, 0.3);
}

.action-btn--ghost:hover {
  background: rgba(113, 119, 133, 0.05);
  color: #414753;
}

/* ===== QR ICON ===== */
.qr-icon {
  font-size: 16px;
  font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 20;
  margin-right: 4px;
  vertical-align: middle;
}

/* ===== EXTRA SECTIONS ===== */
.order-extra {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 12px 24px;
  border-top: 1px solid rgba(193, 198, 214, 0.3);
  font-size: 13px;
}

.order-extra--payment {
  background: #f4f3f8;
  color: #414753;
}

.order-extra--notice {
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  background: rgba(245, 158, 11, 0.08);
  color: #92400e;
  border-top-color: rgba(245, 158, 11, 0.2);
}
.notice-tip-head {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
.notice-tip-body {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.6;
  color: #78350f;
  font-size: 13px;
}

.order-extra--waiting {
  justify-content: space-between;
  gap: 12px 16px;
  background: rgba(0, 89, 181, 0.06);
  color: #0059b5;
  border-top-color: rgba(0, 89, 181, 0.15);
}

.waiting-tip-left {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.waiting-tip-right {
  margin-left: auto;
  text-align: right;
  color: #717785;
  font-size: 12px;
  line-height: 1.4;
  max-width: min(420px, 100%);
}

.order-extra--shipped {
  background: rgba(16, 185, 129, 0.06);
  color: #059669;
  border-top-color: rgba(16, 185, 129, 0.15);
}

.extra-label {
  color: #414753;
  font-size: 12px;
  font-weight: 500;
}

.gateway-option {
  padding: 3px 10px;
  background: #ffffff;
  border: 1px solid #c1c6d6;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 12px;
  color: #414753;
}

.gateway-option:hover {
  border-color: #0059b5;
  color: #0059b5;
}

.gateway-option.active {
  border-color: #0059b5;
  background: rgba(0, 89, 181, 0.06);
  color: #0059b5;
  font-weight: 600;
}

.gateway-empty {
  font-size: 12px;
  color: #717785;
}

/* ===== PAGINATION ===== */
.orders-pagination {
  display: flex;
  justify-content: center;
  padding: 24px 0;
  border-top: 1px solid rgba(193, 198, 214, 0.3);
  margin-top: 16px;
}

/* ===== SHIP DIALOG ===== */
.ship-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.qr-code-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 24px 8px;
}

.qr-code-container :deep(.qr-code-image) {
  max-width: 260px;
  max-height: 260px;
  width: auto;
  height: auto;
  object-fit: contain;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(193, 198, 214, 0.5);
  background: #fff;
}

.ship-logistics {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 4px 8px 12px;
}

.ship-logistics-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  font-size: 14px;
  line-height: 1.5;
}

.ship-logistics-label {
  color: #717785;
  flex-shrink: 0;
}

.ship-logistics-value {
  color: #1a1d26;
  text-align: right;
  word-break: break-all;
}
</style>