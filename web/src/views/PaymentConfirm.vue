<template>
  <div class="confirm-page">
    <SiteHeader />

    <div class="container main-content">
      <div class="confirm-card">
        <h1>{{ $t('paymentConfirm.title') }}</h1>

        <!-- 订单详情 -->
        <div class="order-detail">
          <div class="detail-row">
            <span class="label">{{ $t('paymentConfirm.orderType') }}</span>
            <span class="value">
              <el-tag :type="orderData.type === 'RECHARGE' ? 'success' : 'warning'">
                {{ orderData.type === 'RECHARGE' ? $t('recharge.balanceRecharge') : $t('recharge.renewal') }}
              </el-tag>
            </span>
          </div>

          <div class="detail-row" v-if="orderData.type === 'RENEWAL' && orderData.productName">
            <span class="label">{{ $t('paymentConfirm.product') }}</span>
            <span class="value">{{ orderData.productName }}</span>
          </div>

          <div class="detail-row" v-if="orderData.periodValue">
            <span class="label">{{ $t('paymentConfirm.period') }}</span>
            <span class="value">{{ orderData.periodValue }} {{ getPeriodUnitText(orderData.periodUnit) }}</span>
          </div>

          <div class="detail-row">
            <span class="label">{{ $t('paymentConfirm.amount') }}</span>
            <span class="value amount">${{ orderData.amount?.toFixed(2) }}</span>
          </div>

          <div class="detail-row">
            <span class="label">{{ $t('paymentConfirm.paymentMethod') }}</span>
            <span class="value">
              <div class="payment-method-display">
                <el-icon><component :is="getPaymentIcon(orderData.paymentMethod)" /></el-icon>
                <span>{{ getPaymentLabel(orderData.paymentMethod) }}</span>
              </div>
            </span>
          </div>
        </div>

        <!-- 支付说明 -->
        <div class="payment-tip">
          <p v-if="orderData.paymentMethod === 'USDT'">{{ $t('paymentConfirm.usdtTip') }}</p>
          <p v-else-if="orderData.paymentMethod === 'BANK_CARD'">{{ $t('paymentConfirm.bankCardTip') }}</p>
          <p v-else-if="orderData.paymentMethod === 'BALANCE'">{{ $t('paymentConfirm.balanceTip') }}</p>
          <p v-else>{{ $t('paymentConfirm.defaultTip') }}</p>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button size="large" @click="handleCancel">
            {{ $t('paymentConfirm.cancel') }}
          </el-button>
          <el-button type="primary" size="large" :loading="submitting" @click="handleConfirm">
            {{ $t('paymentConfirm.confirm') }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Wallet, CreditCard, Message, WalletFilled, Money } from '@element-plus/icons-vue'
import SiteHeader from '@/components/SiteHeader.vue'
import { createRecharge } from '@/api/recharge'
import { navigateByPaymentMethod } from '@/utils/stripeCheckout'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const submitting = ref(false)
const orderData = ref<any>({
  type: '',
  amount: 0,
  paymentMethod: '',
  productName: '',
  periodValue: 0,
  periodUnit: ''
})

const iconMap: Record<string, any> = {
  WECHAT: Message,
  ALIPAY: Money,
  ANTOM_ALIPAY: Money,
  USDT: Wallet,
  BANK_CARD: CreditCard,
  BALANCE: WalletFilled
}

function getPaymentIcon(method: string) {
  return iconMap[method] || Wallet
}

function getPaymentLabel(method: string) {
  const labels: Record<string, string> = {
    WECHAT: t('payment.wechatPay'),
    ALIPAY: t('payment.alipay'),
    ANTOM_ALIPAY: t('payment.alipay'),
    USDT: t('payment.usdt'),
    BANK_CARD: t('payment.bankCard'),
    BALANCE: t('payment.balance')
  }
  return labels[method] || method
}

function getPeriodUnitText(unit: string) {
  const units: Record<string, string> = {
    DAY: t('recharge.day'),
    MONTH: t('recharge.month'),
    YEAR: t('recharge.year')
  }
  return units[unit] || unit
}

function handleCancel() {
  router.push('/recharge')
}

async function handleConfirm() {
  if (submitting.value) return
  submitting.value = true

  const requestData: any = {
    type: orderData.value.type,
    amount: orderData.value.amount,
    paymentMethod: orderData.value.paymentMethod
  }

  if (orderData.value.type === 'RENEWAL') {
    requestData.orderId = orderData.value.orderId
    requestData.productName = orderData.value.productName
    requestData.periodValue = orderData.value.periodValue?.toString()
    requestData.periodUnit = orderData.value.periodUnit
    requestData.remarks = orderData.value.remarks
  }

  console.log('Submitting recharge:', requestData)

  try {
    const res: any = await createRecharge(requestData)
    console.log('API response:', res)
    
    // 获取订单信息
    const recharge = res?.recharge
    const orderId = recharge?.rechargeNo || recharge?.id || 'pending'
    const paymentMethod = (requestData.paymentMethod || '').toUpperCase()
    
    console.log('Order created:', { orderId, paymentMethod })
    ElMessage.success(t('paymentConfirm.orderCreated'))

    await navigateByPaymentMethod({
      paymentMethod,
      orderId,
      orderNo: orderId,
      email: authStore.user?.email,
      router,
    })
  } catch (error) {
    console.error('Create recharge failed:', error)
    ElMessage.error(t('paymentConfirm.createFailed'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 从路由参数获取订单数据
  const { type, amount, paymentMethod, orderId, productName, periodValue, periodUnit, remarks } = route.query

  if (!type || !amount || !paymentMethod) {
    ElMessage.error(t('paymentConfirm.invalidOrder'))
    router.push('/recharge')
    return
  }

  orderData.value = {
    type: type as string,
    amount: parseFloat(amount as string),
    paymentMethod: paymentMethod as string,
    orderId: orderId as string,
    productName: productName as string,
    periodValue: periodValue ? parseInt(periodValue as string) : 0,
    periodUnit: periodUnit as string,
    remarks: remarks as string
  }
})
</script>

<style scoped>
.confirm-page {
  min-height: 100vh;
  background: var(--bg-primary);
}

.header {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-subtle);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-sm);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.logo a {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #00d4ff;
}

.nav {
  display: flex;
  gap: 32px;
}

.nav a {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  transition: color 0.3s;
}

.nav a:hover {
  color: #00d4ff;
}

.main-content {
  padding: 60px 20px;
  display: flex;
  justify-content: center;
}

.confirm-card {
  background: var(--bg-card);
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 500px;
  border: 1px solid var(--border-default);
}

.confirm-card h1 {
  font-size: 24px;
  color: var(--text-primary);
  text-align: center;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-subtle);
}

.order-detail {
  background: var(--bg-hover);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-subtle);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row .label {
  color: var(--text-secondary);
  font-size: 14px;
}

.detail-row .value {
  color: var(--text-primary);
  font-weight: 500;
}

.detail-row .amount {
  font-size: 20px;
  font-weight: 700;
  color: #00d4ff;
}

.payment-method-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.payment-method-display .el-icon {
  font-size: 20px;
}

.payment-tip {
  background: rgba(255, 193, 7, 0.1);
  border: 1px solid rgba(255, 193, 7, 0.3);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 32px;
}

.payment-tip p {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.action-buttons .el-button {
  flex: 1;
  height: 48px;
  font-size: 16px;
}

@media (max-width: 768px) {
  .confirm-card {
    padding: 24px;
  }

  .confirm-card h1 {
    font-size: 20px;
  }
}
</style>
