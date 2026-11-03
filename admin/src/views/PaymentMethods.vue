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
        <el-table-column label="图标" width="120">
          <template #default="{ row }">
            <span class="icon-chip" :class="iconClass(row.icon || row.code)">
              {{ iconLabel(row.icon || row.code) }}
            </span>
          </template>
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
          <el-select
            v-model="form.code"
            :disabled="!!form.id"
            filterable
            allow-create
            default-first-option
            placeholder="选择或输入编码"
            style="width: 100%"
            @change="onCodeChange"
          >
            <el-option
              v-for="p in codePresets"
              :key="p.code"
              :label="`${p.code} · ${p.nameZh}`"
              :value="p.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="中文名" required>
          <el-input v-model="form.nameZh" placeholder="前台展示名称" />
        </el-form-item>
        <el-form-item label="英文名">
          <el-input v-model="form.nameEn" />
        </el-form-item>
        <el-form-item label="图标" required>
          <el-select v-model="form.icon" placeholder="选择预制图标" style="width: 100%">
            <el-option v-for="opt in iconPresets" :key="opt.value" :label="opt.label" :value="opt.value">
              <div class="icon-option">
                <span class="icon-chip" :class="opt.css">{{ opt.short }}</span>
                <span>{{ opt.label }}</span>
              </div>
            </el-option>
          </el-select>
          <div v-if="form.icon" class="icon-preview">
            预览：
            <span class="icon-chip" :class="iconClass(form.icon)">{{ iconLabel(form.icon) }}</span>
          </div>
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

const codePresets = [
  { code: 'WECHAT', nameZh: '微信支付', nameEn: 'WeChat Pay', icon: 'wechat', sortOrder: 10 },
  { code: 'ALIPAY', nameZh: '支付宝', nameEn: 'Alipay', icon: 'alipay', sortOrder: 20 },
  { code: 'BANK_CARD', nameZh: '银行卡', nameEn: 'Bank Card', icon: 'bank_card', sortOrder: 30 },
  { code: 'USDT', nameZh: 'USDT', nameEn: 'USDT', icon: 'usdt', sortOrder: 40 }
]

const iconPresets = [
  { value: 'wechat', label: '微信', short: '微', css: 'icon-wechat' },
  { value: 'alipay', label: '支付宝', short: '支', css: 'icon-alipay' },
  { value: 'bank_card', label: '银行卡', short: '卡', css: 'icon-card' },
  { value: 'usdt', label: 'USDT', short: 'U', css: 'icon-usdt' },
  { value: 'balance', label: '余额', short: '余', css: 'icon-balance' },
  { value: 'default', label: '默认', short: '付', css: 'icon-default' }
]

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
    icon: 'default',
    sortOrder: 0,
    enabled: true
  }
}

function resetForm(data?: Partial<ReturnType<typeof emptyForm>>) {
  Object.assign(form, emptyForm(), data || {})
}

function normalizeIconKey(raw: string): string {
  const v = (raw || '').trim().toLowerCase()
  if (!v) return 'default'
  if (v.includes('wechat') || v === 'wx') return 'wechat'
  if (v.includes('alipay') || v === 'ali') return 'alipay'
  if (v.includes('usdt') || v.includes('tether')) return 'usdt'
  if (v.includes('bank') || v.includes('card')) return 'bank_card'
  if (v.includes('balance') || v.includes('wallet')) return 'balance'
  if (['wechat', 'alipay', 'usdt', 'bank_card', 'balance', 'default'].includes(v)) return v
  return 'default'
}

function iconClass(raw: string) {
  const key = normalizeIconKey(raw)
  return iconPresets.find((i) => i.value === key)?.css || 'icon-default'
}

function iconLabel(raw: string) {
  const key = normalizeIconKey(raw)
  return iconPresets.find((i) => i.value === key)?.short || '付'
}

function onCodeChange(code: string) {
  if (form.id) return
  const preset = codePresets.find((p) => p.code === code)
  if (!preset) return
  if (!form.nameZh) form.nameZh = preset.nameZh
  if (!form.nameEn) form.nameEn = preset.nameEn
  form.icon = preset.icon
  if (!form.sortOrder) form.sortOrder = preset.sortOrder
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
    icon: normalizeIconKey(row.icon || row.code),
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
  const code = (form.code || '').trim().toUpperCase()
  const nameZh = (form.nameZh || '').trim()
  if (!code) {
    ElMessage.warning('请填写编码')
    return
  }
  if (!nameZh) {
    ElMessage.warning('请填写中文名')
    return
  }
  if (!form.icon) {
    ElMessage.warning('请选择图标')
    return
  }
  const payload = {
    id: form.id,
    code,
    name: nameZh,
    nameZh,
    nameEn: (form.nameEn || '').trim() || undefined,
    icon: form.icon,
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

<style scoped>
.icon-option {
  display: flex;
  align-items: center;
  gap: 10px;
}
.icon-preview {
  margin-top: 8px;
  color: #666;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.icon-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}
.icon-wechat { background: #07c160; }
.icon-alipay { background: #1677ff; }
.icon-usdt { background: #26a17b; }
.icon-card { background: #1e3a5f; }
.icon-balance { background: #0059b5; }
.icon-default { background: #9e9e9e; }
</style>
