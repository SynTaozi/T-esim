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
            <el-form-item label="SPU 编码" required>
              <el-input v-model="form.spuCode" />
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
              <el-select v-model="rootId" style="width: 100%" @change="loadChildren">
                <el-option v-for="c in roots" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="二级分类" required>
              <el-select v-model="form.categoryId" style="width: 100%">
                <el-option v-for="c in children" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
              </el-select>
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

function addSku() {
  form.skus.push({ labelZh: '', months: 1, durationDays: 30, salePrice: 0, isDefault: form.skus.length === 0, status: 1, sortOrder: form.skus.length })
}

async function loadChildren() {
  form.categoryId = undefined
  if (!rootId.value) {
    children.value = []
    return
  }
  const res = await categoryApi.listChildren(rootId.value)
  children.value = res.data || []
}

async function save() {
  saving.value = true
  try {
    if (isEdit.value) {
      await productApi.update(Number(route.params.id), form)
    } else {
      await productApi.create(form)
    }
    ElMessage.success('已保存')
    router.push('/products')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const rootRes = await categoryApi.listRoots()
  roots.value = rootRes.data || []
  if (!isEdit.value) {
    addSku()
    return
  }
  loading.value = true
  try {
    const res = await productApi.get(Number(route.params.id))
    Object.assign(form, res.data || {})
    if (!form.skus) form.skus = []
    rootId.value = res.data?.parentCategoryId || undefined
    if (rootId.value) {
      const childRes = await categoryApi.listChildren(rootId.value)
      children.value = childRes.data || []
    }
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
</style>
