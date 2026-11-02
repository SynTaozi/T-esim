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
            <el-input v-model="email" type="email" :placeholder="$t('orderLookup.emailPlaceholder')" clearable />
          </el-form-item>
          <el-button type="primary" :loading="loading" class="lookup-btn" @click="search">
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
          <div class="result-row">
            <span>{{ $t('orderLookup.orderNo') }}</span>
            <strong>{{ order.orderNo }}</strong>
          </div>
          <div class="result-row">
            <span>{{ $t('orderLookup.status') }}</span>
            <strong>{{ order.statusText || order.rawStatus || order.status }}</strong>
          </div>
          <div class="result-row">
            <span>{{ $t('orderLookup.amount') }}</span>
            <strong>${{ Number(order.finalAmount ?? order.totalAmount ?? 0).toFixed(2) }} {{ order.currency || 'USD' }}</strong>
          </div>
          <div class="result-row">
            <span>{{ $t('orderLookup.createdAt') }}</span>
            <strong>{{ order.createdAt || '-' }}</strong>
          </div>
          <div class="result-items" v-if="order.items?.length">
            <div class="items-title">{{ $t('orderLookup.items') }}</div>
            <div v-for="item in order.items" :key="item.id" class="item-row">
              <span>{{ item.productName || item.nameZh || item.nameEn }}</span>
              <span>${{ Number(item.subtotal ?? item.price ?? 0).toFixed(2) }}</span>
            </div>
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

const { t } = useI18n({ useScope: 'global' })
const router = useRouter()

const email = ref('')
const loading = ref(false)
const searched = ref(false)
const orders = ref<any[]>([])

function canContinuePay(order: any) {
  if (!order) return false
  if (order.status === 0) return true
  return order.rawStatus === 'PENDING_PAYMENT' || order.paymentStatus === 'PENDING'
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
  background: linear-gradient(180deg, #f7fafc 0%, #eef3f7 100%);
}
.lookup-main {
  padding: 48px 16px 80px;
  max-width: 720px;
}
.lookup-hero h1 {
  margin: 0 0 8px;
  font-size: 32px;
  color: #0f172a;
}
.lookup-hero p {
  margin: 0 0 24px;
  color: #64748b;
}
.lookup-card,
.result-card,
.empty-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.06);
}
.lookup-btn,
.pay-btn {
  width: 100%;
  margin-top: 8px;
}
.lookup-tip {
  margin: 16px 0 0;
  font-size: 13px;
  color: #94a3b8;
}
.result-list {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.empty-card {
  color: #64748b;
  text-align: center;
}
.result-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
  color: #475569;
}
.result-row strong {
  color: #0f172a;
  text-align: right;
}
.items-title {
  margin: 16px 0 8px;
  font-weight: 600;
  color: #0f172a;
}
.item-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  color: #64748b;
}
</style>
