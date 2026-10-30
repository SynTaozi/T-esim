<template>
  <div class="profile-page">

    <!-- 全局导航栏 -->
    <SiteHeader />

    <!-- Mobile TopBar (below lg) -->
    <header class="mobile-topbar">
      <div class="mobile-brand">
        <span class="material-symbols-outlined">person</span>
        {{ authStore.user?.username || $t('profile.personalInfo') }}
      </div>
      <div class="mobile-topbar-right">
        <div class="mobile-topbar-btn" @click="showPasswordDialog = true">
          <span class="material-symbols-outlined">lock_reset</span>
        </div>
      </div>
    </header>

    <!-- Sidebar (lg+) -->
    <nav class="profile-sidebar">
      <!-- Brand -->
      <div class="sidebar-header">
        <div class="sidebar-user-row">
          <div class="sidebar-avatar">
            <img 
              src="https://pub-a9d701ee7be14d628bd79fc511b4e290.r2.dev/front/default_avatar.png" 
              alt="User Avatar"
              class="sidebar-avatar-img"
            />
          </div>
          <div class="sidebar-user-info">
            <h2>{{ authStore.user?.username || 'User' }}</h2>
            <p>{{ authStore.user?.email || 'User Account Dashboard' }}</p>
          </div>
        </div>
      </div>

      <!-- Nav Items -->
      <div class="sidebar-nav-items">
        <div class="nav-item" :class="{ active: currentTab === 'profile' }" @click="switchTab('profile')">
          <span class="material-symbols-outlined nav-icon">person</span>
          <span>{{ $t('profile.personalInfo') }}</span>
        </div>
        <div class="nav-item" :class="{ active: currentTab === 'orders' }" @click="switchTab('orders')">
          <span class="material-symbols-outlined nav-icon">shopping_bag</span>
          <span>{{ $t('profile.myOrders') }}</span>
        </div>
        <div class="nav-item" :class="{ active: currentTab === 'tickets' }" @click="switchTab('tickets')">
          <span class="material-symbols-outlined nav-icon">support_agent</span>
          <span>{{ $t('profile.ticketService') }}</span>
        </div>
        <div class="nav-item" v-if="false" :class="{ active: currentTab === 'phoneNumbers' }" @click="switchTab('phoneNumbers')">
          <span class="material-symbols-outlined nav-icon">cell_tower</span>
          <span>{{ $t('profile.myPhoneNumbers') }}</span>
        </div>
        <div class="nav-item" @click="showPasswordDialog = true">
          <span class="material-symbols-outlined nav-icon">lock_reset</span>
          <span>{{ $t('profile.changePassword') }}</span>
        </div>
      </div>

      <!-- Logout -->
      <div class="sidebar-bottom">
        <div class="logout-item" @click="handleLogout">
          <span class="material-symbols-outlined nav-icon">logout</span>
          <span>{{ $t('profile.logout') }}</span>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="profile-main">
      <div class="main-inner">
        <!-- Page Title Area -->
        <div class="page-title-area">
          <div>
            <h1 class="page-title">{{ currentTabTitle }}</h1>
            <p class="page-desc">{{ currentTabDesc }}</p>
          </div>
        </div>

        <!-- Tab Contents -->
        <div class="tab-content-area">
          <ProfilePersonalInfo
            v-show="currentTab === 'profile'"
            :form="form"
            @save="handleSave"
            @open-password="showPasswordDialog = true"
          />

          <ProfileOrders
            v-show="currentTab === 'orders'"
            :orders="orders"
            :orders-total="ordersTotal"
            :order-page-size="orderPageSize"
            :order-current-page="orderCurrentPage"
            :loading="ordersLoading"
            :order-stats="orderStats"
            :selected-payment="selectedPayment"
            :payment-methods="paymentMethods"
            :get-order-product-name="getOrderProductName"
            :get-order-status-text="getOrderStatusText"
            :get-order-regions="getOrderRegions"
            :get-order-data="getOrderData"
            :get-order-duration="getOrderDuration"
            :get-order-product-type="getOrderProductType"
            :get-payment-text="getPaymentText"
            :format-date="formatDate"
            :format-amount="formatAmount"
            @pay-order="handlePayOrder"
            @cancel-order="handleCancel"
            @select-payment="selectPayment"
            @page-change="handleOrderPageChange"
          />

          <ProfileTickets
            v-show="currentTab === 'tickets'"
            :tickets="tickets"
            :tickets-total="ticketsTotal"
            :page-size="pageSize"
            :current-page="currentPage"
            :loading="ticketsLoading"
            :ticket-stats="ticketStats"
            :get-priority-type="getPriorityType"
            :get-priority-text="getPriorityText"
            :get-status-type="getStatusType"
            :get-status-text="getStatusText"
            :format-date="formatDate"
            @submit-ticket="goToNewTicket"
            @view-ticket="viewTicket"
            @page-change="handlePageChange"
          />

          <ProfilePhoneNumbers
            v-if="false"
            v-show="currentTab === 'phoneNumbers'"
            :phone-numbers="phoneNumbers"
            :phone-total="phoneTotal"
            :page-size="phonePageSize"
            :current-page="phonePage"
            :loading="phoneLoading"
            :phone-stats="{
              total: phoneStats.totalCount,
              active: phoneStats.activeCount,
              expiring: phoneStats.expiringCount,
              expired: phoneStats.expiredCount
            }"
            :get-phone-status="(phone) => ({
              status: phone.status?.toLowerCase() || 'active',
              text: getPhoneStatusText(phone.status),
              canRenew: phone.status === 'ACTIVE' || phone.status === 'EXPIRING'
            })"
            :get-country-flag="() => '🌍'"
            :get-expiry-class="(date) => isExpiringSoon(date) ? 'expiring' : ''"
            :format-date="formatDate"
            @add-phone="() => router.push('/products')"
            @view-phone="viewPhoneDetail"
            @renew-phone="handleRenewPhone"
            @delete-phone="(id) => loadPhoneNumbers()"
            @confirm-renewal="() => {}"
            @page-change="handlePhonePageChange"
          />
        </div>
      </div>
    </main>

    <!-- Mobile Bottom Nav (below md) -->
    <nav class="mobile-bottom-nav">
      <div class="mobile-nav-item" :class="{ active: currentTab === 'profile' }" @click="switchTab('profile')">
        <div class="mobile-nav-icon" :class="{ active: currentTab === 'profile' }">
          <span class="material-symbols-outlined">person</span>
        </div>
        <span>{{ $t('profile.personalInfo') }}</span>
      </div>
      <div class="mobile-nav-item" :class="{ active: currentTab === 'orders' }" @click="switchTab('orders')">
        <div class="mobile-nav-icon" :class="{ active: currentTab === 'orders' }">
          <span class="material-symbols-outlined">shopping_bag</span>
        </div>
        <span>{{ $t('profile.myOrders') }}</span>
      </div>
      <div class="mobile-nav-item" :class="{ active: currentTab === 'tickets' }" @click="switchTab('tickets')">
        <div class="mobile-nav-icon" :class="{ active: currentTab === 'tickets' }">
          <span class="material-symbols-outlined">support_agent</span>
        </div>
        <span>{{ $t('profile.ticketService') }}</span>
      </div>
    </nav>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" :title="$t('profile.changePwdTitle')" width="min(450px, 95vw)">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item :label="$t('profile.currentPwd')" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="$t('profile.newPwd')" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="$t('profile.confirmPwd')" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleChangePassword">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 续费对话框 -->
    <el-dialog
      v-model="showRenewalDialog"
      :title="$t('phoneNumbers.renewTitle')"
      width="1000px"
      class="order-dialog renewal-checkout-dialog"
      :show-close="true"
      append-to-body>
      <div v-if="renewalPhone" class="order-content renewal-v2-content" style="max-height: 65vh; overflow-y: auto;">
        <div class="renewal-v2-hero">
          <div class="hero-left">
            <span class="hero-flag">📱</span>
            <div class="hero-meta">
              <h3>{{ renewalPhone.countryCode }} {{ renewalPhone.phoneNumber }}</h3>
              <p>{{ renewalPhone.country }} · {{ getPhoneValidityText(renewalPhone) }}</p>
              <span class="product-type-badge" v-if="renewalPhone.originalProductId">
                {{ renewalPhone.productType === 1 ? $t('profile.productTypePeriodic') : $t('profile.productTypeUnlimited') }}
              </span>
            </div>
          </div>
          <div class="hero-price">
            <span class="price-tag">{{ $t('phoneNumbers.total') }}</span>
            <div class="price-value">${{ renewalPhone.renewalPrice?.toFixed(2) || '0.00' }} <span>USD</span></div>
          </div>
        </div>

        <div class="renewal-v2-grid">
          <div class="renewal-v2-card renewal-v2-fee">
            <div class="v2-card-title">{{ $t('phoneNumbers.renewalAmount') }}</div>
            <div class="v2-row">
              <span class="v2-label">{{ $t('phoneNumbers.renewTitle') }}</span>
              <span class="v2-amount">${{ renewalPhone.renewalPrice?.toFixed(2) || '0.00' }} <em>USD</em></span>
            </div>
            <div class="v2-row v2-row-total">
              <span class="v2-label">{{ $t('phoneNumbers.total') }}</span>
              <span class="v2-amount">${{ renewalPhone.renewalPrice?.toFixed(2) || '0.00' }} <em>USD</em></span>
            </div>
            <div class="v2-note">{{ getPhoneValidityText(renewalPhone) }} · {{ $t('phoneNumbers.renewTitle') }}</div>
          </div>

          <div class="renewal-v2-card renewal-v2-pay" v-if="dynamicPaymentMethods.length > 0">
            <div class="v2-card-title">{{ $t('phoneNumbers.selectPayment') }}</div>
            <div class="payment-options">
            <label
              v-for="pm in dynamicPaymentMethods"
              :key="pm.code"
              class="payment-option"
              :class="{
                selected: renewalPaymentMethod === pm.code.toLowerCase(),
                disabled: pm.code === 'BALANCE' && userBalance < (renewalPhone?.renewalPrice || 0)
              }">
              <input
                type="radio"
                v-model="renewalPaymentMethod"
                :value="pm.code.toLowerCase()"
                :disabled="pm.code === 'BALANCE' && userBalance < (renewalPhone?.renewalPrice || 0)"
              />
              <div class="payment-content">
                <div class="payment-icon" :class="getPaymentIconClass(pm.code)">
                  <span v-if="pm.code === 'USDT'">₮</span>
                  <svg v-else-if="pm.code === 'WECHAT'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M8.5 11.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm7 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM12 2C6.48 2 2 6.48 2 12c0 2.17.7 4.18 1.88 5.82L2 22l4.18-1.88C7.82 21.3 9.83 22 12 22c5.52 0 10-4.48 10-10S17.52 2 12 2z"/>
                  </svg>
                  <svg v-else-if="pm.code === 'BALANCE'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1.41 16.09V20h-2.67v-1.93c-1.71-.36-3.16-1.46-3.27-3.4h1.96c.1 1.05.82 1.87 2.65 1.87 1.96 0 2.4-.98 2.4-1.59 0-.83-.44-1.61-2.67-2.14-2.48-.6-4.18-1.62-4.18-3.67 0-1.72 1.39-2.84 3.11-3.21V4h2.67v1.95c1.86.45 2.79 1.86 2.85 3.39H14.3c-.05-1.11-.64-1.87-2.22-1.87-1.5 0-2.4.68-2.4 1.64 0 .84.65 1.39 2.67 1.91s4.18 1.39 4.18 3.91c-.01 1.83-1.38 2.83-3.12 3.16z"/>
                  </svg>
                  <svg v-else-if="pm.code === 'CARD' || pm.code === 'BANK_CARD'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z"/>
                  </svg>
                  <span v-else-if="pm.code === 'ALIPAY' || pm.code === 'ANTOM_ALIPAY'" class="alipay-mark">支</span>
                </div>
                <div class="payment-info">
                  <span class="payment-name">{{ getPaymentMethodLabel(pm.code, t) }}</span>
                  <span v-if="pm.code === 'USDT'" class="payment-desc">{{ $t('payment.trc20Note') }}</span>
                  <span v-else-if="pm.code === 'BALANCE'" class="payment-desc balance-desc" :class="{ insufficient: userBalance < (renewalPhone?.renewalPrice || 0) }">
                    {{ userBalance >= (renewalPhone?.renewalPrice || 0) ? '' : $t('payment.balanceInsufficient') }}（${{ userBalance.toFixed(2) }} USD）
                  </span>
                  <span v-else class="payment-desc">{{ getPaymentMethodDesc(pm.code, t, pm.description) }}</span>
                </div>
                <div class="payment-check">
                  <svg v-if="renewalPaymentMethod === pm.code.toLowerCase()" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                  </svg>
                </div>
              </div>
            </label>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showRenewalDialog = false" :disabled="renewalSubmitting" size="large">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" class="btn-submit" @click="confirmRenewal" :disabled="renewalSubmitting" :loading="renewalSubmitting" size="large">
            {{ renewalSubmitting ? $t('checkout.processing') : $t('phoneNumbers.confirmRenew') }} ${{ renewalPhone?.renewalPrice?.toFixed(2) || '0.00' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 套餐详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="$t('phoneNumbers.detailTitle')"
      width="600px"
      class="phone-detail-dialog"
      :show-close="true"
      append-to-body>
      <div v-if="detailPhone" class="pd-body">
        <div class="pd-hero" :class="`pd-hero--${detailPhone.status?.toLowerCase()}`">
          <div class="pd-hero-main">
            <div class="pd-code">{{ detailPhone.countryCode }}</div>
            <div class="pd-number-block">
              <div class="pd-number">{{ detailPhone.phoneNumber }}</div>
              <div class="pd-country">📍 {{ detailPhone.country }}</div>
            </div>
          </div>
          <div class="pd-hero-side">
            <span class="pd-status" :class="`pd-status--${detailPhone.status?.toLowerCase()}`">
              <span class="pd-status-dot"></span>
              {{ getPhoneStatusText(detailPhone.status) }}
            </span>
            <div class="pd-price-block">
              <span class="pd-price-label">{{ $t('phoneNumbers.renewalPrice') }}</span>
              <div class="pd-price">${{ detailPhone.renewalPrice?.toFixed(2) || '0.00' }} <em>USD</em></div>
            </div>
          </div>
        </div>
        <div class="pd-grid">
          <div class="pd-cell">
            <span class="pd-cell-label">{{ $t('phoneNumbers.country') }}</span>
            <span class="pd-cell-value">{{ detailPhone.country || '-' }}</span>
          </div>
          <div class="pd-cell" v-if="detailPhone.carrier">
            <span class="pd-cell-label">{{ $t('phoneNumbers.carrier') }}</span>
            <span class="pd-cell-value">{{ detailPhone.carrier }}</span>
          </div>
          <div class="pd-cell" v-if="detailPhone.numberType">
            <span class="pd-cell-label">{{ $t('phoneNumbers.numberType') }}</span>
            <span class="pd-cell-value">{{ detailPhone.numberType }}</span>
          </div>
          <div class="pd-cell">
            <span class="pd-cell-label">{{ $t('phoneNumbers.validity') }}</span>
            <span class="pd-cell-value">{{ getPhoneValidityText(detailPhone) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="pd-footer">
          <el-button @click="showDetailDialog = false">{{ $t('common.close') }}</el-button>
          <el-button
            v-if="detailPhone.status !== 'CANCELLED'"
            type="primary"
            @click="showDetailDialog = false; handleRenewPhone(detailPhone)">{{ $t('phoneNumbers.renew') }}</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import SiteHeader from '@/components/SiteHeader.vue'
import ProfilePersonalInfo from '@/components/profile/ProfilePersonalInfo.vue'
import ProfileOrders from '@/components/profile/ProfileOrders.vue'
import ProfileTickets from '@/components/profile/ProfileTickets.vue'
import ProfilePhoneNumbers from '@/components/profile/ProfilePhoneNumbers.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, List, ShoppingCart, SwitchButton, ChatDotRound, Lock, Delete, Plus, Wallet as WalletIcon, CreditCard, WalletFilled, Document, Clock, Message, Phone, Location, Van, ChatLineSquare, Warning, Check, Money, CircleCheck, Share } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { paymentMethodApi } from '@/api/paymentMethod'
import { orderApi, Order } from '@/api/order'
import { getTickets } from '@/api/ticket'
import { userPhoneNumberApi } from '@/api/userPhoneNumber'
import { getBalance, createRecharge } from '@/api/recharge'

import { getCurrentLocale } from '@/i18n'
import { navigateToLogin } from '@/utils/authNavigation'
import { navigateByPaymentMethod } from '@/utils/stripeCheckout'
import { isOrderPaymentExpired } from '@/utils/orderPaymentExpire'
import { getPaymentMethodLabel, getPaymentMethodDesc } from '@/utils/paymentMethodLabel'

const { locale, t } = useI18n({ useScope: 'global' })
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const passwordFormRef = ref<FormInstance>()
const showPasswordDialog = ref(false)
const balance = ref(0)
// userBalance 和 balance 相同（都是元），用于内部外部引用
const userBalance = computed(() => balance.value)

// 当前激活的标签页
const currentTab = ref('profile')

// 当前 Tab 标题
const currentTabTitle = computed(() => {
  const map: Record<string, string> = {
    profile: t('profile.personalInfo'),
    orders: t('profile.myOrders'),
    tickets: t('profile.ticketService'),
    recharge: t('profile.recharge'),
    phoneNumbers: t('profile.myPhoneNumbers')
  }
  return map[currentTab.value] || t('profile.personalInfo')
})

const currentTabDesc = computed(() => {
  const map: Record<string, string> = {
    profile: t('profile.personalInfoDesc'),
    orders: t('profile.myOrdersDesc'),
    tickets: t('profile.ticketServiceDesc'),
    recharge: t('profile.rechargeDesc'),
    phoneNumbers: t('profile.myPhoneNumbersDesc')
  }
  return map[currentTab.value] || ''
})

// 订单相关
const orders = ref<Order[]>([])
const ordersTotal = ref(0)
const orderPageSize = ref(3)
const orderCurrentPage = ref(1)
const selectedPayment = ref<Record<number, string>>({})

// 订单汇总（所有订单，不只是当前页）
const orderStats = ref({
  totalCount: 0,
  pendingCount: 0,
  paidCount: 0,
  totalAmount: 0
})

// 购物车相关

// 工单相关
const tickets = ref<any[]>([])
const ticketsLoading = ref(false)
const ordersLoading = ref(false)
const ticketsTotal = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)

// 工单统计
const ticketStats = computed(() => {
  const all = ticketsTotal.value
  const pending = tickets.value.filter(t => t.status === 'OPEN' || t.status === 'PENDING').length
  // 仅关闭的工单算「已解决」；客服已回复(RESOLVED)不算
  const resolved = tickets.value.filter(t => t.status === 'CLOSED').length
  return { all, pending, resolved }
})

// 充值相关
const rechargeList = ref<any[]>([])
const rechargeActiveTab = ref('balance')
const submitting = ref(false)
const rechargeLoading = ref(false)
const rechargePage = ref(1)
const rechargePageSize = ref(10)
const rechargeTotal = ref(0)

// 电话套餐相关
const phoneNumbers = ref<any[]>([])
const phoneLoading = ref(false)
const phonePage = ref(1)
const phonePageSize = ref(4)
const phoneTotal = ref(0)
const phoneStats = ref({
  totalCount: 0,
  activeCount: 0,
  expiredCount: 0,
  totalSpent: 0,
  expiringCount: 0
})

// 充值统计
const rechargeStats = computed(() => {
  const totalRecharge = rechargeList.value
    .filter(r => r.type === 'RECHARGE' && r.status === 'PAID')
    .reduce((sum, r) => sum + (r.amount || 0), 0)
  const successCount = rechargeList.value.filter(r => r.status === 'PAID').length
  const pendingCount = rechargeList.value.filter(r => r.status === 'PENDING').length
  return { totalRecharge, successCount, pendingCount }
})

// 余额充值
const presetAmounts = [10, 20, 50, 100, 200]
const rechargeAmount = ref(50)
const selectedPaymentMethod = ref('') // 默认空，强制用户选择支付方式

// 动态支付方式
const dynamicPaymentMethods = ref<any[]>([])
const iconMap: Record<string, any> = { Message, Money, WalletFilled, CreditCard }

const paymentMethods = computed(() => {
  // 依赖 locale，切换中英文时重新计算 label
  void locale.value
  if (dynamicPaymentMethods.value.length > 0) {
    return dynamicPaymentMethods.value.map((pm: any) => ({
      value: String(pm.code || '').toUpperCase(),
      label: getPaymentMethodLabel(pm.code, t) || pm.code
    }))
  }
  return []
})

// 续费表单
const renewalForm = reactive({
  orderId: '',
  rechargeNo: '',
  periodValue: 1,
  periodUnit: 'MONTH',
  paymentMethod: '' // 默认空，强制用户选择支付方式
})

// 续费弹窗相关
const showRenewalDialog = ref(false)
const renewalPhone = ref<any>(null)
const renewalSubmitting = ref(false)
const renewalPaymentMethod = ref('USDT')

// 详情弹窗相关
const showDetailDialog = ref(false)
const detailPhone = ref<any>(null)

// 余额支付方式 - 排除余额支付
const balancePaymentMethods = computed(() => {
  void locale.value
  if (dynamicPaymentMethods.value.length > 0) {
    return dynamicPaymentMethods.value.filter(pm => pm.code !== 'BALANCE').map(pm => ({
      value: pm.code,
      label: getPaymentMethodLabel(pm.code, t),
      icon: iconMap[pm.icon] || WalletFilled
    }))
  }
  // 默认值（后端未配置时）
  return [
    { value: 'WECHAT', label: t('payment.wechatPay'), icon: Message },
    { value: 'ALIPAY', label: t('payment.alipay'), icon: Money },
    { value: 'USDT', label: t('payment.usdt'), icon: WalletFilled },
    { value: 'BANK_CARD', label: t('payment.bankCard'), icon: CreditCard }
  ]
})

// 个人资料表单
const form = reactive({
  username: '',
  email: '',
  phone: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error(t('profile.pwdMismatch')))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: () => t('profile.inputCurrentPwd'), trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: () => t('profile.inputNewPwd'), trigger: 'blur' },
    { min: 6, message: () => t('profile.pwdTooShort'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: () => t('profile.inputConfirmPwd'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 切换标签页
function switchTab(tab: string) {
  currentTab.value = tab

  // 懒加载各标签页数据
  if (tab === 'orders' && orders.value.length === 0) {
    ordersLoading.value = true
    loadPaymentMethods()
    loadOrders()
  } else if (tab === 'tickets' && tickets.value.length === 0) {
    loadTickets()
  } else if (tab === 'recharge') {
    loadBalance()
    loadRecharges()
    loadPaymentMethods()
  } else if (tab === 'phoneNumbers' && phoneNumbers.value.length === 0) {
    loadPhoneNumbers()
    loadPhoneStats()
  }
}

// 跳转到充值续费页面
function goToRecharge() {
  router.push('/recharge')
}

// 加载余额
function loadBalance() {
  Promise.resolve({ balance: 0 }).then((res: any) => {
    console.log('Balance loaded:', res)
    // 后端返回分，除以100转换为元显示
    const data = res.data || res
    balance.value = (data.balance || 0) / 100
  }).catch((err) => {
    console.error('Load balance error:', err)
    balance.value = 0
  })
}

function loadRecharges() {
  rechargeList.value = []
  rechargeTotal.value = 0
  rechargeLoading.value = false
}

// 加载支付方式
async function loadPaymentMethods() {
  try {
    const res = await paymentMethodApi.getEnabled()
    dynamicPaymentMethods.value = res.data || res || []
    if (dynamicPaymentMethods.value.length > 0 && !selectedPaymentMethod.value) {
      selectedPaymentMethod.value = String(dynamicPaymentMethods.value[0].code || '').toUpperCase()
    }
    initializeOrderPaymentSelections()
  } catch (err) {
    console.error('Load payment methods error:', err)
    dynamicPaymentMethods.value = []
  }
}

function initializeOrderPaymentSelections() {
  if (!orders.value.length || !paymentMethods.value.length) return

  const enabledValues = new Set(paymentMethods.value.map(pm => pm.value))
  const firstEnabled = paymentMethods.value[0]?.value

  orders.value.forEach((order: any) => {
    if (order.status !== 0 || selectedPayment.value[order.id]) return

    const orderMethod = String(order.paymentMethod || '').toUpperCase()
    if (orderMethod && enabledValues.has(orderMethod)) {
      selectedPayment.value[order.id] = orderMethod
      return
    }

    if (firstEnabled) {
      selectedPayment.value[order.id] = firstEnabled
    }
  })
}

// 加载订单
function loadOrders() {
  ordersLoading.value = true
  orderApi.getOrdersPaginated(orderCurrentPage.value - 1, orderPageSize.value).then((res: any) => {
    console.log('Orders API response:', res)
    if (res.code === 200 && res.data) {
      orders.value = res.data.content || []
      ordersTotal.value = res.data.totalElements || 0
      initializeOrderPaymentSelections()
      console.log('Orders loaded:', orders.value.length, 'items:', orders.value)
    }
  }).catch((error: any) => {
    console.error('Failed to load orders', error)
    if (error.code === 401 || error.response?.status === 401) {
      ElMessage.error(t('orders.loginExpired'))
      authStore.logout()
      navigateToLogin(router, route)
    }
  }).finally(() => {
    ordersLoading.value = false
  })
  // 加载所有订单汇总
  loadOrderStats()
}

// 加载所有订单汇总
function loadOrderStats() {
  orderApi.getOrders().then((res: any) => {
    if (res.code === 200 && res.data) {
      const allOrders = res.data || []
      orderStats.value = {
        totalCount: allOrders.length,
        pendingCount: allOrders.filter((o: any) => o.status === 0).length,
        paidCount: allOrders.filter((o: any) => o.status >= 1).length,
        totalAmount: allOrders
          .filter((o: any) => o.status >= 1)
          .reduce((sum: number, o: any) => {
            const amount = typeof o.totalAmount === 'string' ? parseFloat(o.totalAmount) : (o.totalAmount || 0)
            return sum + amount
          }, 0)
      }
    }
  }).catch((err: any) => {
    console.error('Failed to load order stats', err)
  })
}

// 订单分页切换
function handleOrderPageChange(page: number) {
  orderCurrentPage.value = page
  loadOrders()
}

// 加载工单
function loadTickets() {
  ticketsLoading.value = true
  getTickets({ page: currentPage.value - 1, size: pageSize.value })
    .then((res: any) => {
      tickets.value = res.tickets || []
      ticketsTotal.value = res.total || 0
    })
    .catch((err: any) => {
      console.error('Failed to load tickets', err)
      if (err.code === 401 || (err.response && err.response.status === 401)) {
        authStore.logout()
        ElMessageBox.alert(t('newTicket.loginExpired'), t('common.warning'), {
          confirmButtonText: t('common.confirm'),
          type: 'warning'
        }).then(() => {
          navigateToLogin(router, route)
        }).catch(() => {
          navigateToLogin(router, route)
        })
      } else {
        ElMessage.error(err.message || 'Failed to load tickets')
      }
    })
    .finally(() => {
      ticketsLoading.value = false
    })
}

// 格式化日期
function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 支付方式文本（跟随当前语言）
function getPaymentText(method: string): string {
  if (!method) return '-'

  // 如果是 i18n key（如 payment.usdt），提取 code
  const code = method.includes('.') ? method.split('.')[1].toUpperCase() : String(method).toUpperCase()
  const localized = getPaymentMethodLabel(code, t)
  if (localized) return localized

  const map: Record<string, string> = {
    USDT_TRC20: 'USDT (TRC20)',
    USDT_ERC20: 'USDT (ERC20)',
    PAYPAL: 'PayPal'
  }
  return map[code] || method || '-'
}

function getPaymentIconClass(code: string): string {
  const normalized = String(code || '').toUpperCase()
  const classMap: Record<string, string> = {
    USDT: 'usdt-icon',
    WECHAT: 'wechat-icon',
    ALIPAY: 'alipay-icon',
    ANTOM_ALIPAY: 'alipay-icon',
    BANK_CARD: 'card-icon',
    CARD: 'card-icon',
    BALANCE: 'balance-icon'
  }
  return classMap[normalized] || 'default-icon'
}

// 获取订单中第一个产品类型的 item（跳过快递费和设备）
function getProductItem(order: any): any | null {
  if (!order.items || order.items.length === 0) return null
  // 优先查找产品项
  const productItem = order.items.find((item: any) => item.itemType === 'product')
  if (productItem) return productItem
  // 续费订单：查找 renewal 项（productId 实际是 phoneNumberId）
  return order.items.find((item: any) => item.itemType === 'renewal') || null
}

// 获取订单产品名称
function getOrderProductName(order: any): string {
  const productItem = getProductItem(order)
  if (productItem) {
    // 英文环境优先使用 nameEn
    if (locale.value === 'en' && productItem.nameEn) {
      return productItem.nameEn
    }
    return productItem.productName || t('payment.esimPackage')
  }
  // 如果没有产品 item，显示所有 item 的名称
  if (order.items && order.items.length > 0) {
    const names = order.items.map((item: any) => {
      if (locale.value === 'en' && item.nameEn) {
        return item.nameEn
      }
      return item.productName
    }).filter(Boolean)
    return names.join(', ') || t('payment.esimPackage')
  }
  return order.productName || t('payment.esimPackage')
}

// 订单状态映射（中文/英文）
const orderStatusMap: Record<number, Record<string, string>> = {
  0: { zh: '待支付', en: 'Pending Payment' },
  1: { zh: '已支付', en: 'Paid' },
  2: { zh: '已发货', en: 'Shipped' },
  3: { zh: '已完成', en: 'Completed' },
  4: { zh: '已取消', en: 'Cancelled' }
}

// 获取订单状态文本（根据语言）
function getOrderStatusText(status: number): string {
  const lang = locale.value === 'en' ? 'en' : 'zh'
  return orderStatusMap[status]?.[lang] || '-'
}

// 地区代码到名称映射（中文/英文）
const regionMap: Record<string, Record<string, string>> = {
  'Asia': { zh: '亚洲', en: 'Asia' },
  'Europe': { zh: '欧洲', en: 'Europe' },
  'America': { zh: '美洲', en: 'America' },
  'Oceania': { zh: '大洋洲', en: 'Oceania' },
  'Africa': { zh: '非洲', en: 'Africa' },
  'Global': { zh: '全球', en: 'Global' }
}

// 获取适用地区（支持多地区）
function getOrderRegions(order: any): string {
  const productItem = getProductItem(order)
  if (!productItem) return ''
  
  // 从 applicableRegions 获取（JSON 数组格式）
  if (productItem.applicableRegions) {
    try {
      let regions: string[]
      if (typeof productItem.applicableRegions === 'string') {
        regions = JSON.parse(productItem.applicableRegions)
      } else {
        regions = productItem.applicableRegions
      }
      // 根据语言设置返回对应名称
      const lang = locale.value === 'en' ? 'en' : 'zh'
      return regions.map((r: string) => regionMap[r]?.[lang] || r).join(', ')
    } catch (e) {
      return productItem.applicableRegions
    }
  }
  
  // 兼容旧的 region 字段
  if (productItem.region) {
    const lang = locale.value === 'en' ? 'en' : 'zh'
    return regionMap[productItem.region]?.[lang] || productItem.region
  }
  
  return ''
}

// 获取订单覆盖国家
function getOrderCountries(order: any): string {
  if (order.countries) return order.countries
  const productItem = getProductItem(order)
  if (productItem) {
    return productItem.countries || productItem.coveredCountries || ''
  }
  return ''
}

// 获取订单流量规格
function getOrderData(order: any): string {
  if (order.dataLimit) return order.dataLimit
  const productItem = getProductItem(order)
  if (productItem) {
    if (productItem.dataLimit) return productItem.dataLimit
    if (productItem.specifications) return productItem.specifications
  }
  return ''
}

// 获取订单有效期
function getOrderDuration(order: any): string {
  if (order.duration) return order.duration
  if (order.validityPeriod) return order.validityPeriod
  const productItem = getProductItem(order)
  if (productItem) {
    if (productItem.duration) return productItem.duration
    if (productItem.validityPeriod) return productItem.validityPeriod
    // validityDays 是天数
    if (productItem.validityDays) {
      const days = productItem.validityDays
      if (days >= 30) {
        // 【修复】使用本地化的月份文本，确保翻译正确返回
        const monthText = t('profile.month')
        return `${Math.floor(days / 30)}${monthText || '个月'}`
      }
      // 【修复】使用本地化的天数文本，确保翻译正确返回
      const dayText = t('profile.day')
      return `${days}${dayText || '天'}`
    }
  }
  return ''
}

// 获取订单产品类型
// 获取订单产品类型
function getOrderProductType(order: any): string {
  const productItem = getProductItem(order)
  if (productItem && productItem.productType !== null && productItem.productType !== undefined) {
    return productItem.productType === 1 ? t('profile.productTypePeriodic') : t('profile.productTypeUnlimited')
  }
  return '-'
}

// 选择支付方式
function selectPayment(orderId: number, paymentMethod: string) {
  if (!paymentMethods.value.some(pm => pm.value === paymentMethod)) return
  selectedPayment.value[orderId] = paymentMethod
}

// 支付订单
async function handlePayOrder(order: any) {
  // 超时待支付：先取消，不进入支付页（USDT 与其它支付方式统一）
  if (isOrderPaymentExpired(order)) {
    try {
      await orderApi.cancelOrder(order.id)
      ElMessage.warning(t('orders.payExpiredCancelled') || '订单已超时，已自动取消')
      loadOrders()
    } catch (e: any) {
      ElMessage.error(e?.message || t('orders.cancelFailed') || '取消失败')
      loadOrders()
    }
    return
  }

  const enabledPaymentMethods = paymentMethods.value.map(pm => pm.value)
  if (enabledPaymentMethods.length === 0) {
    ElMessage.warning(t('payment.noPaymentMethods') || '后台暂未开启可用支付方式')
    return
  }

  const preferredMethod = String(selectedPayment.value[order.id] || order.paymentMethod || '').toUpperCase()
  const paymentMethod = enabledPaymentMethods.includes(preferredMethod)
    ? preferredMethod
    : enabledPaymentMethods[0]

  selectedPayment.value[order.id] = paymentMethod
  const orderId = order.id

  await navigateByPaymentMethod({
    paymentMethod,
    orderId,
    orderNo: order.orderNo,
    email: authStore.user?.email,
    router,
  })
}

// 格式化金额
function formatAmount(amount: any): string {
  if (amount === null || amount === undefined) return '0.00'
  if (typeof amount === 'string') {
    const parsed = parseFloat(amount)
    return isNaN(parsed) ? '0.00' : parsed.toFixed(2)
  }
  if (typeof amount === 'object' && amount.toFixed) {
    return amount.toFixed(2)
  }
  return Number(amount).toFixed(2)
}

// 获取待支付订单数
function getPendingCount(): number {
  return orders.value.filter(o => o.status === 0).length
}

// 获取已支付订单数
function getPaidCount(): number {
  return orders.value.filter(o => o.status === 1 || o.status === 2 || o.status === 3).length
}

// 获取累计金额
function getTotalAmount(): string {
  const total = orders.value
    .filter(o => o.status >= 1)
    .reduce((sum: number, o: any) => {
      const amount = typeof o.totalAmount === 'string' ? parseFloat(o.totalAmount) : (o.totalAmount || 0)
      return sum + amount
    }, 0)
  return total.toFixed(2)
}

// 工单类型文本
function getTypeText(type: string) {
  const map: any = {
    GENERAL: t('tickets.general'),
    REFUND: t('tickets.refund'),
    TECHNICAL: t('tickets.technical'),
    BILLING: t('tickets.billing'),
    OTHER: t('tickets.other')
  }
  return map[type] || type
}

// 工单优先级
function getPriorityType(priority: string) {
  const map: any = {
    LOW: 'info',
    MEDIUM: '',
    HIGH: 'warning',
    URGENT: 'danger'
  }
  return map[priority] || ''
}

function getPriorityText(priority: string) {
  const map: any = {
    LOW: t('tickets.low'),
    MEDIUM: t('tickets.medium'),
    HIGH: t('tickets.high'),
    URGENT: t('tickets.urgent')
  }
  return map[priority] || priority
}

// 工单状态
function getStatusType(status: string) {
  const map: any = {
    OPEN: 'success',
    PENDING: 'warning',
    RESOLVED: 'info',
    CLOSED: ''
  }
  return map[status] || ''
}

function getStatusText(status: string) {
  const map: any = {
    OPEN: t('tickets.pending'),
    PENDING: t('tickets.processing'),
    RESOLVED: t('tickets.replied'),
    CLOSED: t('tickets.resolved')
  }
  return map[status] || status
}

// 充值状态
function getRechargeStatusType(status: string) {
  const map: any = {
    PENDING: 'warning',
    PAID: 'success',
    FAILED: 'danger',
    REFUNDED: 'info'
  }
  return map[status] || ''
}

function getRechargeStatusText(status: string) {
  const map: any = {
    PENDING: t('recharge.pending'),
    PAID: t('recharge.paid'),
    FAILED: t('recharge.failed'),
    REFUNDED: t('recharge.refunded')
  }
  return map[status] || status
}

// 取消订单
async function handleCancel(orderId: number) {
  try {
    await ElMessageBox.confirm(t('profile.paymentConfirm'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('profile.considerCancel'),
      type: 'warning'
    })
    
    const res: any = await orderApi.cancelOrder(orderId)
    if (res.code === 200) {
      ElMessage.success(t('profile.orderCancelled'))
      loadOrders()
    } else {
      ElMessage.error(res.message || t('profile.cancelFailed'))
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || t('profile.cancelFailed'))
    }
  }
}

// 购物车方法
async function handleQuantityChange(id: number, quantity: number) {
  // await cartStore.updateItem(id, quantity)
}

async function handleRemove(id: number) {
  // await cartStore.removeItem(id)
  // cart removed
}

function handleCheckout() {
  router.push('/checkout')
}

// 工单方法
function handlePageChange(page: number) {
  currentPage.value = page
  loadTickets()
}

function viewTicket(row: any) {
  router.push(`/ticket/${row.id}`)
}

function goToNewTicket() {
  router.push('/ticket/new')
}

// 充值方法
function onOrderChange() {
  if (renewalForm.orderId) {
    renewalForm.periodValue = 1
    renewalForm.periodUnit = 'MONTH'
  }
}

async function handleBalanceRecharge() {
  if (submitting.value) return
  if (rechargeAmount.value <= 0) {
    ElMessage.warning(t('recharge.invalidAmount'))
    return
  }
  // 必须选择支付方式
  if (!selectedPaymentMethod.value) {
    ElMessage.warning(t('recharge.selectPaymentMethod'))
    return
  }

  submitting.value = true

  try {
    const res: any = await createRecharge({
      type: 'RECHARGE',
      amount: rechargeAmount.value,
      paymentMethod: selectedPaymentMethod.value
    })
    
    console.log('Recharge created:', res)
    
    // 获取充值订单号
    const rechargeNo = res?.recharge?.rechargeNo
    const paymentMethod = (res?.recharge?.paymentMethod || selectedPaymentMethod.value).toUpperCase()
    
    if (rechargeNo) {
      await navigateByPaymentMethod({
        paymentMethod,
        orderId: rechargeNo,
        orderNo: rechargeNo,
        email: authStore.user?.email,
        router,
      })
    } else {
      ElMessage.error('创建充值订单失败')
    }
  } catch (err: any) {
    console.error('Recharge error:', err)
    ElMessage.error(err.message || t('recharge.createFailed'))
  } finally {
    submitting.value = false
  }
}

async function handleRenewal() {
  if (submitting.value) return
  if (!renewalForm.orderId) {
    ElMessage.warning(t('recharge.selectOrderTip'))
    return
  }
  // 必须选择支付方式
  if (!renewalForm.paymentMethod) {
    ElMessage.warning(t('recharge.selectPaymentMethod'))
    return
  }


  submitting.value = true
  
  try {
    const res: any = await createRecharge({
      type: 'RENEWAL',
      amount: 4, // 固定金额或根据选择计算
      paymentMethod: renewalForm.paymentMethod,
      orderId: renewalForm.orderId,
      periodValue: renewalForm.periodValue,
      periodUnit: renewalForm.periodUnit,
      remarks: renewalForm.rechargeNo || ''
    })
    
    console.log('Renewal created:', res)
    
    // 获取续费订单号
    const rechargeNo = res?.recharge?.rechargeNo
    const paymentMethod = (res?.recharge?.paymentMethod || renewalForm.paymentMethod).toUpperCase()
    
    if (rechargeNo) {
      await navigateByPaymentMethod({
        paymentMethod,
        orderId: rechargeNo,
        orderNo: rechargeNo,
        email: authStore.user?.email,
        router,
      })
    } else {
      ElMessage.error('创建续费订单失败')
    }
  } catch (err: any) {
    console.error('Renewal error:', err)
    ElMessage.error(err.message || t('recharge.createFailed'))
  } finally {
    submitting.value = false
  }
}

async function handlePay(row: any) {
  const paymentMethod = (row.paymentMethod || 'USDT').toUpperCase()
  const orderId = row.rechargeNo || row.id

  await navigateByPaymentMethod({
    paymentMethod,
    orderId,
    email: authStore.user?.email,
    router,
  })
}

// 保存资料
function handleSave() {
  import('@/api/index').then(({ default: request }) => {
    request.put('/auth/profile', { phone: form.phone }).then(() => {
      ElMessage.success(t('profile.saved'))
    }).catch(() => {
      ElMessage.error(t('common.saveFailed') || '保存失败')
    })
  })
}

// 修改密码
function handleChangePassword() {
  if (!passwordFormRef.value) return

  passwordFormRef.value.validate((valid) => {
    if (valid) {
      import('@/api/index').then(({ default: request }) => {
        request.post('/auth/change-password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        }).then(() => {
          ElMessage.success(t('profile.pwdChanged'))
          showPasswordDialog.value = false
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
        }).catch((err: any) => {
          ElMessage.error(err?.response?.data?.message || err?.message || '修改失败，请检查原密码')
        })
      })
    }
  })
}

// 退出登录
function handleLogout() {
  authStore.logout()
  ElMessage.success(t('profile.logoutSuccess'))
  router.push('/')
}

// 监听充值分页变化
watch(rechargePage, () => {
  if (currentTab.value === 'recharge') {
    loadRecharges()
  }
})

watch(phonePage, () => {
  if (currentTab.value === 'phoneNumbers') {
    loadPhoneNumbers()
  }
})

onMounted(() => {
  // 处理 URL 参数
  if (route.query.ticket === 'true') {
    switchTab('tickets')
  }
  
  // 处理 tab 参数（从其他页面跳转过来时选择对应标签页）
  const tabParam = route.query.tab as string
  if (tabParam && ['profile', 'orders', 'tickets', 'recharge', 'phoneNumbers'].includes(tabParam)) {
    switchTab(tabParam)
  }
  
  if (authStore.user) {
    form.username = authStore.user.username || ''
    form.email = authStore.user.email || ''
    form.phone = authStore.user.phone || ''
  }
  // 从服务端重新加载最新资料（确保手机号等字段是最新的）
  import('@/api/index').then(({ default: request }) => {
    request.get('/auth/profile').then((res: any) => {
      const user = res?.data || res
      if (user) {
        form.phone = user.phone || form.phone
        form.email = user.email || form.email
        form.username = user.username || form.username
      }
    }).catch(() => {})
  })
  loadBalance()
  loadPaymentMethods()
})

// 加载电话套餐列表
function loadPhoneNumbers() {
  phoneLoading.value = true
  userPhoneNumberApi.getList({
    page: phonePage.value - 1,
    size: phonePageSize.value
  }).then((res: any) => {
    if (res.code === 200 && res.data) {
      phoneNumbers.value = res.data.content || []
      phoneTotal.value = res.data.totalElements || 0
    }
  }).catch((err: any) => {
    console.error('Load phone numbers error:', err)
  }).finally(() => {
    phoneLoading.value = false
  })
}

// 电话套餐分页切换
function handlePhonePageChange(page: number) {
  phonePage.value = page
  loadPhoneNumbers()
}

// 加载电话套餐统计
function loadPhoneStats() {
  userPhoneNumberApi.getStats().then((res: any) => {
    if (res.code === 200 && res.data) {
      phoneStats.value = {
        totalCount: res.data.totalCount || 0,
        activeCount: res.data.activeCount || 0,
        expiredCount: res.data.expiredCount || 0,
        totalSpent: res.data.totalSpent || 0,
        expiringCount: res.data.expiringCount || 0
      }
    }
  }).catch((err: any) => {
    console.error('Load phone stats error:', err)
  })
}

// 电话套餐状态
function getPhoneStatusType(status: string) {
  const map: any = {
    ACTIVE: 'success',
    EXPIRED: 'warning',
    CANCELLED: 'info'
  }
  return map[status] || ''
}

function getPhoneStatusText(status: string) {
  const map: any = {
    ACTIVE: t('phoneNumbers.active'),
    EXPIRED: t('phoneNumbers.expired'),
    CANCELLED: t('phoneNumbers.cancelled')
  }
  return map[status] || status
}

// 是否即将到期（7天内）—— 仅内部逻辑使用，不再用于展示
function isExpiringSoon(expiryDate: string): boolean {
  if (!expiryDate) return false
  const expiry = new Date(expiryDate)
  const now = new Date()
  const diff = (expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
  return diff > 0 && diff <= 7
}

// 展示套餐有效期（直接取产品配置的 validityDays，不做任何计算）
function getPhoneValidityText(phone: any): string {
  if (phone.validityDays != null && phone.validityDays > 0) {
    const d = phone.validityDays
    if (d % 365 === 0) return `${d / 365} ${t('phoneNumbers.year') || '年'}`
    if (d % 30 === 0) return `${d / 30} ${t('phoneNumbers.month') || '个月'}`
    return `${d} ${t('footer.days') || '天'}`
  }
  return '-'
}

// 续费电话套餐 - 打开弹窗
async function handleRenewPhone(phone: any) {
  renewalPhone.value = phone
  // 确保支付方式已加载
  if (dynamicPaymentMethods.value.length === 0) {
    try {
      await loadPaymentMethods()
    } catch (e) {
      console.error('Load payment methods error:', e)
    }
  }
  // 默认选中第一个支付方式
  if (dynamicPaymentMethods.value.length > 0) {
    renewalPaymentMethod.value = dynamicPaymentMethods.value[0].code.toLowerCase()
  } else {
    renewalPaymentMethod.value = 'usdt'
  }
  showRenewalDialog.value = true
}

// 确认续费 - 与普通订单一致：创建订单后按支付方式跳转
async function confirmRenewal() {
  if (!renewalPhone.value) return
  
  // 【防抖】防止重复提交
  if (renewalSubmitting.value) {
    ElMessage.warning('续费正在处理中，请勿重复点击')
    return
  }
  renewalSubmitting.value = true
  
  try {
    const paymentMethod = renewalPaymentMethod.value.toUpperCase()
    const renewalProductName = `${renewalPhone.value.phoneNumber}续费`
    const userEmail = authStore.user?.email || authStore.user?.username
    if (!userEmail) {
      ElMessage.error('请先登录后再续费')
      return
    }

    const createRes: any = await orderApi.createOrder({
      email: userEmail,
      paymentMethod: paymentMethod.toLowerCase(),
      orderType: 'RENEWAL',
      phoneNumberId: renewalPhone.value.id,
      periodValue: '1',
      periodUnit: 'MONTH',
      productName: renewalProductName,
      totalAmount: Number(renewalPhone.value.renewalPrice || 0),
      remark: `电话套餐续费 ${renewalPhone.value.phoneNumber}`,
      language: getCurrentLocale() // 传递用户当前语言
    })

    if (createRes.code !== 200 && createRes.code !== 201) {
      ElMessage.error(createRes.message || '创建续费订单失败')
      return
    }

    const order = createRes.data
    const orderId = order?.id
    if (!orderId) {
      ElMessage.error('创建续费订单失败')
      return
    }

    showRenewalDialog.value = false

    await navigateByPaymentMethod({
      paymentMethod,
      orderId,
      email: authStore.user?.email,
      router,
    })
  } catch (err: any) {
    console.error('Renewal error:', err)
    ElMessage.error(err.message || '续费失败')
  } finally {
    renewalSubmitting.value = false
  }
}

// 查看电话套餐详情
function viewPhoneDetail(phone: any) {
  detailPhone.value = phone
  showDetailDialog.value = true
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Hanken+Grotesk:wght@400;500;600;700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

/* ===== PAGE LAYOUT ===== */
.profile-page {
  min-height: 100vh;
  background: #ffffff;
  font-family: 'Hanken Grotesk', system-ui, sans-serif;
}

/* ===== MOBILE TOPBAR ===== */
.mobile-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--site-chrome-divider, rgba(0, 0, 0, 0.05));
  box-shadow: var(--site-header-shadow, 0 8px 20px -12px rgba(0, 0, 0, 0.1));
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
}

.mobile-brand {
  font-size: 22px;
  font-weight: 700;
  color: #0059b5;
  letter-spacing: -0.5px;
  cursor: pointer;
}

.mobile-topbar-right {
  display: flex;
  gap: 8px;
}

.mobile-topbar-btn {
  padding: 8px;
  border-radius: 9999px;
  color: #0059b5;
  cursor: pointer;
  transition: background 0.2s;
}

.mobile-topbar-btn:hover {
  background: #f4f3f8;
}

.mobile-topbar-btn .material-symbols-outlined {
  font-size: 22px;
  display: block;
}

@media (min-width: 1024px) {
  .mobile-topbar { display: none; }
}

/* ===== SIDEBAR ===== */
.profile-sidebar {
  width: var(--site-profile-sidebar-width, 288px);
  height: calc(100vh - 64px);
  position: fixed;
  left: 0;
  top: 64px;
  z-index: 40;
  background: #ffffff;
  border-right: 1px solid var(--site-chrome-divider, rgba(0, 0, 0, 0.05));
  box-shadow: 6px 0 18px -12px rgba(0, 0, 0, 0.09);
  display: none;
  flex-direction: column;
  overflow: hidden;
}

@media (min-width: 1024px) {
  .profile-sidebar { display: flex; }
}

.sidebar-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--site-chrome-divider, rgba(0, 0, 0, 0.05));
  box-shadow: var(--site-header-shadow, 0 8px 20px -12px rgba(0, 0, 0, 0.1));
  flex-shrink: 0;
}

.sidebar-user-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sidebar-avatar {
  width: 48px;
  height: 48px;
  border-radius: 9999px;
  background: rgba(0, 113, 227, 0.12);
  border: 2px solid #c1c6d6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #0059b5;
  font-size: 22px;
  overflow: hidden;
}

.sidebar-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 9999px;
}

.sidebar-user-info h2 {
  font-size: 14px;
  font-weight: 600;
  color: #1a1b1f;
  margin: 0 0 2px 0;
  line-height: 1.3;
}

.sidebar-user-info p {
  font-size: 12px;
  color: #414753;
  margin: 0;
  line-height: 1.3;
}

/* Nav Items */
.sidebar-nav-items {
  flex: 1;
  padding: 16px 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s, color 0.2s;
  color: #414753;
  font-size: 15px;
  font-weight: 400;
  user-select: none;
}

.nav-item:hover {
  background: #eeedf3;
  transform: translateX(4px);
  color: #1a1b1f;
}

.nav-item.active {
  background: #0071e3;
  color: white;
  font-weight: 600;
  transform: scale(0.96);
}

.nav-icon {
  font-size: 22px;
  flex-shrink: 0;
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

.nav-item.active .nav-icon {
  font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

/* Sidebar Bottom (Logout) */
.sidebar-bottom {
  padding: 12px;
  border-top: 1px solid var(--site-chrome-divider, rgba(0, 0, 0, 0.05));
  box-shadow: var(--site-footer-shadow, 0 -8px 20px -12px rgba(0, 0, 0, 0.1));
  flex-shrink: 0;
}

.logout-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  color: #ba1a1a;
  font-size: 15px;
  font-weight: 500;
}

.logout-item:hover {
  background: rgba(186, 26, 26, 0.08);
}

/* ===== MAIN CONTENT ===== */
.profile-main {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  padding-bottom: 88px; /* 无 SiteFooter，为底部导航留白 */
}

@media (min-width: 1024px) {
  .profile-main {
    margin-left: var(--site-profile-sidebar-width, 288px);
    padding-bottom: 48px;
  }
}

@media (max-width: 1023px) {
  .profile-main { padding-top: 64px; /* mobile topbar (header spacer in SiteHeader) */ }
}

.main-inner {
  width: 100%;
  max-width: var(--site-content-max-width, 1300px);
  margin: 0 auto;
  padding: 24px var(--site-content-padding-x, 24px);
  box-sizing: border-box;
}

@media (min-width: 1024px) {
  .main-inner {
    padding: clamp(32px, 3vw, 48px) var(--site-content-padding-x, 24px);
  }
}

/* Page Title */
.page-title-area {
  margin-bottom: 32px;
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  color: #1a1b1f;
  letter-spacing: -0.02em;
  margin: 0 0 8px 0;
  line-height: 1.2;
}

.page-desc {
  font-size: 16px;
  color: #414753;
  margin: 0;
}

/* Tab Content */
.tab-content-area {
  /* child components handle their own card styling */
  min-height: 200px;
}

/* ===== MOBILE BOTTOM NAV ===== */
.mobile-bottom-nav {
  display: flex;
  justify-content: space-around;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  border-top: 1px solid var(--site-chrome-divider, rgba(0, 0, 0, 0.05));
  box-shadow: var(--site-footer-shadow, 0 -8px 20px -12px rgba(0, 0, 0, 0.1));
  z-index: 50;
  padding: 8px 8px 12px;
}

@media (min-width: 1024px) {
  .mobile-bottom-nav { display: none; }
}

.mobile-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #414753;
  font-size: 10px;
  font-weight: 600;
  transition: color 0.2s;
  flex: 1;
}

.mobile-nav-item.active {
  color: #0059b5;
}

.mobile-nav-icon {
  padding: 4px 16px;
  border-radius: 9999px;
}

.mobile-nav-icon.active {
  background: rgba(0, 113, 227, 0.12);
}

.mobile-nav-icon .material-symbols-outlined {
  font-size: 24px;
  display: block;
}

/* ===== DIALOG OVERRIDES ===== */
:deep(.el-input__wrapper) {
  background: #ffffff;
  border: 1px solid #c1c6d6;
  box-shadow: none;
}

:deep(.el-input__inner) {
  color: #1a1b1f;
}

:deep(.el-form-item__label) {
  color: #414753;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background: #f4f3f8;
}

/* Renewal Checkout Dialog (match Products popup) */
:deep(.renewal-checkout-dialog .order-content) {
  color: var(--text-secondary);
}

:deep(.renewal-checkout-dialog .order-product) {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 10px 12px;
  margin-bottom: 12px;
  border: 1px solid var(--border-subtle);
}

:deep(.renewal-checkout-dialog .product-header) {
  display: flex;
  align-items: center;
  gap: 14px;
}

:deep(.renewal-checkout-dialog .product-flag) {
  font-size: 42px;
  line-height: 1;
}

:deep(.renewal-checkout-dialog .product-info) {
  flex: 1;
}

:deep(.renewal-checkout-dialog .product-info h3) {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px;
}

:deep(.renewal-checkout-dialog .product-type) {
  color: var(--text-muted);
  font-size: 13px;
  margin: 0;
}

:deep(.renewal-checkout-dialog .product-price) {
  font-size: 22px;
  font-weight: 700;
  color: var(--accent-primary);
}

:deep(.renewal-checkout-dialog .product-price span) {
  font-size: 13px;
  font-weight: 400;
  color: var(--text-muted);
}

:deep(.renewal-checkout-dialog .order-section) {
  margin-bottom: 12px;
}

:deep(.renewal-checkout-dialog .section-title) {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.renewal-checkout-dialog .order-summary) {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 12px;
  border: 1px solid var(--border-subtle);
}

:deep(.renewal-checkout-dialog .summary-row) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: var(--text-secondary);
}

:deep(.renewal-checkout-dialog .summary-row:not(:last-child)) {
  border-bottom: 1px dashed var(--border-subtle);
}

:deep(.renewal-checkout-dialog .total-row) {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  padding-top: 12px;
  margin-top: 4px;
  border-top: 2px solid var(--accent-primary);
}

:deep(.renewal-checkout-dialog .total-row span:last-child) {
  color: var(--accent-primary);
}

:deep(.renewal-checkout-dialog .payment-options) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

:deep(.renewal-checkout-dialog .payment-option) {
  cursor: pointer;
  transition: all 0.2s;
}

:deep(.renewal-checkout-dialog .payment-option.disabled) {
  opacity: 0.5;
  cursor: not-allowed;
}

:deep(.renewal-checkout-dialog .payment-option.disabled input) {
  pointer-events: none;
}

:deep(.renewal-checkout-dialog .payment-option input) {
  display: none;
}

:deep(.renewal-checkout-dialog .payment-content) {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  transition: all 0.2s;
}

:deep(.renewal-checkout-dialog .payment-option:hover .payment-content) {
  border-color: var(--accent-primary);
}

:deep(.renewal-checkout-dialog .payment-option.selected .payment-content) {
  border-color: var(--accent-primary);
  background: rgba(245, 158, 11, 0.05);
}

:deep(.renewal-checkout-dialog .payment-icon) {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}

:deep(.renewal-checkout-dialog .usdt-icon) {
  background: linear-gradient(135deg, #26a17b 0%, #1d8a62 100%);
  color: #fff;
}

:deep(.renewal-checkout-dialog .wechat-icon) {
  background: #07c160;
  color: #fff;
}

:deep(.renewal-checkout-dialog .wechat-icon svg) {
  width: 22px;
  height: 22px;
}

:deep(.renewal-checkout-dialog .alipay-icon) {
  background: #1677ff;
  color: #fff;
}

:deep(.renewal-checkout-dialog .balance-icon) {
  background: var(--accent-primary);
  color: #000;
}

:deep(.renewal-checkout-dialog .balance-icon svg) {
  width: 22px;
  height: 22px;
}

:deep(.renewal-checkout-dialog .card-icon) {
  background: var(--accent-muted);
  color: var(--accent-primary);
  border: 1px solid var(--accent-primary);
}

:deep(.renewal-checkout-dialog .card-icon svg) {
  width: 22px;
  height: 22px;
}

:deep(.renewal-checkout-dialog .payment-info) {
  flex: 1;
  min-width: 0;
}

:deep(.renewal-checkout-dialog .payment-name) {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.renewal-checkout-dialog .payment-desc) {
  display: block;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}

:deep(.renewal-checkout-dialog .balance-desc.insufficient) {
  color: var(--accent-rose);
}

:deep(.renewal-checkout-dialog .payment-check) {
  width: 20px;
  height: 20px;
  color: var(--accent-primary);
  flex-shrink: 0;
}

:deep(.renewal-checkout-dialog .payment-check svg) {
  width: 100%;
  height: 100%;
}

:deep(.renewal-checkout-dialog .dialog-footer) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.renewal-checkout-dialog .btn-submit) {
  min-width: 140px;
  font-weight: 600;
}

/* 订单子标签页 */
.order-sub-tabs {
  margin-bottom: 20px;
}

.order-sub-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.order-sub-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: var(--border-default);
}

.order-sub-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
  padding: 0 20px;
}

.order-sub-tabs :deep(.el-tabs__item.is-active) {
  color: var(--accent-color);
  font-weight: 600;
}

.order-sub-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--accent-color);
}

.sub-tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-badge {
  margin-left: 4px;
}

.esim-orders-content,
.renewal-orders-content {
  min-height: 200px;
}

/* 订单列表 */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: 16px;
}

/* 订单分页 */
.orders-pagination {
  display: flex;
  justify-content: center;
  padding: 16px;
  margin-top: 8px;
}

.order-card {
  background: var(--bg-primary);
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--border-default);
  margin-bottom: 8px;
  padding: 12px 16px;
}

.order-card:last-child {
  margin-bottom: 0;
}

.order-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.order-row + .order-row {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--border-subtle);
}

.order-row-header {
  font-size: 13px;
}

.order-product-name {
  font-weight: 600;
  color: var(--text-primary);
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-no {
  font-size: 11px;
  color: var(--text-secondary);
  font-family: 'JetBrains Mono', monospace;
  flex: 0 1 auto;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-date {
  font-size: 11px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.order-status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.5px;
  margin-left: auto;
}

.status-0 { 
  background: rgba(251, 191, 36, 0.2); 
  color: #fbbf24; 
}
.status-1 { 
  background: rgba(0, 212, 255, 0.2); 
  color: #38bdf8; 
}
.status-2 { 
  background: rgba(16, 185, 129, 0.2); 
  color: #34d399; 
}
.status-3 { 
  background: rgba(139, 92, 246, 0.2); 
  color: #a78bfa; 
}
.status-4 { 
  background: rgba(244, 63, 94, 0.2); 
  color: #f87171; 
}

/* 订单信息行 */
.order-row-info {
  font-size: 12px;
  display: flex;
  justify-content: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

/* 订单状态提示 */
.order-status-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  margin-top: 8px;
}
.order-status-tip.tip-waiting {
  background: rgba(0, 113, 227, 0.15);
  color: #0071E3;
  border: 1px solid rgba(0, 113, 227, 0.3);
}
.order-status-tip.tip-shipped {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
}
.order-status-tip .el-icon {
  font-size: 14px;
}

.order-info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1 1 calc(50% - 8px);
  min-width: 180px;
  max-width: 100%;
}

.order-info-item .info-label {
  color: var(--text-muted);
}

.order-info-item .info-value {
  color: var(--text-secondary);
}

.order-info-item.price .info-value {
  color: var(--accent-emerald);
  font-weight: 600;
}

/* 操作按钮行 */
.order-row-actions {
  justify-content: space-between;
  min-height: auto;
}

.order-progress-custom {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0;
  padding: 16px 0;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex: 1;
  position: relative;
  z-index: 2;
}

.step-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--bg-secondary);
  border: 2px solid var(--border-default);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.progress-step.completed .step-circle {
  background: var(--accent-primary);
  border-color: var(--accent-primary);
  color: white;
}

.step-label {
  font-size: 11px;
  color: var(--text-secondary);
  text-align: center;
  max-width: 80px;
  line-height: 1.3;
}

.progress-step.completed .step-label {
  color: var(--text-primary);
  font-weight: 500;
}

.progress-line {
  flex: 1;
  height: 2px;
  background: var(--border-default);
  margin: 15px 0;
  transition: background 0.3s ease;
}

.progress-line.active {
  background: var(--accent-primary);
}

.order-progress {
  flex: 1;
}

.order-progress :deep(.el-step__title) {
  font-size: 11px !important;
}

.order-progress :deep(.el-step__icon) {
  width: 18px !important;
  height: 18px !important;
  font-size: 10px !important;
}

.order-progress :deep(.el-step__icon-inner) {
  font-size: 10px !important;
}

.order-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.payment-gateway-section .gateway-title {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.payment-gateway-section .gateway-option {
  white-space: nowrap;
}

.payment-gateway-section .gateway-empty {
  margin-top: 8px;
  color: var(--text-muted);
  font-size: 12px;
}

/* 订单信息区域 */
.order-info-section {
  padding: 0;
}

.payment-alert {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.1), rgba(245, 158, 11, 0.05));
  border: 1px solid rgba(251, 191, 36, 0.2);
  border-radius: 8px;
  padding: 10px 14px;
  margin-bottom: 12px;
}

.payment-alert .el-icon {
  color: #fbbf24;
  margin-right: 6px;
  font-size: 14px;
}

.payment-alert span {
  font-weight: 600;
  color: #fbbf24;
  font-size: 13px;
}

.payment-alert p {
  margin: 6px 0 0 0;
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.5;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 12px;
}

.info-item {
  background: var(--bg-hover);
  border-radius: 8px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  border: 1px solid var(--border-subtle);
}

.info-label {
  font-size: 11px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  word-break: break-all;
}

.info-value.price {
  font-size: 16px;
  color: var(--accent-primary);
}

/* 进度条 */
.order-progress {
  margin-bottom: 12px;
  padding: 16px;
  background: var(--bg-hover);
  border-radius: 10px;
}

/* 强制让所有步骤和连线显示为完成状态 */
:deep(.el-step__title.is-success),
:deep(.el-step__title.is-process) {
  color: var(--accent-emerald) !important;
}
:deep(.el-step__icon.is-success),
:deep(.el-step__icon.is-process) {
  border-color: var(--accent-emerald) !important;
  color: var(--accent-emerald) !important;
  background: var(--accent-emerald) !important;
  color: white !important;
}
:deep(.el-step__icon-inner) {
  color: white !important;
}
:deep(.el-step__line) {
  background-color: var(--accent-emerald) !important;
}

/* 订单取消状态 */
.order-cancelled {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(244, 63, 94, 0.1);
  border-radius: 6px;
  border: 1px solid rgba(244, 63, 94, 0.2);
}

.cancelled-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--accent-rose);
  color: white;
  font-size: 14px;
}

.cancelled-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--accent-rose);
}

:deep(.el-steps) {
  --el-step-border-radius: 20px;
}

:deep(.el-step__title) {
  color: var(--text-muted) !important;
  font-size: 13px;
}

:deep(.el-step__title.is-finish) {
  color: var(--accent-emerald) !important;
}

:deep(.el-step__title.is-process) {
  color: #0071E3 !important;
  font-weight: 600;
}

:deep(.el-step__icon) {
  border-color: var(--border-default) !important;
  background: var(--bg-primary) !important;
}

:deep(.el-step__icon.is-finish) {
  border-color: #34d399 !important;
  background: #34d399 !important;
}

:deep(.el-step__icon.is-process) {
  border-color: #0071E3 !important;
  background: var(--accent-muted) !important;
}

:deep(.el-step__line) {
  background: var(--border-default) !important;
}

:deep(.el-step__line.is-finish) {
  background: #34d399 !important;
}

/* 支付网关切换 */
.payment-gateway-section {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed var(--border-subtle);
}

.gateway-title {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.gateway-option {
  padding: 3px 10px;
  background: var(--bg-primary);
  border: 1px solid var(--border-default);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-secondary);
  font-size: 11px;
}

.gateway-option:hover {
  border-color: var(--accent-primary);
  color: var(--accent-primary);
}

.gateway-option.active {
  border-color: var(--accent-primary);
  background: var(--accent-muted);
  color: var(--accent-primary);
}

.gateway-option .el-icon {
  font-size: 12px;
}

/* 操作按钮 */
.order-actions {
  display: flex;
  gap: 8px;
}

.order-actions .el-button {
  min-width: 100px;
  height: 36px;
  font-size: 13px;
}

.btn-pay {
  background: linear-gradient(135deg, #0071E3, #1D1D1D) !important;
  border: none !important;
  font-weight: 600;
}

.btn-pay:hover {
  background: linear-gradient(135deg, #818cf8, #a78bfa) !important;
}

.btn-cancel {
  border-color: rgba(244, 63, 94, 0.4) !important;
  color: #f87171 !important;
}

.btn-cancel:hover {
  background: rgba(244, 63, 94, 0.1) !important;
  border-color: #f87171 !important;
}

/* 购物车样式 */
.cart-content-wrapper {
  padding: 0;
}

.cart-content-wrapper h2 {
  padding: 24px 32px;
  margin-bottom: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.cart-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 0;
}

.cart-items {
  padding: 24px 32px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
  border-bottom: 1px solid var(--border-subtle);
}

.cart-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.item-info {
  flex: 1;
}

.item-info h3 {
  font-size: 16px;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.item-price {
  color: var(--text-muted);
}

.item-subtotal {
  text-align: right;
  min-width: 100px;
}

.subtotal-label {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
}

.subtotal-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--accent-primary);
}

.cart-summary {
  background: var(--bg-hover);
  border-left: 1px solid var(--border-default);
  padding: 24px 32px;
}

.summary-card h3 {
  font-size: 18px;
  margin-bottom: 20px;
  color: var(--text-primary);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  color: var(--text-secondary);
}

.summary-row.total {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  padding-top: 12px;
  border-top: 1px solid var(--border-default);
  margin-top: 12px;
}

.total-amount {
  color: var(--accent-primary);
  font-size: 24px;
}

.btn-checkout {
  width: 100%;
  margin-top: 20px;
  background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary)) !important;
  border: none !important;
}

.continue-shopping {
  display: block;
  text-align: center;
  margin-top: 12px;
  color: var(--accent-primary);
}

/* 工单样式 */
.tickets-tab {
  padding: 0;
}

.tickets-intro {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(0, 212, 255, 0.15) 100%);
  padding: 24px 32px;
  border-radius: 0;
  margin: 0;
  border-bottom: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow: none;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tickets-intro::before {
  content: '💬';
  position: absolute;
  font-size: 80px;
  right: 0;
  top: -20px;
  opacity: 0.08;
}

.intro-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

.tickets-intro h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.tickets-intro h3::before {
  content: '🎫';
  font-size: 22px;
}

.tickets-intro p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
  font-weight: 500;
}

.intro-submit-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 600;
  background: linear-gradient(135deg, #34C759 0%, #0071E3 100%) !important;
  border: none !important;
  border-radius: 8px;
  color: white;
  transition: all 0.25s;
  white-space: nowrap;
  position: relative;
  z-index: 2;
}

.intro-submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.3);
}

/* 工单汇总 */
.ticket-summary {
  display: flex;
  gap: 12px;
  padding: 20px 32px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-subtle);
}

.ticket-summary .summary-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background: var(--bg-hover);
  border-radius: 10px;
  border: 1px solid var(--border-default);
}

.ticket-summary .summary-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.ticket-summary .summary-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 6px;
}

.ticket-summary .summary-item.pending .summary-value {
  color: #fbbf24;
}

.ticket-summary .summary-item.resolved .summary-value {
  color: #34d399;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--text-muted);
}

.loading-spinner {
  font-size: 40px;
  color: var(--accent-primary);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 32px;
}

.empty-icon {
  width: 70px;
  height: 70px;
  margin: 0 auto 20px;
  color: var(--text-muted);
}

.empty-icon svg {
  width: 100%;
  height: 100%;
}

.empty-state h3 {
  font-size: 18px;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.empty-state p {
  color: var(--text-muted);
  margin: 0 0 20px;
}

.empty-submit-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  background: linear-gradient(135deg, var(--accent-primary) 0%, var(--accent-secondary) 100%) !important;
  border: none !important;
  border-radius: 8px;
  color: #fff;
  transition: all 0.25s;
}

.empty-submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(225, 29, 72, 0.3);
}

/* 工单列表 */
.tickets-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: 16px 32px;
}

.ticket-row {
  background: var(--bg-primary);
  border: 1px solid var(--border-default);
  border-radius: 10px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.25s;
}

.ticket-row:hover {
  border-color: var(--accent-primary);
  background: var(--accent-muted);
}

.ticket-row-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.ticket-row-left {
  flex: 1;
  min-width: 0;
}

.ticket-no {
  font-size: 11px;
  font-family: 'SF Mono', Monaco, monospace;
  color: var(--text-muted);
  background: var(--bg-hover);
  padding: 3px 8px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 8px;
}

.ticket-subject {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ticket-row-right {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 16px;
}

.ticket-row-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ticket-row-meta .meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.ticket-row-meta .meta-item .el-icon {
  font-size: 13px;
}

.ticket-row-meta .view-btn {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--accent-primary);
  font-weight: 500;
}

.ticket-row-meta .view-btn .el-icon {
  transition: transform 0.2s;
}

.ticket-row:hover .ticket-row-meta .view-btn .el-icon {
  transform: translateX(4px);
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 32px;
}

.tickets-tab :deep(.el-pagination .el-pager li) {
  background: var(--bg-hover);
  border: 1px solid var(--border-default);
}

.tickets-tab :deep(.el-pagination .el-pager li:hover) {
  color: var(--accent-primary);
}

.tickets-tab :deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, var(--accent-primary) 0%, var(--accent-secondary) 100%);
  color: #fff;
  border-color: transparent;
}

.tickets-tab :deep(.el-tag) {
  border: none;
}

.empty-tip p {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 24px;
  justify-content: center;
}

/* 充值样式 - 左右布局 */
.recharge-tab {
  padding: 0;
}

.recharge-container {
  display: flex;
  gap: 0;
  min-height: 400px;
}

.recharge-panel {
  width: 320px;
  flex-shrink: 0;
  border-right: 1px solid var(--site-chrome-divider, rgba(0, 0, 0, 0.05));
  box-shadow: 6px 0 18px -12px rgba(0, 0, 0, 0.09);
  padding: 24px;
}

.panel-section {
  margin-bottom: 24px;
}

.panel-section:last-child {
  margin-bottom: 0;
}

.balance-panel {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #0071E3 0%, #1D1D1D 100%);
  border-radius: 12px;
  color: #fff;
}

.balance-icon-wrap {
  width: 52px;
  height: 52px;
  background: rgba(255,255,255,0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.balance-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.balance-info .balance-label {
  font-size: 13px;
  opacity: 0.85;
}

.balance-info .balance-value {
  font-size: 28px;
  font-weight: 700;
}

.form-panel .form-group {
  margin-bottom: 20px;
}

.form-panel label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.quick-amounts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.quick-btn {
  padding: 8px 16px;
  background: var(--bg-primary);
  border: 2px solid var(--border-default);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  border-color: var(--accent-primary);
}

.quick-btn.active {
  background: var(--accent-muted);
  border-color: var(--accent-primary);
  color: var(--accent-primary);
}

.quick-amounts .el-input-number {
  width: 140px;
}

.payment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.payment-list .payment-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: var(--bg-primary);
  border: 2px solid var(--border-default);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.payment-list .payment-btn:hover {
  border-color: var(--accent-primary);
  background: var(--accent-muted);
}

.payment-list .payment-btn.active {
  border-color: var(--accent-primary);
  background: var(--accent-muted);
  color: var(--accent-primary);
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #0071E3 0%, #1D1D1D 100%) !important;
  border: none !important;
}

.records-panel {
  flex: 1;
  padding: 24px;
  min-width: 0;
}

.records-panel :deep(.el-table) {
  --el-table-bg-color: transparent !important;
  --el-table-tr-bg-color: transparent !important;
  --el-table-header-bg-color: var(--bg-hover) !important;
  --el-table-row-hover-bg-color: var(--accent-muted) !important;
  --el-table-border-color: var(--border-subtle) !important;
  --el-table-text-color: var(--text-primary) !important;
  --el-table-header-text-color: var(--text-secondary) !important;
  --el-table-cell-padding: 12px 8px !important;
}

.records-panel :deep(.el-table td.el-table__cell) {
  color: var(--text-primary) !important;
  border-bottom-color: var(--border-subtle) !important;
}

.records-panel :deep(.el-table th.el-table__cell) {
  background: var(--bg-hover) !important;
  color: var(--text-secondary) !important;
}

.records-panel :deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-text-color: var(--text-secondary);
  --el-pagination-button-bg-color: var(--bg-hover);
}

.records-panel :deep(.el-pagination .el-pager li) {
  background: var(--bg-hover);
  color: var(--text-secondary);
}

.records-panel :deep(.el-pagination .el-pager li:hover) {
  color: var(--accent-primary);
}

.records-panel :deep(.el-pagination .el-pager li.is-active) {
  background: var(--accent-primary);
  color: #fff;
}

.records-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.records-title h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.records-title span {
  font-size: 14px;
  color: var(--text-muted);
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.recharge-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-subtle);
  background: var(--bg-hover);
}

.recharge-header h2 {
  margin: 0;
  padding: 0;
  border: none;
  font-size: 18px;
}

/* 充值说明 */
.recharge-intro {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.1), rgba(139, 92, 246, 0.1));
  padding: 20px 24px;
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.recharge-intro h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.recharge-intro p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
}

/* 充值统计 */
.recharge-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.recharge-stats .stat-card {
  background: var(--bg-hover);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--border-subtle);
}

.recharge-stats .stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(0, 212, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

.recharge-stats .stat-icon .el-icon {
  font-size: 20px;
  color: #00d4ff;
}

.recharge-stats .stat-icon.success {
  background: rgba(16, 185, 129, 0.15);
}

.recharge-stats .stat-icon.success .el-icon {
  color: #10b981;
}

.recharge-stats .stat-icon.pending {
  background: rgba(0, 113, 227, 0.15);
}

.recharge-stats .stat-icon.pending .el-icon {
  color: #0071E3;
}

.recharge-stats .stat-info {
  display: flex;
  flex-direction: column;
}

.recharge-stats .stat-label {
  font-size: 12px;
  color: var(--text-muted);
}

.recharge-stats .stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

/* 电话套餐样式 */
.phone-numbers-tab {
  padding: 0;
}

.phone-intro {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(0, 212, 255, 0.15) 100%);
  padding: 24px 28px;
  border-radius: 16px;
  margin-bottom: 24px;
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.phone-intro::before {
  content: '📱';
  position: absolute;
  font-size: 60px;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0.1;
}

.phone-intro h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.phone-intro h3::before {
  content: '🌍';
  font-size: 24px;
}

.phone-intro p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
  font-weight: 500;
}

.phone-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.phone-stats .stat-card {
  background: var(--bg-hover);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--border-subtle);
}

.phone-stats .stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(0, 113, 227, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

.phone-stats .stat-icon .el-icon {
  font-size: 20px;
  color: #0071E3;
}

.phone-stats .stat-icon.active {
  background: rgba(16, 185, 129, 0.15);
}

.phone-stats .stat-icon.active .el-icon {
  color: #10b981;
}

.phone-stats .stat-icon.expired {
  background: rgba(0, 113, 227, 0.15);
}

.phone-stats .stat-icon.expired .el-icon {
  color: #0071E3;
}

.phone-stats .stat-icon.total {
  background: rgba(0, 212, 255, 0.15);
}

.phone-stats .stat-icon.total .el-icon {
  color: #00d4ff;
}

.phone-stats .stat-info {
  display: flex;
  flex-direction: column;
}

.phone-stats .stat-label {
  font-size: 12px;
  color: var(--text-muted);
}

.phone-stats .stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.expiring-alert {
  background: linear-gradient(135deg, rgba(0, 113, 227, 0.1), rgba(0, 113, 227, 0.05));
  border: 1px solid rgba(0, 113, 227, 0.2);
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #0071E3;
  font-size: 14px;
}

.expiring-alert .el-icon {
  font-size: 18px;
}

.phone-list {
  min-height: 200px;
}

.phone-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
  padding: 8px 0;
}

/* 电话套餐分页 */
.phone-pagination {
  display: flex;
  justify-content: center;
  padding: 20px;
  margin-top: 12px;
}

.phone-card {
  background: linear-gradient(
    135deg,
    var(--phone-card-start, rgba(59, 130, 246, 0.1)),
    var(--phone-card-end, rgba(139, 92, 246, 0.1))
  );
  border: 1.5px solid var(--phone-card-border, #3B82F6);
  border-left: 5px solid var(--phone-card-border, #3B82F6);
  border-radius: 16px;
  padding: 20px;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}

.phone-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, var(--phone-card-glow, rgba(59, 130, 246, 0.25)) 0%, transparent 70%);
  pointer-events: none;
  transition: all 0.3s ease;
  z-index: 0;
}

.phone-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 60px var(--phone-card-glow, rgba(59, 130, 246, 0.25)), 0 0 40px var(--phone-card-glow, rgba(59, 130, 246, 0.15));
  border-color: var(--phone-card-border, #3B82F6);
}

.phone-card:hover::before {
  top: -30%;
  right: -30%;
}

.phone-card.status-expired {
  --phone-card-start: rgba(245, 158, 11, 0.08);
  --phone-card-end: rgba(250, 204, 21, 0.08);
  --phone-card-border: #F59E0B;
  --phone-card-glow: rgba(245, 158, 11, 0.25);
}

.phone-card.status-expired:hover {
  box-shadow: 0 20px 60px rgba(245, 158, 11, 0.25), 0 0 40px rgba(245, 158, 11, 0.15);
}

.phone-card.status-cancelled {
  --phone-card-start: rgba(239, 68, 68, 0.05);
  --phone-card-end: rgba(244, 63, 94, 0.05);
  --phone-card-border: #EF4444;
  --phone-card-glow: rgba(239, 68, 68, 0.2);
  opacity: 0.7;
}

/* 根据卡片索引设置不同颜色 */
.phone-card[style*="--card-index: 0"] {
  --phone-card-start: rgba(59, 130, 246, 0.1);
  --phone-card-end: rgba(139, 92, 246, 0.1);
  --phone-card-border: #3B82F6;
  --phone-card-glow: rgba(59, 130, 246, 0.3);
}

.phone-card[style*="--card-index: 1"] {
  --phone-card-start: rgba(34, 197, 94, 0.1);
  --phone-card-end: rgba(16, 185, 129, 0.1);
  --phone-card-border: #22C55E;
  --phone-card-glow: rgba(34, 197, 94, 0.3);
}

.phone-card[style*="--card-index: 2"] {
  --phone-card-start: rgba(6, 182, 212, 0.1);
  --phone-card-end: rgba(14, 165, 233, 0.1);
  --phone-card-border: #06B6D4;
  --phone-card-glow: rgba(6, 182, 212, 0.3);
}

.phone-card[style*="--card-index: 3"] {
  --phone-card-start: rgba(168, 85, 247, 0.1);
  --phone-card-end: rgba(236, 72, 153, 0.1);
  --phone-card-border: #A855F7;
  --phone-card-glow: rgba(168, 85, 247, 0.3);
}

.phone-card[style*="--card-index: 4"] {
  --phone-card-start: rgba(249, 115, 22, 0.1);
  --phone-card-end: rgba(239, 68, 68, 0.1);
  --phone-card-border: #F97316;
  --phone-card-glow: rgba(249, 115, 22, 0.3);
}

.phone-card[style*="--card-index: 5"] {
  --phone-card-start: rgba(59, 130, 246, 0.08);
  --phone-card-end: rgba(6, 182, 212, 0.08);
  --phone-card-border: #0EA5E9;
  --phone-card-glow: rgba(59, 130, 246, 0.25);
}

.phone-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-subtle);
  position: relative;
  z-index: 1;
}

.phone-number {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.country-code {
  background: linear-gradient(135deg, var(--accent-muted), rgba(59, 130, 246, 0.05));
  color: var(--accent-primary);
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  display: inline-block;
  width: fit-content;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.phone-number .number {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: 'JetBrains Mono', 'Courier New', monospace;
  letter-spacing: 1px;
}

.phone-info {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.info-row {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 12px;
  align-items: center;
  padding: 8px 0;
  font-size: 13px;
  line-height: 1.5;
}

.info-row .label {
  color: var(--text-muted);
  font-weight: 500;
  font-size: 12px;
  letter-spacing: 0.3px;
}

.info-row .value {
  color: var(--text-primary);
  font-weight: 600;
  display: flex;
  align-items: center;
}

.info-row .value.price {
  color: var(--accent-primary);
  font-weight: 700;
  font-size: 14px;
}

.info-row .value.expiring {
  color: #0071E3;
  font-weight: 700;
}

.phone-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--border-subtle);
  position: relative;
  z-index: 1;
}

.phone-actions :deep(.el-button) {
  font-size: 12px;
  height: 32px;
}

.phone-list .empty-state {
  text-align: center;
  padding: 60px 32px;
}

.phone-list .empty-icon {
  width: 70px;
  height: 70px;
  margin: 0 auto 20px;
  color: var(--text-muted);
}

.phone-list .empty-icon svg {
  width: 100%;
  height: 100%;
}

.phone-list .empty-state h3 {
  font-size: 18px;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.phone-list .empty-state p {
  color: var(--text-muted);
  margin: 0 0 20px;
}

.records-table .el-table__header-wrapper th {
  background: var(--bg-hover) !important;
  color: var(--text-primary);
  font-weight: 600;
}

.type-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.type-recharge {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}

.type-renewal {
  background: rgba(0, 113, 227, 0.15);
  color: #0071E3;
}

.amount-text {
  font-weight: 600;
  color: var(--accent-color);
}

.status-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.status-paid {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}

.status-pending {
  background: rgba(0, 113, 227, 0.15);
  color: #0071E3;
}

.status-failed, .status-refunded {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.pagination-area {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* Element Plus 浅色主题覆盖 */
:deep(.el-input__wrapper) {
  background: var(--bg-primary);
  border: 1px solid var(--border-default);
  box-shadow: none;
}

:deep(.el-input__inner) {
  color: var(--text-primary);
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background: var(--bg-hover);
}

::deep(.el-input.is-disabled .el-input__inner) {
  color: var(--text-muted);
}

::deep(.el-table) {
  --el-table-bg-color: transparent !important;
  --el-table-tr-bg-color: transparent !important;
  --el-table-header-bg-color: var(--bg-hover) !important;
  --el-table-row-hover-bg-color: var(--accent-muted) !important;
  --el-table-border-color: var(--border-subtle) !important;
  --el-table-text-color: var(--text-primary) !important;
  --el-table-header-text-color: rgba(255, 255, 255, 0.95) !important;
  --el-table-row-hover-bg-color: rgba(99, 102, 241, 0.08) !important;
  --el-table-cell-padding: 14px 0 !important;
  background: transparent !important;
}

:deep(.el-table__inner-wrapper) {
  background: transparent !important;
}

:deep(.el-table tr) {
  background: transparent !important;
}

:deep(.el-table td.el-table__cell) {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.9) !important;
  border-bottom-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-table th.el-table__cell) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: rgba(255, 255, 255, 0.95) !important;
  border-bottom-color: rgba(255, 255, 255, 0.15) !important;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: rgba(255, 255, 255, 0.02) !important;
}

:deep(.el-table__body tr:hover > td) {
  background: rgba(99, 102, 241, 0.1) !important;
}

::deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-text-color: rgba(255, 255, 255, 0.7);
  --el-pagination-button-bg-color: rgba(255, 255, 255, 0.1);
}

::deep(.el-pagination .el-pager li) {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.7);
}

::deep(.el-pagination .el-pager li:hover) {
  color: #0071E3;
}

::deep(.el-pagination .el-pager li.is-active) {
  background: #0071E3;
  color: #fff;
}

::deep(.el-button--primary) {
  --el-button-bg-color: #0071E3;
  --el-button-border-color: #0071E3;
  --el-button-text-color: #fff;
}

@media (max-width: 768px) {
  :deep(.renewal-checkout-dialog .payment-options) {
    grid-template-columns: 1fr;
  }
}

/* 套餐详情对话框样式 */


/* === 套餐详情弹窗 V2 === */
.pd-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-top: 4px;
}

.pd-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: linear-gradient(135deg, var(--bg-card) 0%, rgba(245, 158, 11, 0.07) 100%);
}

.pd-hero--expired {
  background: linear-gradient(135deg, var(--bg-card) 0%, rgba(239, 68, 68, 0.06) 100%);
}

.pd-hero--cancelled {
  opacity: 0.75;
}

.pd-hero-main {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.pd-code {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  background: var(--accent-muted);
  color: var(--accent-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 800;
  flex-shrink: 0;
  letter-spacing: 0.5px;
}

.pd-number-block {
  min-width: 0;
}

.pd-number {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.5px;
  line-height: 1.2;
}

.pd-country {
  margin-top: 5px;
  font-size: 12px;
  color: var(--text-muted);
}

.pd-hero-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  flex-shrink: 0;
}

.pd-status {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.pd-status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  display: inline-block;
}

.pd-status--active {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.pd-status--expired {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.pd-status--cancelled {
  background: rgba(107, 114, 128, 0.15);
  color: #6b7280;
}

.pd-price-block {
  text-align: right;
}

.pd-price-label {
  display: block;
  font-size: 11px;
  color: var(--text-muted);
  margin-bottom: 3px;
}

.pd-price {
  font-size: 22px;
  font-weight: 800;
  color: var(--accent-primary);
  line-height: 1;
  white-space: nowrap;
}

.pd-price em {
  font-style: normal;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-muted);
  margin-left: 3px;
}

.pd-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.pd-cell {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 12px 14px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
}

.pd-cell-label {
  font-size: 11px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.pd-cell-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.pd-value--warn {
  color: #0071E3;
}

.pd-value--danger {
  color: #ef4444;
}

.pd-warn-dot {
  margin-right: 4px;
}

.pd-alert {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
}

.pd-alert--warn {
  background: rgba(0, 113, 227, 0.1);
  color: #0071E3;
  border: 1px solid rgba(0, 113, 227, 0.25);
}

.pd-alert--danger {
  background: rgba(239, 68, 68, 0.08);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.pd-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.info-label {
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 500;
}

.info-value {
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 600;
}

.info-value.price {
  color: #00d4ff;
  font-size: 16px;
  font-weight: 700;
}

.info-value.text-danger {
  color: #ef4444;
}
</style>

<style>
.renewal-checkout-dialog .el-dialog__header {
  border-bottom: 1px solid var(--border-subtle);
  padding: 16px 20px;
  margin: 0;
}

.renewal-checkout-dialog .el-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.renewal-checkout-dialog .el-dialog__body {
  padding: 20px;
}

.renewal-checkout-dialog .el-dialog__footer {
  border-top: 1px solid var(--border-subtle);
  padding: 16px 20px;
}

/* Ensure teleported renewal dialog uses the same layout as Products dialog */
.renewal-checkout-dialog .order-content {
  color: var(--text-secondary);
}

.renewal-checkout-dialog .order-product {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 10px 12px;
  margin-bottom: 12px;
  border: 1px solid var(--border-subtle);
}

.renewal-checkout-dialog .product-header {
  display: flex;
  align-items: center;
  gap: 14px;
  justify-content: space-between;
}

.renewal-checkout-dialog .product-flag {
  font-size: 42px;
  line-height: 1;
}

.renewal-checkout-dialog .product-info {
  flex: 1;
  min-width: 0;
}

.renewal-checkout-dialog .product-info h3 {
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px;
}

.renewal-checkout-dialog .product-type {
  color: var(--text-muted);
  font-size: 13px;
  margin: 0;
}

.renewal-checkout-dialog .product-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--accent-primary);
  white-space: nowrap;
}

.renewal-checkout-dialog .product-price span {
  font-size: 13px;
  font-weight: 400;
  color: var(--text-muted);
}

.renewal-checkout-dialog .order-section {
  margin-bottom: 12px;
}

.renewal-checkout-dialog .section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.renewal-checkout-dialog .order-summary {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 12px;
  border: 1px solid var(--border-subtle);
}

.renewal-checkout-dialog .compact-summary {
  padding: 14px;
}

.renewal-checkout-dialog .summary-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.renewal-checkout-dialog .summary-label {
  color: var(--text-muted);
  font-size: 13px;
}

.renewal-checkout-dialog .summary-value {
  display: inline-flex;
  align-items: baseline;
  justify-content: flex-end;
  gap: 6px;
  min-width: 150px;
  color: var(--text-primary);
  font-weight: 600;
  font-size: 14px;
  white-space: nowrap;
  text-align: right;
}

.renewal-checkout-dialog .amount-main {
  line-height: 1;
}

.renewal-checkout-dialog .amount-unit {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-muted);
}

.renewal-checkout-dialog .summary-row:not(:last-child) {
  border-bottom: 1px dashed var(--border-subtle);
}

.renewal-checkout-dialog .total-row {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  padding-top: 12px;
  margin-top: 6px;
  border-top: 2px solid var(--accent-primary);
}

.renewal-checkout-dialog .total-row span:last-child {
  color: var(--accent-primary);
}

.renewal-checkout-dialog .total-value {
  min-width: 170px;
  font-size: 18px;
  letter-spacing: 0.2px;
}

.renewal-checkout-dialog .total-value .amount-unit {
  font-size: 12px;
  color: var(--accent-primary);
}

.renewal-checkout-dialog .summary-note {
  margin-top: 8px;
  padding-top: 10px;
  border-top: 1px dashed var(--border-subtle);
  color: var(--text-muted);
  font-size: 12px;
  text-align: right;
}

.renewal-checkout-dialog .payment-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.renewal-checkout-dialog .payment-option {
  cursor: pointer;
  transition: all 0.2s;
}

.renewal-checkout-dialog .payment-option input {
  display: none;
}

.renewal-checkout-dialog .payment-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.renewal-checkout-dialog .payment-option.disabled input {
  pointer-events: none;
}

.renewal-checkout-dialog .payment-content {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  transition: all 0.2s;
}

.renewal-checkout-dialog .payment-option:hover .payment-content {
  border-color: var(--accent-primary);
}

.renewal-checkout-dialog .payment-option.selected .payment-content {
  border-color: var(--accent-primary);
  background: rgba(245, 158, 11, 0.05);
}

.renewal-checkout-dialog .payment-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}

.renewal-checkout-dialog .usdt-icon {
  background: linear-gradient(135deg, #26a17b 0%, #1d8a62 100%);
  color: #fff;
}

.renewal-checkout-dialog .wechat-icon {
  background: #07c160;
  color: #fff;
}

.renewal-checkout-dialog .wechat-icon svg {
  width: 22px;
  height: 22px;
}

.renewal-checkout-dialog .alipay-icon {
  background: #1677ff;
  color: #fff;
}

.renewal-checkout-dialog .balance-icon {
  background: var(--accent-primary);
  color: #000;
}

.renewal-checkout-dialog .balance-icon svg {
  width: 22px;
  height: 22px;
}

.renewal-checkout-dialog .card-icon {
  background: var(--accent-muted);
  color: var(--accent-primary);
  border: 1px solid var(--accent-primary);
}

.renewal-checkout-dialog .card-icon svg {
  width: 22px;
  height: 22px;
}

.renewal-checkout-dialog .payment-info {
  flex: 1;
  min-width: 0;
}

.renewal-checkout-dialog .payment-name {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.renewal-checkout-dialog .payment-desc {
  display: block;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}

.renewal-checkout-dialog .balance-desc.insufficient {
  color: var(--accent-rose);
}

.renewal-checkout-dialog .payment-check {
  width: 20px;
  height: 20px;
  color: var(--accent-primary);
  flex-shrink: 0;
}

.renewal-checkout-dialog .payment-check svg {
  width: 100%;
  height: 100%;
}

.renewal-checkout-dialog .dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.renewal-checkout-dialog .btn-submit {
  min-width: 140px;
  font-weight: 600;
}

.renewal-checkout-dialog .renewal-v2-content {
  padding-top: 4px;
}

.renewal-checkout-dialog .renewal-v2-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 14px;
  padding: 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: linear-gradient(135deg, var(--bg-card) 0%, rgba(245, 158, 11, 0.08) 100%);
}

.renewal-checkout-dialog .hero-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.renewal-checkout-dialog .hero-flag {
  width: 46px;
  height: 46px;
  border-radius: var(--radius-sm);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  background: var(--accent-muted);
}

.renewal-checkout-dialog .hero-meta {
  min-width: 0;
}

.renewal-checkout-dialog .hero-meta h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 700;
  line-height: 1.2;
}

.renewal-checkout-dialog .hero-meta p {
  margin: 5px 0 0;
  color: var(--text-muted);
  font-size: 12px;
}

.renewal-checkout-dialog .hero-price {
  text-align: right;
  flex-shrink: 0;
}

.renewal-checkout-dialog .price-tag {
  display: inline-block;
  margin-bottom: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  color: var(--accent-primary);
  background: var(--accent-muted);
}

.renewal-checkout-dialog .price-value {
  color: var(--accent-primary);
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
  white-space: nowrap;
}

.renewal-checkout-dialog .price-value span {
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 500;
}

.renewal-checkout-dialog .renewal-v2-grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 12px;
}

.renewal-checkout-dialog .renewal-v2-card {
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  padding: 14px;
}

.renewal-checkout-dialog .v2-card-title {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 12px;
}

.renewal-checkout-dialog .v2-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  padding: 9px 0;
}

.renewal-checkout-dialog .v2-row:not(:last-of-type) {
  border-bottom: 1px dashed var(--border-subtle);
}

.renewal-checkout-dialog .v2-label {
  color: var(--text-muted);
  font-size: 13px;
}

.renewal-checkout-dialog .v2-amount {
  min-width: 160px;
  text-align: right;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 700;
  white-space: nowrap;
}

.renewal-checkout-dialog .v2-amount em {
  font-style: normal;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-muted);
  margin-left: 4px;
}

.renewal-checkout-dialog .v2-row-total {
  margin-top: 4px;
  border-top: 2px solid var(--accent-primary);
  padding-top: 11px;
}

.renewal-checkout-dialog .v2-row-total .v2-label {
  color: var(--text-primary);
  font-weight: 700;
}

.renewal-checkout-dialog .v2-row-total .v2-amount {
  color: var(--accent-primary);
  font-size: 20px;
}

.renewal-checkout-dialog .v2-row-total .v2-amount em {
  color: var(--accent-primary);
}

.renewal-checkout-dialog .v2-note {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed var(--border-subtle);
  text-align: right;
  color: var(--text-muted);
  font-size: 12px;
}

.renewal-checkout-dialog .renewal-v2-pay .payment-options {
  gap: 9px;
}

.renewal-checkout-dialog .renewal-v2-pay .payment-content {
  padding: 11px 12px;
}

@media (max-width: 768px) {
  .renewal-checkout-dialog .product-header {
    align-items: flex-start;
  }

  .renewal-checkout-dialog .product-price {
    font-size: 18px;
  }

  .renewal-checkout-dialog .summary-value,
  .renewal-checkout-dialog .total-value {
    min-width: 132px;
  }

  .renewal-checkout-dialog .renewal-v2-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .renewal-checkout-dialog .hero-price {
    width: 100%;
    text-align: left;
  }

  .renewal-checkout-dialog .renewal-v2-grid {
    grid-template-columns: 1fr;
  }

  .renewal-checkout-dialog .v2-amount {
    min-width: 136px;
  }
}


</style>
