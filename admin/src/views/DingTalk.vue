<template>
  <div class="dingtalk-page">
    <PageHeader title="钉钉通知" subtitle="支付成功与新工单将通过 Webhook 推送到钉钉群" />

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="Webhook 配置" name="config">
        <div class="tab-header">
          <span class="tab-desc">启用一条配置即可；支付成功 / 新工单 / 测试消息都会走该 Webhook。</span>
          <el-button type="primary" :icon="Plus" @click="openDialog()">新增配置</el-button>
        </div>

        <el-table :data="configs" v-loading="configLoading" border stripe>
          <el-table-column label="名称" prop="name" min-width="120" />
          <el-table-column label="Webhook 地址" prop="webhookUrl" min-width="260" show-overflow-tooltip />
          <el-table-column label="启用" width="90" align="center">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" @change="toggleConfig(row)" />
            </template>
          </el-table-column>
          <el-table-column label="描述" prop="description" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <div class="row-acts">
                <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
                <el-button link :loading="row._testing" @click="handleTest(row)">测试</el-button>
                <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="发送记录" name="logs">
        <div class="tab-header">
          <div class="filter-bar">
            <el-select v-model="logFilter.type" placeholder="全部类型" clearable style="width:160px" @change="loadLogs(1)">
              <el-option label="支付成功通知" value="ORDER_PAID" />
              <el-option label="新工单通知" value="NEW_TICKET" />
              <el-option label="测试消息" value="TEST" />
            </el-select>
            <el-button :icon="Refresh" @click="loadLogs(1)">刷新</el-button>
          </div>
        </div>

        <el-table :data="logs" v-loading="logLoading" border stripe>
          <el-table-column label="类型" width="130">
            <template #default="{ row }">
              <el-tag :type="typeTagType(row.eventType || row.type)" size="small">
                {{ typeLabel(row.eventType || row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="标题" prop="title" min-width="160" show-overflow-tooltip />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
                {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="失败原因" prop="errorMessage" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.errorMessage" style="color:#f56c6c">{{ row.errorMessage }}</span>
              <span v-else style="color:#67c23a">—</span>
            </template>
          </el-table-column>
          <el-table-column label="发送时间" prop="createdAt" width="170" />
          <el-table-column label="内容详情" width="90" align="center">
            <template #default="{ row }">
              <el-button size="small" text @click="showContent(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="logPage"
            v-model:page-size="logSize"
            :total="logTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @current-change="loadLogs"
            @size-change="() => loadLogs(1)"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑钉钉配置' : '新增钉钉配置'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="form.name" placeholder="例：默认钉钉机器人" />
        </el-form-item>
        <el-form-item label="Webhook 地址" prop="webhookUrl">
          <el-input
            v-model="form.webhookUrl"
            type="textarea"
            :rows="3"
            placeholder="https://oapi.dingtalk.com/robot/send?access_token=..."
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="可选描述" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="contentVisible" title="通知内容" width="600px">
      <pre class="content-preview">{{ previewContent }}</pre>
      <template #footer>
        <el-button @click="contentVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { dingtalkApi } from '@/api/dingtalk'

const activeTab = ref('config')
const configs = ref<any[]>([])
const configLoading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  webhookUrl: '',
  enabled: true,
  description: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  webhookUrl: [{ required: true, message: '请输入 Webhook 地址', trigger: 'blur' }]
}

const logs = ref<any[]>([])
const logLoading = ref(false)
const logPage = ref(1)
const logSize = ref(20)
const logTotal = ref(0)
const logFilter = reactive({ type: '' })

const contentVisible = ref(false)
const previewContent = ref('')

onMounted(() => {
  loadConfigs()
  loadLogs(1)
})

async function loadConfigs() {
  configLoading.value = true
  try {
    configs.value = (await dingtalkApi.listConfigs()).data || []
  } finally {
    configLoading.value = false
  }
}

function openDialog(row?: any) {
  editingId.value = row?.id ?? null
  form.name = row?.name || ''
  form.webhookUrl = row?.webhookUrl || ''
  form.enabled = row?.enabled !== false
  form.description = row?.description || ''
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await dingtalkApi.saveConfig({
      id: editingId.value || undefined,
      name: form.name,
      webhookUrl: form.webhookUrl,
      enabled: form.enabled,
      description: form.description
    })
    ElMessage.success(editingId.value ? '配置已更新' : '配置已创建')
    dialogVisible.value = false
    await loadConfigs()
  } finally {
    submitting.value = false
  }
}

async function toggleConfig(row: any) {
  try {
    await dingtalkApi.saveConfig({
      id: row.id,
      name: row.name,
      webhookUrl: row.webhookUrl,
      enabled: row.enabled,
      description: row.description
    })
    ElMessage.success(row.enabled ? '已启用' : '已禁用')
  } catch {
    row.enabled = !row.enabled
  }
}

async function handleTest(row: any) {
  row._testing = true
  try {
    await dingtalkApi.test(row.id)
    ElMessage.success('测试消息发送成功，请查看钉钉群')
    await loadLogs(1)
  } catch {
    // api interceptor
  } finally {
    row._testing = false
  }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确认删除配置「${row.name}」？`, '提示', { type: 'warning' })
  await dingtalkApi.removeConfig(row.id)
  ElMessage.success('已删除')
  await loadConfigs()
}

async function loadLogs(page = logPage.value) {
  logPage.value = page
  logLoading.value = true
  try {
    const res = await dingtalkApi.logs({
      type: logFilter.type || undefined,
      page: page - 1,
      size: logSize.value
    })
    logs.value = res.data?.content || []
    logTotal.value = res.data?.totalElements || 0
  } finally {
    logLoading.value = false
  }
}

function showContent(row: any) {
  previewContent.value = row.content || '（无内容）'
  contentVisible.value = true
}

function typeLabel(type: string) {
  const map: Record<string, string> = {
    ORDER_PAID: '支付成功',
    NEW_TICKET: '新工单',
    TEST: '测试消息'
  }
  return map[type] || type || '—'
}

function typeTagType(type: string) {
  const map: Record<string, string> = {
    ORDER_PAID: 'success',
    NEW_TICKET: 'warning',
    TEST: 'info'
  }
  return (map[type] || '') as any
}
</script>

<style scoped>
.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}
.tab-desc {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.row-acts {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 4px;
}
.row-acts :deep(.el-button) {
  margin: 0;
  padding: 0 4px;
}
.filter-bar {
  display: flex;
  gap: 8px;
  align-items: center;
}
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.content-preview {
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  line-height: 1.6;
  margin: 0;
}
</style>
