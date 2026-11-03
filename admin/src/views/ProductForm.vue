<template>
  <div class="product-form-page" v-loading="loading">
    <PageHeader :title="isEdit ? '编辑产品' : '新建产品'" :subtitle="isEdit ? `ID ${route.params.id}` : '填写基础信息后保存，可继续完善说明与定价'">
      <template #actions>
        <el-button @click="$router.push('/products')">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">{{ isEdit ? '保存' : '创建' }}</el-button>
      </template>
    </PageHeader>

    <el-form :model="form" label-position="top" class="product-form" require-asterisk-position="right">
      <div class="form-layout">
        <div class="form-main">
          <!-- 标题 -->
          <section class="form-card">
            <h3 class="card-title">标题</h3>
            <el-form-item label="中文名称" required>
              <el-input v-model="form.nameZh" size="large" placeholder="前台展示的中文名" />
            </el-form-item>
            <el-form-item label="英文名称">
              <el-input v-model="form.nameEn" placeholder="English name" />
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="中文简介">
                  <el-input v-model="form.summaryZh" type="textarea" :rows="2" placeholder="详情页标题下方短文案" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="英文简介">
                  <el-input v-model="form.summaryEn" type="textarea" :rows="2" placeholder="Short blurb under title" />
                </el-form-item>
              </el-col>
            </el-row>
          </section>

          <!-- 媒体 -->
          <section class="form-card">
            <h3 class="card-title">媒体</h3>
            <el-form-item label="展示图 URL">
              <el-input v-model="form.displayImageUrl" placeholder="https://..." clearable />
              <div class="field-hint">用于列表卡与详情页主图</div>
            </el-form-item>
            <div v-if="form.displayImageUrl" class="media-preview">
              <img :src="form.displayImageUrl" alt="preview" @error="onPreviewError" />
            </div>
          </section>

          <!-- 覆盖与规格 -->
          <section class="form-card">
            <h3 class="card-title">覆盖与规格</h3>
            <el-form-item label="覆盖区域">
              <el-select
                v-model="form.coverageRegions"
                multiple
                filterable
                allow-create
                default-first-option
                collapse-tags
                collapse-tags-tooltip
                placeholder="选择或输入区域，如 Asia / Global"
                style="width: 100%"
              >
                <el-option v-for="r in regionOptions" :key="r" :label="r" :value="r" />
              </el-select>
            </el-form-item>
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12">
                <el-form-item label="流量">
                  <el-input v-model="form.dataLimit" placeholder="如 1GB / Unlimited" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="基础有效天数">
                  <el-input-number v-model="form.baseValidityDays" :min="0" :controls="false" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </section>

          <!-- 说明与特性 -->
          <section class="form-card">
            <div class="card-title-row">
              <h3 class="card-title">说明与特性</h3>
              <el-radio-group v-model="contentLangTab" size="small">
                <el-radio-button label="zh">中文</el-radio-button>
                <el-radio-button label="en">English</el-radio-button>
              </el-radio-group>
            </div>
            <template v-if="contentLangTab === 'zh'">
              <el-form-item label="产品说明">
                <el-input v-model="notesZhText" type="textarea" :rows="5" placeholder="详情页左侧「产品说明」，支持换行" />
              </el-form-item>
              <el-form-item label="产品特性">
                <el-input v-model="featuresZhText" type="textarea" :rows="6" placeholder="每行一条，例如：&#10;高速流量&#10;支持热点&#10;!需解锁设备" />
                <div class="field-hint">一行一条；以 ! 开头显示为提醒样式</div>
              </el-form-item>
            </template>
            <template v-else>
              <el-form-item label="Product details">
                <el-input v-model="notesEnText" type="textarea" :rows="5" placeholder="Left-side product note" />
              </el-form-item>
              <el-form-item label="Product specs">
                <el-input v-model="featuresEnText" type="textarea" :rows="6" placeholder="One feature per line" />
                <div class="field-hint">One per line; prefix with ! for warning style</div>
              </el-form-item>
            </template>
          </section>

          <!-- 定价档 -->
          <section class="form-card">
            <div class="card-title-row">
              <h3 class="card-title">定价档</h3>
              <el-button type="primary" plain size="small" @click="addSku">添加档位</el-button>
            </div>
            <div class="field-hint" style="margin-top: -4px; margin-bottom: 12px">
              可添加多档相同月份；保存时会同步增删改定价档。
            </div>
            <div v-if="!form.skus.length" class="empty-skus">
              暂无定价档，点击右上角添加
            </div>
            <div v-else class="sku-list">
              <div v-for="(row, index) in form.skus" :key="index" class="sku-row">
                <div class="sku-row__main">
                  <el-input v-model="row.labelZh" placeholder="标签，如 1个月" />
                  <el-input-number v-model="row.months" :min="0" :controls="false" placeholder="月" />
                  <el-input-number v-model="row.durationDays" :min="0" :controls="false" placeholder="天数" />
                  <el-input-number v-model="row.salePrice" :min="0" :precision="2" :controls="false" placeholder="售价 USD" />
                </div>
                <div class="sku-row__meta">
                  <el-checkbox v-model="row.isDefault" @change="onDefaultChange(index)">默认档</el-checkbox>
                  <el-button link type="danger" @click="form.skus.splice(index, 1)">删除</el-button>
                </div>
              </div>
            </div>
          </section>
        </div>

        <aside class="form-side">
          <section class="form-card side-card sticky-card">
            <h3 class="card-title">发布</h3>
            <el-form-item label="上架状态">
              <el-radio-group v-model="form.status" class="status-group">
                <el-radio-button label="ON_SALE">上架</el-radio-button>
                <el-radio-button label="DRAFT">草稿</el-radio-button>
                <el-radio-button label="OFF_SALE">下架</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="角标">
              <el-radio-group v-model="badgeMark" class="status-group">
                <el-radio-button label="none">无</el-radio-button>
                <el-radio-button label="hot">Hot</el-radio-button>
                <el-radio-button label="featured">Featured</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="卡片类型">
              <el-radio-group v-model="physicalType" class="status-group">
                <el-radio-button :label="0">eSIM</el-radio-button>
                <el-radio-button :label="1">实体卡</el-radio-button>
              </el-radio-group>
              <div class="field-hint">实体卡下单须选设备并填地址</div>
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" :controls="false" style="width: 100%" />
            </el-form-item>
          </section>

          <section class="form-card side-card">
            <h3 class="card-title">分类</h3>
            <el-form-item label="产品类型">
              <el-select v-model="form.productKind" style="width: 100%">
                <el-option label="ESIM" value="ESIM" />
                <el-option label="PHONE" value="PHONE" />
                <el-option label="DEVICE" value="DEVICE" />
              </el-select>
            </el-form-item>
            <el-form-item label="大分类">
              <div class="select-with-action">
                <el-select v-model="rootId" clearable placeholder="选择大类" style="flex: 1" @change="onRootChange">
                  <el-option v-for="c in roots" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
                </el-select>
                <el-button @click="openCatDialog('root')">+</el-button>
              </div>
            </el-form-item>
            <el-form-item label="二级分类" required>
              <div class="select-with-action">
                <el-select v-model="form.categoryId" clearable placeholder="选择二级分类" style="flex: 1">
                  <el-option v-for="c in children" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
                </el-select>
                <el-button @click="openCatDialog('child')">+</el-button>
              </div>
            </el-form-item>
            <el-form-item label="SPU 编码">
              <el-input v-model="form.spuCode" placeholder="留空自动生成" :disabled="isEdit" />
            </el-form-item>
          </section>

          <section class="form-card side-card side-actions-card">
            <div class="side-actions">
              <el-button class="side-action-btn" @click="$router.push('/products')">取消</el-button>
              <el-button class="side-action-btn" type="primary" :loading="saving" @click="save">
                {{ isEdit ? '保存' : '创建' }}
              </el-button>
            </div>
          </section>
        </aside>
      </div>
    </el-form>

    <el-dialog
      v-model="catVisible"
      :title="catMode === 'root' ? '快速新建大分类' : '快速新建二级分类'"
      width="480px"
      destroy-on-close
    >
      <el-form :model="catForm" label-position="top">
        <el-form-item v-if="catMode === 'child'" label="所属大类">
          <el-select v-model="catForm.parentId" style="width: 100%">
            <el-option v-for="c in roots" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="catForm.code" placeholder="留空自动生成" />
        </el-form-item>
        <el-form-item label="中文名" required>
          <el-input v-model="catForm.nameZh" />
        </el-form-item>
        <el-form-item label="英文名">
          <el-input v-model="catForm.nameEn" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catVisible = false">取消</el-button>
        <el-button type="primary" :loading="catSaving" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { productApi } from '@/api/product'
import { categoryApi } from '@/api/category'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const saving = ref(false)
const roots = ref<any[]>([])
const children = ref<any[]>([])
const rootId = ref<number>()
const contentLangTab = ref<'zh' | 'en'>('zh')
const badgeMark = ref<'none' | 'hot' | 'featured'>('none')
const physicalType = ref<0 | 1>(0)
const notesZhText = ref('')
const notesEnText = ref('')
const featuresZhText = ref('')
const featuresEnText = ref('')
const regionOptions = ['Asia', 'Europe', 'America', 'Oceania', 'Africa', 'Global']

const form = reactive<any>({
  spuCode: '',
  productKind: 'ESIM',
  nameZh: '',
  nameEn: '',
  summaryZh: '',
  summaryEn: '',
  categoryId: undefined,
  status: 'DRAFT',
  sortOrder: 0,
  dataLimit: '',
  baseValidityDays: 30,
  displayImageUrl: '',
  coverageRegions: [] as string[],
  skus: [] as any[]
})

const catVisible = ref(false)
const catSaving = ref(false)
const catMode = ref<'root' | 'child'>('root')
const catForm = reactive({
  parentId: undefined as number | undefined,
  code: '',
  nameZh: '',
  nameEn: ''
})

function featuresToText(raw: unknown): string {
  if (!raw) return ''
  if (Array.isArray(raw)) return raw.map(String).join('\n')
  if (typeof raw === 'string') {
    const s = raw.trim()
    if (!s) return ''
    try {
      const parsed = JSON.parse(s)
      if (Array.isArray(parsed)) return parsed.map(String).join('\n')
    } catch {
      /* plain */
    }
    return s
  }
  return ''
}

function textToFeaturesJson(text: string): string {
  const items = (text || '')
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter(Boolean)
  return JSON.stringify(items)
}

function addSku() {
  form.skus.push({
    labelZh: '',
    months: 1,
    durationDays: 30,
    salePrice: 0,
    isDefault: form.skus.length === 0,
    status: 1,
    sortOrder: form.skus.length
  })
}

function onDefaultChange(index: number) {
  if (!form.skus[index]?.isDefault) return
  form.skus.forEach((s: any, i: number) => {
    if (i !== index) s.isDefault = false
  })
}

function onPreviewError(e: Event) {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
}

async function loadRoots() {
  const res = await categoryApi.listRoots()
  roots.value = res.data || []
}

async function loadChildren(keepSelection = false) {
  if (!keepSelection) form.categoryId = undefined
  if (!rootId.value) {
    children.value = []
    return
  }
  const res = await categoryApi.listChildren(rootId.value)
  children.value = res.data || []
}

function onRootChange() {
  loadChildren(false)
}

function openCatDialog(mode: 'root' | 'child') {
  if (mode === 'child' && !rootId.value) {
    ElMessage.warning('请先选择或新建大分类')
    return
  }
  catMode.value = mode
  catForm.parentId = mode === 'child' ? rootId.value : undefined
  catForm.code = ''
  catForm.nameZh = ''
  catForm.nameEn = ''
  catVisible.value = true
}

async function saveCategory() {
  const nameZh = (catForm.nameZh || '').trim()
  if (!nameZh) {
    ElMessage.warning('请填写中文名')
    return
  }
  const code = (catForm.code || '').trim()
  const payload = {
    nameZh,
    name: nameZh,
    nameEn: (catForm.nameEn || '').trim() || undefined,
    code: code || undefined,
    parentId: catForm.parentId,
    categoryId: catForm.parentId,
    sortOrder: 0,
    status: 1,
    showOnFront: catMode.value === 'root'
  }
  catSaving.value = true
  try {
    if (catMode.value === 'root') {
      const res = await categoryApi.createRoot(payload)
      await loadRoots()
      rootId.value = res.data?.id
      await loadChildren(false)
    } else {
      if (!catForm.parentId) {
        ElMessage.warning('请选择所属大类')
        return
      }
      const res = await categoryApi.createChild(payload)
      rootId.value = catForm.parentId
      await loadChildren(true)
      form.categoryId = res.data?.id
    }
    ElMessage.success('分类已创建')
    catVisible.value = false
  } finally {
    catSaving.value = false
  }
}

async function save() {
  if (!(form.nameZh || '').trim()) {
    ElMessage.warning('请填写中文名')
    return
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择二级分类')
    return
  }
  saving.value = true
  try {
    const statusMap: Record<string, number> = { ON_SALE: 1, DRAFT: 0, OFF_SALE: 0 }
    const payload = {
      name: (form.nameZh || '').trim(),
      nameZh: (form.nameZh || '').trim(),
      nameEn: (form.nameEn || '').trim() || undefined,
      summaryZh: form.summaryZh ?? '',
      summaryEn: form.summaryEn ?? '',
      notesZh: notesZhText.value ?? '',
      notesEn: notesEnText.value ?? '',
      features: textToFeaturesJson(featuresZhText.value),
      featuresEn: textToFeaturesJson(featuresEnText.value),
      spuCode: (form.spuCode || '').trim() || undefined,
      productKind: form.productKind,
      categoryId: form.categoryId,
      subCategoryId: form.categoryId,
      status: statusMap[form.status] ?? 0,
      sortOrder: form.sortOrder ?? 0,
      dataLimit: form.dataLimit || undefined,
      validityDays: form.baseValidityDays ?? 0,
      baseValidityDays: form.baseValidityDays ?? 0,
      displayImageUrl: form.displayImageUrl || undefined,
      imageUrl: form.displayImageUrl || undefined,
      applicableRegions: form.coverageRegions || [],
      isHot: badgeMark.value === 'hot',
      isFeatured: badgeMark.value === 'featured',
      isPhysical: physicalType.value === 1,
      isPhysicalCard: physicalType.value,
      // 始终提交 skus，编辑时后端会按 id 同步增删改
      skus: (form.skus || []).map((s: any, i: number) => ({
        id: s.id,
        skuCode: s.skuCode,
        label: s.labelZh || s.label,
        labelZh: s.labelZh || s.label,
        labelEn: s.labelEn,
        months: s.months,
        durationDays: s.durationDays,
        salePrice: s.salePrice,
        isDefault: !!s.isDefault,
        status: s.status ?? 1,
        sortOrder: i
      }))
    }
    const res = isEdit.value
      ? await productApi.update(Number(route.params.id), payload)
      : await productApi.create(payload)
    if (res && typeof res.code === 'number' && res.code !== 200) {
      ElMessage.error(res.message || '保存失败')
      return
    }
    ElMessage.success('已保存')
    router.push('/products')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadRoots()
  if (!isEdit.value) {
    addSku()
    return
  }
  loading.value = true
  try {
    const res = await productApi.get(Number(route.params.id))
    const data = res.data || {}
    Object.assign(form, data)
    if (!form.skus) form.skus = []
    form.skus = (form.skus || []).map((s: any) => ({
      ...s,
      labelZh: s.labelZh || s.label || ''
    }))
    form.summaryZh = data.summaryZh ?? ''
    form.summaryEn = data.summaryEn ?? ''
    notesZhText.value = data.notesZh ?? ''
    notesEnText.value = data.notesEn ?? ''
    featuresZhText.value = featuresToText(data.features)
    featuresEnText.value = featuresToText(data.featuresEn)
    if (data.isHot) badgeMark.value = 'hot'
    else if (data.isFeatured) badgeMark.value = 'featured'
    else badgeMark.value = 'none'
    physicalType.value = data.isPhysical || data.isPhysicalCard === 1 ? 1 : 0
    form.baseValidityDays = data.baseValidityDays ?? data.validityDays ?? 30
    form.coverageRegions = data.applicableRegions || data.coverageRegions || []
    const subId = data.subCategoryId || data.categoryId
    form.categoryId = subId
    if (subId) {
      for (const r of roots.value) {
        const childRes = await categoryApi.listChildren(r.id)
        const list = childRes.data || []
        if (list.some((c: any) => c.id === subId)) {
          rootId.value = r.id
          children.value = list
          break
        }
      }
    }
    if (data.status === 1 || data.status === 'ON_SALE') form.status = 'ON_SALE'
    else if (data.status === 'DRAFT') form.status = 'DRAFT'
    else form.status = 'OFF_SALE'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.product-form-page {
  padding-bottom: 32px;
}

.form-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 20px;
  align-items: start;
}

.form-main,
.form-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-card {
  background: #fff;
  border: 1px solid var(--admin-border, #e5e7eb);
  border-radius: 12px;
  padding: 20px 20px 8px;
  box-shadow: var(--admin-shadow, 0 1px 2px rgba(15, 23, 42, 0.06));
}

.side-card {
  padding-bottom: 12px;
}

.sticky-card {
  position: sticky;
  top: 16px;
  z-index: 2;
}

.card-title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 650;
  color: var(--admin-text, #0f172a);
}

.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.card-title-row .card-title {
  margin-bottom: 0;
}

.field-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--admin-muted, #64748b);
  line-height: 1.4;
}

.select-with-action {
  display: flex;
  gap: 8px;
  width: 100%;
  align-items: center;
}

.status-group {
  width: 100%;
  display: flex;
}

.status-group :deep(.el-radio-button) {
  flex: 1;
}

.status-group :deep(.el-radio-button__inner) {
  width: 100%;
}

.media-preview {
  margin: -4px 0 14px;
  width: 160px;
  height: 100px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--admin-border, #e5e7eb);
  background: #f8fafc;
}

.media-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.empty-skus {
  padding: 24px 12px 20px;
  text-align: center;
  color: var(--admin-muted, #64748b);
  font-size: 13px;
  border: 1px dashed var(--admin-border, #e5e7eb);
  border-radius: 10px;
  margin-bottom: 12px;
}

.sku-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}

.sku-row {
  border: 1px solid var(--admin-border, #e5e7eb);
  border-radius: 10px;
  padding: 12px;
  background: #fafbfc;
}

.sku-row__main {
  display: grid;
  grid-template-columns: 1.4fr 0.7fr 0.7fr 0.9fr;
  gap: 10px;
}

.sku-row__main :deep(.el-input-number) {
  width: 100%;
}

.sku-row__meta {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.side-actions-card {
  padding: 16px 20px !important;
}

.side-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.side-action-btn {
  width: 100%;
  margin: 0 !important;
}

.product-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.product-form :deep(.el-form-item__label) {
  margin-bottom: 6px;
  font-weight: 550;
  color: var(--admin-muted, #64748b);
  line-height: 1.2;
}

@media (max-width: 960px) {
  .form-layout {
    grid-template-columns: 1fr;
  }

  .sticky-card {
    position: static;
  }

  .sku-row__main {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
