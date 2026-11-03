<template>
  <div>
    <PageHeader title="支付方式" subtitle="启用/禁用与展示配置">
      <template #actions>
        <el-button type="primary" @click="openCreate">新增支付方式</el-button>
      </template>
    </PageHeader>
    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="code" label="编码" width="140" />
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
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑支付方式' : '新增支付方式'"
      width="520px"
      destroy-on-close
    >
      <el-form :model="form" label-width="90px">
        <el-form-item label="编码" required>
          <el-input
            v-model="form.code"
            :disabled="!!form.id"
            placeholder="如 USDT / STRIPE / WECHAT / ALIPAY / BANK_CARD"
          />
        </el-form-item>
        <el-form-item label="中文名" required>
          <el-input v-model="form.nameZh" placeholder="前台展示名称" />
        </el-form-item>
        <el-form-item label="英文名">
          <el-input v-model="form.nameEn" />
        </el-form-item>
        <el-form-item label="中文说明">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="英文说明">
          <el-input v-model="form.descriptionEn" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="图标 URL 或标识" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { paymentMethodApi } from '@/api/cms'

const loading = ref(false)
const saving = ref(false)
const togglingId = ref<number | null>(null)
const rows = ref<any[]>([])
const visible = ref(false)
const form = reactive<any>(emptyForm())

function emptyForm() {
  return {
    id: undefined as number | undefined,
    code: '',
    nameZh: '',
    nameEn: '',
    description: '',
    descriptionEn: '',
    icon: '',
    sortOrder: 0,
    enabled: true
  }
}

function resetForm(data?: Partial<ReturnType<typeof emptyForm>>) {
  Object.assign(form, emptyForm(), data || {})
}

async function load() {
  loading.value = true
  try {
    const res = await paymentMethodApi.list()
    rows.value = Array.isArray(res.data) ? res.data : []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetForm()
  visible.value = true
}

function openEdit(row: any) {
  resetForm({
    id: row.id,
    code: row.code || '',
    nameZh: row.nameZh || row.name || '',
    nameEn: row.nameEn || '',
    description: row.description || '',
    descriptionEn: row.descriptionEn || '',
    icon: row.icon || '',
    sortOrder: row.sortOrder ?? 0,
    enabled: !!row.enabled
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
  const code = (form.code || '').trim()
  const nameZh = (form.nameZh || '').trim()
  if (!code) {
    ElMessage.warning('请填写编码')
    return
  }
  if (!nameZh) {
    ElMessage.warning('请填写中文名')
    return
  }
  const payload = {
    id: form.id,
    code,
    name: nameZh,
    nameZh,
    nameEn: (form.nameEn || '').trim() || undefined,
    description: form.description || undefined,
    descriptionEn: form.descriptionEn || undefined,
    icon: form.icon || undefined,
    sortOrder: form.sortOrder ?? 0,
    enabled: !!form.enabled
  }
  saving.value = true
  try {
    const res = form.id
      ? await paymentMethodApi.update(form.id, payload)
      : await paymentMethodApi.create(payload)
    if (res && typeof res.code === 'number' && res.code !== 200) {
      ElMessage.error(res.message || '保存失败')
      return
    }
    ElMessage.success('已保存')
    visible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row: any) {
  await ElMessageBox.confirm(
    `确认删除支付方式「${row.nameZh || row.name || row.code}」？`,
    '提示',
    { type: 'warning' }
  )
  const res = await paymentMethodApi.remove(row.id)
  if (res && typeof res.code === 'number' && res.code !== 200) {
    ElMessage.error(res.message || '删除失败')
    return
  }
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>
