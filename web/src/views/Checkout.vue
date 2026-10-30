<template>
  <div class="checkout-page">
    <SiteHeader />

    <div class="container main-content">
      <div class="page-header">
        <el-button @click="router.back()" :icon="ArrowLeft">{{ $t('common.back') }}</el-button>
        <h1>{{ $t('checkout.orderConfirm') }}</h1>
      </div>

      <div class="checkout-layout">
        <!-- 左侧：产品信息 -->
        <div class="checkout-main">
          <!-- 产品卡片 -->
          <div class="order-product-card">
            <div class="product-flag">{{ getFlagEmoji(product.country) }}</div>
            <div class="product-info">
              <h3>{{ product.country }} {{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-features">
                <el-tag v-for="f in parseJson(product.features)" :key="f" size="small">{{ f }}</el-tag>
              </div>
            </div>
            <div class="product-price">
              <span class="price">${{ totalPrice.toFixed(2) }}</span>
              <span class="currency">USD</span>
            </div>
          </div>

          <!-- eSIM 关联选项 -->
          <div class="options-section">
            <h4>{{ $t('checkout.esimOptional') }}</h4>
            <div class="option-list">
              <div
                :class="['option-item', { active: esimOption === 'esim' }]"
                @click="esimOption = 'esim'">
                <div class="option-radio">
                  <span :class="['radio-dot', { checked: esimOption === 'esim' }]"></span>
                </div>
                <div class="option-content">
                  <div class="option-title">{{ $t('checkout.esimPhone') }}</div>
                  <div class="option-desc">
                    {{ $t('checkout.esimDesc') }}
                    <router-link to="/profile">{{ $t('checkout.cardCenter') }}</router-link> {{ $t('checkout.add') }}
                  </div>
                </div>
              </div>
              <div
                :class="['option-item', { active: esimOption === 'none' }]"
                @click="esimOption = 'none'">
                <div class="option-radio">
                  <span :class="['radio-dot', { checked: esimOption === 'none' }]"></span>
                </div>
                <div class="option-content">
                  <div class="option-title">{{ $t('checkout.noNeed') }}</div>
                  <div class="option-desc">{{ $t('checkout.noNeedDesc') }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 实体设备选项 -->
          <div class="options-section">
            <h4>{{ $t('checkout.optionalDevice') }} <span class="hint">{{ $t('checkout.oneShipping') }}</span></h4>
            <div class="option-list">
              <div
                v-for="device in devices"
                :key="device.id"
                :class="['option-item', { active: selectedDevice === device.id }]"
                @click="toggleDevice(device.id)">
                <div class="option-radio">
                  <span :class="['radio-dot', { checked: selectedDevice === device.id }]"></span>
                </div>
                <div class="option-content">
                  <div class="option-title">{{ locale === 'zh' ? device.name : device.nameEn }}</div>
                  <div class="option-desc">{{ locale === 'zh' ? device.description : device.descriptionEn }}</div>
                </div>
                <div class="option-price">${{ device.price.toFixed(2) }}</div>
              </div>
            </div>
            <div class="shipping-note" v-if="selectedDevice">
              {{ $t('checkout.shippingNote') }}
            </div>
          </div>

          <!-- 收货地址 -->
          <div class="options-section" v-if="selectedDevice">
            <h4>{{ $t('checkout.shippingAddr') }}</h4>
            <el-form :model="addressForm" label-width="100px">
              <el-form-item :label="$t('checkout.recipient')">
                <el-input v-model="addressForm.name" :placeholder="$t('checkout.recipientPlaceholder')" />
              </el-form-item>
              <el-form-item :label="$t('checkout.phoneNum')">
                <el-input v-model="addressForm.phone" :placeholder="$t('checkout.phonePlaceholder')" />
              </el-form-item>
              <el-form-item :label="$t('checkout.detailAddr')">
                <el-input v-model="addressForm.address" type="textarea" :rows="3" :placeholder="$t('checkout.detailAddrPlaceholder')" />
              </el-form-item>
            </el-form>
          </div>

          <!-- 订单备注 -->
          <div class="options-section">
            <h4>{{ $t('checkout.orderRemark') }} <span class="optional-hint">{{ $t('checkout.optional') }}</span></h4>
            <el-input
              v-model="customerRemark"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              :placeholder="$t('checkout.orderRemarkPlaceholder')"
            />
          </div>

          <!-- 支付方式 -->
          <div class="options-section">
            <h4>{{ $t('checkout.selectPaymentMethod') }}</h4>
            <div class="payment-methods">
              <div
                :class="['payment-method', { active: paymentMethod === 'usdt' }]"
                @click="paymentMethod = 'usdt'">
                <span class="payment-icon">U</span>
                <span>USDT</span>
              </div>
              <div
                :class="['payment-method', { active: paymentMethod === 'wechat' }]"
                @click="paymentMethod = 'wechat'">
                <span class="payment-icon wechat-icon">微</span>
                <span>{{ $t('payment.wechatPay') }}</span>
              </div>
              <div
                :class="['payment-method', { active: paymentMethod === 'alipay' }]"
                @click="paymentMethod = 'alipay'">
                <span class="payment-icon alipay-icon">支</span>
                <span>{{ $t('payment.alipay') }}</span>
              </div>
              <div
                :class="['payment-method', { active: paymentMethod === 'bankcard' }]"
                @click="paymentMethod = 'bankcard'">
                <span class="payment-icon">卡</span>
                <span>{{ $t('payment.bankCard') }}</span>
              </div>
            </div>
          </div>

          <el-button type="primary" size="large" class="submit-btn" @click="handleSubmitOrder" :disabled="submitting" :loading="submitting">
            {{ submitting ? $t('checkout.processing') : $t('checkout.submitOrder') }}
          </el-button>
        </div>

        <!-- 右侧：价格汇总 -->
        <div class="checkout-summary">
          <div class="summary-card">
            <h3>{{ $t('checkout.orderSettlement') }}</h3>

            <div class="summary-product">
              <div class="product-name">{{ product.name }}</div>
              <div class="product-meta">{{ getFlagEmoji(product.country) }} {{ product.country }}</div>
            </div>

            <div class="summary-lines">
              <div class="summary-line">
                <span>{{ $t('checkout.packageFee') }}</span>
                <span class="amount">${{ product.monthlyPrice.toFixed(2) }}</span>
              </div>
              <template v-if="selectedDevice">
                <div class="summary-line">
                  <span>{{ selectedDeviceInfo?.name || selectedDeviceInfo?.nameEn }}</span>
                  <span class="amount">${{ selectedDeviceInfo?.price.toFixed(2) }}</span>
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
              <span>{{ $t('phoneNumbers.total') }}</span>
              <span class="total-price">${{ payablePrice.toFixed(2) }} USD</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import SiteHeader from '@/components/SiteHeader.vue'
import { orderApi } from '@/api/order'
import { validateCoupon } from '@/api/coupon'
import { resolveCouponTip, resolveCouponTipOrNull } from '@/utils/couponMessage'
import { phoneNumberApi, Device } from '@/api/phoneNumber'
import { paymentApi } from '@/api/payment'
import { useI18n } from 'vue-i18n'
import { getCurrentLocale } from '@/i18n'
import { useSiteConfigStore } from '@/stores/siteConfig'
import { navigateToLogin } from '@/utils/authNavigation'
import { navigateByPaymentMethod } from '@/utils/stripeCheckout'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const siteConfig = useSiteConfigStore()
const { locale, t } = useI18n({ useScope: 'global' })
const submitting = ref(false)

// 产品信息
const product = ref<any>({
  id: 1,
  country: '新加坡',
  name: '每天500MB有效期1天(达量降速)',
  description: '全球流量套餐',
  monthlyPrice: 0.86,
  maintenancePrice: 0,
  features: '["5G网络","全程不限速","独立IP"]',
  stock: 99,
  countryCode: '+65'
})

// 选项
const esimOption = ref('esim')
const selectedDevice = ref<number | string>('')
const paymentMethod = ref('usdt')
const userBalance = ref(0)
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

// 地址表单
const addressForm = ref({
  name: '',
  phone: '',
  address: ''
})

/** 用户下单备注（可选） */
const customerRemark = ref('')

// 计算总价
const totalPrice = computed(() => {
  let price = product.value.monthlyPrice
  if (selectedDevice.value) {
    const device = devices.value.find(d => d.id === selectedDevice.value)
    if (device) {
      price += device.price
      price += Number(siteConfig.defaultShippingFee ?? 0)
    }
  }
  return price
})

const couponInput = ref('')
const couponLoading = ref(false)
const couponApplied = ref(false)
const couponMessage = ref('')
const appliedCode = ref('')
const appliedDiscount = ref(0)

const payablePrice = computed(() =>
  Math.max(0, Number((totalPrice.value - appliedDiscount.value).toFixed(2)))
)

watch(totalPrice, () => {
  if (couponApplied.value) {
    applyCoupon()
  }
})

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
    const res: any = await validateCoupon(code, totalPrice.value, product.value?.id)
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

// 获取选中的设备信息
const selectedDeviceInfo = computed(() => {
  return devices.value.find(d => d.id === selectedDevice.value)
})

function getFlagEmoji(country: string): string {
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

function toggleDevice(deviceId: number | string) {
  if (selectedDevice.value === deviceId) {
    selectedDevice.value = ''
  } else {
    selectedDevice.value = deviceId
  }
}

async function loadBalance() {
  userBalance.value = 0
}

// 监听登录状态变化，重新加载余额
watch(() => authStore.isLoggedIn, (loggedIn) => {
  console.log('Login state changed:', loggedIn)
  if (loggedIn) {
    loadBalance()
  }
})

async function handleSubmitOrder() {
  // 检查登录状态
  if (!authStore.isLoggedIn) {
    ElMessage.warning(t('checkout.pleaseLogin'))
    navigateToLogin(router, route)
    return
  }

  // 获取当前用户邮箱
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
    // 构建订单数据
    const orderData = {
      email: userEmail,
      remark: JSON.stringify({
        productId: product.value.id,
        productName: product.value.name,
        productCountry: product.value.country,
        esimOption: esimOption.value,
        device: selectedDevice.value,
        totalAmount: totalPrice.value
      }),
      shippingAddress: selectedDevice.value ? JSON.stringify(addressForm.value) : null,
      paymentMethod: paymentMethod.value,
      language: getCurrentLocale(),
      productId: product.value.id,
      totalAmount: totalPrice.value,
      couponCode: appliedCode.value || undefined,
      customerRemark: customerRemark.value.trim() || undefined
    }

    // 创建订单
    const res: any = await orderApi.createOrder(orderData)
    if (res.code === 200 || res.code === 201) {
      const order = res.data
      const orderId = order.id || order.orderNo
      const payAmount = Number(order.finalAmount ?? payablePrice.value)
      
      // 根据支付方式跳转到对应页面
      switch (paymentMethod.value) {
        case 'usdt':
          // 创建 USDT 支付订单
          try {
            const paymentRes: any = await paymentApi.createUsdtPayment({
              orderNo: order.orderNo,
              amount: payAmount,
              productName: product.value.name,
              remark: `购买 ${product.value.country} ${product.value.name}`
            })
            if (paymentRes.success) {
              console.log('USDT payment created:', paymentRes)
              router.push('/checkout/usdt/' + orderId)
            } else if (paymentRes.code === 'PAYMENT_API_ERROR') {
              ElMessage.error('订单创建失败，请重新下单')
              router.push('/product/' + product.value.id)
            } else {
              console.log('USDT payment creation returned:', paymentRes)
              router.push('/checkout/usdt/' + orderId)
            }
          } catch (e: any) {
            console.error('Failed to create USDT payment:', e)
            ElMessage.error('订单创建失败，请重新下单')
            router.push('/product/' + product.value.id)
          }
          break
        case 'wechat':
        case 'alipay':
        case 'bankcard':
        case 'antom_alipay':
          await navigateByPaymentMethod({
            paymentMethod: paymentMethod.value,
            orderId,
            orderNo: order.orderNo,
            email: authStore.user?.email,
            router,
          })
          break
        default:
          router.push('/profile')
      }
    } else {
      ElMessage.error(resolveCouponTipOrNull(t, { message: res.message }) || res.message || t('checkout.createOrderFailed'))
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
      ElMessage.error(resolveCouponTipOrNull(t, { message: error.message }) || error.message || t('checkout.createOrderFailed'))
    }
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  // 从 URL 查询参数读取产品信息
  const query = route.query
  if (query.productId) {
    product.value = {
      id: query.productId,
      name: query.name || '',
      country: query.country || '',
      description: query.description || '',
      monthlyPrice: parseFloat(query.price as string) || 0,
      maintenancePrice: 0,
      features: query.features || '[]',
      stock: 99,
      countryCode: query.countryCode || '',
      operator: query.operator || ''
    }
  }
  
  // 加载设备列表
  try {
    console.log('开始加载设备列表...')
    const res: any = await phoneNumberApi.getDevices()
    console.log('设备列表API响应:', res)
    
    if (res && res.data) {
      // 数据可能直接是数组或在data字段中
      const deviceData = Array.isArray(res.data) ? res.data : (Array.isArray(res.data.data) ? res.data.data : res.data)
      
      // 确保price和shippingFee都是数字类型
      devices.value = deviceData.map((device: any) => ({
        ...device,
        price: typeof device.price === 'string' ? parseFloat(device.price) : device.price || 0,
        shippingFee: typeof device.shippingFee === 'string' ? parseFloat(device.shippingFee) : device.shippingFee || 0
      }))
      
      console.log('设备列表加载成功:', devices.value)
    } else {
      console.warn('未获取到设备数据，使用默认设备列表')
      devices.value = defaultDevices
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
    // 加载失败时使用默认设备列表
    devices.value = defaultDevices
  }
  
  // 如果有购物车，先加载购物车
  // cart removed: buy-now only
  
  loadBalance()
})
</script>

<style scoped>
.checkout-page {
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

.checkout-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}

.checkout-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 统一卡片 */
.order-product-card,
.options-section,
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

.product-price {
  text-align: right;
  flex-shrink: 0;
}

.product-price .price {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary, #0059b5);
  display: block;
}

.product-price .currency {
  color: var(--text-muted);
  font-size: 14px;
}

.options-section {
  padding: 20px 24px 24px;
}

.options-section h4 {
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 16px;
  padding-left: 12px;
  border-left: 3px solid var(--primary, #0059b5);
  display: flex;
  align-items: center;
  gap: 8px;
}

.options-section h4 .hint,
.options-section h4 .optional-hint {
  font-weight: 400;
  font-size: 12px;
  color: var(--text-muted, #717785);
  margin-left: 6px;
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 14px 16px;
  background: var(--surface-container-low, #f4f3f8);
  border: 1px solid var(--outline-variant, #c1c6d6);
  border-radius: var(--radius-md, 12px);
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
}

.option-item:hover {
  border-color: color-mix(in srgb, var(--primary, #0059b5) 40%, transparent);
  background: var(--surface-container, #eeedf3);
}

.option-item.active {
  border-color: var(--primary, #0059b5);
  background: color-mix(in srgb, var(--primary, #0059b5) 6%, #fff);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--primary, #0059b5) 12%, transparent);
}

.option-radio {
  padding-top: 2px;
  flex-shrink: 0;
}

.radio-dot {
  width: 20px;
  height: 20px;
  border: 2px solid var(--outline-variant, #c1c6d6);
  border-radius: 50%;
  display: block;
  transition: all 0.2s;
}

.radio-dot.checked {
  border-color: var(--primary, #0059b5);
  background: var(--primary, #0059b5);
  box-shadow: inset 0 0 0 4px #fff;
}

.option-content {
  flex: 1;
  min-width: 0;
}

.option-title {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
}

.option-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.option-desc a {
  color: var(--primary, #0059b5);
}

.option-price {
  color: var(--primary, #0059b5);
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

.shipping-note {
  margin-top: 12px;
  padding: 12px 14px;
  background: color-mix(in srgb, var(--accent-amber, #ff9500) 10%, #fff);
  border: 1px solid color-mix(in srgb, var(--accent-amber, #ff9500) 25%, transparent);
  border-radius: var(--radius-md, 12px);
  font-size: 13px;
  color: #9a6700;
}

.options-section :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}

.options-section :deep(.el-input__wrapper),
.options-section :deep(.el-textarea__inner) {
  background: var(--surface, #fff);
  box-shadow: 0 0 0 1px var(--outline-variant, #c1c6d6) inset;
}

.options-section :deep(.el-input__wrapper.is-focus),
.options-section :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--primary, #0059b5) inset;
}

.payment-methods {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.payment-method {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  background: var(--surface-container-low, #f4f3f8);
  border: 1px solid var(--outline-variant, #c1c6d6);
  border-radius: var(--radius-md, 12px);
  cursor: pointer;
  transition: all 0.2s;
}

.payment-method:hover {
  border-color: color-mix(in srgb, var(--primary, #0059b5) 40%, transparent);
  background: var(--surface-container, #eeedf3);
}

.payment-method.active {
  border-color: var(--primary, #0059b5);
  background: color-mix(in srgb, var(--primary, #0059b5) 6%, #fff);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--primary, #0059b5) 12%, transparent);
}

.payment-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary, #0059b5), #0071e3);
  border-radius: 10px;
  font-size: 18px;
  font-weight: bold;
  color: white;
}

.payment-icon.wechat-icon {
  background: linear-gradient(135deg, #07c160, #059669);
}

.payment-icon.alipay-icon {
  background: linear-gradient(135deg, #1677ff, #0958d9);
}

.payment-method span:not(.payment-icon):not(.balance-info) {
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 500;
}

.balance-info {
  font-size: 11px;
  color: var(--text-muted);
}

.balance-info.insufficient {
  color: var(--accent-rose, #ff3b30);
}

.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md, 12px) !important;
}

.checkout-summary {
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
  .checkout-layout {
    grid-template-columns: 1fr;
  }

  .checkout-summary {
    position: static;
  }

  .payment-methods {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
