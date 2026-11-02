<template>
  <el-tag :type="tagType" size="small" effect="light">
    {{ displayLabel }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: string | number
  label?: string
}>()

/** 通用状态码 → 中文（订单/商品/工单等） */
const STATUS_LABELS: Record<string, string> = {
  // 订单
  PENDING_PAYMENT: '待支付',
  PENDING: '待处理',
  PAID: '已支付',
  PROCESSING: '处理中',
  FULFILLING: '履约中',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  CANCELED: '已取消',
  // 数字状态（兼容）
  '0': '待支付',
  '1': '已支付',
  '2': '已发货',
  '3': '已完成',
  '4': '已取消',
  // 商品 / 通用
  ON_SALE: '在售',
  OFF_SALE: '下架',
  DRAFT: '草稿',
  ACTIVE: '启用',
  DISABLED: '禁用',
  ENABLED: '启用',
  SUCCESS: '成功',
  FAILED: '失败',
  EXHAUSTED: '已用尽',
  // 工单
  OPEN: '待处理',
  RESOLVED: '已解决',
  CLOSED: '已关闭',
  REJECTED: '已拒绝',
  WARNING: '警告',
  ERROR: '错误'
}

const normalized = computed(() => String(props.status ?? '').trim())

const displayLabel = computed(() => {
  if (props.label) return props.label
  const key = normalized.value
  const upper = key.toUpperCase()
  return STATUS_LABELS[key] || STATUS_LABELS[upper] || key || '-'
})

const tagType = computed(() => {
  const s = normalized.value.toLowerCase()
  if (['1', 'active', 'enabled', 'success', 'paid', 'resolved', 'closed', 'shipped', 'completed', 'on_sale'].includes(s)) {
    return 'success'
  }
  if (['0', 'pending_payment', 'pending', 'open', 'processing', 'fulfilling', 'warning', 'draft'].includes(s)) {
    return 'warning'
  }
  if (['4', 'cancelled', 'canceled', 'disabled', 'exhausted', 'off_sale', 'failed', 'rejected', 'error', 'danger'].includes(s)) {
    return 'info'
  }
  return ''
})
</script>
