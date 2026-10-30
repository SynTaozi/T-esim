<template>
  <div class="product-detail-page">
    <SiteHeader />

    <!-- Loading -->
    <div v-if="loading" class="pd-loading">
      <div class="pd-loading-spin"></div>
    </div>

    <!-- 404 -->
    <div v-else-if="!product" class="pd-empty">
      <p>{{ $t('common.noData') }}</p>
      <button class="pd-back-btn" @click="router.push('/products')">{{ $t('nav.products') }}</button>
    </div>

    <!-- Main Content -->
    <main v-else class="pd-main">

      <!-- HERO SECTION -->
      <section class="pd-hero">
        <!-- Left: Visual -->
        <div class="pd-visual">
          <div class="pd-image-wrap">
            <img
              v-if="(product as any).displayImageUrl || product.imageUrl"
              :src="(product as any).displayImageUrl || product.imageUrl"
              :alt="getProductName(product)"
              class="pd-hero-img"
            />
            <div v-else class="pd-hero-placeholder">
              <span class="pd-flag-xl">{{ getProductFlag(product) }}</span>
            </div>
            <div class="pd-image-overlay"></div>
            <div class="pd-badges">
              <span v-if="product.categoryCode === 'PHONE' || product.itemType === 1" class="pd-badge pd-badge--surface">
                Phone SIM
              </span>
            </div>
          </div>

          <!-- 产品说明 + 产品特性 -->
          <div
            v-if="getProductNote(product) || getPhoneAllFeatures(product).length"
            class="pd-bento-card pd-bento-card--specs"
          >
            <div v-if="getProductNote(product)" class="pd-note-block">
              <h2 class="pd-bento-title">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><path d="M14 2v6h6M8 13h8M8 17h8M8 9h2"/></svg>
                {{ $t('phoneNumbers.productNote') }}
              </h2>
              <p class="pd-note-text">{{ getProductNote(product) }}</p>
            </div>
            <div v-if="getPhoneAllFeatures(product).length" class="pd-specs-block" :class="{ 'pd-specs-block--split': !!getProductNote(product) }">
              <h2 class="pd-bento-title">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg>
                {{ $t('phoneNumbers.productCharacteristics') }}
              </h2>
              <div class="pd-features-grid">
                <div
                  v-for="(feat, idx) in getPhoneAllFeatures(product)"
                  :key="idx"
                  :class="['pd-feature-item', feat.startsWith('!') ? 'pd-feat-warn' : '']">
                  <span>{{ feat }}</span>
                </div>
              </div>
            </div>
          </div>

        </div>

        <!-- Right: sticky checkout -->
        <aside class="pd-purchase">
          <div class="pd-purchase-card">
            <header class="pd-purchase-head">
              <div class="pd-product-flag-name">
                <span class="pd-flag-md">{{ getProductFlag(product) }}</span>
                <div class="pd-purchase-head-text">
                  <h1 class="pd-product-title">{{ getProductName(product) }}</h1>
                  <div class="pd-head-price">
                    <template v-if="productPricingTiers.length === 0 || selectedTier">
                      <span class="pd-head-amount">${{ payablePrice.toFixed(2) }}</span>
                      <span class="pd-head-currency">USD</span>
                    </template>
                    <span v-else class="pd-head-hint">{{ $t('plans.selectPeriodFirst') || 'Select a period first' }}</span>
                  </div>
                </div>
              </div>
            </header>

            <div class="pd-purchase-body">
              <!-- Period -->
              <section class="pd-block" v-if="productPricingTiers.length > 0">
                <div class="pd-block-head">
                  <span class="pd-step">1</span>
                  <span>{{ $t('plans.selectPeriod') || 'Select Plan Period' }}</span>
                  <span class="pd-required">*</span>
                </div>
                <div class="pd-tier-chips">
                  <label
                    v-for="tier in productPricingTiers"
                    :key="tier.id"
                    class="pd-tier-chip"
                    :class="{ selected: selectedTier?.id === tier.id }">
                    <input type="radio" :value="tier" v-model="selectedTier" />
                    <div class="pd-tier-chip-inner">
                      <div class="pd-tier-top">
                        <span class="pd-tier-months">{{ tier.months }}{{ locale === 'zh' ? '个月' : ' mo' }}</span>
                        <span v-if="tier.label" class="pd-tier-badge">{{ tier.label }}</span>
                      </div>
                      <div class="pd-tier-prices">
                        <span class="pd-tier-total">${{ tier.totalPrice }}</span>
                        <span class="pd-tier-per">{{ (tier.totalPrice / tier.months).toFixed(2) }}/{{ locale === 'zh' ? '月' : 'mo' }}</span>
                      </div>
                    </div>
                  </label>
                </div>
              </section>

              <!-- Devices -->
              <section class="pd-block">
                <div class="pd-block-head">
                  <span class="pd-step">{{ productPricingTiers.length > 0 ? 2 : 1 }}</span>
                  <span>{{ (product as any)?.isPhysicalCard ? $t('plans.selectDevice') : $t('plans.optionalPhysicalDevices') }}</span>
                  <span :class="(product as any)?.isPhysicalCard ? 'pd-required' : 'pd-hint'">
                    {{ (product as any)?.isPhysicalCard ? '*' : $t('checkout.oneShipping') }}
                  </span>
                </div>
                <div class="pd-devices">
                  <label
                    v-for="device in devices"
                    :key="device.id"
                    class="pd-device"
                    :class="{ selected: selectedDevices.includes(device.id) }">
                    <input type="checkbox" :value="device.id" v-model="selectedDevices" />
                    <div class="pd-device-inner">
                      <div class="pd-device-text">
                        <span class="pd-device-name">{{ locale === 'zh' ? device.name : device.nameEn }}</span>
                        <span class="pd-device-desc">{{ locale === 'zh' ? device.description : device.descriptionEn }}</span>
                      </div>
                      <span class="pd-device-price">{{ device.isFree ? $t('phoneNumbers.free') : '$' + device.price.toFixed(2) }}</span>
                    </div>
                  </label>
                </div>
                <div class="pd-shipping-note" v-if="hasDevice">
                  {{ $t('checkout.shippingNote') }} <strong>{{ siteConfig.formatShippingFee() }}</strong>
                </div>
              </section>

              <!-- Shipping -->
              <section class="pd-block" v-if="hasDevice">
                <div class="pd-block-head">
                  <span class="pd-step">{{ productPricingTiers.length > 0 ? 3 : 2 }}</span>
                  <span>{{ $t('checkout.shippingAddress') }}</span>
                  <span class="pd-required">*</span>
                </div>
                <div class="pd-address-form">
                  <div class="pd-form-row">
                    <div class="pd-form-item">
                      <label>{{ $t('checkout.recipientName') }}</label>
                      <el-input v-model="shippingAddress.name" :placeholder="$t('checkout.recipientNamePlaceholder')" size="small" />
                    </div>
                    <div class="pd-form-item">
                      <label>{{ $t('checkout.contactPhone') }}</label>
                      <el-input v-model="shippingAddress.phone" :placeholder="$t('checkout.contactPhonePlaceholder')" size="small" />
                    </div>
                  </div>
                  <div class="pd-form-row">
                    <div class="pd-form-item">
                      <label>{{ $t('checkout.province') }}</label>
                      <el-input v-model="shippingAddress.province" :placeholder="$t('checkout.provincePlaceholder')" size="small" />
                    </div>
                    <div class="pd-form-item">
                      <label>{{ $t('checkout.city') }}</label>
                      <el-input v-model="shippingAddress.city" :placeholder="$t('checkout.cityPlaceholder')" size="small" />
                    </div>
                  </div>
                  <div class="pd-form-row">
                    <div class="pd-form-item">
                      <label>{{ $t('checkout.postalCode') }}</label>
                      <el-input v-model="shippingAddress.postalCode" :placeholder="$t('checkout.postalCodePlaceholder')" size="small" />
                    </div>
                  </div>
                  <div class="pd-form-row">
                    <div class="pd-form-item pd-form-item--full">
                      <label>{{ $t('checkout.detailedAddress') }}</label>
                      <el-input v-model="shippingAddress.address" :placeholder="$t('checkout.detailedAddressPlaceholder')" type="textarea" :rows="2" size="small" />
                    </div>
                  </div>
                </div>
              </section>

              <!-- Remark (compact) -->
              <section class="pd-block pd-block--tight">
                <div class="pd-block-head pd-block-head--muted">
                  <span>{{ $t('checkout.orderRemark') }}</span>
                  <span class="pd-optional">{{ $t('checkout.optional') }}</span>
                </div>
                <el-input
                  v-model="customerRemark"
                  maxlength="500"
                  size="small"
                  :placeholder="$t('checkout.orderRemarkPlaceholder')"
                />
              </section>

              <!-- Summary -->
              <section class="pd-block pd-block--summary">
                <div class="pd-block-head">
                  <span>{{ $t('phoneNumbers.orderSummary') }}</span>
                </div>
                <div class="pd-summary">
                  <div class="pd-summary-row">
                    <span>{{ $t('checkout.packageFee') }}</span>
                    <span v-if="selectedTier">
                      ${{ selectedTier.totalPrice }}
                      <small class="pd-summary-sub">({{ selectedTier.months }}{{ locale === 'zh' ? '个月' : ' mo' }})</small>
                    </span>
                    <span v-else>${{ product.price }}</span>
                  </div>
                  <div class="pd-summary-row" v-if="hasDevice">
                    <span>{{ $t('phoneNumbers.deviceFee') }}</span>
                    <span>${{ deviceTotal.toFixed(2) }}</span>
                  </div>
                  <div class="pd-summary-row" v-if="hasDevice">
                    <span>{{ $t('phoneNumbers.expressFee') }}</span>
                    <span>{{ siteConfig.formatShippingFee() }}</span>
                  </div>
                  <div class="pd-summary-row" v-if="appliedDiscount > 0">
                    <span>{{ $t('checkout.couponDiscount') }}</span>
                    <span class="pd-discount">-${{ appliedDiscount.toFixed(2) }}</span>
                  </div>
                </div>
                <div class="pd-coupon">
                  <div class="pd-coupon-row">
                    <el-input
                      v-model="couponInput"
                      :placeholder="$t('checkout.couponPlaceholder')"
                      clearable
                      size="small"
                      @keyup.enter="applyCoupon"
                    />
                    <el-button :loading="couponLoading" size="small" @click="applyCoupon">{{ $t('checkout.couponApply') }}</el-button>
                  </div>
                  <div class="pd-coupon-tip" v-if="couponMessage" :class="{ error: !couponApplied }">{{ couponMessage }}</div>
                </div>
              </section>

              <!-- Payment -->
              <section class="pd-block" v-if="enabledPaymentMethods.length > 0">
                <div class="pd-block-head">
                  <span class="pd-step">{{ hasDevice ? (productPricingTiers.length > 0 ? 4 : 3) : (productPricingTiers.length > 0 ? 3 : 2) }}</span>
                  <span>{{ $t('phoneNumbers.selectPayment') }}</span>
                </div>
                <div class="pd-payments">
                  <label
                    v-for="pm in enabledPaymentMethods"
                    :key="pm.code"
                    class="pd-payment"
                    :class="{
                      selected: paymentMethod === pm.code.toLowerCase(),
                      disabled: pm.code === 'BALANCE' && userBalance < payablePrice
                    }">
                    <input
                      type="radio"
                      v-model="paymentMethod"
                      :value="pm.code.toLowerCase()"
                      :disabled="pm.code === 'BALANCE' && userBalance < payablePrice"
                    />
                    <div class="pd-payment-inner">
                      <div class="pd-payment-icon" :class="getPaymentIconClass(pm.code)">
                        <span v-if="pm.code === 'USDT'">₮</span>
                        <svg v-else-if="pm.code === 'WECHAT'" viewBox="0 0 24 24" fill="currentColor">
                          <path d="M9.5 4C6.46 4 4 6.24 4 9c0 1.57.81 2.96 2.08 3.88L5.5 15.5l2.42-1.21c.48.14.99.21 1.53.21.2 0 .39-.01.58-.03C9.7 13.6 9.5 12.72 9.5 11.8c0-3.04 2.81-5.5 6.28-5.5.2 0 .39.01.58.02C15.5 4.9 12.74 4 9.5 4zm-2.75 3.75c.48 0 .88.4.88.88s-.4.87-.88.87-.87-.39-.87-.87.39-.88.87-.88zm4.5 0c.48 0 .88.4.88.88s-.4.87-.88.87-.87-.39-.87-.87.39-.88.87-.88zM15.78 7.5c-2.9 0-5.25 1.97-5.25 4.4 0 2.43 2.35 4.4 5.25 4.4.57 0 1.12-.09 1.63-.24L19.5 17l-.75-1.95C19.55 14.3 20 13.2 20 11.9c0-2.43-2.35-4.4-5.22-4.4zm-1.78 3.25c.35 0 .63.28.63.63s-.28.62-.63.62-.62-.27-.62-.62.28-.63.62-.63zm3.5 0c.35 0 .63.28.63.63s-.28.62-.63.62-.62-.27-.62-.62.28-.63.62-.63z"/>
                        </svg>
                        <span v-else-if="pm.code === 'ALIPAY' || pm.code === 'ANTOM_ALIPAY'" class="pd-brand-mark">支</span>
                        <svg v-else-if="pm.code === 'BANK_CARD' || pm.code === 'CARD'" viewBox="0 0 24 24" fill="currentColor">
                          <path d="M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z"/>
                        </svg>
                        <svg v-else-if="pm.code === 'BALANCE'" viewBox="0 0 24 24" fill="currentColor">
                          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1.41 16.09V20h-2.67v-1.93c-1.71-.36-3.16-1.46-3.27-3.4h1.96c.1 1.05.82 1.87 2.65 1.87 1.96 0 2.4-.98 2.4-1.59 0-.83-.44-1.61-2.67-2.14-2.48-.6-4.18-1.62-4.18-3.67 0-1.72 1.39-2.84 3.11-3.21V4h2.67v1.95c1.86.45 2.79 1.86 2.85 3.39H14.3c-.05-1.11-.64-1.87-2.22-1.87-1.5 0-2.4.68-2.4 1.64 0 .84.65 1.39 2.67 1.91s4.18 1.39 4.18 3.91c-.01 1.83-1.38 2.83-3.12 3.16z"/>
                        </svg>
                        <svg v-else viewBox="0 0 24 24" fill="currentColor">
                          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
                        </svg>
                      </div>
                      <div class="pd-payment-text">
                        <span class="pd-payment-name">{{ getPaymentMethodLabel(pm.code, t) }}</span>
                        <span
                          v-if="pm.code === 'BALANCE' && userBalance < payablePrice"
                          class="pd-payment-desc pd-insufficient"
                        >{{ $t('checkout.balanceInsufficient') }}</span>
                      </div>
                    </div>
                  </label>
                </div>
              </section>
            </div>

            <footer class="pd-purchase-footer">
              <div class="pd-footer-total">
                <span>{{ $t('phoneNumbers.total') }}</span>
                <strong v-if="productPricingTiers.length === 0 || selectedTier">${{ payablePrice.toFixed(2) }} USD</strong>
                <strong v-else class="pd-total-na">—</strong>
              </div>
              <button
                class="pd-buy-btn"
                @click="handleBuyNow"
                :disabled="placingOrder"
                :class="{ loading: placingOrder }">
                <svg v-if="!placingOrder" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 01-8 0"/></svg>
                <svg v-else class="pd-spin-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" opacity="0.3"/><path d="M21 12a9 9 0 00-9-9"/></svg>
                {{ placingOrder ? $t('checkout.processing') : $t('phoneNumbers.checkout') }}
              </button>
              <p class="pd-buy-note">
                {{ $t('profile.secureCheckout') }} · {{ $t('profile.instantDelivery') }}
              </p>
            </footer>
          </div>
        </aside>
      </section>

      <!-- Full-width setup guide -->
      <section class="pd-bento-card pd-setup-card">
        <h2 class="pd-bento-title">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2.5"/><circle cx="12" cy="18" r="1" fill="currentColor" stroke="none"/></svg>
          {{ $t('tutorial.title') || 'Easy 3-Step Setup' }}
        </h2>
        <div class="pd-setup-steps">
          <div class="pd-setup-step">
            <div class="pd-setup-icon">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg>
            </div>
            <h3>1. {{ $t('tutorial.step1Title') || 'Purchase' }}</h3>
            <p>{{ $t('tutorial.step1Desc') || 'Buy your plan online and receive a QR code via email.' }}</p>
          </div>
          <div class="pd-setup-step">
            <div class="pd-setup-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><path d="M14 14h7M14 17h4M17 14v4"/></svg>
            </div>
            <h3>2. {{ $t('tutorial.step2Title') || 'Scan' }}</h3>
            <p>{{ $t('tutorial.step2Desc') || "Scan the QR code to install your eSIM." }}</p>
          </div>
          <div class="pd-setup-step">
            <div class="pd-setup-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1.5 8.5C8 1 16 1 22.5 8.5"/><path d="M5 12c4-4 10-4 14 0"/><path d="M8.5 15.5c2-2 5-2 7 0"/><circle cx="12" cy="19" r="1" fill="currentColor" stroke="none"/></svg>
            </div>
            <h3>3. {{ $t('tutorial.step3Title') || 'Connect' }}</h3>
            <p>{{ $t('tutorial.step3Desc') || 'Turn on your eSIM when you arrive to connect instantly.' }}</p>
          </div>
        </div>
      </section>

    </main>

    <PurchaseNoticeDialog
      v-model="purchaseNoticeVisible"
      @confirm="submitOrderAfterNotice"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { applyPageSeo } from '@/utils/seo'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { productApi, Product } from '@/api/product'
import { pricingTierApi, ProductPricingTier } from '@/api/pricingTier'
import { phoneNumberApi, Device } from '@/api/phoneNumber'
import { orderApi } from '@/api/order'
import { validateCoupon } from '@/api/coupon'
import { resolveCouponTip, resolveCouponTipOrNull } from '@/utils/couponMessage'
import { paymentMethodApi } from '@/api/paymentMethod'
import { ElMessage } from 'element-plus'
import SiteHeader from '@/components/SiteHeader.vue'
import PurchaseNoticeDialog from '@/components/PurchaseNoticeDialog.vue'
import { getCurrentLocale } from '@/i18n'
import { getCountryFlagEmoji } from '@/utils/countryDisplay'
import { useSiteConfigStore } from '@/stores/siteConfig'
import { navigateToLogin } from '@/utils/authNavigation'
import { navigateByPaymentMethod } from '@/utils/stripeCheckout'
import { getPaymentMethodLabel } from '@/utils/paymentMethodLabel'

const { locale, t } = useI18n({ useScope: 'global' })
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const siteConfig = useSiteConfigStore()

const product = ref<Product | null>(null)
const loading = ref(true)

const selectedDevices = ref<number[]>([])
const paymentMethod = ref('usdt')
const userBalance = ref(0)
const enabledPaymentMethods = ref<any[]>([])
const placingOrder = ref(false)
const purchaseNoticeVisible = ref(false)
const devices = ref<Device[]>([])
const productPricingTiers = ref<ProductPricingTier[]>([])
const selectedTier = ref<ProductPricingTier | null>(null)
const shippingAddress = ref({ name: '', phone: '', province: '', city: '', postalCode: '', address: '' })
const customerRemark = ref('')

const deviceTotal = computed(() => {
  return selectedDevices.value.reduce((sum, deviceId) => {
    const device = devices.value.find(d => d.id === deviceId)
    return sum + (device?.price || 0)
  }, 0)
})

const hasDevice = computed(() => selectedDevices.value.length > 0)

const shippingFeeAmount = computed(() => {
  if (!hasDevice.value) return 0
  return Number(siteConfig.defaultShippingFee ?? 0)
})

const packagePrice = computed(() => {
  if (selectedTier.value) return selectedTier.value.totalPrice
  return product.value?.price || 0
})

const orderTotal = computed(() => {
  return packagePrice.value + deviceTotal.value + shippingFeeAmount.value
})

const couponInput = ref('')
const couponLoading = ref(false)
const couponApplied = ref(false)
const couponMessage = ref('')
const appliedCode = ref('')
const appliedDiscount = ref(0)

const payablePrice = computed(() =>
  Math.max(0, Number((orderTotal.value - appliedDiscount.value).toFixed(2)))
)

watch(orderTotal, () => {
  if (couponApplied.value) applyCoupon()
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
    const res: any = await validateCoupon(code, orderTotal.value, product.value?.id)
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

// Helpers

function getProductName(p: Product): string {
  if (locale.value === 'en' && p.nameEn) return p.nameEn
  return p.name
}

function applyProductSeo() {
  const p = product.value
  if (!p) return
  const name = getProductName(p)
  const bits = [
    p.dataLimit || null,
    p.validityDays ? `${p.validityDays}${locale.value === 'en' ? ' days' : '天'}` : null
  ].filter(Boolean)
  const spec = bits.length ? bits.join(' · ') : ''
  const isEn = locale.value === 'en'
  const title = isEn
    ? `${name}${spec ? ` (${spec})` : ''} | Tesim eSIM`
    : `${name}${spec ? `（${spec}）` : ''} | Tesim eSIM`
  const description = isEn
    ? `Buy ${name} on Tesim.${spec ? ` ${spec}.` : ''} Global eSIM and physical SIM with instant checkout.`
    : `在 Tesim 购买「${name}」。${spec ? `${spec}。` : ''}全球 eSIM / 实体卡，即买即用。`
  const image = (p as any).displayImageUrl || p.imageUrl || '/logo.png'
  applyPageSeo({
    title,
    description,
    path: `/products/${p.id}`,
    image,
    type: 'product'
  })
}

watch(locale, () => {
  if (product.value) applyProductSeo()
})

function getProductFlag(p: Product): string {
  try {
    const countries = JSON.parse(p.countries || '[]')
    if (countries.length > 0) {
      const flag = getCountryFlagEmoji(countries[0])
      if (flag !== '🌐') return flag
    }
  } catch {}
  return '\ud83c\udf0d'
}

function noteToText(raw?: string): string {
  if (!raw) return ''
  const trimmed = raw.trim()
  if (!trimmed) return ''
  if (trimmed.startsWith('[')) {
    try {
      const parsed = JSON.parse(trimmed)
      if (Array.isArray(parsed)) return parsed.map((item) => String(item).trim()).filter(Boolean).join('\n')
    } catch {
      /* plain text */
    }
  }
  return trimmed
}
function getProductNote(p: Product): string {
  const en = noteToText((p as any).importantLabelsEn)
  const zh = noteToText((p as any).importantLabels)
  return locale.value === 'en' && en ? en : zh
}
function getPhoneAllFeatures(p: Product): string[] {
  try {
    const hasEn = locale.value === 'en' && p.featuresEn && p.featuresEn !== '[]'
    return JSON.parse(hasEn ? p.featuresEn! : (p.features || '[]'))
  } catch { return [] }
}
function getPaymentIconClass(code: string): string {
  const m: Record<string, string> = {
    USDT: 'usdt-icon',
    WECHAT: 'wechat-icon',
    ALIPAY: 'alipay-icon',
    ANTOM_ALIPAY: 'alipay-icon',
    BANK_CARD: 'card-icon',
    CARD: 'card-icon',
    BALANCE: 'balance-icon'
  }
  return m[code] || 'default-icon'
}

async function handleBuyNow() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning(t('checkout.pleaseLogin'))
    navigateToLogin(router, route)
    return
  }
  if (productPricingTiers.value.length > 0 && !selectedTier.value) {
    ElMessage.warning(t('plans.selectPeriodFirst') || 'Please select a plan period'); return
  }
  const isPhysical = !!(product.value as any)?.isPhysicalCard
  if (isPhysical && selectedDevices.value.length === 0) {
    ElMessage.warning(t('plans.physicalRequiresDevice') || 'Please select at least one device for physical SIM'); return
  }
  if (hasDevice.value) {
    if (!shippingAddress.value.name) { ElMessage.warning(t('checkout.nameRequired') || 'Please enter recipient name'); return }
    if (!shippingAddress.value.phone) { ElMessage.warning(t('checkout.phoneRequired') || 'Please enter phone number'); return }
    if (!shippingAddress.value.province) { ElMessage.warning(t('checkout.provinceRequired') || 'Please enter province'); return }
    if (!shippingAddress.value.city) { ElMessage.warning(t('checkout.cityRequired') || 'Please enter city'); return }
    if (!shippingAddress.value.address) { ElMessage.warning(t('checkout.addressRequired') || 'Please enter address'); return }
  }
  if (!product.value) return
  if (placingOrder.value) { ElMessage.warning(t('checkout.processing') || 'Order is being processed, please wait'); return }
  purchaseNoticeVisible.value = true
}

async function submitOrderAfterNotice() {
  if (!product.value) return
  if (placingOrder.value) { ElMessage.warning(t('checkout.processing') || 'Order is being processed, please wait'); return }
  placingOrder.value = true
  try {
    await siteConfig.ensureShippingFee()
    const paymentMethodUpper = paymentMethod.value.toUpperCase()
    const orderData: any = {
      email: authStore.user?.email || '',
      paymentMethod: paymentMethodUpper,
      productId: product.value.id,
      devices: selectedDevices.value.map(id => String(id)),
      totalAmount: orderTotal.value,
      couponCode: appliedCode.value || undefined,
      language: getCurrentLocale(),
      customerRemark: customerRemark.value.trim() || undefined,
      ...(selectedTier.value ? { pricingTierId: selectedTier.value.id, selectedMonths: selectedTier.value.months } : {})
    }
    if (hasDevice.value) {
      const addr = shippingAddress.value
      orderData.shippingAddress = `${addr.name} ${addr.phone} ${addr.province} ${addr.city} ${addr.postalCode || ''} ${addr.address}`
    }
    const res: any = await orderApi.createOrder(orderData)
    if (res.code === 200) {
      ElMessage.success(t('checkout.processing'))
      const orderId = res.data.id
      const orderNo = res.data.orderNo || String(orderId)
      await navigateByPaymentMethod({
        paymentMethod: paymentMethodUpper,
        orderId,
        orderNo,
        email: authStore.user?.email,
        checkoutUrl: res.data.checkoutUrl,
        router,
      })
    } else {
      ElMessage.error(resolveCouponTipOrNull(t, { message: res.message }) || res.message || t('checkout.createOrderFailed'))
    }
  } catch (error: any) {
    ElMessage.error(resolveCouponTipOrNull(t, { message: error.message }) || error.message || t('checkout.createOrderFailed'))
  } finally {
    placingOrder.value = false
  }
}

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const [productRes, devicesRes, paymentRes] = await Promise.all([
      productApi.getProductById(id),
      phoneNumberApi.getDevices(),
      paymentMethodApi.getEnabled()
    ])

    if ((productRes as any).code === 200) {
      product.value = (productRes as any).data
      applyProductSeo()
      const tiersRes: any = await pricingTierApi.getActiveByProduct(id)
      if (tiersRes.code === 200) productPricingTiers.value = tiersRes.data || []
    }

    if (devicesRes && (devicesRes as any).data) {
      devices.value = ((devicesRes as any).data as any[]).map((d: any) => ({
        ...d,
        price: typeof d.price === 'string' ? parseFloat(d.price) : (d.price || 0),
        shippingFee: typeof d.shippingFee === 'string' ? parseFloat(d.shippingFee) : (d.shippingFee || 0)
      }))
    }

    if (paymentRes && (paymentRes as any).length > 0) {
      // 钱包余额支付已下线
      enabledPaymentMethods.value = (paymentRes as any[]).filter((m: any) => m.code !== 'BALANCE')
      const codes = enabledPaymentMethods.value.map((m: any) => m.code)
      if (!codes.includes(paymentMethod.value.toUpperCase()) && codes.length > 0) {
        paymentMethod.value = codes[0].toLowerCase()
      }
    }

  } catch (error) {
    console.error('Failed to load product detail', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* ==========================================
   PRODUCT DETAIL PAGE - Material Design 3
   ========================================== */

.product-detail-page {
  min-height: 100vh;
  background: var(--background, #ffffff);
}

/* Loading */
.pd-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}
.pd-loading-spin {
  width: 40px;
  height: 40px;
  border: 3px solid var(--outline-variant, #c1c6d6);
  border-top-color: var(--primary, #0059b5);
  border-radius: 50%;
  animation: pd-spin 0.8s linear infinite;
}
@keyframes pd-spin { to { transform: rotate(360deg); } }

.pd-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 20px;
  color: var(--text-muted, #888);
}
.pd-back-btn {
  padding: 10px 24px;
  border-radius: 8px;
  background: var(--primary, #0059b5);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

/* Main container */
.pd-main {
  max-width: 1280px;
  margin: 0 auto;
  padding: 150px 24px 80px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Hero: content left + sticky checkout right */
.pd-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.9fr);
  gap: 24px;
  align-items: start;
}

.pd-visual {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}
.pd-image-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 10;
  max-height: 420px;
  flex-shrink: 0;
  border-radius: 16px;
  overflow: hidden;
  background: var(--surface-container, #eeedf3);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
}
.pd-hero-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.pd-hero-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.pd-flag-xl { font-size: 120px; }
.pd-image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(26, 27, 31, 0.5) 0%, transparent 50%);
}
.pd-badges {
  position: absolute;
  top: 16px;
  left: 16px;
  display: flex;
  gap: 8px;
}
.pd-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 700;
}
.pd-badge--primary {
  background: var(--primary, #0059b5);
  color: #fff;
}
.pd-badge--surface {
  background: rgba(255,255,255,0.92);
  color: var(--text-primary, #1a1b1f);
  border: 1px solid var(--outline-variant, #c1c6d6);
}

/* Sticky purchase / checkout rail */
.pd-purchase {
  position: sticky;
  top: 96px;
  align-self: start;
  min-width: 0;
}
.pd-purchase-card {
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(193, 198, 214, 0.35);
  box-shadow: 0 12px 36px rgba(15, 23, 42, 0.06);
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 112px);
  overflow: hidden;
}
.pd-purchase-head {
  padding: 18px 20px 14px;
  border-bottom: 1px solid rgba(193, 198, 214, 0.28);
  background: linear-gradient(180deg, #f8fafc 0%, #fff 100%);
  flex-shrink: 0;
}
.pd-product-flag-name {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.pd-purchase-head-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.pd-flag-md { font-size: 28px; line-height: 1; margin-top: 2px; }
.pd-product-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary, #1a1b1f);
  margin: 0;
  line-height: 1.3;
}
.pd-head-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.pd-head-amount {
  font-size: 26px;
  font-weight: 800;
  color: var(--primary, #0059b5);
  letter-spacing: -0.02em;
  line-height: 1;
}
.pd-head-currency {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted, #717785);
}
.pd-head-hint {
  font-size: 13px;
  color: #ba1a1a;
  font-weight: 600;
}
.pd-purchase-body {
  padding: 8px 20px 12px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}
.pd-block {
  padding: 12px 0;
  border-bottom: 1px solid rgba(193, 198, 214, 0.22);
}
.pd-block:last-child { border-bottom: none; }
.pd-block--tight { padding-top: 10px; padding-bottom: 10px; }
.pd-block--summary {
  background: #f8fafc;
  margin: 4px -12px 0;
  padding: 12px;
  border: 1px solid rgba(193, 198, 214, 0.25);
  border-radius: 12px;
}
.pd-block-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary, #1a1b1f);
}
.pd-block-head--muted {
  font-weight: 600;
  color: var(--text-muted, #717785);
  margin-bottom: 6px;
}
.pd-step {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--primary, #0059b5);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.pd-required { color: #ba1a1a; font-weight: 700; }
.pd-optional { color: var(--text-muted, #717785); font-weight: 400; font-size: 12px; margin-left: auto; }
.pd-hint { color: var(--text-muted, #717785); font-weight: 400; font-size: 12px; }
.pd-purchase-footer {
  flex-shrink: 0;
  padding: 14px 20px 16px;
  border-top: 1px solid rgba(193, 198, 214, 0.28);
  background: #fff;
  box-shadow: 0 -8px 20px rgba(15, 23, 42, 0.04);
}
.pd-footer-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 13px;
  color: var(--text-muted, #717785);
}
.pd-footer-total strong {
  font-size: 20px;
  color: var(--primary, #0059b5);
  font-weight: 800;
  letter-spacing: -0.02em;
}

/* Pricing tier chips */
.pd-tier-chips {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 8px;
}
.pd-tier-chip { cursor: pointer; display: block; }
.pd-tier-chip input { display: none; }
.pd-tier-chip-inner {
  border: 1.5px solid var(--outline-variant, #c1c6d6);
  border-radius: 10px;
  padding: 10px 12px;
  background: #fff;
  transition: all 0.15s;
}
.pd-tier-chip.selected .pd-tier-chip-inner {
  border-color: var(--primary, #0059b5);
  background: color-mix(in srgb, var(--primary, #0059b5) 6%, transparent);
}
.pd-tier-top {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}
.pd-tier-months { font-size: 13px; font-weight: 700; color: var(--text-primary, #1a1b1f); }
.pd-tier-badge {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 100px;
  background: var(--primary, #0059b5);
  color: #fff;
  font-weight: 700;
}
.pd-tier-prices { display: flex; flex-direction: column; gap: 1px; }
.pd-tier-total { font-size: 14px; font-weight: 700; color: var(--primary, #0059b5); }
.pd-tier-per { font-size: 11px; color: var(--text-muted, #717785); }

/* Devices */
.pd-devices { display: flex; flex-direction: column; gap: 6px; }
.pd-device { cursor: pointer; display: block; }
.pd-device input { display: none; }
.pd-device-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border: 1.5px solid var(--outline-variant, #c1c6d6);
  border-radius: 10px;
  background: #fff;
  transition: all 0.15s;
}
.pd-device.selected .pd-device-inner {
  border-color: var(--primary, #0059b5);
  background: color-mix(in srgb, var(--primary, #0059b5) 6%, transparent);
}
.pd-device-text { display: flex; flex-direction: column; gap: 1px; flex: 1; min-width: 0; }
.pd-device-name { font-size: 13px; font-weight: 600; color: var(--text-primary, #1a1b1f); }
.pd-device-desc { font-size: 11px; color: var(--text-muted, #717785); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.pd-device-price { font-size: 13px; font-weight: 700; color: var(--primary, #0059b5); flex-shrink: 0; }
.pd-shipping-note {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-muted, #717785);
  margin-top: 8px;
}

/* Address form */
.pd-address-form { display: flex; flex-direction: column; gap: 10px; }
.pd-form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.pd-form-item { display: flex; flex-direction: column; gap: 4px; }
.pd-form-item--full { grid-column: 1 / -1; }
.pd-form-item label { font-size: 12px; color: var(--text-muted, #717785); font-weight: 500; }

/* Cost summary */
.pd-summary { display: flex; flex-direction: column; gap: 6px; margin-bottom: 8px; }
.pd-summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-muted, #717785);
}
.pd-summary-sub { font-size: 11px; color: var(--text-muted, #717785); }
.pd-discount { color: #16a34a; font-weight: 600; }
.pd-coupon { margin-top: 4px; }
.pd-coupon-row {
  display: flex;
  gap: 8px;
  align-items: stretch;
}
.pd-coupon-row :deep(.el-input__wrapper) {
  border-radius: 6px;
  min-height: 32px;
}
.pd-coupon-row :deep(.el-button) {
  border-radius: 6px;
  height: 32px;
  padding: 0 12px;
}
.pd-coupon-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #16a34a;
}
.pd-coupon-tip.error { color: #ef4444; }
.pd-total-na { color: #ba1a1a !important; }

/* Payment methods — compact grid */
.pd-payments {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
}
.pd-payment { cursor: pointer; display: block; min-width: 0; }
.pd-payment input { display: none; }
.pd-payment-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border: 1.5px solid var(--outline-variant, #c1c6d6);
  border-radius: 8px;
  background: #fff;
  transition: all 0.15s;
  min-height: 40px;
}
.pd-payment.selected .pd-payment-inner {
  border-color: var(--primary, #0059b5);
  background: color-mix(in srgb, var(--primary, #0059b5) 6%, transparent);
  box-shadow: inset 0 0 0 1px var(--primary, #0059b5);
}
.pd-payment.disabled .pd-payment-inner { opacity: 0.5; cursor: not-allowed; }
.pd-payment-icon {
  width: 24px; height: 24px;
  border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 900; flex-shrink: 0;
  color: #fff;
}
.pd-payment-icon svg { width: 14px; height: 14px; }
.pd-brand-mark {
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}
.usdt-icon { background: #26a17b; color: #fff; }
.wechat-icon { background: #07c160; color: #fff; }
.alipay-icon { background: #1677ff; color: #fff; }
.balance-icon { background: var(--primary, #0059b5); color: #fff; }
.card-icon { background: #1e3a5f; color: #fff; }
.default-icon { background: #9e9e9e; color: #fff; }
.pd-payment-text { display: flex; flex-direction: column; gap: 0; flex: 1; min-width: 0; }
.pd-payment-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary, #1a1b1f);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pd-payment-desc { font-size: 10px; color: var(--text-muted, #717785); line-height: 1.2; }
.pd-insufficient { color: #ba1a1a !important; }

/* Buy Now button */
.pd-buy-btn {
  width: 100%;
  padding: 14px;
  border-radius: 10px;
  background: var(--primary, #0059b5);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(0, 89, 181, 0.25);
}
.pd-buy-btn svg { width: 18px; height: 18px; }
.pd-buy-btn:hover:not(:disabled) {
  background: #00458f;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 89, 181, 0.3);
}
.pd-buy-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.pd-spin-icon { animation: pd-spin 0.8s linear infinite; }
.pd-buy-note {
  text-align: center;
  font-size: 11px;
  color: var(--text-muted, #717785);
  margin: 8px 0 0;
  line-height: 1.4;
}
.pd-setup-card { margin-top: 4px; }

.pd-bento {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  align-items: start;
}
.pd-bento-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.pd-bento-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  border: 1px solid rgba(193, 198, 214, 0.2);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.03);
}
.pd-bento-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary, #1a1b1f);
  margin: 0 0 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.pd-bento-title svg { width: 20px; height: 20px; color: var(--primary, #0059b5); }

.pd-note-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-primary, #1a1b1f);
}
.pd-specs-block--split {
  margin-top: 20px;
  padding-top: 8px;
  border-top: 1px solid rgba(193, 198, 214, 0.35);
}

/* 产品特性 */
.pd-features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
}
.pd-feature-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: var(--text-primary, #1a1b1f);
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--surface-container-low, #f4f3f8);
  line-height: 1.4;
}
.pd-feat-warn { background: #fff3f3; color: #ba1a1a; }

/* Setup steps */
.pd-setup-steps {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.pd-setup-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
}
.pd-setup-icon {
  width: 56px; height: 56px;
  border-radius: 50%;
  background: color-mix(in srgb, var(--primary, #0059b5) 12%, transparent);
  display: flex; align-items: center; justify-content: center;
  color: var(--primary, #0059b5);
}
.pd-setup-icon svg { width: 24px; height: 24px; }
.pd-setup-step h3 { font-size: 14px; font-weight: 700; color: var(--text-primary, #1a1b1f); margin: 0; }
.pd-setup-step p { font-size: 13px; color: var(--text-muted, #717785); margin: 0; line-height: 1.4; }

@media (max-width: 1024px) {
  .pd-hero { grid-template-columns: 1fr; }
  .pd-purchase {
    position: static;
    top: auto;
  }
  .pd-purchase-card {
    max-height: none;
  }
  .pd-image-wrap {
    aspect-ratio: 16 / 10;
    max-height: 280px;
  }
  .pd-bento { grid-template-columns: 1fr; }
}

@media (max-width: 600px) {
  .pd-main { padding: 120px 16px 60px; }
  .pd-setup-steps { grid-template-columns: 1fr; }
  .pd-form-row { grid-template-columns: 1fr; }
  .pd-tier-chips { grid-template-columns: repeat(2, 1fr); }
  .pd-features-grid { grid-template-columns: 1fr; }
  .pd-payments { grid-template-columns: 1fr; }
  .pd-purchase-head { padding: 16px; }
  .pd-purchase-body { padding: 4px 16px 12px; }
  .pd-purchase-footer { padding: 12px 16px 14px; }
}
</style>

