<template>
  <div class="confirm-page">
    <SiteHeader />

    <div class="container main-content">
      <div class="page-header">
        <el-button @click="router.back()" :icon="ArrowLeft">{{ $t('common.back') }}</el-button>
        <h1>{{ $t('orderConfirm.title') }}</h1>
      </div>

      <div class="confirm-layout">
        <!-- 左侧：订单详情 -->
        <div class="confirm-main">
          <!-- 产品信息 -->
          <div class="order-product-card" v-if="orderData.type === 'PURCHASE'">
            <div class="product-flag">{{ getFlagEmoji(orderData.product?.country) }}</div>
            <div class="product-info">
              <h3>{{ orderData.product?.country }} {{ orderData.product?.name }}</h3>
              <p class="product-desc">{{ orderData.product?.description }}</p>
              <div class="product-features">
                <el-tag v-for="f in parseJson(orderData.product?.features)" :key="f" size="small">{{ f }}</el-tag>
                <el-tag :type="orderData.product?.productType === 1 ? 'success' : 'warning'" size="small">
                  {{ orderData.product?.productType === 1 ? $t('profile.productTypePeriodic') : $t('profile.productTypeUnlimited') }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 订单详情卡片 -->
          <div class="detail-card">
            <h3>{{ $t('orderConfirm.orderDetails') }}</h3>
            
            <div class="detail-item">
              <span class="label">{{ $t('orderConfirm.orderType') }}</span>
              <span class="value">
                <el-tag :type="getOrderTypeTag()">
                  {{ getOrderTypeText() }}
                </el-tag>
              </span>
            </div>

            <template v-if="orderData.type === 'PURCHASE'">
              <div class="detail-item">
                <span class="label">{{ $t('orderConfirm.esimOption') }}</span>
                <span class="value">{{ orderData.esimOption === 'esim' ? $t('checkout.esimPhone') : $t('checkout.noNeed') }}</span>
              </div>
              
              <div class="detail-item">
                <span class="label">{{ $t('orderConfirm.device') }}</span>
                <span class="value">{{ getDeviceName() }}</span>
              </div>

              <div class="detail-item" v-if="orderData.selectedDevice">
                <span class="label">{{ $t('orderConfirm.shipping') }}</span>
                <span class="value">
                  <div v-if="orderData.addressForm">
                    <div>{{ orderData.addressForm.name }}</div>
                    <div class="address-detail">{{ orderData.addressForm.phone }}</div>
                    <div class="address-detail">{{ orderData.addressForm.address }}</div>
                  </div>
                  <span v-else>-</span>
                </span>
              </div>

              <div class="detail-item detail-item--block">
                <span class="label">{{ $t('checkout.orderRemark') }} <span class="optional-hint">{{ $t('checkout.optional') }}</span></span>
                <el-input
                  v-model="customerRemark"
                  type="textarea"
                  :rows="2"
                  maxlength="500"
                  show-word-limit
                  :placeholder="$t('checkout.orderRemarkPlaceholder')"
                />
              </div>
            </template>

            <div class="detail-item">
              <span class="label">{{ $t('orderConfirm.paymentMethod') }}</span>
              <span class="value">
                <div class="payment-method-display">
                  <el-icon><component :is="getPaymentIcon(orderData.paymentMethod)" /></el-icon>
                  <span>{{ getPaymentLabel(orderData.paymentMethod) }}</span>
                </div>
              </span>
            </div>
          </div>

          <!-- 支付提示 -->
          <div class="payment-tip">
            <el-icon><InfoFilled /></el-icon>
            <p v-if="orderData.paymentMethod === 'usdt'">{{ $t('orderConfirm.usdtTip') }}</p>
            <p v-else-if="orderData.paymentMethod === 'wechat'">{{ $t('orderConfirm.wechatTip') }}</p>
            <p v-else-if="orderData.paymentMethod === 'alipay'">{{ $t('orderConfirm.alipayTip') }}</p>
            <p v-else-if="orderData.paymentMethod === 'bankcard'">{{ $t('orderConfirm.bankCardTip') }}</p>
            <p v-else-if="orderData.paymentMethod === 'balance'">{{ $t('orderConfirm.balanceTip') }}</p>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button size="large" :disabled="submitting" @click="handleCancel">
              {{ $t('orderConfirm.cancel') }}
            </el-button>
            <el-button type="primary" size="large" :disabled="submitting" :loading="submitting" @click="handleConfirm">
              {{ submitting ? $t('checkout.processing') : $t('orderConfirm.confirm') }}
            </el-button>
          </div>
        </div>

        <!-- 右侧：价格汇总 -->
        <div class="confirm-summary">
          <div class="summary-card">
            <h3>{{ $t('orderConfirm.orderSettlement') }}</h3>

            <div class="summary-product" v-if="orderData.type === 'PURCHASE'">
              <div class="product-name">{{ orderData.product?.name }}</div>
              <div class="product-meta">{{ getFlagEmoji(orderData.product?.country) }} {{ orderData.product?.country }}</div>
            </div>

            <div class="summary-lines">
              <div class="summary-line">
                <span>{{ $t('orderConfirm.packageFee') }}</span>
                <span class="amount">${{ orderData.product?.monthlyPrice?.toFixed(2) || '0.00' }}</span>
              </div>
              <template v-if="orderData.selectedDevice && selectedDeviceInfo">
                <div class="summary-line">
                  <span>{{ locale === 'zh' ? selectedDeviceInfo.name : selectedDeviceInfo.nameEn }}</span>
                  <span class="amount">${{ selectedDeviceInfo.price.toFixed(2) }}</span>
                </div>
                <div class="summary-line">
                  <span>{{ $t('checkout.shippingCost') }}</span>
                  <span class="amount">${{ Number(siteConfig.defaultShippingFee ?? 0).toFixed(2) }}</span>
                </div>
              </template>
              <div class="summary-line" v-if="appliedDiscount > 0">
                <span>{{ $t('checkout.couponDiscount') }}</span>
                <span class="amount discount">-${{ appliedDiscount.toFixed(2) }}</span>
              </div>
            </div>

            <div class="coupon-box">
              <div class="coupon-label">{{ $t('checkout.couponCode') }}</div>
              <div class="coupon-row">
                <el-input
                  v-model="couponInput"
                  :placeholder="$t('checkout.couponPlaceholder')"
                  clearable
                  @keyup.enter="applyCoupon"
                />
                <el-button :loading="couponLoading" @click="applyCoupon">{{ $t('checkout.couponApply') }}</el-button>
              </div>
              <div class="coupon-tip" v-if="couponMessage" :class="{ error: !couponApplied }">{{ couponMessage }}</div>
            </div>

            <div class="summary-total">
              <span>{{ $t('orderConfirm.total') }}</span>
              <span class="total-price">${{ payablePrice.toFixed(2) }} USD</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import SiteHeader from '@/components/SiteHeader.vue'
import { orderApi } from '@/api/order'
import { validateCoupon } from '@/api/coupon'
import { resolveCouponTip, resolveCouponTipOrNull } from '@/utils/couponMessage'
import { paymentApi } from '@/api/payment'
import { phoneNumberApi, Device } from '@/api/phoneNumber'
import { useI18n } from 'vue-i18n'
import { getCurrentLocale } from '@/i18n'
import { useSiteConfigStore } from '@/stores/siteConfig'
import { navigateToLogin } from '@/utils/authNavigation'
import { navigateByPaymentMethod } from '@/utils/stripeCheckout'
import { Wallet, CreditCard, Message, WalletFilled, Money } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const siteConfig = useSiteConfigStore()
const { locale, t } = useI18n({ useScope: 'global' })
const submitting = ref(false)

const orderData = ref<any>({
  type: '',
  product: null,
  esimOption: '',
  selectedDevice: '',
  totalAmount: 0,
  paymentMethod: '',
  addressForm: null
})

const couponInput = ref('')
const couponLoading = ref(false)
const couponApplied = ref(false)
const couponMessage = ref('')
const appliedCode = ref('')
const appliedDiscount = ref(0)
const customerRemark = ref('')

const payablePrice = computed(() =>
  Math.max(0, Number(((orderData.value.totalAmount || 0) - appliedDiscount.value).toFixed(2)))
)

async function applyCoupon() {
  const code = couponInput.value.trim()
  if (!code) {
    couponApplied.value = false
    appliedCode.value = ''
    appliedDiscount.value = 0
    couponMessage.value = ''
    return
  }
  couponLoading.value = true
  try {
    const res: any = await validateCoupon(code, orderData.value.totalAmount || 0, orderData.value.product?.id)
    const data = res.data || res
    if (res.code === 200 && data?.valid !== false) {
      couponApplied.value = true
      appliedCode.value = data.code || code.toUpperCase()
      appliedDiscount.value = Number(data.discountAmount || 0)
      couponMessage.value = t('checkout.couponAppliedTip', { amount: appliedDiscount.value.toFixed(2) })
    } else {
      couponApplied.value = false
      appliedDiscount.value = 0
      appliedCode.value = ''
      couponMessage.value = resolveCouponTip(t, {
        messageKey: data?.messageKey,
        messageParam: data?.messageParam,
        message: res.message || data?.message
      })
    }
  } catch (e: any) {
    couponApplied.value = false
    appliedDiscount.value = 0
    appliedCode.value = ''
    couponMessage.value = resolveCouponTip(t, {
      messageKey: e?.response?.data?.data?.messageKey,
      messageParam: e?.response?.data?.data?.messageParam,
      message: e?.message
    })
  } finally {
    couponLoading.value = false
  }
}

const devices = ref<Device[]>([])

// 默认设备列表（备用）
const defaultDevices: Device[] = [
  {
    id: 1,
    name: '免费SIM实体卡',
    nameEn: 'Free SIM Card',
    description: 'Tesim国行手机流量实体SIM卡,即时OTA订购全球流量,无电话号码',
    descriptionEn: 'Tesim SIM Card, instant OTA order for global data, no phone number',
    price: 0,
    isFree: true,
    deviceType: 'SIM_CARD',
    shippingFee: 0
  },
  {
    id: 2,
    name: '可编程SIM实体卡',
    nameEn: 'Programmable SIM Card',
    description: '可编程SIM实体卡，支持写入eSIM配置，电话号码eSIM必买',
    descriptionEn: 'Programmable SIM card, supports writing eSIM configuration, required for phone number eSIM',
    price: 3.99,
    isFree: false,
    deviceType: 'PROGRAMMABLE_SIM',
    shippingFee: 0
  },
  {
    id: 3,
    name: '写卡器',
    nameEn: 'SIM Card Reader/Writer',
    description: 'SIM卡写卡器配合可编程SIM实体卡使用，写eSIM二维码到实体卡',
    descriptionEn: 'SIM card reader/writer, used with programmable SIM card to write eSIM QR code to physical card',
    price: 4.99,
    isFree: false,
    deviceType: 'READER',
    shippingFee: 0
  }
]

const iconMap: Record<string, any> = {
  wechat: Message,
  alipay: Money,
  usdt: Wallet,
  bankcard: CreditCard,
  balance: WalletFilled
}

function getFlagEmoji(country: string): string {
  if (!country) return '🌐'
  const flagMap: Record<string, string> = {
    '新加坡': '🇸🇬', '美国': '🇺🇸', '英国': '🇬🇧', '日本': '🇯🇵',
    '韩国': '🇰🇷', '德国': '🇩🇪', '法国': '🇫🇷', '澳大利亚': '🇦🇺',
    '加拿大': '🇨🇦', '香港': '🇭🇰', '台湾': '🇹🇼', '中国': '🇨🇳'
  }
  return flagMap[country] || '🌐'
}

function parseJson(str: string): string[] {
  try {
    return JSON.parse(str)
  } catch {
    return []
  }
}

function getOrderTypeTag() {
  const typeMap: Record<string, string> = {
    PURCHASE: 'primary',
    RECHARGE: 'success',
    RENEWAL: 'warning'
  }
  return typeMap[orderData.value.type] || 'info'
}

function getOrderTypeText() {
  const typeMap: Record<string, string> = {
    PURCHASE: t('orderConfirm.purchase'),
    RECHARGE: t('recharge.balanceRecharge'),
    RENEWAL: t('recharge.renewal')
  }
  return typeMap[orderData.value.type] || orderData.value.type
}

function getDeviceName() {
  if (!orderData.value.selectedDevice) {
    return t('checkout.noDevice')
  }
  
  // 尝试用设备ID查找设备
  const device = devices.value.find(d => d.id === parseInt(orderData.value.selectedDevice) || d.id === orderData.value.selectedDevice)
  if (device) {
    return locale.value === 'zh' ? device.name : device.nameEn
  }
  
  // 回退到硬编码的映射（为了兼容旧数据）
  const deviceMap: Record<string, string> = {
    'free-sim': t('checkout.freeSim'),
    'programmable-sim': t('checkout.programmableSim'),
    'writer': t('checkout.simWriter')
  }
  return deviceMap[orderData.value.selectedDevice] || orderData.value.selectedDevice
}

// 获取选中的设备详细信息
function getSelectedDeviceInfo() {
  if (!orderData.value.selectedDevice) {
    return null
  }
  return devices.value.find(d => d.id === parseInt(orderData.value.selectedDevice) || d.id === orderData.value.selectedDevice)
}

const selectedDeviceInfo = ref<Device | null>(null)

function updateSelectedDeviceInfo() {
  selectedDeviceInfo.value = getSelectedDeviceInfo()
}

function getPaymentIcon(method: string) {
  return iconMap[method] || Wallet
}

function getPaymentLabel(method: string) {
  const labels: Record<string, string> = {
    wechat: t('payment.wechatPay'),
    alipay: t('payment.alipay'),
    usdt: t('payment.usdt'),
    bankcard: t('payment.bankCard'),
    balance: t('orderConfirm.balancePayment')
  }
  return labels[method] || method
}

function handleCancel() {
  router.back()
}

async function handleConfirm() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning(t('checkout.pleaseLogin'))
    navigateToLogin(router, route)
    return
  }

  const userEmail = authStore.user?.email || authStore.user?.username
  if (!userEmail) {
    ElMessage.warning(t('checkout.loginRequired'))
    authStore.logout()
    navigateToLogin(router, route)
    return
  }

  // 【防抖】防止重复提交
  if (submitting.value) {
    ElMessage.warning('订单正在处理中，请勿重复点击')
    return
  }
  submitting.value = true

  try {
    await siteConfig.ensureShippingFee()
    if (orderData.value.type === 'PURCHASE') {
      // 购买订单
      const orderPayload = {
        email: userEmail,
        remark: JSON.stringify({
          productId: orderData.value.product?.id,
          productName: orderData.value.product?.name,
          productCountry: orderData.value.product?.country,
          esimOption: orderData.value.esimOption,
          device: orderData.value.selectedDevice,
          totalAmount: orderData.value.totalAmount
        }),
        shippingAddress: orderData.value.selectedDevice ? JSON.stringify(orderData.value.addressForm) : null,
        paymentMethod: orderData.value.paymentMethod,
        language: getCurrentLocale(),
        productId: orderData.value.product?.id,
        totalAmount: orderData.value.totalAmount,
        couponCode: appliedCode.value || undefined,
        customerRemark: customerRemark.value.trim() || undefined
      }

      const res: any = await orderApi.createOrder(orderPayload)
      
      if (res.code === 200 || res.code === 201) {
        const order = res.data
        console.log('✅ Order created successfully:', order)
        
        // 获取有效的订单ID（优先使用 id，然后使用 orderNo）
        const orderId = order.id || order.orderNo
        console.log('🔑 Order ID:', orderId)
        
        if (!orderId) {
          console.error('❌ No valid order ID found in response:', order)
          ElMessage.error('订单创建成功但无法获取订单号，请联系客服')
          return
        }
        
        // 清除确认数据
        sessionStorage.removeItem('orderConfirmData')
        
        ElMessage.success(t('orderConfirm.orderCreated'))
        
        // 添加小延迟确保后端数据已持久化
        await new Promise(resolve => setTimeout(resolve, 300))
        
        // 跳转到支付页面
        console.log('🔄 Redirecting to payment page with method:', orderData.value.paymentMethod)
        switch (orderData.value.paymentMethod) {
          case 'usdt':
            // 创建 USDT 支付订单
            try {
              console.log('📱 Creating USDT payment for order:', order.orderNo)
              const paymentRes: any = await paymentApi.createUsdtPayment({
                orderNo: order.orderNo,
                amount: Number(order.finalAmount ?? payablePrice.value),
                productName: orderData.value.product?.name || '产品购买',
                remark: `购买 ${orderData.value.product?.country || ''} ${orderData.value.product?.name || ''}`
              })
              console.log('✅ USDT payment created:', paymentRes)
              if (paymentRes.success) {
                console.log('USDT payment created:', paymentRes)
              }
            } catch (e) {
              console.error('❌ Failed to create USDT payment:', e)
              // 即使 USDT 支付创建失败，仍然跳转，因为订单已创建
            }
            console.log('🔀 Navigating to /checkout/usdt/' + orderId)
            router.push('/checkout/usdt/' + orderId)
            break
          case 'wechat':
          case 'alipay':
          case 'bankcard':
          case 'antom_alipay':
            await navigateByPaymentMethod({
              paymentMethod: orderData.value.paymentMethod,
              orderId,
              orderNo: order?.orderNo || orderId,
              email: authStore.user?.email,
              router,
            })
            break
          case 'balance':
            console.log('🔀 Navigating to /checkout/balance/' + orderId)
            router.push('/checkout/balance/' + orderId)
            break
          default:
            console.warn('⚠️ Unknown payment method:', orderData.value.paymentMethod)
            router.push('/profile')
        }
      } else {
        ElMessage.error(resolveCouponTipOrNull(t, { message: res.message }) || res.message || t('orderConfirm.createFailed'))
      }
    } else if (orderData.value.type === 'RECHARGE' || orderData.value.type === 'RENEWAL') {
      // 充值/续费订单
      // 复用PaymentConfirm的逻辑
      const { createRecharge } = await import('@/api/recharge')
      
      const requestData: any = {
        type: orderData.value.type,
        amount: orderData.value.totalAmount,
        paymentMethod: orderData.value.paymentMethod.toUpperCase()
      }

      if (orderData.value.type === 'RENEWAL') {
        requestData.orderId = orderData.value.orderId
        requestData.productName = orderData.value.productName
        requestData.periodValue = orderData.value.periodValue?.toString()
        requestData.periodUnit = orderData.value.periodUnit
        requestData.remarks = orderData.value.remarks
      }

      createRecharge(requestData)
        .then(() => {
          ElMessage.success(t('orderConfirm.orderCreated'))
          sessionStorage.removeItem('orderConfirmData')
          router.push('/recharge')
        })
        .catch(() => {
          ElMessage.error(t('orderConfirm.createFailed'))
        })
    }
  } catch (error: any) {
    console.error('Order creation failed:', error)
    if (error.response?.status === 401) {
      ElMessageBox.confirm(t('orders.loginExpired'), t('common.notice'), {
        confirmButtonText: t('orders.loginRequired'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }).then(() => {
        authStore.logout()
        navigateToLogin(router, route)
      })
    } else {
      ElMessage.error(resolveCouponTipOrNull(t, { message: error.message }) || error.message || t('orderConfirm.createFailed'))
    }
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  // 加载设备列表
  try {
    console.log('开始加载设备列表...')
    const res: any = await phoneNumberApi.getDevices()
    console.log('设备列表API响应:', res)
    
    if (res && res.data) {
      // 数据可能直接是数组或在data字段中
      const deviceData = Array.isArray(res.data) ? res.data : (Array.isArray(res.data.data) ? res.data.data : res.data)
      
      if (deviceData && deviceData.length > 0) {
        // 确保price和shippingFee都是数字类型
        devices.value = deviceData.map((device: any) => ({
          ...device,
          price: typeof device.price === 'string' ? parseFloat(device.price) : (device.price || 0),
          shippingFee: typeof device.shippingFee === 'string' ? parseFloat(device.shippingFee) : (device.shippingFee || 0)
        }))
        
        console.log('设备列表加载成功:', devices.value)
      } else {
        console.warn('设备列表为空，使用默认设备列表')
        devices.value = defaultDevices
      }
    } else {
      console.warn('未获取到设备数据，使用默认设备列表')
      devices.value = defaultDevices
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
    // 加载失败时使用默认设备列表
    devices.value = defaultDevices
  }
  
  // 从sessionStorage获取订单数据
  const storedData = sessionStorage.getItem('orderConfirmData')
  
  if (storedData) {
    try {
      orderData.value = JSON.parse(storedData)
    } catch {
      ElMessage.error(t('orderConfirm.invalidOrder'))
      router.push('/checkout')
    }
  } else {
    // 从路由参数获取
    const { type, totalAmount, paymentMethod } = route.query
    
    if (!type || !totalAmount || !paymentMethod) {
      ElMessage.error(t('orderConfirm.invalidOrder'))
      router.push('/checkout')
      return
    }
    
    orderData.value = {
      type: type as string,
      totalAmount: parseFloat(totalAmount as string),
      paymentMethod: paymentMethod as string,
      product: null,
      esimOption: '',
      selectedDevice: '',
      addressForm: null
    }
  }
  
  // 更新选中的设备信息
  updateSelectedDeviceInfo()
})
</script>

<style scoped>
.confirm-page {
  min-height: 100vh;
  background: #e8f1fa;
}

.main-content {
  max-width: var(--site-content-max-width, 1300px);
  margin: 0 auto;
  padding: 40px 24px 64px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 28px;
}

.page-header h1 {
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.confirm-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}

.confirm-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-product-card,
.detail-card,
.summary-card {
  background: var(--surface, #fff);
  border: 1px solid var(--outline-variant, #c1c6d6);
  border-radius: var(--radius-lg, 16px);
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0, 0, 0, 0.06));
}

.order-product-card {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 24px;
}

.product-flag {
  font-size: 56px;
  line-height: 1;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-info h3 {
  color: var(--text-primary);
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 8px;
}

.product-desc {
  color: var(--text-secondary);
  margin: 0 0 12px;
  font-size: 14px;
  line-height: 1.5;
}

.product-features .el-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.detail-card {
  padding: 20px 24px 24px;
}

.detail-card h3 {
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 16px;
  padding: 0 0 14px 12px;
  border-left: 3px solid var(--primary, #0059b5);
  border-bottom: 1px solid var(--outline-variant, #c1c6d6);
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 12px 14px;
  margin-bottom: 8px;
  background: var(--surface-container-low, #f4f3f8);
  border: 1px solid var(--outline-variant, #c1c6d6);
  border-radius: var(--radius-md, 12px);
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item--block {
  flex-direction: column;
  align-items: stretch;
}

.detail-item--block .label {
  margin-bottom: 4px;
}

.detail-item--block .optional-hint {
  font-weight: 400;
  font-size: 12px;
  color: var(--text-muted, #717785);
  margin-left: 4px;
}

.detail-item .label {
  color: var(--text-secondary);
  font-size: 14px;
  flex-shrink: 0;
}

.detail-item .value {
  color: var(--text-primary);
  font-weight: 500;
  text-align: right;
  font-size: 14px;
}

.address-detail {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.payment-method-display {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: var(--surface, #fff);
  border: 1px solid var(--outline-variant, #c1c6d6);
  border-radius: 8px;
}

.payment-method-display .el-icon {
  font-size: 18px;
  color: var(--primary, #0059b5);
}

.payment-tip {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: color-mix(in srgb, var(--accent-amber, #ff9500) 10%, #fff);
  border: 1px solid color-mix(in srgb, var(--accent-amber, #ff9500) 28%, transparent);
  border-radius: var(--radius-md, 12px);
}

.payment-tip .el-icon {
  color: #d97706;
  font-size: 20px;
  margin-top: 2px;
  flex-shrink: 0;
}

.payment-tip p {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.action-buttons {
  display: flex;
  gap: 12px;
  padding-top: 4px;
}

.action-buttons .el-button {
  flex: 1;
  height: 48px;
  font-size: 15px;
  border-radius: var(--radius-md, 12px);
}

.confirm-summary {
  position: sticky;
  top: 88px;
  height: fit-content;
}

.summary-card {
  padding: 24px;
  border-top: 3px solid var(--primary, #0059b5);
}

.summary-card h3 {
  color: var(--text-primary);
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--outline-variant, #c1c6d6);
}

.summary-product {
  margin-bottom: 16px;
  padding: 12px 14px;
  background: var(--surface-container-low, #f4f3f8);
  border-radius: var(--radius-md, 12px);
}

.summary-product .product-name {
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 14px;
}

.summary-product .product-meta {
  font-size: 13px;
  color: var(--text-secondary);
}

.summary-lines {
  margin-bottom: 8px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  color: var(--text-secondary);
  font-size: 14px;
  border-bottom: 1px dashed var(--outline-variant, #c1c6d6);
}

.summary-line:last-child {
  border-bottom: none;
}

.summary-line .amount {
  color: var(--text-primary);
  font-weight: 500;
}

.summary-line .amount.discount {
  color: #16a34a;
}

.coupon-box {
  margin: 16px 0;
  padding-top: 12px;
  border-top: 1px dashed var(--outline-variant, #c1c6d6);
}

.coupon-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.coupon-row {
  display: flex;
  gap: 8px;
  align-items: stretch;
}

.coupon-row :deep(.el-input__wrapper) {
  border-radius: 6px;
  min-height: 40px;
  padding: 4px 12px;
}

.coupon-row :deep(.el-input__inner) {
  height: 32px;
  line-height: 32px;
}

.coupon-row :deep(.el-button) {
  border-radius: 6px;
  height: 40px;
  padding: 0 16px;
}

.coupon-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #16a34a;
}

.coupon-tip.error {
  color: #ef4444;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  margin-top: 8px;
  border-top: 2px solid var(--outline-variant, #c1c6d6);
}

.summary-total span:first-child {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 15px;
}

.total-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary, #0059b5);
}

@media (max-width: 900px) {
  .confirm-layout {
    grid-template-columns: 1fr;
  }

  .confirm-summary {
    position: static;
  }
}
</style>
