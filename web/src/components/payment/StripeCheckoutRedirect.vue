<template>
  <div class="stripe-redirect-page">
    <SiteHeader />
    <div class="redirect-card">
      <div v-if="error" class="error-state">
        <h2>{{ errorTitle }}</h2>
        <p>{{ error }}</p>
        <el-button v-if="method !== 'WECHAT'" type="primary" @click="retry">{{ $t('common.retry') || '重试' }}</el-button>
        <el-button type="primary" @click="goBack">{{ backLabel }}</el-button>
        <el-button v-if="method === 'WECHAT'" @click="retry">{{ $t('common.retry') || '重试' }}</el-button>
      </div>
      <div v-else class="loading-state">
        <el-icon class="is-loading" :size="36"><Loading /></el-icon>
        <h2>{{ title }}</h2>
        <p>{{ $t('payment.submitRedirect') || '正在跳转到安全支付页面…' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import SiteHeader from '@/components/SiteHeader.vue'
import { orderApi } from '@/api/order'
import { paymentApi } from '@/api/payment'
import { useAuthStore } from '@/stores/auth'

const props = withDefaults(
  defineProps<{
    paymentMethod?: string
  }>(),
  { paymentMethod: '' }
)

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const authStore = useAuthStore()

const error = ref('')
const redirecting = ref(false)

/** 同页跳转到 Stripe 收银台 */
function goToCheckoutUrl(url: string) {
  const href = String(url)
  const absolute =
    href.startsWith('http') || href.startsWith('//')
      ? href
      : `${window.location.origin}${href.startsWith('/') ? '' : '/'}${href}`
  redirecting.value = true
  window.location.assign(absolute)
}

function resolveMethod(): 'WECHAT' | 'ALIPAY' | 'BANK_CARD' {
  const fromProp = String(props.paymentMethod || '').toUpperCase()
  const fromMeta = String(route.meta.paymentMethod || '').toUpperCase()
  const fromParam = String(route.params.method || '').toUpperCase()
  const raw = fromProp || fromMeta || fromParam
  if (raw === 'WECHAT' || raw === 'ALIPAY' || raw === 'BANK_CARD') return raw
  if (raw === 'BANKCARD' || raw === 'CARD') return 'BANK_CARD'
  // path /checkout/wechat/:id
  const path = route.path.toLowerCase()
  if (path.includes('/wechat')) return 'WECHAT'
  if (path.includes('/alipay')) return 'ALIPAY'
  return 'BANK_CARD'
}

const method = computed(() => resolveMethod())

const title = computed(() => {
  switch (method.value) {
    case 'WECHAT':
      return t('payment.wechatPay') || '微信支付'
    case 'ALIPAY':
      return t('payment.alipay') || '支付宝'
    case 'BANK_CARD':
      return t('payment.bankCard') || '银行卡'
    default:
      return 'Stripe'
  }
})

const errorTitle = computed(() =>
  method.value === 'WECHAT'
    ? (t('payment.launchPayFailedTitle') || '拉取支付失败')
    : (t('payment.payFailed') || '无法发起支付')
)

const backLabel = computed(() =>
  method.value === 'WECHAT'
    ? (t('payment.chooseOtherPayment') || '选择其他支付方式')
    : (t('payment.backOrder') || '返回')
)

async function resolveOrderRef(): Promise<string> {
  const id = String(route.params.id || '')
  if (!id) throw new Error(t('payment.orderNotFound') || '订单不存在')

  if (id.startsWith('RC') || id.startsWith('RN') || id.startsWith('ES')) {
    return id
  }

  const numId = Number(id)
  if (!Number.isNaN(numId)) {
    const res: any = await orderApi.getOrderById(numId)
    if (res?.code === 200 && res.data?.orderNo) {
      if (res.data.checkoutUrl) {
        goToCheckoutUrl(res.data.checkoutUrl)
        return res.data.orderNo
      }
      return res.data.orderNo
    }
  }
  return id
}

async function startCheckout() {
  error.value = ''
  try {
    const qUrl = route.query.checkoutUrl
    if (typeof qUrl === 'string' && qUrl.startsWith('http')) {
      goToCheckoutUrl(qUrl)
      return
    }

    const orderRef = await resolveOrderRef()
    if (redirecting.value) return

    const res: any = await paymentApi.createStripeCheckoutSession({
      orderNo: orderRef,
      paymentMethod: method.value,
      email: authStore.user?.email || undefined
    })

    const url = res?.url || res?.checkoutUrl || res?.data?.url || res?.data?.checkoutUrl
    if (res?.success === false || !url) {
      throw new Error(res?.message || '创建支付会话失败')
    }
    goToCheckoutUrl(url)
  } catch (e: any) {
    console.error('[Stripe] redirect failed', e)
    // 微信经 mocdocs 转发：连接失败或拉起失败时不展示技术错误
    if (method.value === 'WECHAT') {
      error.value = t('payment.launchPayFailed') || '拉取支付失败，请选择其他支付方式'
    } else {
      error.value = e?.message || String(e)
    }
  }
}

function retry() {
  startCheckout()
}

function goBack() {
  if (window.history.length > 1) router.go(-1)
  else router.push('/products')
}

onMounted(() => {
  startCheckout()
})
</script>

<style scoped>
.stripe-redirect-page {
  min-height: 100vh;
  background: var(--bg-elevated, #f5f7fa);
}
.redirect-card {
  max-width: 480px;
  margin: 80px auto;
  padding: 40px 32px;
  background: var(--bg-card, #fff);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  text-align: center;
}
.loading-state h2,
.error-state h2 {
  margin: 16px 0 8px;
  font-size: 1.25rem;
}
.loading-state p,
.error-state p {
  color: #666;
  margin-bottom: 20px;
}
.error-state .el-button {
  margin: 0 6px;
}
</style>
