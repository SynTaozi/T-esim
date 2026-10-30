<template>
  <div class="order-detail-page">
    <SiteHeader />

    <div class="container main-content" v-if="order">
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('orderDetail.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/orders' }">{{ $t('orderDetail.myOrders') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ order.orderNo }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 订单状态 -->
      <div class="order-status-card">
        <div class="status-info">
          <h2>{{ $t('orderDetail.orderStatus') }}</h2>
          <div class="status-badge" :class="`status-${order.status}`">
            {{ order.statusText }}
          </div>
        </div>
        <div class="status-tips" v-if="order.status === 0">
          <p>{{ $t('orderDetail.payNow') }}</p>
          <el-button type="primary" @click="handlePay">{{ $t('orderDetail.payNowBtn') }}</el-button>
        </div>
        <div class="status-tips" v-else-if="order.status === 1">
          <p>{{ $t('orderDetail.paySuccess') }}</p>
        </div>
        <div class="status-tips" v-else-if="order.status === 2">
          <p>{{ $t('orderDetail.shipped') }}</p>
          <p v-if="order.shippingCarrier || order.trackingNumber">
            <strong v-if="order.shippingCarrier">{{ getCarrierName(order.shippingCarrier) }}</strong>
            <br v-if="order.shippingCarrier && order.trackingNumber">
            <span v-if="order.trackingNumber">{{ order.trackingNumber }}</span>
          </p>
        </div>
        <div
          class="status-tips status-tips--notice"
          v-if="order.customerNoticeSentAt && order.customerNoticeContent"
        >
          <p><strong>{{ $t('profile.customerNoticeTip') }}</strong></p>
          <p style="white-space: pre-wrap;">{{ order.customerNoticeContent }}</p>
        </div>
      </div>

      <div class="detail-content">
        <!-- 订单信息 -->
        <div class="info-section">
          <h3>{{ $t('orderDetail.orderInfo') }}</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">{{ $t('orderDetail.orderNumber') }}</span>
              <span class="value">{{ order.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('orderDetail.orderTime') }}</span>
              <span class="value">{{ formatDate(order.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('orderDetail.paymentMethod') }}</span>
              <span class="value">{{ getPaymentMethod(order.paymentMethod) }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('orderDetail.receiverEmail') }}</span>
              <span class="value">{{ order.email }}</span>
            </div>
          </div>
        </div>

        <!-- 收货地址 -->
        <div class="info-section" v-if="order.shippingAddress">
          <h3>{{ $t('orderDetail.shippingAddress') || '收货地址' }}</h3>
          <div class="address-display">
            {{ order.shippingAddress }}
          </div>
        </div>

        <!-- 物流信息：无运单号/快递服务商则不展示 -->
        <div class="info-section" v-if="order.shippingCarrier || order.trackingNumber">
          <h3>{{ $t('orderDetail.shippingInfo') || '物流信息' }}</h3>
          <div class="info-grid">
            <div class="info-item" v-if="order.shippingCarrier">
              <span class="label">{{ $t('orderDetail.shippingCarrier') || '快递服务商' }}</span>
              <span class="value">{{ getCarrierName(order.shippingCarrier) }}</span>
            </div>
            <div class="info-item" v-if="order.trackingNumber">
              <span class="label">{{ $t('orderDetail.trackingNumber') || '运单号' }}</span>
              <span class="value tracking-value">{{ order.trackingNumber }}</span>
            </div>
          </div>
        </div>

        <!-- eSIM 激活二维码（数字发货可无物流字段） -->
        <div class="info-section" v-if="order.trackingQrCode">
          <h3>{{ $t('orderDetail.qrCode') || 'ESIM二维码' }}</h3>
          <div class="tracking-qr-section">
            <EsimQrImage :value="order.trackingQrCode" img-class="tracking-qr-image" :size="240" />
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="info-section">
          <h3>{{ $t('orderDetail.productInfo') }}</h3>
          <div class="product-list">
            <div v-for="item in order.items" :key="item.id" class="product-item">
              <div class="product-image">📱</div>
              <div class="product-info">
                <h4>{{ displayItemName(item) }}</h4>
                <p>{{ $t('orderDetail.unitPrice') }}: ¥{{ item.price }} x {{ item.quantity }}</p>
              </div>
              <div class="product-price">¥{{ item.subtotal }}</div>
            </div>
          </div>
        </div>

        <!-- 订单金额 -->
        <div class="info-section">
          <h3>{{ $t('orderDetail.orderAmount') }}</h3>
          <div class="amount-summary">
            <div class="amount-row">
              <span>{{ $t('orderDetail.totalAmount') }}</span>
              <span>¥{{ order.totalAmount }}</span>
            </div>
            <div class="amount-row">
              <span>{{ $t('orderDetail.shippingFee') }}</span>
              <span>¥0.00</span>
            </div>
            <div class="amount-row total">
              <span>{{ $t('orderDetail.actualPaid') }}</span>
              <span class="total-value">¥{{ order.totalAmount }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button @click="$router.push('/orders')">{{ $t('orderDetail.backToList') }}</el-button>
        <el-button
          v-if="order.status === 0"
          type="danger"
          plain
          @click="handleCancel"
        >
          {{ $t('orderDetail.cancelThis') }}
        </el-button>
      </div>
    </div>

    <el-empty v-else :description="$t('orderDetail.orderNotExist')"></el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi, Order } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import SiteHeader from '@/components/SiteHeader.vue'
import EsimQrImage from '@/components/EsimQrImage.vue'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()

const order = ref<Order | null>(null)

function displayItemName(item: { productName?: string; nameEn?: string; nameZh?: string }) {
  if (locale.value === 'en') {
    return item.nameEn || item.productName || ''
  }
  return item.nameZh || item.productName || ''
}

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const res: any = await orderApi.getOrderById(id)
    if (res.code === 200) {
      order.value = res.data
    }
  } catch (error) {
    console.error('Failed to get order details', error)
  }
})

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleString('zh-CN')
}

function getPaymentMethod(method: string): string {
  const map: Record<string, string> = {
    alipay: t('payment.alipay'),
    wechat: t('payment.wechatPay'),
    card: t('payment.bankCard')
  }
  return map[method] || method
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

async function handlePay() {
  try {
    await ElMessageBox.confirm(t('orderDetail.confirmPay'), t('common.notice'), {
      confirmButtonText: t('orderDetail.confirmPayBtn'),
      cancelButtonText: t('common.cancel'),
      type: 'info'
    })

    const res: any = await orderApi.payOrder(order.value!.id)
    if (res.code === 200) {
      ElMessage.success(t('orderDetail.paySuccessMsg'))
      order.value = res.data
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('orderDetail.payFailed'))
    }
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm(t('orders.confirmCancel'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('orders.considerCancel'),
      type: 'warning'
    })

    const res: any = await orderApi.cancelOrder(order.value!.id)
    if (res.code === 200) {
      ElMessage.success(t('orders.orderCancelled'))
      order.value = res.data
    } else {
      ElMessage.error(res.message || t('orders.cancelFailed'))
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || t('orders.cancelFailed'))
    }
  }
}
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  background: #ffffff;
}

.header {
  background: rgba(10, 10, 26, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
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
  padding: 40px 20px;
}

.breadcrumb {
  margin-bottom: 24px;
}

.order-status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-info h2 {
  font-size: 16px;
  margin-bottom: 8px;
  opacity: 0.8;
}

.status-badge {
  display: inline-block;
  padding: 8px 20px;
  background: rgba(255,255,255,0.2);
  border-radius: 20px;
  font-size: 18px;
  font-weight: 600;
}

.status-tips {
  text-align: right;
}

.status-tips p {
  margin-bottom: 12px;
  opacity: 0.9;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-section {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.info-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.info-item .value {
  font-weight: 500;
  color: white;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.product-image {
  font-size: 40px;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin-bottom: 4px;
  color: white;
}

.product-info p {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.product-price {
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.amount-summary {
  max-width: 300px;
  margin-left: auto;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.amount-row.total {
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.total-value {
  color: #00d4ff;
  font-size: 24px;
}

.address-display {
  color: white;
  font-size: 15px;
  line-height: 1.6;
  padding: 8px 0;
}

.tracking-value {
  color: #00d4ff;
  font-family: monospace;
  letter-spacing: 1px;
}

.tracking-qr-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 212, 255, 0.2);
}

.qr-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.tracking-qr-image {
  max-width: 300px;
  max-height: 300px;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  padding: 8px;
  background: rgba(0, 212, 255, 0.05);
}

.action-buttons {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .order-status-card {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .status-tips {
    text-align: center;
  }
}
</style>
