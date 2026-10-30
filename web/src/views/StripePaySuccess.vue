<template>
  <div class="stripe-result-page">
    <SiteHeader />
    <div class="result-card">
      <div v-if="checking" class="state">
        <el-icon class="is-loading" :size="36"><Loading /></el-icon>
        <h2>{{ $t('payment.processing') || '正在确认支付结果…' }}</h2>
        <p v-if="orderNo">{{ $t('payment.orderNo') || '订单号' }}: {{ orderNo }}</p>
      </div>
      <div v-else-if="paid" class="state success">
        <h2>{{ $t('usdtPay.paySuccess') || '支付成功' }}</h2>
        <p v-if="orderNo">{{ $t('payment.orderNo') || '订单号' }}: {{ orderNo }}</p>
        <el-button type="primary" @click="goOrders">{{ $t('nav.profile') || '我的订单' }}</el-button>
      </div>
      <div v-else class="state">
        <h2>{{ $t('payment.processing') || '支付处理中' }}</h2>
        <p>{{ tip || ($t('payment.submitRedirect') || '若已完成支付，请稍后在订单中查看状态。') }}</p>
        <p v-if="orderNo">{{ $t('payment.orderNo') || '订单号' }}: {{ orderNo }}</p>
        <el-button type="primary" @click="goOrders">{{ $t('nav.profile') || '个人中心' }}</el-button>
        <el-button @click="recheck">{{ $t('common.retry') || '刷新状态' }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import SiteHeader from '@/components/SiteHeader.vue'
import { getRechargeByNo } from '@/api/recharge'
import { paymentApi } from '@/api/payment'
import { orderApi } from '@/api/order'

const route = useRoute()
const router = useRouter()
const orderNo = ref(String(route.query.orderNo || ''))
const sessionId = ref(String(route.query.session_id || route.query.sessionId || ''))
const checking = ref(true)
const paid = ref(false)
const tip = ref('')
let timer: ReturnType<typeof setInterval> | null = null
let attempts = 0

async function confirmViaStripe() {
  if (!sessionId.value) return false
  try {
    const res: any = await paymentApi.confirmStripeSession(sessionId.value)
    if (res?.orderNo) orderNo.value = res.orderNo
    return !!(res?.paid || res?.paymentStatus === 'paid')
  } catch (e: any) {
    tip.value = e?.message || ''
    return false
  }
}

async function checkLocalStatus(): Promise<boolean> {
  const no = orderNo.value
  if (!no) return false

  if (no.startsWith('RC') || no.startsWith('RN')) {
    const res: any = await getRechargeByNo(no)
    return res?.recharge?.status === 'PAID' || res?.status === 'PAID'
  }

  try {
    const payRes: any = await paymentApi.queryPaymentByOrderNo(no)
    if (payRes?.status === 'PAID' || payRes?.paymentStatus === 'PAID') return true
  } catch {
    // ignore
  }

  try {
    const orderRes: any = await orderApi.getOrderByNo(no)
    const order = orderRes?.data || orderRes
    if (order && (order.status === 1 || order.status === 2 || order.paymentStatus === 'PAID')) {
      return true
    }
  } catch {
    // ignore
  }
  return false
}

let confirmAttempted = false

async function checkStatus() {
  checking.value = true
  try {
    // 主动确认只调一次，避免轮询重复触发钉钉/发卡平台
    if (sessionId.value && !confirmAttempted) {
      confirmAttempted = true
      const ok = await confirmViaStripe()
      if (ok) {
        paid.value = true
        return
      }
    }
    paid.value = await checkLocalStatus()
  } finally {
    checking.value = false
  }
}

function startPolling() {
  stopPolling()
  timer = setInterval(async () => {
    attempts += 1
    // 微信/支付宝多为异步支付：前几次轮询继续主动 confirm，避免 webhook 漏单时一直未支付
    if (sessionId.value && attempts <= 5) {
      const okConfirm = await confirmViaStripe()
      if (okConfirm) {
        paid.value = true
        stopPolling()
        return
      }
    }
    const ok = await checkLocalStatus()
    if (ok) {
      paid.value = true
      stopPolling()
      return
    }
    if (attempts >= 15) {
      stopPolling()
      tip.value = tip.value || '支付结果确认较慢，请稍后在订单中查看，或点击刷新'
    }
  }, 2000)
}

function stopPolling() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function recheck() {
  attempts = 0
  // 用户手动刷新时允许再确认一次
  confirmAttempted = false
  checkStatus().then(() => {
    if (!paid.value) startPolling()
  })
}

function goOrders() {
  router.push('/profile')
}

onMounted(async () => {
  await checkStatus()
  if (!paid.value) startPolling()
})

onUnmounted(() => stopPolling())
</script>

<style scoped>
.stripe-result-page {
  min-height: 100vh;
  background: var(--bg-elevated, #f5f7fa);
}
.result-card {
  max-width: 480px;
  margin: 80px auto;
  padding: 40px 32px;
  background: var(--bg-card, #fff);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  text-align: center;
}
.state h2 {
  margin: 16px 0 8px;
}
.state p {
  color: #666;
  margin-bottom: 12px;
}
.state .el-button {
  margin: 0 6px;
}
.success h2 {
  color: #16a34a;
}
</style>
