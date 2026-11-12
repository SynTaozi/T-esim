<template>
  <div>
    <PageHeader title="工单管理" subtitle="客服回复与关闭" />
    <div class="page-card">
      <div class="filter-bar">
        <el-select v-model="query.status" clearable placeholder="状态" style="width:140px" @change="search">
          <el-option label="OPEN" value="OPEN" />
          <el-option label="PENDING" value="PENDING" />
          <el-option label="CLOSED" value="CLOSED" />
        </el-select>
      </div>
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="ticketNo" label="工单号" min-width="140" />
        <el-table-column prop="subject" label="主题" min-width="180" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="open(row)">处理</el-button>
            <el-button link :disabled="row.status === 'CLOSED'" @click="close(row.id)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.size"
          :current-page="query.page + 1"
          @current-change="(p: number) => { query.page = p - 1; load() }"
        />
      </div>
    </div>

    <el-drawer v-model="visible" title="工单详情" size="560px" class="ticket-drawer">
      <template v-if="detail">
        <div class="chat-card">
          <div class="chat-header">
            <div>
              <h3>{{ detail.subject }}</h3>
              <p>
                {{ detail.ticketNo }}
                <span v-if="detail.type || detail.category"> · {{ detail.type || detail.category }}</span>
                <span> · {{ detail.status }}</span>
              </p>
            </div>
            <el-tag v-if="detail.status === 'CLOSED'" size="small" type="info">已关闭</el-tag>
            <el-tag v-else size="small" type="success">处理中</el-tag>
          </div>

          <div class="chat-body">
            <div
              v-for="m in thread"
              :key="m.key"
              class="chat-row"
              :class="{ me: m.admin }"
            >
              <div class="av" :class="{ mine: m.admin }">{{ m.admin ? 'S' : 'U' }}</div>
              <div class="bubble-wrap">
                <div class="bubble" :class="{ mine: m.admin }">
                  <p v-if="m.text" class="bubble-text">{{ m.text }}</p>
                  <div v-if="m.images.length" class="images">
                    <el-image
                      v-for="(src, i) in m.images"
                      :key="src + i"
                      :src="src"
                      :preview-src-list="m.images"
                      :initial-index="i"
                      fit="cover"
                      class="thumb"
                    />
                  </div>
                </div>
                <div class="bubble-time">
                  {{ m.admin ? '客服' : '用户' }}
                  <span v-if="m.createdAt"> · {{ m.createdAt }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="detail.status !== 'CLOSED'" class="chat-composer">
            <div class="composer-main">
              <el-input
                v-model="reply"
                type="textarea"
                :rows="3"
                placeholder="输入回复…"
                :disabled="sending"
                resize="none"
              />
              <div class="composer-upload">
                <el-upload
                  v-model:file-list="fileList"
                  list-type="picture-card"
                  :auto-upload="false"
                  :limit="5"
                  multiple
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  :before-upload="beforeUpload"
                  :on-exceed="() => ElMessage.warning('最多 5 张图片')"
                  :disabled="sending"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
                <p class="upload-hint">支持 JPG / PNG / GIF / WEBP，单张不超过 5MB</p>
              </div>
            </div>
            <el-button type="primary" class="send-btn" :loading="sending" @click="send">发送</el-button>
          </div>
          <el-alert
            v-else
            type="info"
            :closable="false"
            title="工单已关闭，无法继续回复"
            class="closed-alert"
          />
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadFile, UploadRawFile } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { ticketApi } from '@/api/ticket'
import { uploadImage } from '@/api/file'
import { splitTicketContent } from '@/utils/ticketContent'

const loading = ref(false)
const sending = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const visible = ref(false)
const detail = ref<any>(null)
const reply = ref('')
const fileList = ref<UploadFile[]>([])
const query = reactive({ page: 0, size: 20, status: '' })

function beforeUpload(rawFile: UploadRawFile): boolean {
  const ok = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(rawFile.type)
  if (!ok) {
    ElMessage.error('仅支持 JPG / PNG / GIF / WEBP')
    return false
  }
  if (rawFile.size > 5 * 1024 * 1024) {
    ElMessage.error('图片不能超过 5MB')
    return false
  }
  return true
}

type ThreadMsg = {
  key: string
  admin: boolean
  text: string
  images: string[]
  createdAt?: string
}

const thread = computed<ThreadMsg[]>(() => {
  if (!detail.value) return []
  const list: ThreadMsg[] = []
  const first = splitTicketContent(detail.value.content)
  if (first.text || first.images.length) {
    list.push({
      key: 'main',
      admin: false,
      text: first.text,
      images: first.images,
      createdAt: detail.value.createdAt
    })
  }
  for (const r of detail.value.replies || []) {
    const parsed = splitTicketContent(r.content)
    list.push({
      key: String(r.id || `${r.createdAt}-${list.length}`),
      admin: !!r.admin || String(r.senderType || '').toUpperCase() === 'ADMIN',
      text: parsed.text,
      images: parsed.images,
      createdAt: r.createdAt
    })
  }
  return list
})

async function load() {
  loading.value = true
  try {
    const res = await ticketApi.page({ ...query })
    rows.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 0
  load()
}

async function open(row: any) {
  const res = await ticketApi.get(row.id)
  detail.value = res.data
  reply.value = ''
  fileList.value = []
  visible.value = true
}

async function send() {
  if (!detail.value) return
  const text = reply.value.trim()
  const hasFiles = fileList.value.some((f) => !!f.raw)
  if (!text && !hasFiles) {
    ElMessage.warning('请输入回复内容或上传图片')
    return
  }
  sending.value = true
  try {
    const imageUrls: string[] = []
    for (const f of fileList.value) {
      if (!f.raw) continue
      const up = await uploadImage(f.raw as File, 'tickets')
      const url = up?.data?.url
      if (url) imageUrls.push(url)
    }
    const res: any = await ticketApi.reply(detail.value.id, text, imageUrls)
    if (res && typeof res.code === 'number' && res.code !== 200) {
      ElMessage.error(res.message || '回复失败')
      return
    }
    ElMessage.success('已回复')
    reply.value = ''
    fileList.value = []
    await open(detail.value)
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '回复失败')
  } finally {
    sending.value = false
  }
}

async function close(id: number) {
  try {
    const res: any = await ticketApi.close(id)
    if (res && typeof res.code === 'number' && res.code !== 200) {
      ElMessage.error(res.message || '关闭失败')
      return
    }
    ElMessage.success('已关闭')
    if (detail.value?.id === id) {
      await open({ id })
    }
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '关闭失败')
  }
}

onMounted(load)
</script>

<style scoped>
.chat-card {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  margin: -4px -4px 0;
}
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  padding: 4px 4px 14px;
  border-bottom: 1px solid #e5e7eb;
}
.chat-header h3 {
  margin: 0 0 6px;
  font-size: 16px;
  line-height: 1.35;
}
.chat-header p {
  margin: 0;
  color: var(--admin-muted, #64748b);
  font-size: 12px;
}
.chat-body {
  flex: 1;
  overflow: auto;
  padding: 16px 4px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: #fafafa;
  border-radius: 12px;
  margin-top: 12px;
}
.chat-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  max-width: 90%;
}
.chat-row.me {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.av {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  font-size: 11px;
  font-weight: 700;
  background: #dbeafe;
  color: #1d4ed8;
}
.av.mine {
  background: #111827;
  color: #fff;
}
.bubble-wrap {
  min-width: 0;
}
.bubble {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px 16px 16px 4px;
  padding: 10px 12px;
  font-size: 14px;
  line-height: 1.55;
  color: #111827;
}
.bubble.mine {
  background: #003ec7;
  color: #fff;
  border-color: transparent;
  border-radius: 16px 16px 4px 16px;
}
.bubble-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}
.images {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}
.thumb {
  width: 72px;
  height: 72px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.08);
  cursor: pointer;
}
.bubble-time {
  margin-top: 4px;
  font-size: 11px;
  color: #9ca3af;
}
.chat-row.me .bubble-time {
  text-align: right;
}
.chat-composer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: end;
}
.composer-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.chat-composer :deep(.el-textarea__inner) {
  border-radius: 12px;
  min-height: 72px !important;
}
.composer-upload :deep(.el-upload--picture-card),
.composer-upload :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 64px;
  height: 64px;
}
.upload-hint {
  margin: 4px 0 0;
  font-size: 12px;
  color: #94a3b8;
}
.send-btn {
  height: 44px;
  border-radius: 12px;
  font-weight: 700;
  align-self: end;
}
.closed-alert {
  margin-top: 12px;
}
</style>
