<template>
  <div>
    <PageHeader title="订单管理" subtitle="查询、手动发货与通知" />
    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.orderNo" clearable placeholder="订单号" style="width:180px" @keyup.enter="search" />
        <el-input v-model="query.email" clearable placeholder="买家邮箱" style="width:200px" @keyup.enter="search" />
        <el-select v-model="query.status" clearable placeholder="状态" style="width:140px">
          <el-option v-for="s in statuses" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" stripe @row-click="openDetail">
        <el-table-column prop="orderNo" label="订单号" min-width="150" />
        <el-table-column label="买家" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.guest ? 'warning' : 'info'">{{ row.guest ? '游客' : '会员' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="邮箱" min-width="160">
          <template #default="{ row }">{{ row.buyerEmail || row.email }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :status="row.rawStatus || row.status" />
          </template>
        </el-table-column>
        <el-table-column label="应付" width="100">
          <template #default="{ row }">{{ row.payableAmount ?? row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openDetail(row)">详情</el-button>
            <el-button
              link
              type="success"
              :disabled="!canFulfill(row)"
              @click.stop="openFulfill(row)"
            >发货</el-button>
            <el-button link @click.stop="resend(row.id)">重发通知</el-button>
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

    <el-drawer v-model="visible" title="订单详情" size="520px">
      <template v-if="detail">
        <p><b>订单号：</b>{{ detail.orderNo }}</p>
        <p><b>买家：</b>{{ detail.guest ? '游客' : '会员' }}<span v-if="detail.memberId">（ID {{ detail.memberId }}）</span></p>
        <p><b>状态：</b><StatusTag :status="detail.rawStatus || detail.status" /></p>
        <p><b>金额：</b>{{ detail.payableAmount ?? detail.totalAmount }} {{ detail.currency || 'USD' }}</p>
        <p><b>邮箱：</b>{{ detail.buyerEmail || detail.email }}</p>
        <p v-if="detail.shippingCarrier || detail.trackingNumber"><b>物流：</b>{{ detail.shippingCarrier || '-' }} / {{ detail.trackingNumber || '-' }}</p>
        <el-divider />
        <el-table :data="detail.items || []" size="small">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="quantity" label="数量" width="70" />
          <el-table-column prop="price" label="单价" width="90" />
        </el-table>
        <el-divider />
        <el-button v-if="canFulfill(detail)" type="success" style="width:100%;margin-bottom:12px" @click="openFulfill(detail)">手动发货</el-button>
        <el-select v-model="newStatus" placeholder="更新状态" style="width:100%;margin-bottom:8px">
          <el-option v-for="s in statuses" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-button type="primary" @click="updateStatus">更新状态</el-button>
      </template>
    </el-drawer>

    <el-drawer v-model="fulfillVisible" title="手动发货" size="520px" destroy-on-close>
      <template v-if="fulfillRow">
        <p class="fulfill-tip">订单 {{ fulfillRow.orderNo }} — 至少填写物流单号或上传激活二维码图片之一。</p>
        <el-form label-position="top" class="fulfill-form">
          <el-form-item label="承运商">
            <el-select v-model="shipForm.shippingCarrier" clearable placeholder="可选" style="width:100%">
              <el-option label="顺丰速运" value="SF" />
              <el-option label="圆通速递" value="YTO" />
              <el-option label="中通快递" value="ZTO" />
              <el-option label="韵达快递" value="YD" />
              <el-option label="申通快递" value="STO" />
              <el-option label="极兔速递" value="JTSD" />
              <el-option label="京东物流" value="JD" />
              <el-option label="邮政EMS" value="EMS" />
              <el-option label="德邦快递" value="DBL" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="物流单号">
            <el-input v-model="shipForm.trackingNumber" placeholder="实体物流单号（可选）" />
          </el-form-item>
          <el-form-item label="手机号码">
            <el-input v-model="shipForm.shippingPhone" placeholder="发货所用手机号，展示给用户查看" />
          </el-form-item>
          <el-form-item label="激活码/二维码">
            <div class="qr-upload">
              <el-upload
                :show-file-list="false"
                accept="image/jpeg,image/png,image/gif,image/webp"
                :http-request="uploadQr"
                :disabled="qrUploading"
              >
                <div v-if="shipForm.trackingQrCode" class="qr-preview" @click.stop>
                  <img :src="qrPreviewSrc" alt="二维码" />
                </div>
                <el-button v-else :loading="qrUploading" type="primary" plain>
                  {{ qrUploading ? '上传中…' : '上传二维码图片' }}
                </el-button>
              </el-upload>
              <div v-if="shipForm.trackingQrCode" class="qr-actions">
                <el-upload
                  :show-file-list="false"
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  :http-request="uploadQr"
                  :disabled="qrUploading"
                >
                  <el-button size="small" :loading="qrUploading">重新上传</el-button>
                </el-upload>
                <el-button size="small" type="danger" plain :disabled="qrUploading" @click="clearQr">移除</el-button>
              </div>
              <p class="field-hint">请上传二维码图片（JPG / PNG / WEBP），勿手动填写链接</p>
            </div>
          </el-form-item>
          <el-form-item label="发货备注">
            <el-input v-model="shipForm.shippingRemark" type="textarea" :rows="3" placeholder="可选" />
          </el-form-item>
        </el-form>
        <div class="fulfill-actions">
          <el-button @click="fulfillVisible = false">取消</el-button>
          <el-button type="primary" :loading="fulfilling" @click="confirmFulfill">确认发货</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type UploadRequestOptions } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { orderApi } from '@/api/order'
import { uploadImage } from '@/api/file'

const statuses = [
  { value: 'PENDING_PAYMENT', label: '待支付' },
  { value: 'PAID', label: '已支付' },
  { value: 'PROCESSING', label: '处理中' },
  { value: 'SHIPPED', label: '已发货' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' }
]

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const visible = ref(false)
const detail = ref<any>(null)
const newStatus = ref('')
const query = reactive({ page: 0, size: 20, orderNo: '', email: '', status: '' })

const fulfillVisible = ref(false)
const fulfillRow = ref<any>(null)
const fulfilling = ref(false)
const qrUploading = ref(false)
const shipForm = reactive({
  shippingCarrier: '',
  trackingNumber: '',
  shippingPhone: '',
  trackingQrCode: '',
  shippingRemark: ''
})

const qrPreviewSrc = computed(() => {
  const u = (shipForm.trackingQrCode || '').trim()
  if (!u) return ''
  if (u.startsWith('http://') || u.startsWith('https://') || u.startsWith('/')) return u
  return u
})

function canFulfill(row: any) {
  const s = String(row?.rawStatus || row?.status || '').toUpperCase()
  return ['PAID', 'PROCESSING', 'FULFILLING', 'SHIPPED', '1', '2'].includes(s)
}

async function load() {
  loading.value = true
  try {
    const res = await orderApi.page({ ...query })
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

async function openDetail(row: any) {
  const res = await orderApi.get(row.id)
  detail.value = res.data
  newStatus.value = res.data?.rawStatus || res.data?.status || ''
  visible.value = true
}

async function updateStatus() {
  if (!detail.value) return
  await orderApi.updateStatus(detail.value.id, newStatus.value)
  ElMessage.success('已更新')
  openDetail(detail.value)
  load()
}

function openFulfill(row: any) {
  fulfillRow.value = row
  shipForm.shippingCarrier = row.shippingCarrier || ''
  shipForm.trackingNumber = row.trackingNumber || ''
  shipForm.shippingPhone = ''
  shipForm.trackingQrCode = row.trackingQrCode || ''
  shipForm.shippingRemark = ''
  visible.value = false
  fulfillVisible.value = true
}

async function uploadQr(options: UploadRequestOptions) {
  const file = options.file as File
  if (!file) return
  qrUploading.value = true
  try {
    const res = await uploadImage(file, 'shipping')
    const url = res?.data?.url
    if (!url) {
      throw new Error(res?.message || '上传失败')
    }
    shipForm.trackingQrCode = url
    ElMessage.success('二维码已上传')
    options.onSuccess?.(res as any)
  } catch (e: any) {
    const msg = e?.message || '上传失败'
    ElMessage.error(msg)
    options.onError?.(e)
  } finally {
    qrUploading.value = false
  }
}

function clearQr() {
  shipForm.trackingQrCode = ''
}

async function confirmFulfill() {
  if (!fulfillRow.value) return
  if (!shipForm.trackingNumber.trim() && !shipForm.trackingQrCode.trim()) {
    ElMessage.warning('请至少填写物流单号或上传二维码图片')
    return
  }
  fulfilling.value = true
  try {
    const res: any = await orderApi.fulfill(fulfillRow.value.id, {
      shippingCarrier: shipForm.shippingCarrier || undefined,
      trackingNumber: shipForm.trackingNumber.trim() || undefined,
      shippingPhone: shipForm.shippingPhone.trim() || undefined,
      trackingQrCode: shipForm.trackingQrCode.trim() || undefined,
      shippingRemark: shipForm.shippingRemark.trim() || undefined
    })
    ElMessage.success(res?.data?.message || res?.message || '发货成功')
    fulfillVisible.value = false
    if (detail.value?.id === fulfillRow.value.id) {
      await openDetail(fulfillRow.value)
    }
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '发货失败')
  } finally {
    fulfilling.value = false
  }
}

async function resend(id: number) {
  await orderApi.resendNotification(id)
  ElMessage.success('已重发')
}

onMounted(load)
</script>

<style scoped>
.fulfill-tip {
  margin: 0 0 16px;
  color: #6e6e73;
  font-size: 13px;
  line-height: 1.5;
}
.fulfill-form {
  padding-bottom: 72px;
}
.fulfill-actions {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}
.qr-upload {
  width: 100%;
}
.qr-preview {
  width: 180px;
  height: 180px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  background: #f8fafc;
  display: grid;
  place-items: center;
}
.qr-preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  display: block;
}
.qr-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  align-items: center;
}
.field-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}
</style>
