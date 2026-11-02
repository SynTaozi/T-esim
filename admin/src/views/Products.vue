<template>
  <div>
    <PageHeader title="产品管理" subtitle="管理 eSIM / 实体卡商品与上下架状态">
      <template #actions>
        <el-button @click="$router.push('/products/plan-sync')">套餐同步</el-button>
        <el-button type="primary" @click="$router.push('/products/create')">添加产品</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="filters.name" placeholder="搜索产品名称" clearable style="width: 220px" @keyup.enter="reload" />
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px" @change="reload">
          <el-option label="上架" value="ON_SALE" />
          <el-option label="下架" value="OFF_SALE" />
        </el-select>
        <el-button type="primary" @click="reload">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column prop="stock" label="库存" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" :label="statusLabel(row.status)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/products/edit/${row.id}`)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="load"
          @size-change="reload"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { productApi } from '@/api/product'

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filters = reactive<{ name: string; status?: string }>({ name: '' })

function statusLabel(status: string | number) {
  if (status === 1 || status === 'ON_SALE' || status === 'ACTIVE') return '上架'
  if (status === 0 || status === 'OFF_SALE' || status === 'DISABLED') return '下架'
  return String(status ?? '-')
}

function resetFilters() {
  filters.name = ''
  filters.status = undefined
  reload()
}

function reload() {
  page.value = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res = await productApi.page({
      page: page.value - 1,
      size: size.value,
      name: filters.name || undefined,
      status: filters.status
    })
    const data = (res as any)?.data ?? res
    rows.value = data?.content ?? data?.list ?? data?.records ?? (Array.isArray(data) ? data : [])
    total.value = data?.totalElements ?? data?.total ?? rows.value.length
  } catch {
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
