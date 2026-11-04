<template>
  <div>
    <PageHeader title="邮件模板" subtitle="系统通知邮件模板（支持中英文）" />
    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="code" label="编码" width="200" show-overflow-tooltip />
        <el-table-column prop="name" label="名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="subject" label="中文主题" min-width="180" show-overflow-tooltip />
        <el-table-column label="启用" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="!!row.enabled"
              :loading="togglingId === row.id"
              @change="(v: boolean | string | number) => toggle(row, !!v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link @click="openPreview(row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="visible"
      title="编辑模板"
      width="820px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form v-loading="formLoading" :model="form" label-width="96px">
        <el-form-item label="编码">
          <el-input :model-value="form.code" disabled />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" placeholder="可选" />
        </el-form-item>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 14px"
          :title="varHint(form.code)"
        />

        <el-divider content-position="left">中文</el-divider>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="主题" required>
          <el-input v-model="form.subject" />
        </el-form-item>
        <el-form-item label="正文" required>
          <el-input
            v-model="form.body"
            type="textarea"
            :rows="10"
            placeholder="支持 HTML，变量如 {{code}} {{orderNo}}"
          />
        </el-form-item>

        <el-divider content-position="left">English</el-divider>
        <el-form-item label="Name">
          <el-input v-model="form.nameEn" />
        </el-form-item>
        <el-form-item label="Subject">
          <el-input v-model="form.subjectEn" />
        </el-form-item>
        <el-form-item label="Body">
          <el-input
            v-model="form.bodyEn"
            type="textarea"
            :rows="8"
            placeholder="HTML body (en)"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="模板预览" width="820px" destroy-on-close>
      <el-tabs v-model="previewTab">
        <el-tab-pane label="中文" name="zh">
          <div class="preview-subject">{{ previewData.subject || '（无主题）' }}</div>
          <div class="preview-body" v-html="previewData.body || '<p style=color:#999>暂无正文</p>'" />
        </el-tab-pane>
        <el-tab-pane label="English" name="en">
          <div class="preview-subject">{{ previewData.subjectEn || '(no subject)' }}</div>
          <div class="preview-body" v-html="previewData.bodyEn || '<p style=color:#999>No body</p>'" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { emailTemplateApi } from '@/api/email'

const loading = ref(false)
const formLoading = ref(false)
const saving = ref(false)
const togglingId = ref<number | null>(null)
const rows = ref<any[]>([])
const visible = ref(false)
const previewVisible = ref(false)
const previewTab = ref('zh')
const previewData = reactive({
  subject: '',
  body: '',
  subjectEn: '',
  bodyEn: ''
})

const form = reactive({
  id: 0,
  code: '',
  description: '',
  name: '',
  subject: '',
  body: '',
  nameEn: '',
  subjectEn: '',
  bodyEn: '',
  enabled: true
})

function varHint(code: string) {
  const map: Record<string, string> = {
    AUTH_REGISTER_CODE: '变量：{{code}} / ${CODE}、{{email}} / ${EMAIL}、{{validityMinutes}} / ${VALIDITY_MINUTES}',
    AUTH_RESET_CODE: '变量：{{code}}、{{email}}、{{validityMinutes}}（也可用 ${CODE} 写法）',
    ORDER_SHIPPING: '变量：{{orderNo}} {{packageName}} {{shippingCarrier}} {{trackingNumber}} {{phone}} {{qrCodeUrl}} {{remark}} {{shippingTime}}；支持 {{#if xxx}}...{{/if}}',
    ORDER_CUSTOMER_NOTICE: '变量：{{subject}} {{orderNo}} {{noticeContent}}',
    TICKET_REPLY: '变量：{{ticketNo}} {{subject}} {{replyContent}} {{repliedBy}} {{ticketUrl}}'
  }
  return map[code] || '支持 {{var}}、${VAR}，以及 {{#if var}}...{{/if}}'
}

function resetForm() {
  form.id = 0
  form.code = ''
  form.description = ''
  form.name = ''
  form.subject = ''
  form.body = ''
  form.nameEn = ''
  form.subjectEn = ''
  form.bodyEn = ''
  form.enabled = true
}

function fillForm(data: any) {
  form.id = data?.id || 0
  form.code = data?.code || ''
  form.description = data?.description || ''
  form.name = data?.name || ''
  form.subject = data?.subject || ''
  form.body = data?.body || ''
  form.nameEn = data?.nameEn || ''
  form.subjectEn = data?.subjectEn || ''
  form.bodyEn = data?.bodyEn || ''
  form.enabled = data?.enabled !== false
}

async function load() {
  loading.value = true
  try {
    rows.value = (await emailTemplateApi.list()).data || []
  } finally {
    loading.value = false
  }
}

async function openEdit(row: any) {
  visible.value = true
  formLoading.value = true
  resetForm()
  try {
    const res = await emailTemplateApi.get(row.id)
    fillForm(res.data || row)
  } catch {
    fillForm(row)
  } finally {
    formLoading.value = false
  }
}

async function save() {
  if (!form.id) return
  if (!form.subject?.trim() && !form.subjectEn?.trim()) {
    ElMessage.warning('请至少填写中文或英文主题')
    return
  }
  saving.value = true
  try {
    await emailTemplateApi.update(form.id, {
      name: form.name,
      nameEn: form.nameEn,
      subject: form.subject,
      subjectEn: form.subjectEn,
      body: form.body,
      bodyEn: form.bodyEn,
      description: form.description,
      enabled: form.enabled
    })
    ElMessage.success('已保存')
    visible.value = false
    await load()
  } catch {
    // interceptor shows error
  } finally {
    saving.value = false
  }
}

async function toggle(row: any, enabled: boolean) {
  if (togglingId.value) return
  togglingId.value = row.id
  const prev = row.enabled
  row.enabled = enabled
  try {
    await emailTemplateApi.setEnabled(row.id, enabled)
  } catch {
    row.enabled = prev
  } finally {
    togglingId.value = null
  }
}

async function openPreview(row: any) {
  previewTab.value = 'zh'
  previewData.subject = ''
  previewData.body = ''
  previewData.subjectEn = ''
  previewData.bodyEn = ''
  previewVisible.value = true
  try {
    const res = await emailTemplateApi.preview(row.id)
    const data = res.data || {}
    // 后端字段是 body / bodyEn，不是 bodyHtml
    previewData.subject = data.subject || row.subject || ''
    previewData.body = data.body || row.body || ''
    previewData.subjectEn = data.subjectEn || row.subjectEn || ''
    previewData.bodyEn = data.bodyEn || row.bodyEn || ''
  } catch {
    previewData.subject = row.subject || ''
    previewData.body = row.body || ''
    previewData.subjectEn = row.subjectEn || ''
    previewData.bodyEn = row.bodyEn || ''
  }
}

onMounted(load)
</script>

<style scoped>
.preview-subject {
  font-weight: 700;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}
.preview-body {
  min-height: 120px;
  max-height: 480px;
  overflow: auto;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}
</style>
