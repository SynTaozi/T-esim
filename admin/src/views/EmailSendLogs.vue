<template>
  <div>
    <PageHeader title="邮件发送日志" subtitle="注册验证码、发货通知、工单回复等投递记录" />
    <div class="page-card" style="margin-bottom: 12px">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部" style="width: 140px">
            <el-option label="待发送" value="PENDING" />
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-input v-model="filters.bizType" clearable placeholder="如 ORDER_SHIPPING" style="width: 180px" />
        </el-form-item>
        <el-form-item label="收件人">
          <el-input v-model="filters.toEmail" clearable placeholder="邮箱" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="reload">查询</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column prop="bizType" label="业务" width="170" show-overflow-tooltip />
        <el-table-column prop="bizId" label="业务ID" width="140" show-overflow-tooltip />
        <el-table-column prop="toEmail" label="收件人" min-width="180" show-overflow-tooltip />
        <el-table-column prop="subject" label="主题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'SUCCESS'" type="success" size="small">成功</el-tag>
            <el-tag v-else-if="row.status === 'FAILED'" type="danger" size="small">失败</el-tag>
            <el-tag v-else type="info" size="small">待发送</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 14px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="load"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="发送详情" width="720px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="状态">{{ current.status }}</el-descriptions-item>
        <el-descriptions-item label="业务">{{ current.bizType }} / {{ current.bizId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收件人">{{ current.toEmail }}</el-descriptions-item>
        <el-descriptions-item label="主题">{{ current.subject }}</el-descriptions-item>
        <el-descriptions-item label="错误">{{ current.errorMessage || '-' }}</el-descriptions-item>
        <el-descriptions-item label="正文">
          <div class="mail-body" v-html="current.body" />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { emailSendLogApi } from '@/api/email'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const filters = reactive({ status: '', bizType: '', toEmail: '' })
const detailVisible = ref(false)
const current = ref<any>(null)

async function load() {
  loading.value = true
  try {
    const res = await emailSendLogApi.page({
      status: filters.status || undefined,
      bizType: filters.bizType || undefined,
      toEmail: filters.toEmail || undefined,
      page: page.value - 1,
      size
    })
    rows.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

function openDetail(row: any) {
  current.value = row
  detailVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.mail-body {
  max-height: 320px;
  overflow: auto;
  padding: 8px;
  background: #fafafa;
  border-radius: 6px;
}
</style>
