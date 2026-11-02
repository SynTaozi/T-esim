<template>
  <div>
    <PageHeader title="产品大分类" subtitle="一级分类管理">
      <template #actions>
        <el-button type="primary" @click="openEdit()">新建</el-button>
      </template>
    </PageHeader>
    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="code" label="编码" width="140" />
        <el-table-column prop="nameZh" label="中文名" />
        <el-table-column prop="nameEn" label="英文名" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }"><StatusTag :status="row.status === 1 ? 'ACTIVE' : 'DISABLED'" /></template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑分类' : '新建分类'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="中文名"><el-input v-model="form.nameZh" /></el-form-item>
        <el-form-item label="英文名"><el-input v-model="form.nameEn" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="前台展示"><el-switch v-model="form.showOnFront" /></el-form-item>
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
import StatusTag from '@/components/common/StatusTag.vue'
import { categoryApi } from '@/api/category'

const loading = ref(false)
const rows = ref<any[]>([])
const visible = ref(false)
const form = reactive<any>({})

async function load() {
  loading.value = true
  try {
    const res = await categoryApi.listRoots()
    rows.value = res.data || []
  } finally {
    loading.value = false
  }
}
function openEdit(row?: any) {
  Object.assign(form, row || { code: '', nameZh: '', nameEn: '', sortOrder: 0, status: 1, showOnFront: true })
  visible.value = true
}
async function save() {
  if (form.id) await categoryApi.updateRoot(form.id, form)
  else await categoryApi.createRoot(form)
  ElMessage.success('已保存')
  visible.value = false
  load()
}
async function remove(id: number) {
  await ElMessageBox.confirm('确认删除？', '提示')
  await categoryApi.removeRoot(id)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>
