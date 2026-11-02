<template>
  <div>
    <PageHeader title="App下载二维码" subtitle="配置 iOS / Android 下载入口">
      <template #actions><el-button type="primary" @click="edit()">新建</el-button></template>
    </PageHeader>
    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="platform" label="平台" width="100" />
        <el-table-column prop="titleZh" label="标题" />
        <el-table-column prop="downloadUrl" label="下载链接" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="160"><template #default="{row}"><el-button link type="primary" @click="edit(row)">编辑</el-button><el-button link type="danger" @click="remove(row.id)">删除</el-button></template></el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="visible" title="App 下载配置" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="平台"><el-select v-model="form.platform" style="width:100%"><el-option label="IOS" value="IOS"/><el-option label="ANDROID" value="ANDROID"/></el-select></el-form-item>
        <el-form-item label="标题中文"><el-input v-model="form.titleZh"/></el-form-item>
        <el-form-item label="标题英文"><el-input v-model="form.titleEn"/></el-form-item>
        <el-form-item label="下载链接"><el-input v-model="form.downloadUrl"/></el-form-item>
        <el-form-item label="二维码图"><el-input v-model="form.qrImageUrl"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { appDownloadApi } from '@/api/cms'
const loading=ref(false); const rows=ref<any[]>([]); const visible=ref(false); const form=reactive<any>({})
async function load(){loading.value=true;try{rows.value=(await appDownloadApi.list()).data||[]}finally{loading.value=false}}
function edit(row?:any){Object.assign(form,row||{platform:'IOS',titleZh:'',titleEn:'',downloadUrl:'',qrImageUrl:''});visible.value=true}
async function save(){await appDownloadApi.save(form);ElMessage.success('已保存');visible.value=false;load()}
async function remove(id:number){await ElMessageBox.confirm('确认删除？');await appDownloadApi.remove(id);load()}
onMounted(load)
</script>
