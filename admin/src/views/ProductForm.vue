<template>
  <div>
    <PageHeader :title="isEdit ? '编辑产品' : '新建产品'" subtitle="维护商品信息、覆盖区域与定价档">
      <template #actions>
        <el-button @click="$router.back()">返回</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </PageHeader>

    <div class="page-card" v-loading="loading">
      <el-form :model="form" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="SPU 编码">
              <el-input v-model="form.spuCode" placeholder="留空自动生成" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型">
              <el-select v-model="form.productKind" style="width: 100%">
                <el-option label="ESIM" value="ESIM" />
                <el-option label="PHONE" value="PHONE" />
                <el-option label="DEVICE" value="DEVICE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中文名" required>
              <el-input v-model="form.nameZh" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文名">
              <el-input v-model="form.nameEn" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="大分类">
              <div class="select-with-action">
                <el-select v-model="rootId" style="flex: 1" clearable @change="onRootChange">
                  <el-option v-for="c in roots" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
                </el-select>
                <el-button @click="openCatDialog('root')">+</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="二级分类" required>
              <div class="select-with-action">
                <el-select v-model="form.categoryId" style="flex: 1" clearable>
                  <el-option v-for="c in children" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
                </el-select>
                <el-button @click="openCatDialog('child')">+</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="上架" value="ON_SALE" />
                <el-option label="草稿" value="DRAFT" />
                <el-option label="下架" value="OFF_SALE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="流量">
              <el-input v-model="form.dataLimit" placeholder="如 1GB / Unlimited" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效天数">
              <el-input-number v-model="form.baseValidityDays" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="中文简介">
              <el-input v-model="form.summaryZh" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="展示图 URL">
              <el-input v-model="form.displayImageUrl" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="覆盖区域">
              <el-select v-model="form.coverageRegions" multiple filterable allow-create style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标记">
              <el-checkbox v-model="form.isFeatured">精选</el-checkbox>
              <el-checkbox v-model="form.isHot">热门</el-checkbox>
              <el-checkbox v-model="form.isPhysical">实物</el-checkbox>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="sku-head">
        <h3>定价档</h3>
        <el-button size="small" @click="addSku">添加档位</el-button>
      </div>
      <el-table :data="form.skus" size="small">
        <el-table-column label="中文标签">
          <template #default="{ row }"><el-input v-model="row.labelZh" /></template>
        </el-table-column>
        <el-table-column label="月" width="90">
          <template #default="{ row }"><el-input-number v-model="row.months" :min="0" controls-position="right" /></template>
        </el-table-column>
        <el-table-column label="天数" width="100">
          <template #default="{ row }"><el-input-number v-model="row.durationDays" :min="0" controls-position="right" /></template>
        </el-table-column>
        <el-table-column label="售价" width="120">
          <template #default="{ row }"><el-input-number v-model="row.salePrice" :min="0" :precision="2" controls-position="right" /></template>
        </el-table-column>
        <el-table-column label="默认" width="70">
          <template #default="{ row }"><el-switch v-model="row.isDefault" /></template>
        </el-table-column>
        <el-table-column label="" width="70">
          <template #default="{ $index }">
            <el-button link type="danger" @click="form.skus.splice($index, 1)">删</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="catVisible"
      :title="catMode === 'root' ? '快速新建大分类' : '快速新建二级分类'"
      width="480px"
      destroy-on-close
    >
      <el-form :model="catForm" label-width="90px">
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
const form = reactive<any>({
  spuCode: '',
  productKind: 'ESIM',
  nameZh: '',
  nameEn: '',
  summaryZh: '',
  categoryId: undefined,
  status: 'DRAFT',
  sortOrder: 0,
  dataLimit: '',
  baseValidityDays: 30,
  displayImageUrl: '',
  coverageRegions: [],
  isFeatured: false,
  isHot: false,
  isPhysical: false,
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
    showOnFront: true
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
      ...form,
      name: (form.nameZh || '').trim(),
      spuCode: (form.spuCode || '').trim() || undefined,
      categoryId: form.categoryId,
      subCategoryId: form.categoryId,
      status: statusMap[form.status] ?? 0
    }
    if (isEdit.value) {
      await productApi.update(Number(route.params.id), payload)
    } else {
      await productApi.create(payload)
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
    Object.assign(form, res.data || {})
    if (!form.skus) form.skus = []
    // 后端 VO 的 categoryId 即二级分类；需反查所属大类
    const subId = res.data?.subCategoryId || res.data?.categoryId
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
    // VO 用 1/0，表单用字符串
    if (res.data?.status === 1) form.status = 'ON_SALE'
    else if (res.data?.status === 0) form.status = 'OFF_SALE'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.sku-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 12px 0;
}
.sku-head h3 {
  margin: 0;
  font-size: 15px;
}
.select-with-action {
  display: flex;
  gap: 8px;
  width: 100%;
  align-items: center;
}
</style>
