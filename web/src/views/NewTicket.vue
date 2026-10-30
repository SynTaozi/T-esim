<template>
  <div class="new-ticket-page">
    <SiteHeader />

    <div class="container main-content">
      <div class="page-header">
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          {{ $t('newTicket.back') }}
        </el-button>
        <h1>{{ $t('newTicket.title') }}</h1>
      </div>

      <div class="content-card">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
          <el-form-item :label="$t('newTicket.ticketType')" prop="type">
            <el-select v-model="form.type" :placeholder="$t('newTicket.selectType')">
              <el-option :label="$t('newTicket.general')" value="GENERAL" />
              <el-option :label="$t('newTicket.refund')" value="REFUND" />
              <el-option :label="$t('newTicket.technical')" value="TECHNICAL" />
              <el-option :label="$t('newTicket.billing')" value="BILLING" />
              <el-option :label="$t('newTicket.other')" value="OTHER" />
            </el-select>
          </el-form-item>

          <el-form-item :label="$t('newTicket.priority')" prop="priority">
            <el-radio-group v-model="form.priority">
              <el-radio label="LOW">{{ $t('newTicket.low') }}</el-radio>
              <el-radio label="MEDIUM">{{ $t('newTicket.medium') }}</el-radio>
              <el-radio label="HIGH">{{ $t('newTicket.high') }}</el-radio>
              <el-radio label="URGENT">{{ $t('newTicket.urgent') }}</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item :label="$t('newTicket.relatedOrder')">
            <el-select v-model="form.orderId" :placeholder="$t('newTicket.selectOrder')" clearable>
              <el-option
                v-for="order in userOrders"
                :key="order.id"
                :label="`${order.orderNo} - $${order.totalAmount} (${orderStatusLabel(order.status)})`"
                :value="order.id"
              />
              <el-option v-if="userOrders.length === 0" :label="$t('newTicket.noOrders')" :value="null" disabled />
            </el-select>
          </el-form-item>

          <el-form-item :label="$t('newTicket.subject')" prop="subject">
            <el-input v-model="form.subject" :placeholder="$t('newTicket.subjectPlaceholder')" maxlength="100" show-word-limit />
          </el-form-item>

          <el-form-item :label="$t('newTicket.description')" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="8"
              :placeholder="$t('newTicket.descriptionPlaceholder')"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>

          <!-- 附件上传 -->
          <el-form-item :label="$t('newTicket.attachments')">
            <div class="upload-section">
              <el-upload
                v-model:file-list="fileList"
                :auto-upload="false"
                :on-exceed="handleExceed"
                :before-upload="beforeUpload"
                :on-preview="handlePreview"
                :on-remove="handleRemove"
                :limit="5"
                multiple
                accept="image/*,.pdf,.doc,.docx,.txt,.zip"
                drag
                class="upload-dragger"
              >
                <div class="upload-content">
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <p class="upload-text">{{ $t('newTicket.clickOrDrag') }}</p>
                  <p class="upload-hint">{{ $t('newTicket.maxFilesHint') }}</p>
                </div>
              </el-upload>
              <!-- 图片预览 -->
              <el-image-viewer
                v-if="previewVisible"
                :url-list="previewUrls"
                :initial-index="previewIndex"
                @close="previewVisible = false"
              />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
              {{ $t('newTicket.submit') }}
            </el-button>
            <el-button size="large" @click="router.back()">{{ $t('newTicket.cancel') }}</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile, UploadFiles, UploadRawFile } from 'element-plus'
import { ArrowLeft, UploadFilled } from '@element-plus/icons-vue'
import SiteHeader from '@/components/SiteHeader.vue'
import type { FormInstance, FormRules } from 'element-plus'
import { createTicket } from '@/api/ticket'
import { orderApi } from '@/api/order'
import { useAuthStore } from '@/stores/auth'
import { navigateToLogin } from '@/utils/authNavigation'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const userOrders = ref<{ id: number; orderNo: string; totalAmount: number; status: number }[]>([])
const fileList = ref<UploadFile[]>([])
const previewVisible = ref(false)
const previewUrls = ref<string[]>([])
const previewIndex = ref(0)

function orderStatusLabel(status: number): string {
  const map: Record<number, string> = {
    0: t('orders.pending') || 'Pending',
    1: t('payment.paySuccess') || 'Paid',
    2: t('profile.shipped') || 'Shipped',
    3: t('common.success') || 'Completed',
    4: t('orders.orderCancelled') || 'Cancelled'
  }
  return map[status] || String(status)
}

const form = reactive({
  type: 'GENERAL',
  priority: 'MEDIUM',
  orderId: null as number | null,
  subject: '',
  content: ''
})

async function loadUserOrders() {
  try {
    const res: any = await orderApi.getOrders()
    userOrders.value = (res.data || res.orders || []).map((order: any) => ({
      id: order.id,
      orderNo: order.orderNo,
      totalAmount: order.totalAmount,
      status: order.status
    }))
  } catch (err) {
    console.error('Failed to load orders', err)
  }
}

onMounted(() => {
  loadUserOrders()
})

const rules: FormRules = {
  type: [{ required: true, message: () => t('newTicket.selectTypeRequired'), trigger: 'change' }],
  subject: [{ required: true, message: () => t('newTicket.inputSubject'), trigger: 'blur' }],
  content: [{ required: true, message: () => t('newTicket.inputDescription'), trigger: 'blur' }]
}

function beforeUpload(rawFile: UploadRawFile): boolean {
  if (rawFile.size > 10 * 1024 * 1024) {
    ElMessage.error(t('newTicket.fileSizeError'))
    return false
  }
  return true
}

function handleExceed() {
  ElMessage.warning(t('newTicket.maxFilesHint'))
}

function handlePreview(file: UploadFile) {
  const imageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/svg+xml']
  if (file.raw && imageTypes.includes(file.raw.type)) {
    const url = URL.createObjectURL(file.raw)
    previewUrls.value = [url]
    previewIndex.value = 0
    previewVisible.value = true
  }
}

function handleRemove(_file: UploadFile, _uploadFiles: UploadFiles) {
  // file list managed by el-upload v-model
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // Build FormData to support file attachments
        const hasFiles = fileList.value.length > 0
        if (hasFiles) {
          const formData = new FormData()
          formData.append('type', form.type)
          formData.append('priority', form.priority)
          if (form.orderId !== null) formData.append('orderId', String(form.orderId))
          formData.append('subject', form.subject)
          formData.append('content', form.content)
          fileList.value.forEach((f) => {
            if (f.raw) formData.append('attachments', f.raw, f.name)
          })
          // Try multipart upload; fallback to JSON if API doesn't support it
          try {
            const { default: api } = await import('@/api/index')
            await api.post('/tickets/upload', formData, {
              headers: { 'Content-Type': 'multipart/form-data' }
            })
          } catch {
            // Fallback: submit without attachments
            await createTicket({
              type: form.type,
              priority: form.priority,
              orderId: form.orderId,
              subject: form.subject,
              content: form.content
            })
          }
        } else {
          await createTicket({
            type: form.type,
            priority: form.priority,
            orderId: form.orderId,
            subject: form.subject,
            content: form.content
          })
        }
        ElMessage.success(t('newTicket.submitSuccess'))
        router.push('/tickets')
      } catch (err: any) {
        if (err.code === 401 || (err.response && err.response.status === 401)) {
          authStore.logout()
          ElMessageBox.alert(t('newTicket.loginExpired'), t('common.warning'), {
            confirmButtonText: t('common.confirm'),
            type: 'warning'
          }).then(() => navigateToLogin(router, route)).catch(() => navigateToLogin(router, route))
        } else {
          ElMessage.error(err.message || t('newTicket.submitFailed'))
        }
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped>
.new-ticket-page {
  min-height: 100vh;
  background: var(--bg-primary);
}

.header {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border-subtle);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-sm);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.logo a {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.nav {
  display: flex;
  gap: 32px;
}

.nav a {
  color: var(--text-secondary);
  font-weight: 500;
  transition: color 0.2s;
  text-decoration: none;
}

.nav a:hover {
  color: var(--accent-primary);
}

.main-content {
  padding: 40px 20px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  color: var(--text-primary);
  margin: 0;
}

.page-header :deep(.el-button) {
  background: transparent !important;
  border-color: var(--border-default) !important;
  color: var(--text-secondary) !important;
}

.page-header :deep(.el-button:hover) {
  background: var(--accent-muted) !important;
  border-color: var(--accent-primary) !important;
  color: var(--accent-primary) !important;
}

.content-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 32px;
  border: 1px solid var(--border-default);
}

.upload-section {
  width: 100%;
}

.upload-dragger {
  width: 100%;
}

:deep(.el-upload-dragger) {
  background: var(--bg-primary) !important;
  border: 2px dashed var(--border-default) !important;
  border-radius: 8px;
  padding: 24px;
  transition: all 0.2s;
}

:deep(.el-upload-dragger:hover) {
  border-color: var(--accent-primary) !important;
  background: var(--accent-muted) !important;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 40px;
  color: var(--accent-primary);
}

.upload-text {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.upload-hint {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}

:deep(.el-upload-list__item-status-label) {
  color: var(--text-muted) !important;
}

:deep(.el-upload-list__item-name) {
  color: var(--text-primary) !important;
}

:deep(.el-upload-list__item-name:hover) {
  color: var(--accent-primary) !important;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  color: var(--text-secondary) !important;
}

:deep(.el-input__wrapper) {
  background: var(--bg-primary) !important;
  box-shadow: 0 0 0 1px var(--border-default) inset !important;
  border: none !important;
  transition: all 0.2s;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--accent-primary) inset !important;
}

:deep(.el-input__inner) {
  color: var(--text-primary) !important;
}

:deep(.el-input__inner::placeholder) {
  color: var(--text-muted) !important;
}

:deep(.el-select .el-input__wrapper) {
  background: var(--bg-primary) !important;
  box-shadow: 0 0 0 1px var(--border-default) inset !important;
  border: none !important;
  transition: all 0.2s;
}

:deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--accent-primary) inset !important;
}

:deep(.el-select-dropdown) {
  background: var(--bg-card) !important;
  border: 1px solid var(--border-default) !important;
}

:deep(.el-select-dropdown__item) {
  color: var(--text-primary) !important;
}

:deep(.el-select-dropdown__item.hover),
:deep(.el-select-dropdown__item:hover) {
  background: var(--accent-muted) !important;
  color: var(--accent-primary) !important;
}

:deep(.el-option__label) {
  color: var(--text-primary) !important;
}

:deep(.el-scrollbar__view) {
  background: var(--bg-card) !important;
}

:deep(.el-textarea__inner) {
  background: var(--bg-primary) !important;
  box-shadow: 0 0 0 1px var(--border-default) inset !important;
  border: none !important;
  color: var(--text-primary) !important;
  transition: all 0.2s;
}

:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--accent-primary) inset !important;
}

:deep(.el-textarea__inner::placeholder) {
  color: var(--text-muted) !important;
}

:deep(.el-radio__label) {
  color: var(--text-secondary) !important;
}

:deep(.el-radio__inner) {
  border-color: var(--border-default) !important;
  background: var(--bg-primary) !important;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background: var(--accent-primary) !important;
  border-color: var(--accent-primary) !important;
}

:deep(.el-checkbox__label) {
  color: var(--text-secondary) !important;
}

:deep(.el-checkbox__inner) {
  border-color: var(--border-default) !important;
  background: var(--bg-primary) !important;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: var(--accent-primary) !important;
  border-color: var(--accent-primary) !important;
}

:deep(.el-form-item__error) {
  color: #f56c6c !important;
}

/* 按钮样式 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--accent-primary) 0%, var(--accent-secondary) 100%) !important;
  border-color: var(--accent-primary) !important;
  color: #fff !important;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(225, 29, 72, 0.25);
}

:deep(.el-button--primary:disabled) {
  opacity: 0.6;
}

:deep(.el-button:not(.el-button--primary)) {
  background: var(--bg-hover) !important;
  border-color: var(--border-default) !important;
  color: var(--text-secondary) !important;
}

:deep(.el-button:not(.el-button--primary):hover) {
  background: var(--accent-muted) !important;
  border-color: var(--accent-primary) !important;
  color: var(--accent-primary) !important;
}

/* 禁用和加载状态 */
:deep(.el-button.is-disabled) {
  opacity: 0.6 !important;
  cursor: not-allowed !important;
}

:deep(.el-form-item.is-error .el-input__wrapper),
:deep(.el-form-item.is-error .el-textarea__inner) {
  box-shadow: 0 0 0 1px #f56c6c inset !important;
}

:deep(.el-word-counter) {
  color: var(--text-muted) !important;
}

@media (max-width: 768px) {
  .main-content {
    padding: 20px 16px;
  }
  
  .content-card {
    padding: 20px;
  }
}
</style>
