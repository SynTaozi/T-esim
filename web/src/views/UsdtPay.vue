<template>
  <div class="checkout-page">
    <SiteHeader />

    <main class="checkout-main" v-loading="loading">
      <!-- Breadcrumb / Back -->
      <div class="checkout-header-area">
        <a href="#" class="back-link" @click.prevent="goBack">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M10 12L6 8L10 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ $t('payment.back') }}
        </a>
        <h1 class="checkout-heading">{{ $t('usdtPay.pageTitle') || 'Secure Checkout' }}</h1>
        <p class="checkout-subheading">{{ $t('usdtPay.pageSubtitle') || 'Complete your purchase to activate your eSIM instantly.' }}</p>
      </div>

      <div class="checkout-grid">
        <!-- Left Column: Order Summary -->
        <div class="checkout-card order-card">
          <h2 class="card-section-title">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
            {{ $t('usdtPay.orderInfo') }}
          </h2>

          <!-- Status Banner -->
          <div class="status-banner status-pending" v-if="displayStatus === 0">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <span>{{ $t('usdtPay.orderValidRemaining') }}</span>
            <strong>{{ formatTime(countdown) }}</strong>
          </div>
          <div class="status-banner status-success" v-else-if="displayStatus === 1">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            <span>{{ $t('usdtPay.orderPaid') }}</span>
          </div>
          <div class="status-banner status-expired" v-else-if="displayStatus === 4">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            <span>{{ $t('usdtPay.orderExpired') }}</span>
          </div>

          <!-- Product Card -->
          <div class="product-summary-card" v-if="order || recharge">
            <div class="product-badge">
              <span>TRC20</span>
            </div>
            <div class="product-summary-info">
              <template v-if="order?.items?.length">
                <h3 class="product-summary-name">{{ getLocalizedProductName(currentProductItem) || getLocalizedProductName(order.items[0]) }}</h3>
                <p class="product-summary-meta" v-if="displayValidityText">{{ displayValidityText }}</p>
              </template>
              <template v-else-if="isRechargeOrder">
                <h3 class="product-summary-name">{{ recharge.productName || $t('recharge.title') }}</h3>
                <p class="product-summary-meta">{{ recharge.type === 'RECHARGE' ? $t('recharge.recharge') : $t('recharge.renewal') }}</p>
              </template>
            </div>
            <div class="product-summary-price">${{ displayAmount }}</div>
          </div>

          <!-- Order Detail Rows -->
          <div class="price-breakdown" v-if="order || recharge">
            <div class="price-row">
              <span class="price-label">{{ $t('usdtPay.orderNo') }}</span>
              <span class="price-value order-no-value">{{ displayOrderNo }}</span>
            </div>
            <div class="price-row" v-if="isRechargeOrder">
              <span class="price-label">{{ $t('recharge.typeCol') }}</span>
              <span class="price-value">{{ recharge.type === 'RECHARGE' ? $t('recharge.recharge') : $t('recharge.renewal') }}</span>
            </div>
            <div class="price-row">
              <span class="price-label">{{ $t('usdtPay.usdAmount') }}</span>
              <span class="price-value">${{ displayAmount }}</span>
            </div>
            <div class="price-row">
              <span class="price-label">{{ $t('usdtPay.network') }}</span>
              <span class="price-value network-badge">TRC20</span>
            </div>
          </div>

          <!-- Total -->
          <div class="price-total">
            <span>{{ $t('usdtPay.paymentAmount') }}</span>
            <span class="price-total-value">{{ displayUsdtAmount }} <em>USDT</em></span>
          </div>

          <!-- QR Code -->
          <div class="qr-section" v-if="order || recharge">
            <div class="qr-wrapper">
              <canvas ref="qrCanvas" class="qr-canvas"></canvas>
            </div>
            <small class="qr-label">{{ $t('usdtPay.scanPay') }}</small>
          </div>
        </div>

        <!-- Right Column: USDT Payment Details -->
        <div class="checkout-card payment-card">
          <!-- Notice -->
          <div class="notice-banner">
            <span class="notice-gem">💎</span>
            <p>{{ $t('usdtPay.notice') }}</p>
          </div>

          <!-- Amount to Pay (pending state) -->
          <template v-if="displayStatus === 0 || displayStatus === undefined">
            <div class="amount-highlight">
              <p class="amount-highlight-label">{{ $t('usdtPay.paymentAmount') }}</p>
              <div class="amount-highlight-value" @click="copyUsdtAmount">
                <span class="amount-highlight-number">{{ displayUsdtAmount }}</span>
                <span class="amount-highlight-unit">USDT</span>
                <span class="copy-chip" :class="{ copied: copiedUsdt }">
                  {{ copiedUsdt ? $t('usdtPay.copied') : $t('usdtPay.clickToCopy') }}
                </span>
              </div>
              <p class="amount-highlight-note">{{ $t('usdtPay.exactAmountNote') }}</p>
            </div>

            <!-- Address -->
            <div class="address-block">
              <label class="address-label">{{ $t('usdtPay.usdtAddress') }}</label>
              <div class="address-display">
                <code class="address-text">{{ usdtAddress }}</code>
                <button class="copy-address-btn" @click="copyAddress">{{ $t('usdtPay.copy') }}</button>
              </div>
            </div>

            <!-- Tips -->
            <div class="tips-block">
              <h4 class="tips-heading">⚠️ {{ $t('usdtPay.importantTips') }}</h4>
              <ul class="tips-list">
                <li>{{ $t('usdtPay.useTRC20') }}</li>
                <li>{{ $t('usdtPay.exactAmount') }}<strong>{{ displayUsdtAmount }} USDT</strong></li>
                <li>{{ $t('usdtPay.billValidTime') }}</li>
              </ul>
            </div>

            <!-- Actions -->
            <div class="action-buttons">
              <button class="btn-primary-action" @click="checkPayment" :disabled="checking">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                {{ checking ? $t('usdtPay.checking') : $t('usdtPay.checkPayment') }}
              </button>
              <button class="btn-secondary-action" @click="cancelOrder" :disabled="cancelling" v-if="!isRechargeOrder">
                {{ cancelling ? $t('usdtPay.cancelling') : $t('usdtPay.cancelOrder') }}
              </button>
            </div>
          </template>

          <!-- Success State -->
          <div class="result-state" v-else-if="displayStatus === 1">
            <div class="result-icon result-success">✓</div>
            <h4 class="result-title">{{ $t('usdtPay.paySuccess') }}</h4>
            <p class="result-desc">{{ $t('usdtPay.preparingEsim') }}</p>
            <button class="btn-primary-action" @click="router.push(isRechargeOrder ? '/profile?tab=recharge' : '/profile?tab=orders')">
              {{ isRechargeOrder ? $t('recharge.title') : $t('usdtPay.viewOrder') }}
            </button>
          </div>

          <!-- Failed / Expired State -->
          <div class="result-state" v-else-if="displayStatus === 4">
            <div class="result-icon result-failed">✕</div>
            <h4 class="result-title">{{ $t('usdtPay.orderCancelled') }}</h4>
            <p class="result-desc">{{ $t('usdtPay.backToProductsTip') }}</p>
            <button class="btn-primary-action" @click="router.push(isRechargeOrder ? '/recharge' : '/products')">
              {{ isRechargeOrder ? $t('recharge.title') : $t('usdtPay.backToProducts') }}
            </button>
          </div>

          <p class="security-note">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            {{ $t('payment.secureEncrypted') || 'Payments are secure and encrypted.' }}
          </p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { orderApi, Order } from '@/api/order'
import { paymentApi, UsdtPaymentOrder } from '@/api/payment'
import SiteHeader from '@/components/SiteHeader.vue'
import { useAuthStore } from '@/stores/auth'
import { navigateToLogin } from '@/utils/authNavigation'
import QRCode from 'qrcode'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { locale, t } = useI18n({ useScope: 'global' })

const order = ref<Order | null>(null)
const recharge = ref<any>(null)
const loading = ref(true)
const countdown = ref(0)
const usdtAddress = ref('TNnJePpZySFmLyd3U8WmHKbvymU6zc3Ngv')
const usdtAmount = ref('0.0000')
const qrCanvas = ref<HTMLCanvasElement | null>(null)
const checking = ref(false)
const cancelling = ref(false)
const paymentOrder = ref<UsdtPaymentOrder | null>(null)
const orderCreateTime = ref(0)
const copiedUsdt = ref(false)
let timer: any = null

// 获取本地化产品名称
function getLocalizedProductName(item: any): string {
  if (!item) return ''
  // 续费订单直接返回 productName
  if (item.itemType === 'renewal') {
    return item.productName || ''
  }
  // 根据语言设置返回对应名称
  if (locale.value === 'en' && item.nameEn) {
    return item.nameEn
  }
  return item.productName || ''
}

// 判断是充值订单还是购买订单
const isRechargeOrder = computed(() => {
  return recharge.value !== null
})

// 获取金额 - 根据订单类型
const displayAmount = computed(() => {
  if (recharge.value?.amount) {
    return recharge.value.amount
  }
  return order.value?.totalAmount || 0
})

// 获取订单号
const displayOrderNo = computed(() => {
  return recharge.value?.rechargeNo || order.value?.orderNo || ''
})

// 三方支付商品名：与产品中心保持一致，续费订单在名称后追加"续费"
const displayProductName = computed(() => {
  if (recharge.value) {
    const baseName = recharge.value.productName || recharge.value.rechargeNo || t('recharge.title')
    if (recharge.value.type === 'RENEWAL') {
      return String(baseName).includes('续费') ? String(baseName) : `${baseName}续费`
    }
    return baseName
  }

  if (order.value?.items?.length) {
    const productItem = order.value.items.find((item: any) => item.itemType === 'product') || order.value.items[0]
    // 根据语言设置返回对应名称
    if (locale.value === 'en' && productItem?.nameEn) {
      return productItem.nameEn
    }
    return productItem?.productName || order.value.orderNo || t('payment.esimPackage')
  }

  return order.value?.orderNo || t('payment.esimPackage')
})

const currentProductItem = computed(() => {
  if (!order.value?.items?.length) return null
  return order.value.items.find((item: any) => item.itemType === 'product') || order.value.items[0]
})

const displayValidityText = computed(() => {
  const days = currentProductItem.value?.validityDays
  if (!days) return ''
  if (days % 365 === 0) return `${days / 365} ${t('phoneNumbers.year') || '年'}`
  if (days % 30 === 0) return `${days / 30} ${t('phoneNumbers.month') || '个月'}`
  return `${days} ${t('footer.days') || '天'}`
})

// 获取状态
const displayStatus = computed(() => {
  if (recharge.value) {
    // 充值订单状态: PENDING=0, PAID=1, FAILED=4
    const statusMap: Record<string, number> = {
      'PENDING': 0,
      'PAID': 1,
      'FAILED': 4
    }
    return statusMap[recharge.value.status] ?? recharge.value.status
  }
  return order.value?.status
})

// USDT金额 - 从支付订单获取或计算
const displayUsdtAmount = computed(() => {
  if (paymentOrder.value?.usdtAmount) {
    return paymentOrder.value.usdtAmount.toString()
  }
  return (displayAmount.value * 1.0).toString()
})

// 生成二维码内容 - 只包含收款地址
const qrContent = computed(() => {
  if (!usdtAddress.value) return ''
  return usdtAddress.value
})

// 生成二维码
async function generateQRCode() {
  await nextTick()
  if (!qrCanvas.value || !qrContent.value) return
  try {
    await QRCode.toCanvas(qrCanvas.value, qrContent.value, {
      width: 120,
      margin: 1,
      color: {
        dark: '#1E293B',
        light: '#ffffff'
      }
    })
  } catch (err) {
    console.error('QR Code generation failed:', err)
  }
}

function formatTime(seconds: number): string {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

function copyAddress() {
  navigator.clipboard.writeText(usdtAddress.value)
  ElMessage.success(t('usdtPay.addressCopied'))
}

function copyUsdtAmount() {
  navigator.clipboard.writeText(displayUsdtAmount.value)
  copiedUsdt.value = true
  ElMessage.success(t('usdtPay.amountCopied'))
  setTimeout(() => {
    copiedUsdt.value = false
  }, 2000)
}

// 返回上一页
function goBack() {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    // 如果没有历史记录，默认跳转到首页或充值页
    router.push(isRechargeOrder.value ? '/recharge' : '/')
  }
}

async function fetchOrder() {
  // 检查登录状态
  if (!authStore.isLoggedIn) {
    ElMessage.warning(t('usdtPay.pleaseLogin'))
    navigateToLogin(router, route)
    return
  }

  const idParam = route.params.id as string
  
  // 尝试作为充值订单号获取 (充值订单号格式: RC20260xxx)
  if (idParam.startsWith('RC')) {
    try {
      const res: any = await fetch(`/api/recharges/rechargeNo/${idParam}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }).then(r => r.json())
      
      if (res.recharge) {
        recharge.value = res.recharge
        // 获取充值订单的 USDT 支付信息
        await fetchUsdtPaymentByOrderNo(idParam)
        loading.value = false
        return
      }
    } catch (e) {
      console.error('Failed to fetch recharge:', e)
    }
  }
  
  // 尝试作为数字ID获取订单
  const id = Number(idParam)
  if (!isNaN(id)) {
    try {
      const res: any = await orderApi.getOrderById(id)
      if (res.code === 200) {
        order.value = res.data
        // 获取订单的 USDT 支付信息
        await fetchUsdtPaymentByOrderNo(res.data.orderNo)
        loading.value = false
        return
      }
    } catch (error: any) {
      console.error('Failed to get order', error)
      if (error.code === 401 || error.response?.status === 401) {
        ElMessage.error(t('usdtPay.loginExpired'))
        authStore.logout()
        navigateToLogin(router, route)
      }
    }
  }
  
  loading.value = false
}

// 获取 USDT 支付订单信息
async function fetchUsdtPaymentByOrderNo(orderNo: string) {
  try {
    // 【优先】从订单对象读取支付信息（新设计：下单时已同步创建并持久化到 DB）
    if (order.value?.usdtAddress && order.value?.usdtAmount) {
      paymentOrder.value = {
        success: true,
        usdtAmount: order.value.usdtAmount,
        usdtAddress: order.value.usdtAddress,
        status: order.value.paymentStatus || 'PENDING'
      }
      usdtAddress.value = order.value.usdtAddress
      usdtAmount.value = String(order.value.usdtAmount)
      orderCreateTime.value = (order.value as any).createTime || Date.now()
      if (order.value.paymentExpireTime) {
        countdown.value = Math.max(0, Math.floor((Number(order.value.paymentExpireTime) - Date.now()) / 1000))
      }
      return
    }

    // 旧流程 / 充值订单：通过 API 查询支付状态
    const res: any = await paymentApi.queryPaymentByOrderNo(orderNo)
    if (res.success) {
      paymentOrder.value = res
      usdtAddress.value = res.usdtAddress || ''
      usdtAmount.value = res.usdtAmount?.toString() || '0'
      orderCreateTime.value = res.createTime || Date.now()
      // 使用第三方返回的过期时间计算倒计时
      if (res.expireTime) {
        countdown.value = Math.max(0, Math.floor((res.expireTime - Date.now()) / 1000))
      }
    } else {
      // 如果没有找到支付订单，检查订单是否已过期
      const orderCreateTime = order.value?.createTime || recharge.value?.createTime
      const orderExpired = orderCreateTime && (Date.now() > orderCreateTime + 20 * 60 * 1000)
      
      if (orderExpired) {
        // 订单已过期，显示已过期状态
        console.log('Order has expired, cannot create new payment')
        countdown.value = 0
      } else {
        // 订单未过期，自动创建一个
        console.log('Payment order not found, creating new one...')
        await createUsdtPayment(orderNo, displayAmount.value, displayProductName.value)
      }
    }
  } catch (e) {
    console.error('Failed to fetch USDT payment info:', e)
  }
}

// 创建 USDT 支付订单
async function createUsdtPayment(orderNo: string, amount: number, productName: string) {
  try {
    const res: any = await paymentApi.createUsdtPayment({
      orderNo,
      amount,
      productName,
      remark: ''
    })
    if (res.success) {
      paymentOrder.value = res
      usdtAddress.value = res.usdtAddress || ''
      usdtAmount.value = res.usdtAmount?.toString() || '0'
      orderCreateTime.value = res.createTime || Date.now()
      // 使用第三方返回的过期时间计算倒计时
      if (res.expireTime) {
        countdown.value = Math.max(0, Math.floor((res.expireTime - Date.now()) / 1000))
      }
    }
  } catch (e) {
    console.error('Failed to create USDT payment:', e)
  }
}

// 监听订单变化和地址变化，生成二维码
watch([order, recharge, qrCanvas, usdtAddress], () => {
  if ((order.value || recharge.value) && qrCanvas.value) {
    generateQRCode()
  }
}, { immediate: true })

// 轮询支付状态
async function pollPaymentStatus() {
  try {
    const orderNo = displayOrderNo.value
    if (!orderNo) return
    
    const res: any = await paymentApi.queryPaymentByOrderNo(orderNo)
    if (res.success) {
      console.log('【轮询】支付状态查询结果:', {
        orderNo,
        status: res.status,
        timestamp: new Date().toISOString()
      })
      
      // 检测到支付成功
      if (res.status === 'PAID') {
        // 支付成功，停止定时器
        if (timer) {
          clearInterval(timer)
          timer = null
        }
        
        console.log('【轮询】检测到支付成功，准备跳转:', { orderNo })
        ElMessage.success(t('usdtPay.paySuccess') || '支付成功')
        
        // 等待 2 秒后再跳转，确保用户能看到成功提示
        setTimeout(() => {
          // 跳转到个人中心
          // 注意：type === 'RECHARGE' 才跳转到充值标签页，'RENEWAL' 跳转到订单标签页
          if (recharge.value?.type === 'RECHARGE') {
            router.push('/profile?tab=recharge')
          } else {
            router.push('/profile?tab=orders')
          }
        }, 2000)
      }
    }
  } catch (e) {
    console.error('【轮询】支付状态查询失败:', e)
  }
}

async function checkPayment() {
  checking.value = true
  
  try {
    let currentStatus = null
    
    if (isRechargeOrder.value) {
      // 充值订单 - 查询状态
      const rechargeNo = recharge.value.rechargeNo
      const res: any = await fetch(`/api/recharges/rechargeNo/${rechargeNo}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }).then(r => r.json())
      
      if (res.recharge) {
        currentStatus = res.recharge.status
        recharge.value = res.recharge
      }
    } else {
      // 普通订单 - 查询状态
      const id = Number(route.params.id)
      const res: any = await orderApi.getOrderById(id)
      if (res.code === 200) {
        currentStatus = res.data.status
        order.value = res.data
      }
    }
    
    // 根据查询结果进行处理
    if (isRechargeOrder.value) {
      // 充值/续费订单状态
      if (currentStatus === 'PAID' || currentStatus === 1) {
        // 已支付，根据类型跳转到对应页面
        ElMessage.success(t('usdtPay.paymentDetected'))
        await new Promise(resolve => setTimeout(resolve, 1000))
        // type === 'RECHARGE' 跳转到充值页，'RENEWAL' 跳转到订单页
        if (recharge.value?.type === 'RECHARGE') {
          router.push('/profile?tab=recharge')
        } else {
          router.push('/profile?tab=orders')
        }
      } else if (currentStatus === 'PENDING' || currentStatus === 0) {
        // 未支付，提示延迟
        ElMessage.warning(t('usdtPay.paymentDelayed'))
      } else {
        // 其他状态
        ElMessage.error(t('usdtPay.checkFailed'))
      }
    } else {
      // 普通订单状态：0=待支付, 1=已支付, 2=已发货, 4=已取消
      if (currentStatus === 1) {
        // 已支付，跳转到个人中心订单页
        ElMessage.success(t('usdtPay.paymentDetected'))
        await new Promise(resolve => setTimeout(resolve, 1000))
        router.push('/profile?tab=orders')
      } else if (currentStatus === 0) {
        // 未支付，提示延迟
        ElMessage.warning(t('usdtPay.paymentDelayed'))
      } else {
        // 其他状态
        ElMessage.error(t('usdtPay.checkFailed'))
      }
    }
  } catch (error: any) {
    console.error('查询支付状态失败:', error)
    ElMessage.error(error.message || t('usdtPay.checkFailed'))
  } finally {
    checking.value = false
  }
}

async function cancelOrder() {
  cancelling.value = true
  
  try {
    if (isRechargeOrder.value) {
      // 充值订单取消
      ElMessage.info('充值订单无法取消')
    } else {
      // 普通订单取消
      const id = Number(route.params.id)
      const res: any = await orderApi.cancelOrder(id)
      if (res.code === 200) {
        order.value = res.data
        ElMessage.success(t('usdtPay.cancelSuccess'))
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || t('usdtPay.cancelFailed'))
  } finally {
    cancelling.value = false
  }
}

onMounted(async () => {
  await fetchOrder()
  
  // 启动轮询定时器
  console.log('【支付页面】启动轮询定时器 - 轮询间隔: 1秒', {
    orderNo: displayOrderNo.value,
    timestamp: new Date().toISOString()
  })
  
  timer = setInterval(async () => {
    // 只有当倒计时已设置（大于0）时才递减
    if (countdown.value > 0 && displayStatus.value === 0) {
      countdown.value--
    }
    // 轮询检查支付状态
    if (displayStatus.value === 0) {
      await pollPaymentStatus()
    }
    // 倒计时结束且未支付，自动取消订单
    if (countdown.value === 0 && displayStatus.value === 0 && order.value?.createTime) {
      handleExpired()
    }
  }, 1000)
})

// 订单过期处理
async function handleExpired() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  
  try {
    if (isRechargeOrder.value) {
      // 充值订单已过期，刷新状态
      recharge.value = { ...recharge.value, status: 'FAILED' }
    } else {
      // 普通订单自动取消
      const id = Number(route.params.id)
      if (id && !isNaN(id)) {
        await orderApi.cancelOrder(id)
      }
      order.value = { ...order.value, status: 4 }
    }
  } catch (error) {
    console.error('Order expired handling failed:', error)
  }
}

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    console.log('【支付页面】清理轮询定时器', {
      orderNo: displayOrderNo.value,
      timestamp: new Date().toISOString()
    })
  }
})
</script>

<style scoped>
/* 设计变量挂在本页根节点（scoped 内 :root 不生效） */
.checkout-page {
  --ds-bg:                  #e8f1fa;
  --ds-surface:             #ffffff;
  --ds-surface-low:         #f0f4f9;
  --ds-surface-high:        #e2e8f0;
  --ds-on-surface:          #1a1b1f;
  --ds-on-surface-variant:  #414753;
  --ds-primary:             #0059b5;
  --ds-primary-container:   #0071e3;
  --ds-on-primary:          #ffffff;
  --ds-outline-variant:     #c1c6d6;
}

/* ── Page Shell ─────────────────────────────────────────────────────── */
.checkout-page {
  min-height: 100vh;
  background: var(--ds-bg);
  font-family: 'Manrope', 'Inter', system-ui, sans-serif;
  color: var(--ds-on-surface);
  display: flex;
  flex-direction: column;
}

/* ── Main Content ────────────────────────────────────────────────────── */
.checkout-main {
  flex: 1;
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 40px 24px 64px;
  background: transparent;
}

/* ── Header area (back link + title) ────────────────────────────────── */
.checkout-header-area {
  margin-bottom: 32px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: var(--ds-on-surface-variant);
  text-decoration: none;
  transition: color 0.2s;
  margin-bottom: 16px;
}
.back-link:hover { color: var(--ds-primary); }

.checkout-heading {
  font-family: 'Space Grotesk', 'Manrope', sans-serif;
  font-size: 32px;
  font-weight: 700;
  line-height: 1.25;
  letter-spacing: -0.01em;
  color: var(--ds-on-surface);
  margin: 0 0 8px;
}

.checkout-subheading {
  font-size: 18px;
  line-height: 1.55;
  color: var(--ds-on-surface-variant);
  margin: 0;
}

/* ── Two-Column Grid ────────────────────────────────────────────────── */
.checkout-grid {
  display: grid;
  grid-template-columns: 5fr 7fr;
  gap: 24px;
  align-items: start;
}

/* ── Card Base ──────────────────────────────────────────────────────── */
.checkout-card {
  background: var(--ds-surface);
  border-radius: 16px;
  padding: 28px 32px;
  box-shadow: 0 4px 16px rgba(0, 60, 120, 0.08);
  border: 1px solid var(--ds-outline-variant);
}

.order-card {
  position: sticky;
  top: 88px;
  border-top: 3px solid var(--ds-primary);
}

.payment-card {
  border-top: 3px solid #26a17b;
}

/* ── Card Section Title ─────────────────────────────────────────────── */
.card-section-title {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 24px;
  font-weight: 600;
  color: var(--ds-on-surface);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
}
.card-section-title svg { color: var(--ds-on-surface-variant); flex-shrink: 0; }

/* ── Status Banner ──────────────────────────────────────────────────── */
.status-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 16px;
}
.status-banner.status-pending {
  background: #eff6ff;
  color: #1d4ed8;
}
.status-banner.status-pending strong {
  font-size: 17px;
  font-weight: 700;
  margin-left: auto;
}
.status-banner.status-success {
  background: #f0fdf4;
  color: #15803d;
}
.status-banner.status-expired {
  background: #fef2f2;
  color: #b91c1c;
}

/* ── Product Summary Card ───────────────────────────────────────────── */
.product-summary-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--ds-surface-low);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 24px;
  border: 1px solid var(--ds-outline-variant);
}

.product-badge {
  width: 64px;
  height: 48px;
  background: #dbeafe;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: var(--ds-primary);
  flex-shrink: 0;
  overflow: hidden;
}

.product-summary-info {
  flex: 1;
  min-width: 0;
}
.product-summary-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--ds-on-surface);
  margin: 0 0 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.product-summary-meta {
  font-size: 12px;
  color: var(--ds-on-surface-variant);
  margin: 0;
}

.product-summary-price {
  font-size: 18px;
  font-weight: 600;
  color: var(--ds-on-surface);
  flex-shrink: 0;
}

/* ── Price Breakdown ────────────────────────────────────────────────── */
.price-breakdown {
  border-top: 1px solid var(--ds-outline-variant);
  padding-top: 16px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: var(--ds-on-surface-variant);
  padding: 8px 12px;
  background: var(--ds-surface-low);
  border-radius: 8px;
  border: 1px solid color-mix(in srgb, var(--ds-outline-variant) 60%, transparent);
}

.price-label { font-weight: 400; }

.price-value {
  font-weight: 500;
  color: var(--ds-on-surface);
}

.order-no-value {
  font-family: monospace;
  font-size: 12px;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: right;
}

.network-badge {
  background: #ecfeff;
  color: #0e7490;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 9999px;
}

/* ── Price Total ────────────────────────────────────────────────────── */
.price-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(193, 198, 214, 0.3);
  font-family: 'Space Grotesk', sans-serif;
  font-size: 24px;
  font-weight: 600;
  color: var(--ds-on-surface);
  margin-bottom: 20px;
}
.price-total-value {
  color: var(--ds-primary);
  font-weight: 700;
}
.price-total-value em {
  font-style: normal;
  font-size: 16px;
  font-weight: 500;
  margin-left: 4px;
  color: var(--ds-on-surface-variant);
}

/* ── QR Code ────────────────────────────────────────────────────────── */
.qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  background: var(--ds-surface-low);
  padding: 16px;
  border-radius: 10px;
  border: 1px solid var(--ds-outline-variant);
}

.qr-wrapper {
  padding: 8px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid var(--ds-outline-variant);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.qr-canvas { display: block; }

.qr-label {
  font-size: 12px;
  color: var(--ds-on-surface-variant);
}

/* ── Notice Banner ──────────────────────────────────────────────────── */
.notice-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: color-mix(in srgb, #26a17b 8%, #fff);
  border: 1px solid color-mix(in srgb, #26a17b 22%, transparent);
  border-radius: 10px;
  padding: 14px 18px;
  margin-bottom: 24px;
}
.notice-gem { font-size: 22px; flex-shrink: 0; }
.notice-banner p {
  margin: 0;
  font-size: 14px;
  color: var(--ds-on-surface-variant);
  line-height: 1.6;
}

/* ── Amount Highlight ───────────────────────────────────────────────── */
.amount-highlight {
  background: linear-gradient(135deg, rgba(0, 89, 181, 0.05), rgba(0, 113, 227, 0.07));
  border: 2px solid var(--ds-primary);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  text-align: center;
}

.amount-highlight-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--ds-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.6px;
  margin: 0 0 8px;
}

.amount-highlight-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  flex-wrap: wrap;
  gap: 6px;
  cursor: pointer;
  padding: 10px;
  border-radius: 8px;
  transition: background 0.2s;
  user-select: none;
}
.amount-highlight-value:hover { background: rgba(0, 89, 181, 0.08); }

.amount-highlight-number {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 48px;
  font-weight: 800;
  color: var(--ds-primary);
  letter-spacing: -1.5px;
  line-height: 1;
}

.amount-highlight-unit {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 22px;
  font-weight: 700;
  color: var(--ds-primary);
  align-self: flex-end;
  margin-bottom: 4px;
}

.copy-chip {
  font-size: 11px;
  font-weight: 500;
  color: var(--ds-on-surface-variant);
  background: var(--ds-surface-low);
  border: 1px solid var(--ds-outline-variant);
  padding: 3px 8px;
  border-radius: 9999px;
  white-space: nowrap;
  transition: all 0.2s;
}
.copy-chip.copied {
  background: #f0fdf4;
  color: #15803d;
  border-color: #bbf7d0;
}

.amount-highlight-note {
  font-size: 12px;
  color: var(--ds-primary);
  font-weight: 500;
  margin: 10px 0 0;
}

/* ── Address Block ──────────────────────────────────────────────────── */
.address-block {
  margin-bottom: 24px;
}
.address-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--ds-on-surface);
  margin-bottom: 8px;
  font-family: 'Space Grotesk', sans-serif;
}

.address-display {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--ds-surface-low);
  border: 1px solid var(--ds-outline-variant);
  border-radius: 8px;
  padding: 12px 16px;
}

.address-text {
  flex: 1;
  font-size: 12px;
  font-family: 'Courier New', monospace;
  color: var(--ds-on-surface);
  word-break: break-all;
  line-height: 1.5;
}

.copy-address-btn {
  flex-shrink: 0;
  background: var(--ds-primary-container);
  color: var(--ds-on-primary);
  border: none;
  border-radius: 6px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
  font-family: inherit;
}
.copy-address-btn:hover { opacity: 0.88; }

/* ── Tips Block ─────────────────────────────────────────────────────── */
.tips-block {
  background: color-mix(in srgb, var(--ds-primary) 5%, #fff);
  border: 1px solid color-mix(in srgb, var(--ds-primary) 15%, transparent);
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 24px;
}

.tips-heading {
  font-size: 14px;
  font-weight: 700;
  color: var(--ds-primary);
  margin: 0 0 10px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tips-list li {
  font-size: 13px;
  color: var(--ds-on-surface-variant);
  line-height: 1.7;
}

.tips-list li strong {
  color: var(--ds-primary);
  font-weight: 700;
}

/* ── Action Buttons ─────────────────────────────────────────────────── */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.btn-primary-action {
  width: 100%;
  background: var(--ds-primary-container);
  color: var(--ds-on-primary);
  border: none;
  border-radius: 8px;
  padding: 16px 24px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: opacity 0.2s;
  font-family: inherit;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.btn-primary-action:hover:not(:disabled) { opacity: 0.9; }
.btn-primary-action:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-secondary-action {
  width: 100%;
  background: var(--ds-surface);
  color: var(--ds-on-surface);
  border: 1px solid var(--ds-outline-variant);
  border-radius: 8px;
  padding: 14px 24px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  font-family: inherit;
}
.btn-secondary-action:hover:not(:disabled) { background: var(--ds-surface-low); }
.btn-secondary-action:disabled { opacity: 0.5; cursor: not-allowed; }

/* ── Result / Status States ─────────────────────────────────────────── */
.result-state {
  text-align: center;
  padding: 32px 0 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.result-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
}
.result-success { background: #16a34a; }
.result-failed  { background: #dc2626; }

.result-title {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 22px;
  font-weight: 600;
  color: var(--ds-on-surface);
  margin: 0;
}

.result-desc {
  font-size: 14px;
  color: var(--ds-on-surface-variant);
  margin: 0;
  line-height: 1.5;
}

/* ── Security Note ──────────────────────────────────────────────────── */
.security-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;
  color: var(--ds-on-surface-variant);
  margin: 16px 0 0;
  text-align: center;
}

/* ── Footer ─────────────────────────────────────────────────────────── */
.checkout-footer {
  background: var(--surface-container);
  border-top: 1px solid rgba(193, 198, 214, 0.3);
  padding: 32px 24px;
  text-align: center;
}

.footer-copy {
  font-size: 12px;
  color: var(--ds-on-surface-variant);
  margin: 0 0 8px;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 16px;
}
.footer-links a {
  font-size: 12px;
  color: var(--ds-primary);
  text-decoration: none;
}
.footer-links a:hover { text-decoration: underline; }

/* ── Responsive ─────────────────────────────────────────────────────── */
@media (max-width: 1024px) {
  .checkout-grid {
    grid-template-columns: 1fr;
  }
  .order-card {
    position: static;
  }
}

@media (max-width: 640px) {
  .checkout-main {
    padding: 32px 16px;
  }

  .checkout-card {
    padding: 20px 16px;
  }

  .checkout-heading {
    font-size: 24px;
  }

  .checkout-subheading {
    font-size: 15px;
  }

  .amount-highlight-number {
    font-size: 36px;
  }

  .amount-highlight-unit {
    font-size: 18px;
  }
}
</style>
