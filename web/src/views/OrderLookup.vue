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

        <article v-for="order in orders" :key="order.id || order.orderNo" class="result-card">
          <header class="result-head">
            <div class="result-head-main">
              <span class="order-no">{{ order.orderNo }}</span>
              <span class="status-pill" :class="'s-' + statusKey(order)">
                {{ statusLabel(order) }}
              </span>
            </div>
            <div class="result-amount">
              ${{ Number(order.finalAmount ?? order.payableAmount ?? order.totalAmount ?? 0).toFixed(2) }}
              <small>{{ order.currency || 'USD' }}</small>
            </div>
          </header>

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

          <div v-if="order.items?.length" class="items-block">
            <div class="block-title">{{ $t('orderLookup.items') }}</div>
            <div v-for="item in order.items" :key="item.id" class="item-row">
              <span class="item-name">{{ itemName(item) }}</span>
              <span class="item-price">${{ Number(item.subtotal ?? item.price ?? 0).toFixed(2) }}</span>
            </div>
          </div>

          <div v-if="hasShipping(order)" class="shipping-block">
            <div class="block-title">{{ $t('orderLookup.shippingInfo') }}</div>
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
              <div class="block-title">{{ $t('orderLookup.esimOrQr') }}</div>
              <code class="qr-code">{{ order.trackingQrCode }}</code>
              <el-button size="small" @click="copyText(order.trackingQrCode)">
                {{ $t('orderLookup.copy') }}
              </el-button>
            </div>
          </div>

          <div v-if="order.customerNoticeContent" class="notice-block">
            <div class="block-title">{{ $t('orderLookup.notice') }}</div>
            <p>{{ order.customerNoticeContent }}</p>
          </div>

          <el-button
            v-if="canContinuePay(order)"
            type="primary"
            class="pay-btn"
            @click="continuePay(order)"
          >
            {{ $t('orderLookup.continuePay') }}
          </el-button>
        </article>
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

async function search() {
  const em = email.value.trim()
  if (!em) {
    ElMessage.warning(t('checkout.emailRequired'))
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(em)) {
    ElMessage.warning(t('checkout.emailInvalid'))
    return
  }
  loading.value = true
  orders.value = []
  searched.value = false
  try {
    const res: any = await orderApi.guestLookup({ email: em })
    if (res?.code === 200) {
      orders.value = Array.isArray(res.data) ? res.data : []
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
  background:
    radial-gradient(ellipse 80% 50% at 50% -10%, rgba(0, 89, 181, 0.08), transparent),
    linear-gradient(180deg, #f8fafc 0%, #eef2f6 100%);
}
.lookup-main {
  padding: 48px 16px 80px;
  max-width: 760px;
}
.lookup-hero h1 {
  margin: 0 0 8px;
  font-size: clamp(26px, 4vw, 34px);
  letter-spacing: -0.02em;
  color: #0f172a;
}
.lookup-hero p {
  margin: 0 0 28px;
  color: #64748b;
  line-height: 1.5;
}
.lookup-card,
.result-card,
.empty-card {
  background: #fff;
  border-radius: 18px;
  padding: 24px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 10px 36px rgba(15, 23, 42, 0.05);
}
.lookup-btn,
.pay-btn {
  width: 100%;
  margin-top: 4px;
}
.lookup-tip {
  margin: 14px 0 0;
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.5;
}
.result-list {
  margin-top: 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.empty-card {
  color: #64748b;
  text-align: center;
  padding: 36px 20px;
}
.result-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}
.result-head-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}
.order-no {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  word-break: break-all;
}
.status-pill {
  display: inline-flex;
  align-self: flex-start;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 650;
}
.status-pill.s-pending { background: #fff7ed; color: #c2410c; }
.status-pill.s-paid { background: #eff6ff; color: #1d4ed8; }
.status-pill.s-shipped { background: #ecfdf5; color: #047857; }
.status-pill.s-completed { background: #f0fdf4; color: #15803d; }
.status-pill.s-cancelled { background: #f8fafc; color: #64748b; }
.result-amount {
  font-size: 22px;
  font-weight: 750;
  color: #0f172a;
  white-space: nowrap;
}
.result-amount small {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  margin-left: 2px;
}
.meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 14px 0;
  border-top: 1px solid #f1f5f9;
  border-bottom: 1px solid #f1f5f9;
}
.meta-label {
  display: block;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 2px;
}
.meta-value {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}
.block-title {
  margin: 16px 0 8px;
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}
.item-row,
.ship-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 0;
  font-size: 14px;
  color: #475569;
}
.item-name { min-width: 0; }
.item-price { font-weight: 650; color: #0f172a; flex-shrink: 0; }
.ship-row span { color: #94a3b8; flex-shrink: 0; }
.ship-row strong { text-align: right; color: #0f172a; font-weight: 650; }
.ship-row--top { align-items: flex-start; }
.mono { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; letter-spacing: 0.02em; }
.shipping-block,
.notice-block {
  margin-top: 4px;
  padding: 4px 12px 12px;
  border-radius: 12px;
  background: #f8fafc;
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
  border-top: 1px dashed #e2e8f0;
}
.qr-code {
  display: block;
  margin: 8px 0;
  padding: 10px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e2e8f0;
  font-size: 12px;
  word-break: break-all;
  color: #334155;
}
@media (max-width: 560px) {
  .meta-grid { grid-template-columns: 1fr; }
  .result-head { flex-direction: column; }
}
</style>
