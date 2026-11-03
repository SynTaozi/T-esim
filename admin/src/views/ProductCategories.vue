<template>
  <div>
    <PageHeader title="产品分类" subtitle="大分类与二级分类统一管理">
      <template #actions>
        <el-button type="primary" @click="openEdit()">新建大分类</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <el-table
        :data="treeRows"
        v-loading="loading"
        stripe
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="nameZh" label="名称" min-width="180">
          <template #default="{ row }">
            <span>{{ row.nameZh || row.code }}</span>
            <el-tag v-if="!row.parentId" size="small" class="level-tag" type="info">大类</el-tag>
            <el-tag v-else size="small" class="level-tag">二级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nameEn" label="英文名" min-width="140" />
        <el-table-column prop="code" label="编码" width="160" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <StatusTag :status="row.status === 1 ? 'ACTIVE' : 'DISABLED'" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.parentId" link type="primary" @click="openEdit(undefined, row.id)">添加子类</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item v-if="form.parentId" label="所属大类">
          <el-select v-model="form.parentId" style="width: 100%" :disabled="!!form.id">
            <el-option v-for="c in roots" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="form.code" placeholder="留空自动生成" />
        </el-form-item>
        <el-form-item label="中文名" required>
          <el-input v-model="form.nameZh" placeholder="必填" />
        </el-form-item>
        <el-form-item label="英文名">
          <el-input v-model="form.nameEn" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item v-if="!form.parentId" label="前台展示">
          <el-switch v-model="form.showOnFront" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { categoryApi } from '@/api/category'

type CatRow = {
  id?: number
  parentId?: number | null
  code?: string
  nameZh?: string
  nameEn?: string
  sortOrder?: number
  status?: number
  showOnFront?: boolean
  children?: CatRow[]
}

const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const roots = ref<CatRow[]>([])
const treeRows = ref<CatRow[]>([])
const form = reactive<CatRow>({})

const dialogTitle = computed(() => {
  if (form.id) return form.parentId ? '编辑二级分类' : '编辑大分类'
  return form.parentId ? '新建二级分类' : '新建大分类'
})

function resetForm(patch: CatRow = {}) {
  Object.keys(form).forEach((k) => delete (form as any)[k])
  Object.assign(form, {
    id: undefined,
    parentId: undefined,
    code: '',
    nameZh: '',
    nameEn: '',
    sortOrder: 0,
    status: 1,
    showOnFront: true,
    ...patch
  })
}

function buildPayload() {
  const code = (form.code || '').trim()
  return {
    nameZh: (form.nameZh || '').trim(),
    name: (form.nameZh || '').trim(),
    nameEn: (form.nameEn || '').trim() || undefined,
    code: code || undefined,
    parentId: form.parentId || undefined,
    categoryId: form.parentId || undefined,
    sortOrder: form.sortOrder ?? 0,
    status: form.status ?? 1,
    // 二级分类不进前台顶栏
    showOnFront: form.parentId ? false : (form.showOnFront ?? true)
  }
}

async function load() {
  loading.value = true
  try {
    const rootRes = await categoryApi.listRoots()
    const rootList: CatRow[] = rootRes.data || []
    roots.value = rootList
    const trees: CatRow[] = []
    for (const root of rootList) {
      const childRes = await categoryApi.listChildren(root.id)
      trees.push({
        ...root,
        parentId: null,
        children: (childRes.data || []).map((c: CatRow) => ({
          ...c,
          parentId: root.id,
          children: undefined
        }))
      })
    }
    treeRows.value = trees
  } finally {
    loading.value = false
  }
}

/** row 有值=编辑；parentIdForChild 有值=在该大类下新建二级 */
function openEdit(row?: CatRow, parentIdForChild?: number) {
  if (row) {
    resetForm({
      id: row.id,
      parentId: row.parentId || undefined,
      code: row.code || '',
      nameZh: row.nameZh || '',
      nameEn: row.nameEn || '',
      sortOrder: row.sortOrder ?? 0,
      status: row.status ?? 1,
      showOnFront: row.showOnFront ?? true
    })
  } else if (parentIdForChild) {
    resetForm({ parentId: parentIdForChild })
  } else {
    resetForm()
  }
  visible.value = true
}

async function save() {
  const payload = buildPayload()
  if (!payload.nameZh) {
    ElMessage.warning('请填写中文名')
    return
  }
  if (!form.id && form.parentId && !payload.parentId) {
    ElMessage.warning('请选择所属大类')
    return
  }
  saving.value = true
  try {
    const isChild = !!form.parentId
    if (form.id) {
      if (isChild) await categoryApi.updateChild(form.id, payload)
      else await categoryApi.updateRoot(form.id, payload)
    } else if (isChild) {
      await categoryApi.createChild(payload)
    } else {
      await categoryApi.createRoot(payload)
    }
    ElMessage.success('已保存')
    visible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row: CatRow) {
  if (!row.id) return
  const tip = row.parentId
    ? '确认删除该二级分类？'
    : '确认删除该大分类？需先无子分类。'
  await ElMessageBox.confirm(tip, '提示')
  if (row.parentId) await categoryApi.removeChild(row.id)
  else await categoryApi.removeRoot(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.level-tag {
  margin-left: 8px;
  vertical-align: middle;
}
</style>
