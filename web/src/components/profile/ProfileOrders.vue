<template>
  <div class="orders-page">
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

    <section class="strip">
      <div class="strip-item">
        <div>
          <span>{{ $t('profile.totalOrders') || 'Total Orders' }}</span>
          <strong>{{ orderStats.totalCount }}</strong>
        </div>
        <i class="dot blue"></i>
      </div>
      <div class="strip-item">
        <div>
          <span>{{ $t('profile.pendingPayment') || 'Pending Payment' }}</span>
          <strong>{{ orderStats.pendingCount }}</strong>
        </div>
        <i class="dot warn"></i>
      </div>
      <div class="strip-item">
        <div>
          <span>{{ $t('profile.paid') || 'Paid / Active' }}</span>
          <strong>{{ orderStats.paidCount }}</strong>
        </div>
        <i class="dot ok"></i>
      </div>
      <div class="strip-item">
        <div>
          <span>{{ $t('profile.cumulativeAmount') || 'Total Spent' }}</span>
          <strong>${{ orderStats.totalAmount.toFixed(2) }}</strong>
        </div>
        <i class="dot"></i>
      </div>
    </section>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>{{ $t('common.loading') }}</p>
    </div>

    <section v-else-if="orders && orders.length > 0" class="list-card">
      <div class="list-card-h">
        <h2>{{ $t('orders.recentTransactions') }}</h2>
        <p>{{ $t('profile.myOrders') }}</p>
      </div>

      <article
        v-for="order in orders"
        :key="order.id"
        class="purchase"
      >
        <div class="p-icon">
          <span class="material-symbols-outlined">sim_card</span>
        </div>
        <div class="p-body">
          <p class="p-title">
            {{ getOrderProductName(order) }}
            <span class="status" :class="`status-${order.status ?? 0}`">{{ getOrderStatusText(order.status) }}</span>
          </p>
          <p class="p-meta">
            {{ [order.createdAt ? formatDate(order.createdAt) : null, order.orderNo, getOrderRegions(order), getOrderData(order), getOrderDuration(order)].filter(Boolean).join(' · ') }}
          </p>

          <div v-if="order.status === 0" class="pay-row">
            <span>{{ $t('profile.switchPayment') }}</span>
            <button
              v-for="pm in paymentMethods"
              :key="pm.value"
              type="button"
              class="chip"
              :class="{ on: selectedPayment[order.id] === pm.value }"
              @click="$emit('select-payment', order.id, pm.value)"
            >{{ pm.label }}</button>
            <span v-if="paymentMethods.length === 0" class="chip-empty">
              {{ $t('payment.noPaymentMethods') || '后台暂未开启可用支付方式' }}
            </span>
          </div>

          <div v-if="order.customerNoticeSentAt && order.customerNoticeContent" class="tip tip-notice">
            <strong>{{ $t('profile.customerNoticeTip') }}</strong>
            <p>{{ order.customerNoticeContent }}</p>
          </div>
          <div v-else-if="order.status === 1" class="tip tip-wait">
            <span>{{ $t('profile.paidWaitingShip') }}</span>
            <em>{{ $t('profile.phoneCardShipWithin24h') }}</em>
          </div>
          <div v-else-if="order.status === 2" class="tip tip-ship">
            {{ $t('profile.shippedCheckEmail') }}
          </div>
        </div>

        <div class="p-right">
          <div class="p-price">${{ formatAmount(order.totalAmount) }}</div>
          <div class="actions">
            <button
              v-if="order.status === 0"
              type="button"
              class="pill pill-blue"
              @click="$emit('pay-order', order)"
            >{{ $t('orders.payNow') }}</button>
            <button
              v-if="order.status === 2 && canShowShipDialog(order)"
              type="button"
              class="pill pill-line"
              @click="showShipInfo(order)"
            >{{ $t('orders.viewShipping') }}</button>
            <button
              v-if="order.status === 0"
              type="button"
              class="pill pill-line"
              @click="$emit('cancel-order', order.id)"
            >{{ $t('orders.cancelOrder') }}</button>
          </div>
        </div>
      </article>
    </section>

    <el-empty v-else :description="$t('orders.noOrders')">
      <router-link to="/products">
        <el-button type="primary" round>{{ $t('orders.goShopping') }}</el-button>
      </router-link>
    </el-empty>

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
import { ref } from 'vue'
import type { Order } from '@/api/order'
import EsimQrImage from '@/components/EsimQrImage.vue'

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
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

.orders-page {
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
  grid-template-columns: repeat(4, 1fr);
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
  gap: 8px;
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
  flex-shrink: 0;
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
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.list-card-h h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.list-card-h p {
  margin: 0;
  color: var(--muted);
  font-size: 12px;
}

.purchase {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: start;
  padding: 14px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.purchase:last-child {
  border-bottom: 0;
  padding-bottom: 2px;
}

.p-icon {
  width: 44px;
  height: 44px;
  border-radius: 11px;
  background: var(--fill);
  border: 1px solid rgba(0, 0, 0, 0.04);
  display: grid;
  place-items: center;
  color: var(--blue);
}

.p-icon .material-symbols-outlined {
  font-size: 20px;
}

.p-title {
  margin: 0 0 3px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.p-meta {
  margin: 0;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.45;
}

.status {
  margin-left: 6px;
  font-size: 12px;
  font-weight: 600;
}
.status-0 { color: var(--warn); }
.status-1, .status-2 { color: var(--ok); }
.status-3, .status-4 { color: var(--muted); }

.p-right {
  text-align: right;
  white-space: nowrap;
}

.p-price {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 8px;
}

.actions {
  display: flex;
  gap: 6px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.pill {
  height: 28px;
  padding: 0 12px;
  border-radius: 980px;
  border: 0;
  cursor: pointer;
  font: 600 12px inherit;
  display: inline-flex;
  align-items: center;
}

.pill-blue {
  background: var(--blue);
  color: #fff;
}

.pill-line {
  background: var(--fill);
  color: var(--text);
  border: 1px solid var(--line);
}

.pay-row {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 10px;
  background: var(--fill);
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  font-size: 12px;
  color: var(--muted);
}

.chip {
  padding: 4px 9px;
  border-radius: 980px;
  border: 1px solid var(--line);
  background: #fff;
  cursor: pointer;
  font: 600 12px inherit;
  color: var(--text);
}

.chip.on {
  border-color: var(--blue);
  color: var(--blue);
  background: var(--blue-soft);
}

.chip-empty {
  font-size: 12px;
  color: var(--muted);
}

.tip {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.45;
}

.tip-notice {
  background: rgba(255, 149, 0, 0.1);
  color: #8a5a00;
}

.tip-notice strong { display: block; margin-bottom: 4px; }
.tip-notice p { margin: 0; white-space: pre-wrap; }

.tip-wait {
  background: var(--blue-soft);
  color: var(--blue);
  display: flex;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}

.tip-wait em {
  font-style: normal;
  color: var(--muted);
}

.tip-ship {
  background: rgba(52, 199, 89, 0.1);
  color: #248a3d;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 16px;
  color: var(--muted);
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border: 2px solid rgba(0, 113, 227, 0.15);
  border-top-color: var(--blue);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.orders-pagination {
  display: flex;
  justify-content: center;
  padding: 4px 0 8px;
}

.ship-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.qr-code-container {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}

.qr-code-container :deep(.qr-code-image) {
  max-width: 260px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: #fff;
}

.ship-logistics {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ship-logistics-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}

.ship-logistics-label { color: var(--muted); }
.ship-logistics-value { text-align: right; word-break: break-all; }

@media (max-width: 860px) {
  .strip { grid-template-columns: 1fr 1fr; }
  .purchase {
    grid-template-columns: 40px 1fr;
  }
  .p-right {
    grid-column: 2;
    text-align: left;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    white-space: normal;
  }
  .p-price { margin-bottom: 0; }
}
</style>
