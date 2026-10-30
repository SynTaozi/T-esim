<template>
  <div class="stripe-result-page">
    <SiteHeader />
    <div class="result-card">
      <h2>{{ $t('payment.payFailed') || '支付已取消' }}</h2>
      <p v-if="orderNo">{{ $t('payment.orderNo') || '订单号' }}: {{ orderNo }}</p>
      <p>{{ $t('payment.backOrder') || '你可以返回订单重新发起支付。' }}</p>
      <el-button type="primary" @click="goProfile">{{ $t('nav.profile') || '个人中心' }}</el-button>
      <el-button @click="goProducts">{{ $t('nav.products') || '产品中心' }}</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'

const route = useRoute()
const router = useRouter()
const orderNo = computed(() => String(route.query.orderNo || ''))

function goProfile() {
  router.push('/profile')
}
function goProducts() {
  router.push('/products')
}
</script>

<style scoped>
.stripe-result-page {
  min-height: 100vh;
  background: var(--bg-elevated, #f5f7fa);
}
.result-card {
  max-width: 480px;
  margin: 80px auto;
  padding: 40px 32px;
  background: var(--bg-card, #fff);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  text-align: center;
}
.result-card h2 {
  margin-bottom: 12px;
}
.result-card p {
  color: #666;
  margin-bottom: 12px;
}
.result-card .el-button {
  margin: 8px 6px 0;
}
</style>
