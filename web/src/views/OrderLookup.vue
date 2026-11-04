<template>
  <div class="order-lookup-page">
    <SiteHeader />
    <main class="lookup-main container">
      <section class="lookup-hero">
        <h1>{{ $t('orderLookup.title') }}</h1>
        <p>{{ $t('orderLookup.subtitle') }}</p>
      </section>

      <section class="lookup-card">
        <el-form label-position="top" @submit.prevent="search">
          <el-form-item :label="$t('orderLookup.email')" required>
            <el-input
              v-model="email"
              type="email"
              size="large"
              :placeholder="$t('orderLookup.emailPlaceholder')"
              clearable
            />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="lookup-btn" @click="search">
            {{ $t('orderLookup.search') }}
          </el-button>
        </el-form>
        <p class="lookup-tip">{{ $t('orderLookup.tip') }}</p>
      </section>

      <section v-if="searched" class="result-list">
        <div v-if="!orders.length" class="empty-card">
          {{ $t('orderLookup.notFound') }}
        </div>
        <div v-else class="result-meta">
          {{ $t('orderLookup.resultCount', { count: total }) }}
        </div>

        <article
          v-for="order in orders"
          :key="order.id || order.orderNo"
          class="m2-card"
        >
          <div class="m2-top">
            <div class="m2-top-left">
              {{ $t('orderLookup.orderNo') }} {{ order.orderNo }}
              <span v-if="order.createdAt" class="dot">·</span>
              <span v-if="order.createdAt">{{ formatShortDate(order.createdAt) }}</span>
            </div>
            <div class="m2-status" :class="'s-' + statusKey(order)">
              {{ statusLabel(order) }}
            </div>
          </div>

          <div class="m2-mid">
            <div class="m2-pic" aria-hidden="true">{{ productInitial(order) }}</div>
            <div class="m2-info">
              <div class="m2-title">{{ primaryItemName(order) }}</div>
              <div class="m2-sku">
                <span>{{ $t('orderLookup.paymentMethod') }}：{{ paymentLabel(order.paymentMethod) }}</span>
                <span v-if="order.trackingNumber">
                  · {{ $t('orderLookup.trackingNo') }} {{ order.trackingNumber }}
                </span>
                <span v-if="extraItemCount(order) > 0">
                  · {{ $t('orderLookup.moreItems', { n: extraItemCount(order) }) }}
                </span>
              </div>
            </div>
            <div class="m2-price">
              ${{ itemPrice(order).toFixed(2) }}
            </div>
          </div>

          <div class="m2-bot">
            <div class="m2-sum">
              {{ canContinuePay(order) ? $t('orderLookup.amountDue') : $t('orderLookup.paidAmount') }}
              <b>${{ orderAmount(order).toFixed(2) }}</b>
              <small>{{ order.currency || 'USD' }}</small>
            </div>
            <div class="m2-btns">
              <button
                v-if="hasShipping(order)"
                type="button"
                class="m2-btn"
                @click="togglePanel(order, 'ship')"
              >
                {{ isPanel(order, 'ship') ? $t('orderLookup.collapse') : $t('orderLookup.viewShipping') }}
              </button>
              <button
                type="button"
                class="m2-btn"
                @click="togglePanel(order, 'detail')"
              >
                {{ isPanel(order, 'detail') ? $t('orderLookup.collapse') : $t('orderLookup.orderDetails') }}
              </button>
              <button
                v-if="canContinuePay(order)"
                type="button"
                class="m2-btn primary"
                @click="continuePay(order)"
              >
                {{ $t('orderLookup.continuePay') }}
              </button>
            </div>
          </div>

          <div v-if="isPanel(order, 'ship') && hasShipping(order)" class="m2-panel">
            <div v-if="order.shippingCarrier" class="ship-row">
              <span>{{ $t('orderLookup.carrier') }}</span>
              <strong>{{ order.shippingCarrier }}</strong>
            </div>
            <div v-if="order.trackingNumber" class="ship-row">
              <span>{{ $t('orderLookup.trackingNo') }}</span>
              <strong class="mono">{{ order.trackingNumber }}</strong>
            </div>
            <div v-if="order.shippingAddress" class="ship-row ship-row--top">
              <span>{{ $t('orderLookup.shippingAddress') }}</span>
              <strong>{{ order.shippingAddress }}</strong>
            </div>
            <div v-if="order.trackingQrCode" class="qr-block">
              <div class="panel-title">{{ $t('orderLookup.esimOrQr') }}</div>
              <code class="qr-code">{{ order.trackingQrCode }}</code>
              <el-button size="small" @click="copyText(order.trackingQrCode)">
                {{ $t('orderLookup.copy') }}
              </el-button>
            </div>
          </div>

          <div v-if="isPanel(order, 'detail')" class="m2-panel">
            <div class="panel-title">{{ $t('orderLookup.items') }}</div>
            <div v-for="item in order.items || []" :key="item.id" class="item-row">
              <span class="item-name">{{ itemName(item) }}</span>
              <span class="item-price">${{ Number(item.subtotal ?? item.price ?? 0).toFixed(2) }}</span>
            </div>
            <div v-if="!order.items?.length" class="muted">—</div>

            <div class="meta-grid">
              <div class="meta-item">
                <span class="meta-label">{{ $t('orderLookup.createdAt') }}</span>
                <span class="meta-value">{{ order.createdAt || '—' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">{{ $t('orderLookup.paymentMethod') }}</span>
                <span class="meta-value">{{ paymentLabel(order.paymentMethod) }}</span>
              </div>
            </div>

            <template v-if="hasShipping(order)">
              <div class="panel-title">{{ $t('orderLookup.shippingInfo') }}</div>
              <div v-if="order.shippingCarrier" class="ship-row">
                <span>{{ $t('orderLookup.carrier') }}</span>
                <strong>{{ order.shippingCarrier }}</strong>
              </div>
              <div v-if="order.trackingNumber" class="ship-row">
                <span>{{ $t('orderLookup.trackingNo') }}</span>
                <strong class="mono">{{ order.trackingNumber }}</strong>
              </div>
              <div v-if="order.shippingAddress" class="ship-row ship-row--top">
                <span>{{ $t('orderLookup.shippingAddress') }}</span>
                <strong>{{ order.shippingAddress }}</strong>
              </div>
              <div v-if="order.trackingQrCode" class="qr-block">
                <div class="panel-title">{{ $t('orderLookup.esimOrQr') }}</div>
                <code class="qr-code">{{ order.trackingQrCode }}</code>
                <el-button size="small" @click="copyText(order.trackingQrCode)">
                  {{ $t('orderLookup.copy') }}
                </el-button>
              </div>
            </template>

            <div v-if="order.customerNoticeContent" class="notice-block">
              <div class="panel-title">{{ $t('orderLookup.notice') }}</div>
              <p>{{ order.customerNoticeContent }}</p>
            </div>
          </div>
        </article>

        <div v-if="total > pageSize" class="pager">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            background
            :disabled="loading"
            @current-change="onPageChange"
          />
        </div>
      </section>
    </main>
    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import { orderApi } from '@/api/order'
import { navigateByPaymentMethod } from '@/utils/stripeCheckout'
import { getPaymentMethodLabel } from '@/utils/paymentMethodLabel'

const { t, locale } = useI18n({ useScope: 'global' })
const router = useRouter()

const email = ref('')
const loading = ref(false)
const searched = ref(false)
const orders = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 5
/** orderKey -> 'ship' | 'detail' | null */
const openPanels = ref<Record<string, 'ship' | 'detail' | null>>({})

function orderKey(order: any) {
  return String(order?.id || order?.orderNo || '')
}

function isPanel(order: any, kind: 'ship' | 'detail') {
  return openPanels.value[orderKey(order)] === kind
}

function togglePanel(order: any, kind: 'ship' | 'detail') {
  const key = orderKey(order)
  openPanels.value[key] = openPanels.value[key] === kind ? null : kind
}

function statusKey(order: any): string {
  const s = Number(order?.status)
  if (s === 1) return 'paid'
  if (s === 2) return 'shipped'
  if (s === 3) return 'completed'
  if (s === 4) return 'cancelled'
  const raw = String(order?.rawStatus || order?.paymentStatus || '').toUpperCase()
  if (raw === 'PAID' || raw === 'FULFILLING' || raw === 'SUCCESS') return 'paid'
  if (raw === 'SHIPPED') return 'shipped'
  if (raw === 'COMPLETED') return 'completed'
  if (raw === 'CANCELLED') return 'cancelled'
  return 'pending'
}

function statusLabel(order: any) {
  const key = statusKey(order)
  const map: Record<string, string> = {
    pending: 'orderLookup.statusPending',
    paid: 'orderLookup.statusPaid',
    shipped: 'orderLookup.statusShipped',
    completed: 'orderLookup.statusCompleted',
    cancelled: 'orderLookup.statusCancelled'
  }
  return t(map[key] || map.pending)
}

function paymentLabel(method: string | undefined) {
  if (!method) return '—'
  return getPaymentMethodLabel(method, t)
}

function itemName(item: any) {
  if (locale.value === 'en') {
    return item.nameEn || item.productName || item.nameZh || '—'
  }
  return item.nameZh || item.productName || item.nameEn || '—'
}

function primaryItem(order: any) {
  const items = order?.items || []
  return (
    items.find((i: any) => i.itemType === 'product' || i.itemType === 'renewal')
    || items[0]
    || null
  )
}

function primaryItemName(order: any) {
  const item = primaryItem(order)
  return item ? itemName(item) : (order?.productName || '—')
}

function productInitial(order: any) {
  const name = primaryItemName(order)
  const ch = String(name).trim().charAt(0)
  return ch || 'T'
}

function extraItemCount(order: any) {
  const n = (order?.items || []).length
  return Math.max(0, n - 1)
}

function itemPrice(order: any) {
  const item = primaryItem(order)
  if (item) return Number(item.subtotal ?? item.price ?? 0)
  return orderAmount(order)
}

function orderAmount(order: any) {
  return Number(order.finalAmount ?? order.payableAmount ?? order.totalAmount ?? 0)
}

function formatShortDate(raw: string) {
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return raw
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${mm}-${dd} ${hh}:${mi}`
}

function hasShipping(order: any) {
  return !!(
    order?.trackingNumber
    || order?.shippingCarrier
    || order?.trackingQrCode
    || (order?.shippingAddress && Number(order?.status) >= 1)
  )
}

function canContinuePay(order: any) {
  if (!order) return false
  if (order.status === 0) return true
  return order.rawStatus === 'PENDING_PAYMENT' || order.paymentStatus === 'PENDING'
}

async function copyText(text: string) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(t('orderLookup.copied'))
  } catch {
    ElMessage.error(t('orderLookup.copyFailed'))
  }
}

async function fetchOrders(resetPage = false) {
  const em = email.value.trim()
  if (!em) {
    ElMessage.warning(t('checkout.emailRequired'))
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(em)) {
    ElMessage.warning(t('checkout.emailInvalid'))
    return
  }
  if (resetPage) page.value = 1
  loading.value = true
  openPanels.value = {}
  try {
    const res: any = await orderApi.guestLookup({
      email: em,
      page: page.value,
      pageSize
    })
    if (res?.code === 200) {
      const data = res.data
      // Compatible with older List payload if API not yet upgraded
      if (Array.isArray(data)) {
        orders.value = data
        total.value = data.length
      } else {
        orders.value = Array.isArray(data?.content) ? data.content : []
        total.value = Number(data?.totalElements ?? orders.value.length)
      }
      searched.value = true
    } else {
      ElMessage.error(res?.message || t('orderLookup.notFound'))
    }
  } catch (e: any) {
    ElMessage.error(e?.message || t('orderLookup.notFound'))
  } finally {
    loading.value = false
  }
}

async function search() {
  orders.value = []
  total.value = 0
  searched.value = false
  await fetchOrders(true)
}

async function onPageChange(p: number) {
  page.value = p
  await fetchOrders(false)
}

async function continuePay(order: any) {
  if (!order) return
  const method = (order.paymentMethod || 'USDT').toString()
  await navigateByPaymentMethod({
    paymentMethod: method,
    orderId: order.id,
    orderNo: order.orderNo,
    email: order.email || email.value,
    router
  })
}
</script>

<style scoped>
.order-lookup-page {
  min-height: 100vh;
  background: #f5f5f5;
}
.lookup-main {
  padding: 36px 16px 72px;
  max-width: 760px;
}
.lookup-hero h1 {
  margin: 0 0 6px;
  font-size: 22px;
  color: #111;
}
.lookup-hero p {
  margin: 0 0 18px;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}
.lookup-card,
.empty-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
}
.lookup-btn {
  width: 100%;
}
.lookup-tip {
  margin: 12px 0 0;
  font-size: 13px;
  color: #999;
  line-height: 1.5;
}
.result-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.result-meta {
  font-size: 13px;
  color: #666;
}
.pager {
  display: flex;
  justify-content: center;
  padding: 8px 0 4px;
}
.empty-card {
  color: #666;
  text-align: center;
  padding: 36px 20px;
}

.m2-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
}
.m2-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}
.m2-top-left {
  color: #999;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.m2-top-left .dot {
  margin: 0 4px;
}
.m2-status {
  flex-shrink: 0;
  font-weight: 700;
  color: #ff5000;
}
.m2-status.s-paid,
.m2-status.s-shipped,
.m2-status.s-completed {
  color: #00a26a;
}
.m2-status.s-cancelled {
  color: #999;
}

.m2-mid {
  display: flex;
  gap: 12px;
  padding: 14px;
  align-items: flex-start;
}
.m2-pic {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  background: #f7f8fa;
  display: grid;
  place-items: center;
  font-size: 26px;
  font-weight: 700;
  color: #94a3b8;
  flex-shrink: 0;
}
.m2-info {
  flex: 1;
  min-width: 0;
}
.m2-title {
  font-size: 14px;
  font-weight: 650;
  line-height: 1.4;
  color: #111;
}
.m2-sku {
  margin-top: 6px;
  font-size: 12px;
  color: #999;
  line-height: 1.45;
}
.m2-price {
  text-align: right;
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  color: #111;
}

.m2-bot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 12px 14px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}
.m2-sum {
  font-size: 13px;
  color: #666;
}
.m2-sum b {
  color: #111;
  font-size: 16px;
  margin-left: 4px;
}
.m2-sum small {
  margin-left: 2px;
  font-size: 12px;
  color: #999;
}
.m2-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}
.m2-btn {
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 999px;
  padding: 7px 14px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  line-height: 1.2;
}
.m2-btn:hover {
  border-color: #bbb;
}
.m2-btn.primary {
  border-color: #ff5000;
  color: #ff5000;
}
.m2-btn.primary:hover {
  background: #fff7f2;
}

.m2-panel {
  padding: 12px 14px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}
.panel-title {
  margin: 4px 0 8px;
  font-size: 13px;
  font-weight: 700;
  color: #111;
}
.item-row,
.ship-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 6px 0;
  font-size: 14px;
  color: #475569;
}
.item-name { min-width: 0; }
.item-price { font-weight: 650; color: #111; flex-shrink: 0; }
.ship-row span { color: #999; flex-shrink: 0; }
.ship-row strong { text-align: right; color: #111; font-weight: 650; }
.ship-row--top { align-items: flex-start; }
.mono { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; letter-spacing: 0.02em; }
.meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 10px 0;
  margin: 8px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}
.meta-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}
.meta-value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.notice-block {
  margin-top: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f7f8fa;
}
.notice-block p {
  margin: 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.55;
  white-space: pre-wrap;
}
.qr-block {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed #e8e8e8;
}
.qr-code {
  display: block;
  margin: 8px 0;
  padding: 10px;
  border-radius: 8px;
  background: #f7f8fa;
  border: 1px solid #eee;
  font-size: 12px;
  word-break: break-all;
  color: #333;
}
.muted { color: #999; font-size: 13px; }

@media (max-width: 560px) {
  .meta-grid { grid-template-columns: 1fr; }
  .m2-bot { align-items: flex-start; }
  .m2-btns { width: 100%; }
  .m2-btn { flex: 1; text-align: center; }
}
</style>
