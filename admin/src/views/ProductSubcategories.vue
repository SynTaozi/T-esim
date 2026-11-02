<template>
  <div>
    <PageHeader title="产品二级分类" subtitle="挂在大分类下的子类">
      <template #actions>
        <el-button type="primary" @click="openEdit()">新建</el-button>
      </template>
    </PageHeader>
    <div class="page-card">
      <div class="filter-bar">
        <el-select v-model="parentId" clearable placeholder="父分类" style="width: 200px" @change="load">
          <el-option v-for="c in roots" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
        </el-select>
      </div>
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="parentId" label="父ID" width="80" />
        <el-table-column prop="code" label="编码" width="140" />
        <el-table-column prop="nameZh" label="中文名" />
        <el-table-column prop="nameEn" label="英文名" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑二级分类' : '新建二级分类'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="父分类">
          <el-select v-model="form.parentId" style="width: 100%">
            <el-option v-for="c in roots" :key="c.id" :label="c.nameZh || c.code" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="中文名"><el-input v-model="form.nameZh" /></el-form-item>
        <el-form-item label="英文名"><el-input v-model="form.nameEn" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { categoryApi } from '@/api/category'

const loading = ref(false)
const rows = ref<any[]>([])
const roots = ref<any[]>([])
const parentId = ref<number>()
const visible = ref(false)
const form = reactive<any>({})

async function load() {
  loading.value = true
  try {
    const res = await categoryApi.listChildren(parentId.value)
    rows.value = res.data || []
  } finally {
    loading.value = false
  }
}
function openEdit(row?: any) {
  Object.assign(form, row || { parentId: parentId.value, code: '', nameZh: '', nameEn: '', sortOrder: 0, status: 1, showOnFront: true })
  visible.value = true
}
async function save() {
  if (form.id) await categoryApi.updateChild(form.id, form)
  else await categoryApi.createChild(form)
  ElMessage.success('已保存')
  visible.value = false
  load()
}
async function remove(id: number) {
  await ElMessageBox.confirm('确认删除？', '提示')
  await categoryApi.removeChild(id)
  ElMessage.success('已删除')
  load()
}
onMounted(async () => {
  const res = await categoryApi.listRoots()
  roots.value = res.data || []
  load()
})
</script>
