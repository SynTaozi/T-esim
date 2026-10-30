<template>
  <div class="content-card phone-numbers-tab">
    <div class="phone-header">
      <h2>{{ $t('profile.phoneNumbers') }}</h2>
      <el-button type="primary" class="add-phone-btn" @click="$emit('add-phone')">
        <el-icon><Plus /></el-icon>
        {{ $t('profile.addPhoneNumber') }}
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="phone-stats">
      <div class="stat-item">
        <span class="stat-value">{{ phoneStats.total }}</span>
        <span class="stat-label">全部号码</span>
      </div>
      <div class="stat-item active">
        <span class="stat-value">{{ phoneStats.active }}</span>
        <span class="stat-label">正常使用</span>
      </div>
      <div class="stat-item expiring">
        <span class="stat-value">{{ phoneStats.expiring }}</span>
        <span class="stat-label">即将过期</span>
      </div>
      <div class="stat-item expired">
        <span class="stat-value">{{ phoneStats.expired }}</span>
        <span class="stat-label">已过期</span>
      </div>
    </div>

    <!-- 电话号码列表 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="loading-spinner"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <template v-else>
      <div class="phone-grid">
        <div v-for="phone in phoneNumbers" :key="phone.id" class="phone-card" 
             :class="{ [`status-${getPhoneStatus(phone).status}`]: true }">
          
          <!-- 卡片头 -->
          <div class="card-header">
            <div class="phone-number-display">
              <span class="country-flag">{{ getCountryFlag(phone.countryCode) }}</span>
              <span class="number">{{ phone.number }}</span>
            </div>
            <el-dropdown trigger="click" @command="handleCardCommand(phone, $event)">
              <el-icon class="more-icon"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="view">{{ $t('profile.view') }}</el-dropdown-item>
                  <el-dropdown-item command="renew" v-if="getPhoneStatus(phone).canRenew">
                    {{ $t('profile.renewPlan') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete">{{ $t('profile.delete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!-- 状态指示 -->
          <div class="card-status">
            <span class="status-badge" :class="`badge-${getPhoneStatus(phone).status}`">
              {{ getPhoneStatus(phone).text }}
            </span>
            <span class="validity-text" v-if="phone.validityDays">
              {{ phone.validityDays }}天有效期
            </span>
          </div>

          <!-- 方案信息 -->
          <div class="card-info">
            <div class="info-row">
              <span class="label">{{ $t('profile.planName') }}：</span>
              <span class="value">{{ phone.planName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">{{ $t('profile.data') }}：</span>
              <span class="value">{{ phone.dataSpec || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">{{ $t('profile.expireDate') }}：</span>
              <span class="value" :class="getExpiryClass(phone.expiryDate)">
                {{ phone.expiryDate ? formatDate(phone.expiryDate) : '-' }}
              </span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="card-actions">
            <el-button v-if="getPhoneStatus(phone).canRenew" type="primary" size="small" 
                       @click="$emit('renew-phone', phone)">
              {{ $t('profile.renewNow') }}
            </el-button>
            <el-button v-if="getPhoneStatus(phone).status === 'active'" size="small"
                       @click="$emit('view-phone', phone)">
              {{ $t('profile.details') }}
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="phoneNumbers.length === 0" class="empty-state">
        <div class="empty-icon">📱</div>
        <h3>{{ $t('profile.noPhoneNumbers') }}</h3>
        <p>还没有电话号码，现在就添加一个吧</p>
        <el-button type="primary" @click="$emit('add-phone')">
          <el-icon><Plus /></el-icon>
          {{ $t('profile.addPhoneNumber') }}
        </el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="phoneTotal > pageSize">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="phoneTotal"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="$emit('page-change', $event)"
        />
      </div>
    </template>

    <!-- 更新对话框 -->
    <el-dialog v-model="renewDialogVisible" :title="$t('profile.renewPlan')" width="500px">
      <template v-if="selectedPhone">
        <div class="renewal-form">
          <div class="form-item">
            <label>{{ $t('profile.currentPlan') }}：</label>
            <span>{{ selectedPhone.planName }}</span>
          </div>
          <div class="form-item">
            <label>{{ $t('profile.renewalPeriod') }}：</label>
            <el-select v-model="renewalPeriod">
              <el-option label="1 月" value="1"></el-option>
              <el-option label="3 月" value="3"></el-option>
              <el-option label="12 月" value="12"></el-option>
            </el-select>
          </div>
          <div class="form-item">
            <label>{{ $t('profile.renewalCost') }}：</label>
            <span class="cost">¥{{ renewalCost }}</span>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="renewDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleRenewal">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" :title="$t('profile.phoneDetails')" width="600px">
      <template v-if="selectedPhone">
        <div class="detail-info">
          <div class="detail-row">
            <label>{{ $t('profile.phoneNumber') }}：</label>
            <span>{{ selectedPhone.number }}</span>
          </div>
          <div class="detail-row">
            <label>{{ $t('profile.countryRegion') }}：</label>
            <span>{{ selectedPhone.countryName }} {{ selectedPhone.countryCode }}</span>
          </div>
          <div class="detail-row">
            <label>{{ $t('profile.planName') }}：</label>
            <span>{{ selectedPhone.planName }}</span>
          </div>
          <div class="detail-row">
            <label>{{ $t('profile.data') }}：</label>
            <span>{{ selectedPhone.dataSpec }}</span>
          </div>
          <div class="detail-row">
            <label>{{ $t('profile.validity') }}：</label>
            <span>{{ selectedPhone.validityDays }} 天</span>
          </div>
          <div class="detail-row">
            <label>{{ $t('profile.expiryDate') }}：</label>
            <span>{{ selectedPhone.expiryDate ? formatDate(selectedPhone.expiryDate) : '-' }}</span>
          </div>
          <div class="detail-row">
            <label>{{ $t('profile.purchaseDate') }}：</label>
            <span>{{ selectedPhone.createdAt ? formatDate(selectedPhone.createdAt) : '-' }}</span>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="detailDialogVisible = false">{{ $t('common.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Plus, MoreFilled, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps<{
  phoneNumbers: any[]
  phoneTotal: number
  pageSize: number
  currentPage: number
  loading: boolean
  phoneStats: {
    total: number
    active: number
    expiring: number
    expired: number
  }
  getPhoneStatus: (phone: any) => { status: string; text: string; canRenew: boolean }
  getCountryFlag: (code: string) => string
  getExpiryClass: (expiryDate: string) => string
  formatDate: (dateStr: string) => string
}>()

const emit = defineEmits<{
  'add-phone': []
  'view-phone': [phone: any]
  'renew-phone': [phone: any]
  'delete-phone': [id: number]
  'confirm-renewal': [phone: any, period: number, cost: number]
  'page-change': [page: number]
}>()

const renewDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const selectedPhone = ref<any>(null)
const renewalPeriod = ref('1')
const renewalCost = ref(0)

function handleCardCommand(phone: any, command: string) {
  selectedPhone.value = phone
  
  switch (command) {
    case 'view':
      detailDialogVisible.value = true
      break
    case 'renew':
      renewDialogVisible.value = true
      break
    case 'delete':
      ElMessageBox.confirm(
        `确定要删除 ${phone.number} 吗？`,
        'Warning',
        { confirmButtonText: '确定', cancelButtonText: '取消' }
      ).then(() => {
        emit('delete-phone', phone.id)
      }).catch(() => {})
      break
  }
}

function handleRenewal() {
  emit('confirm-renewal', selectedPhone.value, parseInt(renewalPeriod.value), renewalCost.value)
  renewDialogVisible.value = false
}
</script>

<style scoped>
.content-card {
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-default);
  box-shadow: var(--shadow-sm);
  padding: 0;
}

.phone-numbers-tab {
  padding: 0;
}

.phone-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-subtle);
  background: var(--bg-hover);
}

.phone-header h2 {
  margin: 0;
  padding: 0;
  font-size: 18px;
  color: var(--text-primary);
  font-weight: 600;
}

.add-phone-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  border: none !important;
  border-radius: 8px;
}

.phone-stats {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-subtle);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background: var(--bg-hover);
  border-radius: 8px;
  border: 1px solid var(--border-default);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.stat-item.active .stat-value {
  color: #10b981;
}

.stat-item.expiring .stat-value {
  color: #fbbf24;
}

.stat-item.expired .stat-value {
  color: #f87171;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--text-muted);
}

.loading-spinner {
  font-size: 40px;
  color: var(--accent-primary);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.phone-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  padding: 24px;
  background: var(--bg-primary);
  min-height: 200px;
}

.phone-card {
  background: var(--bg-card);
  border: 2px solid var(--border-default);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.25s;
}

.phone-card:hover {
  border-color: var(--accent-primary);
  box-shadow: var(--shadow-sm);
}

.phone-card.status-active {
  border-color: rgba(16, 185, 129, 0.3);
}

.phone-card.status-expiring {
  border-color: rgba(251, 191, 36, 0.3);
}

.phone-card.status-expired {
  border-color: rgba(244, 63, 94, 0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.phone-number-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.country-flag {
  font-size: 20px;
}

.number {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: 'JetBrains Mono', monospace;
}

.more-icon {
  cursor: pointer;
  padding: 4px;
  color: var(--text-muted);
  transition: all 0.2s;
}

.more-icon:hover {
  color: var(--accent-primary);
}

.card-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.badge-active {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.badge-expiring {
  background: rgba(251, 191, 36, 0.15);
  color: #fbbf24;
}

.badge-expired {
  background: rgba(244, 63, 94, 0.15);
  color: #f87171;
}

.validity-text {
  font-size: 11px;
  color: var(--text-muted);
}

.card-info {
  margin-bottom: 12px;
  padding: 12px;
  background: var(--bg-hover);
  border-radius: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  line-height: 1.8;
}

.info-row .label {
  color: var(--text-muted);
}

.info-row .value {
  color: var(--text-secondary);
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.card-actions button {
  flex: 1;
}

.empty-state {
  text-align: center;
  padding: 60px 32px;
}

.empty-icon {
  font-size: 70px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 18px;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.empty-state p {
  color: var(--text-muted);
  margin: 0 0 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px;
}

.renewal-form,
.detail-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item,
.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item label,
.detail-row label {
  color: var(--text-secondary);
  font-weight: 500;
}

.cost {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent-primary);
}

.renewal-form :deep(.el-select) {
  width: 200px;
}
</style>
