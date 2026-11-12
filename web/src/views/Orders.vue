<template>
  <div class="orders-page">
    <SiteHeader />

    <div class="container main-content">
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('orders.home') || 'Home' }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $t('orders.myOrders') || 'My Orders' }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="orders-container">
        <h2>{{ $t('orders.myOrders') || 'My Orders' }}</h2>

        <!-- 状态过滤 -->
        <div class="filter-section">
          <el-radio-group v-model="statusFilter" @change="loadOrders" size="default">
            <el-radio-button label="">{{ $t('orders.all') || 'All' }}</el-radio-button>
            <el-radio-button label="0">{{ $t('orders.pending') || 'Pending Payment' }}</el-radio-button>
            <el-radio-button label="1">{{ $t('orders.paid') || 'Paid' }}</el-radio-button>
            <el-radio-button label="2">{{ $t('orders.shipped') || 'Shipped' }}</el-radio-button>
            <el-radio-button label="3">{{ $t('orders.completed') || 'Completed' }}</el-radio-button>
            <el-radio-button label="4">{{ $t('orders.cancelled') || 'Cancelled' }}</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 订单列表 -->
        <div v-if="loading" class="loading-container">
          <el-spinner />
        </div>

        <div v-else-if="orders.length === 0" class="empty-state">
          <p>{{ $t('orders.noOrders') || 'No orders found' }}</p>
          <el-button type="primary" @click="$router.push('/products')">
            {{ $t('orders.continueShopping') || 'Continue Shopping' }}
          </el-button>
        </div>

        <div v-else class="orders-list">
          <div v-for="order in orders" :key="order.id" class="order-card">
            <div class="order-header">
              <div class="order-info">
                <h3>{{ $t('orders.orderNumber') || 'Order Number' }}: {{ order.orderNo }}</h3>
                <p class="order-date">{{ formatDate(order.createdAt) }}</p>
              </div>
              <div class="order-status" :class="`status-${order.status}`">
                {{ getStatusText(order.status) }}
              </div>
            </div>

            <div class="order-items">
              <div v-for="item in order.items" :key="item.id" class="item-row">
                <div class="item-image">📱</div>
                <div class="item-details">
                  <p class="item-name">{{ displayItemName(item) }}</p>
                  <p class="item-sku">x{{ item.quantity }}</p>
                </div>
                <div class="item-price">${{ item.subtotal }}</div>
              </div>
            </div>

            <div v-if="order.status === 0" class="pay-methods">
              <span class="pay-methods-label">{{ $t('profile.switchPayment') || '支付方式' }}</span>
              <el-radio-group
                :model-value="selectedPayment[order.id]"
                size="small"
                @update:model-value="(v: string) => selectPayment(order.id, v)"
              >
                <el-radio-button
                  v-for="pm in paymentMethods"
                  :key="pm.value"
                  :label="pm.value"
                >{{ pm.label }}</el-radio-button>
              </el-radio-group>
              <span v-if="!paymentMethods.length" class="pay-methods-empty">
                {{ $t('payment.noPaymentMethods') || '后台暂未开启可用支付方式' }}
              </span>
            </div>

            <div class="order-footer">
              <div class="order-total">
                <span>{{ $t('orders.total') || 'Total' }}:</span>
                <span class="total-amount">${{ order.totalAmount }}</span>
              </div>
              <div class="order-actions">
                <el-button link type="primary" @click="viewOrderDetail(order.id)">
                  {{ $t('orders.viewDetails') || 'View Details' }}
                </el-button>
                <el-button
                  v-if="order.status === 0"
                  link
                  type="primary"
                  :loading="payingId === order.id"
                  @click="payOrder(order)"
                >
                  {{ $t('orders.pay') || 'Pay Now' }}
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <el-pagination
          v-if="orders.length > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="loadOrders"
          style="margin-top: 20px; text-align: right;"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import SiteHeader from '@/components/SiteHeader.vue'
import { useAuthStore } from '@/stores/auth'
import { orderApi } from '@/api/order'
import { paymentMethodApi } from '@/api/paymentMethod'
import { getPaymentMethodLabel } from '@/utils/paymentMethodLabel'
import { navigateByPaymentMethod } from '@/utils/stripeCheckout'

const router = useRouter()
const authStore = useAuthStore()
const { t, locale } = useI18n()

function displayItemName(item: { productName?: string; nameEn?: string; nameZh?: string }) {
  if (locale.value === 'en') {
    return item.nameEn || item.productName || ''
  }
  return item.nameZh || item.productName || ''
}

const orders = ref<any[]>([])
const loading = ref(true)
const payingId = ref<number | null>(null)
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const paymentMethods = ref<{ value: string; label: string }[]>([])
const selectedPayment = ref<Record<number, string>>({})

const statusMap: Record<number, string> = {
  0: 'Pending Payment',
  1: 'Paid',
  2: 'Shipped',
  3: 'Completed',
  4: 'Cancelled'
}

const formatDate = (date: string | Date) => {
  return new Date(date).toLocaleDateString()
}

const getStatusText = (status: number) => {
  return statusMap[status] || 'Unknown'
}

function selectPayment(orderId: number, method: string) {
  selectedPayment.value[orderId] = String(method || '').toUpperCase()
}

function syncSelectedPayments() {
  const enabled = new Set(paymentMethods.value.map((pm) => pm.value))
  for (const order of orders.value) {
    if (order.status !== 0 || selectedPayment.value[order.id]) continue
    const fromOrder = String(order.paymentMethod || '').toUpperCase()
    if (fromOrder && enabled.has(fromOrder)) {
      selectedPayment.value[order.id] = fromOrder
    }
  }
}

async function loadPaymentMethods() {
  try {
    const res: any = await paymentMethodApi.getEnabled()
    const list = res?.data ?? res ?? []
    paymentMethods.value = (Array.isArray(list) ? list : [])
      .map((pm: any) => ({
        value: String(pm.code || '').toUpperCase(),
        label: getPaymentMethodLabel(pm.code, t) || pm.code
      }))
      .filter((pm: { value: string }) => !!pm.value)
  } catch {
    paymentMethods.value = []
  }
}

const loadOrders = async () => {
  loading.value = true
  try {
    const response = await orderApi.getMyOrders({
      page: currentPage.value,
      pageSize: pageSize.value,
      status: statusFilter.value ? parseInt(statusFilter.value) : undefined
    })

    orders.value = response.data?.content || []
    total.value = response.data?.totalElements || 0
    syncSelectedPayments()
  } catch (error) {
    ElMessage.error('Failed to load orders')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const viewOrderDetail = (orderId: number) => {
  router.push(`/order/${orderId}`)
}

async function payOrder(order: any) {
  if (!paymentMethods.value.length) {
    ElMessage.warning(t('payment.noPaymentMethods') || '后台暂未开启可用支付方式')
    return
  }
  const method = String(selectedPayment.value[order.id] || '').toUpperCase()
  if (!method || !paymentMethods.value.some((pm) => pm.value === method)) {
    ElMessage.warning(t('payment.selectPaymentMethod') || '请先选择支付方式')
    return
  }
  payingId.value = order.id
  try {
    await navigateByPaymentMethod({
      paymentMethod: method,
      orderId: order.id,
      orderNo: order.orderNo,
      email: order.buyerEmail || order.email || authStore.user?.email,
      router
    })
  } catch (e: any) {
    ElMessage.error(e?.message || t('orderDetail.payFailed') || '支付跳转失败')
  } finally {
    payingId.value = null
  }
}

onMounted(async () => {
  await loadPaymentMethods()
  await loadOrders()
})
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.main-content {
  flex: 1;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.orders-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.orders-container h2 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #333;
}

.filter-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.loading-container {
  text-align: center;
  padding: 40px;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.order-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #fafafa;
  border-bottom: 1px solid #eee;
}

.order-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.order-date {
  margin: 0;
  font-size: 14px;
  color: #999;
}

.order-status {
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 500;
  font-size: 14px;
}

.order-status.status-0 {
  background-color: #fef0f0;
  color: #f56c6c;
}

.order-status.status-1 {
  background-color: #f0f9ff;
  color: #409eff;
}

.order-status.status-2 {
  background-color: #f0f9ff;
  color: #409eff;
}

.order-status.status-3 {
  background-color: #f0f9f0;
  color: #67c23a;
}

.order-status.status-4 {
  background-color: #faf0f0;
  color: #909399;
}

.order-items {
  padding: 16px;
  background: white;
}

.item-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.item-row:last-child {
  border-bottom: none;
}

.item-image {
  font-size: 32px;
  margin-right: 16px;
  width: 50px;
  text-align: center;
}

.item-details {
  flex: 1;
}

.item-name {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.item-sku {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.item-price {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.pay-methods {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #eee;
  background: #fff;
}
.pay-methods-label {
  font-size: 13px;
  color: #666;
}
.pay-methods-empty {
  font-size: 13px;
  color: #999;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #fafafa;
  border-top: 1px solid #eee;
}

.order-total {
  font-size: 14px;
  color: #666;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
  margin-left: 8px;
}

.order-actions {
  display: flex;
  gap: 12px;
}
</style>
