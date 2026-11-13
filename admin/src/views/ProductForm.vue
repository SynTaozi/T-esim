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
            <h3 class="card-title">{{ isDevice ? '设备信息' : '标题' }}</h3>
            <el-form-item label="中文名称" required>
              <el-input v-model="form.nameZh" size="large" placeholder="前台展示的中文名" />
            </el-form-item>
            <el-form-item label="英文名称">
              <el-input v-model="form.nameEn" placeholder="English name" />
            </el-form-item>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="中文简介">
                  <el-input
                    v-model="form.summaryZh"
                    type="textarea"
                    :rows="isDevice ? 3 : 2"
                    placeholder="前台悬停/列表展示的中文简介"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="英文简介">
                  <el-input
                    v-model="form.summaryEn"
                    type="textarea"
                    :rows="isDevice ? 3 : 2"
                    placeholder="English summary for storefront"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item v-if="isDevice" label="价格 (USD)" required>
              <el-input-number v-model="devicePrice" :min="0" :precision="2" :controls="false" style="width: 220px" />
            </el-form-item>
          </section>

          <!-- 媒体（非 DEVICE） -->
          <section v-if="!isDevice" class="form-card">
            <h3 class="card-title">媒体</h3>
            <el-form-item label="展示图 URL">
              <el-input v-model="form.displayImageUrl" placeholder="https://..." clearable />
              <div class="field-hint">用于列表卡与详情页主图</div>
            </el-form-item>
            <div v-if="form.displayImageUrl" class="media-preview">
              <img :src="form.displayImageUrl" alt="preview" @error="onPreviewError" />
            </div>
          </section>

          <!-- 覆盖与规格（非 DEVICE） -->
          <section v-if="!isDevice" class="form-card">
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

          <!-- 说明与特性（非 DEVICE） -->
          <section v-if="!isDevice" class="form-card">
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

          <!-- 定价（非 DEVICE） -->
          <section v-if="!isDevice" class="form-card">
            <div class="card-title-row">
              <h3 class="card-title">定价</h3>
              <el-radio-group v-model="form.priceMode" size="small" @change="onPriceModeChange">
                <el-radio-button label="MONTHLY">月份定价</el-radio-button>
                <el-radio-button label="FLAT">一口价</el-radio-button>
              </el-radio-group>
            </div>

            <template v-if="form.priceMode === 'FLAT'">
              <div class="field-hint" style="margin-top: -4px; margin-bottom: 12px">
                一口价含快递；eSIM / 实体卡可分别定价。前台不展示周期档位。
              </div>
              <el-row :gutter="16">
                <el-col v-if="cardTypes.includes('ESIM')" :xs="24" :sm="12">
                  <el-form-item label="eSIM 价格 (USD)" required>
                    <el-input-number v-model="flatEsimPrice" :min="0" :precision="2" :controls="false" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col v-if="cardTypes.includes('PHYSICAL')" :xs="24" :sm="12">
                  <el-form-item label="实体卡价格 (USD)" required>
                    <el-input-number v-model="flatPhysicalPrice" :min="0" :precision="2" :controls="false" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </template>

            <template v-else>
              <div class="card-title-row" style="margin-top: 4px">
                <div class="field-hint" style="margin: 0">可添加多档月份；eSIM / 实体卡分别定价。</div>
                <el-button type="primary" plain size="small" @click="addSku">添加档位</el-button>
              </div>
              <div v-if="!form.skus.length" class="empty-skus">
                暂无定价档，点击右上角添加
              </div>
              <div v-else class="sku-list">
                <div v-for="(row, index) in form.skus" :key="index" class="sku-row">
                  <div class="sku-row__main sku-row__main--monthly">
                    <el-input v-model="row.labelZh" placeholder="标签，如 1个月" />
                    <el-input-number v-model="row.months" :min="1" :controls="false" placeholder="月" />
                    <el-input-number v-model="row.durationDays" :min="0" :controls="false" placeholder="天数" />
                    <el-input-number
                      v-if="cardTypes.includes('ESIM')"
                      v-model="row.salePrice"
                      :min="0"
                      :precision="2"
                      :controls="false"
                      placeholder="eSIM 价"
                    />
                    <el-input-number
                      v-if="cardTypes.includes('PHYSICAL')"
                      v-model="row.physicalSalePrice"
                      :min="0"
                      :precision="2"
                      :controls="false"
                      placeholder="实体卡价"
                    />
                  </div>
                  <div class="sku-row__meta">
                    <el-checkbox v-model="row.isDefault" @change="onDefaultChange(index)">默认档</el-checkbox>
                    <el-button link type="danger" @click="form.skus.splice(index, 1)">删除</el-button>
                  </div>
                </div>
              </div>
            </template>
          </section>
        </div>

        <aside class="form-side">
          <!-- 产品类型优先置顶 -->
          <section class="form-card side-card">
            <h3 class="card-title">产品类型</h3>
            <el-form-item label="类型" required>
              <el-select v-model="form.productKind" style="width: 100%" @change="onProductKindChange">
                <el-option label="ESIM 流量套餐" value="ESIM" />
                <el-option label="PHONE 手机号" value="PHONE" />
                <el-option label="DEVICE 配套设备" value="DEVICE" />
              </el-select>
              <div class="field-hint">先选择类型；DEVICE 仅需名称/简介/价格等基础字段</div>
            </el-form-item>
            <template v-if="!isDevice">
              <el-form-item label="大分类">
                <div class="select-with-action">
                  <el-select v-model="rootId" clearable placeholder="选择大类" style="flex: 1" @change="onRootChange">
                    <el-option v-for="c in roots" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
                  </el-select>
                  <el-button @click="openCatDialog('root')">+</el-button>
                </div>
              </el-form-item>
              <el-form-item label="二级分类">
                <div class="select-with-action">
                  <el-select v-model="form.categoryId" clearable placeholder="可选，选择二级分类" style="flex: 1">
                    <el-option v-for="c in children" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
                  </el-select>
                  <el-button @click="openCatDialog('child')">+</el-button>
                </div>
              </el-form-item>
              <el-form-item label="SPU 编码">
                <el-input v-model="form.spuCode" placeholder="留空自动生成" :disabled="isEdit" />
              </el-form-item>
            </template>
          </section>

          <section class="form-card side-card">
            <h3 class="card-title">发布</h3>
            <el-form-item label="上架状态">
              <el-radio-group v-model="form.status" class="status-group">
                <el-radio-button label="ON_SALE">上架</el-radio-button>
                <el-radio-button label="DRAFT">草稿</el-radio-button>
                <el-radio-button label="OFF_SALE">下架</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="!isDevice" label="角标">
              <el-radio-group v-model="badgeMark" class="status-group">
                <el-radio-button label="none">无</el-radio-button>
                <el-radio-button label="hot">Hot</el-radio-button>
                <el-radio-button label="featured">Featured</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="!isDevice" label="卡片类型">
              <el-checkbox-group v-model="cardTypes" class="card-type-group">
                <el-checkbox label="ESIM">eSIM</el-checkbox>
                <el-checkbox label="PHYSICAL">实体卡</el-checkbox>
              </el-checkbox-group>
              <div class="field-hint">可多选；前台仅展示勾选的交付方式。含实体卡时须填收货地址；无配套设备时不展示设备选项</div>
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" :controls="false" style="width: 100%" />
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
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
/** 卡片类型多选：ESIM / PHYSICAL，各自控制前台交付方式 */
const cardTypes = ref<string[]>(['ESIM', 'PHYSICAL'])
const notesZhText = ref('')
const notesEnText = ref('')
const featuresZhText = ref('')
const featuresEnText = ref('')
const regionOptions = ['Asia', 'Europe', 'America', 'Oceania', 'Africa', 'Global']

const form = reactive<any>({
  spuCode: '',
  productKind: 'ESIM',
  priceMode: 'MONTHLY',
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

const isDevice = computed(() => String(form.productKind || '').toUpperCase() === 'DEVICE')

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
    months: form.priceMode === 'FLAT' ? 0 : 1,
    durationDays: form.priceMode === 'FLAT' ? 0 : 30,
    salePrice: 0,
    physicalSalePrice: 0,
    isDefault: form.skus.length === 0,
    status: 1,
    sortOrder: form.skus.length
  })
}

function ensureFlatSku() {
  if (!form.skus.length) {
    form.skus.push({
      labelZh: '一口价',
      labelEn: 'Flat',
      months: 0,
      durationDays: form.baseValidityDays || 0,
      salePrice: 0,
      physicalSalePrice: 0,
      isDefault: true,
      status: 1,
      sortOrder: 0
    })
  } else if (form.skus.length > 1) {
    form.skus.splice(1)
  }
  const row = form.skus[0]
  row.months = 0
  row.isDefault = true
  row.labelZh = row.labelZh || '一口价'
  if (row.physicalSalePrice == null) row.physicalSalePrice = row.salePrice ?? 0
}

const flatEsimPrice = computed({
  get: () => Number(form.skus?.[0]?.salePrice ?? 0),
  set: (v: number) => {
    ensureFlatSku()
    form.skus[0].salePrice = v
  }
})

const flatPhysicalPrice = computed({
  get: () => Number(form.skus?.[0]?.physicalSalePrice ?? form.skus?.[0]?.salePrice ?? 0),
  set: (v: number) => {
    ensureFlatSku()
    form.skus[0].physicalSalePrice = v
  }
})

function onPriceModeChange() {
  if (form.priceMode === 'FLAT') {
    ensureFlatSku()
  } else if (!form.skus.length) {
    addSku()
  } else {
    form.skus.forEach((s: any) => {
      if (!s.months || s.months <= 0) s.months = 1
      if (s.physicalSalePrice == null) s.physicalSalePrice = s.salePrice ?? 0
    })
  }
}

function ensureDeviceSku() {
  if (!form.skus.length) {
    form.skus.push({
      labelZh: '默认',
      labelEn: 'Default',
      months: 0,
      durationDays: 0,
      salePrice: 0,
      isDefault: true,
      status: 1,
      sortOrder: 0
    })
  } else {
    form.skus[0].isDefault = true
    form.skus[0].labelZh = form.skus[0].labelZh || '默认'
  }
}

const devicePrice = computed({
  get: () => Number(form.skus?.[0]?.salePrice ?? 0),
  set: (v: number) => {
    ensureDeviceSku()
    form.skus[0].salePrice = v
  }
})

function onProductKindChange() {
  if (isDevice.value) {
    ensureDeviceSku()
    // DEVICE 仅保留一档价格
    if (form.skus.length > 1) {
      form.skus.splice(1)
    }
    badgeMark.value = 'none'
    cardTypes.value = ['ESIM', 'PHYSICAL']
  } else if (!form.skus.length) {
    addSku()
  }
}

function onDefaultChange(index: number) {
  if (!form.skus[index]?.isDefault) return
  form.skus.forEach((s: any, i: number) => {
    if (i !== index) s.isDefault = false
  })
}

watch(isDevice, (v) => {
  if (v) onProductKindChange()
})

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
  if (!isDevice.value && !cardTypes.value.length) {
    ElMessage.warning('请至少选择一种卡片类型（eSIM / 实体卡）')
    return
  }
  if (isDevice.value) {
    ensureDeviceSku()
  } else if (form.priceMode === 'FLAT') {
    ensureFlatSku()
  } else if (!form.skus.length) {
    ElMessage.warning('请至少添加一个月份定价档')
    return
  }
  saving.value = true
  try {
    const statusMap: Record<string, number> = { ON_SALE: 1, DRAFT: 0, OFF_SALE: 0 }
    const summaryZh = form.summaryZh ?? ''
    const summaryEn = form.summaryEn ?? ''
    const priceMode = isDevice.value ? 'FLAT' : (form.priceMode === 'FLAT' ? 'FLAT' : 'MONTHLY')
    const skuSource = isDevice.value
      ? form.skus.slice(0, 1)
      : priceMode === 'FLAT'
        ? form.skus.slice(0, 1)
        : form.skus || []
    const payload = {
      name: (form.nameZh || '').trim(),
      nameZh: (form.nameZh || '').trim(),
      nameEn: (form.nameEn || '').trim() || undefined,
      summaryZh,
      summaryEn,
      // DEVICE 简介同步到 description，供前台配套设备列表/悬停详情使用
      description: isDevice.value ? summaryZh : undefined,
      descriptionEn: isDevice.value ? summaryEn : undefined,
      notesZh: isDevice.value ? '' : (notesZhText.value ?? ''),
      notesEn: isDevice.value ? '' : (notesEnText.value ?? ''),
      features: isDevice.value ? '[]' : textToFeaturesJson(featuresZhText.value),
      featuresEn: isDevice.value ? '[]' : textToFeaturesJson(featuresEnText.value),
      spuCode: (form.spuCode || '').trim() || undefined,
      productKind: form.productKind,
      priceMode,
      categoryId: isDevice.value ? undefined : form.categoryId,
      subCategoryId: isDevice.value ? undefined : form.categoryId,
      status: statusMap[form.status] ?? 0,
      sortOrder: form.sortOrder ?? 0,
      dataLimit: isDevice.value ? undefined : (form.dataLimit || undefined),
      validityDays: isDevice.value ? 0 : (form.baseValidityDays ?? 0),
      baseValidityDays: isDevice.value ? 0 : (form.baseValidityDays ?? 0),
      displayImageUrl: isDevice.value ? undefined : (form.displayImageUrl || undefined),
      imageUrl: isDevice.value ? undefined : (form.displayImageUrl || undefined),
      applicableRegions: isDevice.value ? [] : (form.coverageRegions || []),
      isHot: !isDevice.value && badgeMark.value === 'hot',
      isFeatured: !isDevice.value && badgeMark.value === 'featured',
      supportEsim: isDevice.value ? false : cardTypes.value.includes('ESIM'),
      isPhysical: isDevice.value ? false : cardTypes.value.includes('PHYSICAL'),
      isPhysicalCard: isDevice.value
        ? 0
        : (!cardTypes.value.includes('ESIM') && cardTypes.value.includes('PHYSICAL') ? 1 : 0),
      skus: skuSource.map((s: any, i: number) => {
        const esim = Number(s.salePrice ?? 0)
        const physical = Number(s.physicalSalePrice ?? esim)
        return {
          id: s.id,
          skuCode: s.skuCode,
          label: s.labelZh || s.label || (isDevice.value ? '默认' : priceMode === 'FLAT' ? '一口价' : ''),
          labelZh: s.labelZh || s.label || (isDevice.value ? '默认' : priceMode === 'FLAT' ? '一口价' : ''),
          labelEn: s.labelEn || (isDevice.value ? 'Default' : priceMode === 'FLAT' ? 'Flat' : undefined),
          months: isDevice.value || priceMode === 'FLAT' ? 0 : s.months,
          durationDays: isDevice.value ? 0 : (s.durationDays ?? form.baseValidityDays ?? 0),
          salePrice: cardTypes.value.includes('ESIM') || isDevice.value ? esim : physical,
          physicalSalePrice: cardTypes.value.includes('PHYSICAL') || isDevice.value ? physical : esim,
          isDefault: isDevice.value || priceMode === 'FLAT' ? true : !!s.isDefault,
          status: s.status ?? 1,
          sortOrder: i
        }
      })
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
    form.skus = (form.skus || []).map((s: any) => {
      const rawLabel = String(s.labelZh || s.label || '').trim()
      // 历史误把 skuCode 当标签，加载时清空，避免再次写回前台
      const labelZh = /^SKU/i.test(rawLabel) ? '' : rawLabel
      return {
        ...s,
        labelZh,
        physicalSalePrice: s.physicalSalePrice != null ? s.physicalSalePrice : s.salePrice
      }
    })
    form.summaryZh = data.summaryZh ?? ''
    form.summaryEn = data.summaryEn ?? ''
    notesZhText.value = data.notesZh ?? ''
    notesEnText.value = data.notesEn ?? ''
    featuresZhText.value = featuresToText(data.features)
    featuresEnText.value = featuresToText(data.featuresEn)
    if (data.isHot) badgeMark.value = 'hot'
    else if (data.isFeatured) badgeMark.value = 'featured'
    else badgeMark.value = 'none'
    {
      const types: string[] = []
      if (data.supportEsim != null || data.supportPhysical != null) {
        if (data.supportEsim !== false && data.supportEsim !== 0) types.push('ESIM')
        if (data.supportPhysical !== false && data.supportPhysical !== 0) types.push('PHYSICAL')
      } else if (data.isPhysicalCard === 1 || data.isPhysical === true) {
        types.push('PHYSICAL')
      } else {
        types.push('ESIM', 'PHYSICAL')
      }
      cardTypes.value = types.length ? types : ['ESIM', 'PHYSICAL']
    }
    {
      const mode = String(data.priceMode || '').toUpperCase()
      if (mode === 'FLAT' || mode === 'MONTHLY') {
        form.priceMode = mode
      } else {
        const hasMonthly = (form.skus || []).some((s: any) => Number(s.months) > 0)
        form.priceMode = hasMonthly ? 'MONTHLY' : 'FLAT'
      }
      if (form.priceMode === 'FLAT') ensureFlatSku()
    }
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
    if (isDevice.value) ensureDeviceSku()
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

.card-type-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
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
.sku-row__main--monthly {
  grid-template-columns: 1.1fr 0.55fr 0.55fr 0.85fr 0.85fr;
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

  .sku-row__main {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
