<template>
  <div v-loading="loading">
    <PageHeader title="经营仪表盘" subtitle="实时掌握订单、收入与运营概览">
      <template #actions>
        <el-button type="primary" :loading="loading" @click="load">刷新数据</el-button>
      </template>
    </PageHeader>

    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-label">今日订单</div>
        <div class="kpi-value">{{ stats.todayOrders ?? '-' }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">今日成交额</div>
        <div class="kpi-value">{{ formatMoney(stats.todayRevenue) }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">待处理订单</div>
        <div class="kpi-value">{{ stats.pendingOrders ?? '-' }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">总用户数</div>
        <div class="kpi-value">{{ stats.totalUsers ?? '-' }}</div>
      </div>
    </div>

    <div v-if="empty" class="page-card empty-state">
      <el-empty description="暂无统计数据，接入 GET /admin/dashboard/stats 后将自动展示" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { dashboardApi } from '@/api/dashboard'

const loading = ref(false)
const empty = ref(true)
const stats = reactive<Record<string, number | undefined>>({
  todayOrders: undefined,
  todayRevenue: undefined,
  pendingOrders: undefined,
  totalUsers: undefined
})

function formatMoney(value?: number) {
  if (value === undefined || value === null || Number.isNaN(value)) return '-'
  return `¥${Number(value).toFixed(2)}`
}

async function load() {
  loading.value = true
  try {
    const res = await dashboardApi.stats()
    const data = (res as any)?.data ?? res ?? {}
    stats.todayOrders = data.todayOrders
    stats.todayRevenue = data.todayRevenue
    stats.pendingOrders = data.pendingOrders
    stats.totalUsers = data.totalUsers
    empty.value = [stats.todayOrders, stats.todayRevenue, stats.pendingOrders, stats.totalUsers]
      .every((v) => v === undefined || v === null)
  } catch {
    empty.value = true
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.empty-state {
  margin-top: 16px;
}
</style>
