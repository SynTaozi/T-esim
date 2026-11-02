<template>
  <div>
    <PageHeader title="支付方式" subtitle="启用/禁用与展示配置" />
    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column label="名称" min-width="140">
          <template #default="{ row }">{{ row.nameZh || row.name || row.code }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="启用" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="!!row.enabled"
              :loading="togglingId === row.id"
              @change="(v: boolean) => toggleEnabled(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="visible" title="编辑支付方式" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="中文名"><el-input v-model="form.nameZh" /></el-form-item>
        <el-form-item label="英文名"><el-input v-model="form.nameEn" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { paymentMethodApi } from '@/api/cms'

const loading = ref(false)
const saving = ref(false)
const togglingId = ref<number | null>(null)
const rows = ref<any[]>([])
const visible = ref(false)
const form = reactive<any>({})

async function load() {
  loading.value = true
  try {
    const res = await paymentMethodApi.list()
    rows.value = Array.isArray(res.data) ? res.data : []
  } finally {
    loading.value = false
  }
}

function edit(row: any) {
  Object.assign(form, {
    id: row.id,
    code: row.code,
    nameZh: row.nameZh || row.name || '',
    nameEn: row.nameEn || '',
    icon: row.icon || '',
    sortOrder: row.sortOrder ?? 0,
    enabled: row.enabled
  })
  visible.value = true
}

async function toggleEnabled(row: any, enabled: boolean) {
  const prev = !!row.enabled
  row.enabled = enabled
  togglingId.value = row.id
  try {
    await paymentMethodApi.updateEnabled(row.id, enabled)
    ElMessage.success('已保存')
  } catch {
    row.enabled = prev
  } finally {
    togglingId.value = null
  }
}

async function saveForm() {
  if (!form.id) return
  saving.value = true
  try {
    await paymentMethodApi.update(form.id, {
      id: form.id,
      code: form.code,
      name: form.nameZh,
      nameZh: form.nameZh,
      nameEn: form.nameEn,
      icon: form.icon,
      sortOrder: form.sortOrder,
      enabled: form.enabled
    })
    ElMessage.success('已保存')
    visible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
