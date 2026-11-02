<template>
  <div>
    <PageHeader title="邮件模板" subtitle="系统通知邮件模板" />
    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="code" label="编码" width="160" />
        <el-table-column prop="name" label="名称" />
        <el-table-column label="启用" width="90"><template #default="{row}"><el-switch :model-value="row.enabled" @change="(v:boolean)=>toggle(row,v)"/></template></el-table-column>
        <el-table-column label="操作" width="160"><template #default="{row}"><el-button link type="primary" @click="edit(row)">编辑</el-button><el-button link @click="preview(row.id)">预览</el-button></template></el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="visible" title="编辑模板" width="720px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="主题"><el-input v-model="form.subject"/></el-form-item>
        <el-form-item label="正文"><el-input v-model="form.bodyHtml" type="textarea" :rows="12"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="previewVisible" title="预览" width="720px"><div v-html="previewHtml"></div></el-dialog>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { emailTemplateApi } from '@/api/email'
const loading=ref(false); const rows=ref<any[]>([]); const visible=ref(false); const previewVisible=ref(false); const previewHtml=ref(''); const form=reactive<any>({})
async function load(){loading.value=true;try{rows.value=(await emailTemplateApi.list()).data||[]}finally{loading.value=false}}
async function edit(row:any){const res=await emailTemplateApi.get(row.id);Object.assign(form,res.data||row);visible.value=true}
async function save(){await emailTemplateApi.update(form.id,form);ElMessage.success('已保存');visible.value=false;load()}
async function toggle(row:any,enabled:boolean){await emailTemplateApi.setEnabled(row.id,enabled);load()}
async function preview(id:number){const res=await emailTemplateApi.preview(id);previewHtml.value=res.data?.bodyHtml||res.data?.html||'';previewVisible.value=true}
onMounted(load)
</script>
