<template>
  <div class="profile-recharge-disabled">
    <p>{{ $t('profile.rechargeDesc') || 'Wallet recharge is not available.' }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  balance?: number
  rechargeList?: any[]
  rechargeTotal?: number
  pageSize?: number
  currentPage?: number
  rechargeAmount?: number
  selectedPaymentMethod?: string
  paymentMethods?: any[]
  presetAmounts?: number[]
  loading?: boolean
  submitting?: boolean
  getStatusType?: Function
  getStatusText?: Function
  formatDate?: Function
}>()
defineEmits([
  'set-amount',
  'set-payment-method',
  'submit-recharge',
  'continue-payment',
  'page-change'
])
</script>

<style scoped>
.profile-recharge-disabled {
  padding: 2rem;
  color: var(--text-secondary, #86868B);
}
</style>
