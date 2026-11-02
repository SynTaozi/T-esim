<template>
  <div>
    <PageHeader title="钉钉通知" subtitle="Webhook 配置与日志">
      <template #actions><el-button type="primary" @click="edit()">新建</el-button></template>
    </PageHeader>
    <div class="page-card" style="margin-bottom:16px">
      <el-table :data="configs" v-loading="loading" stripe>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="webhookUrl" label="Webhook" min-width="220" show-overflow-tooltip />
        <el-table-column label="启用" width="90"><template #default="{row}"><StatusTag :status="row.enabled ? 'ACTIVE' : 'DISABLED'"/></template></el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{row}">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link @click="test(row.id)">测试</el-button>
            <el-button link type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="page-card">
      <h3>通知日志</h3>
      <el-table :data="logs" size="small" stripe>
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column prop="eventType" label="事件" width="140" />
        <el-table-column prop="success" label="成功" width="80"><template #default="{row}">{{ row.success ? '是' : '否' }}</template></el-table-column>
        <el-table-column prop="responseBody" label="响应" show-overflow-tooltip />
      </el-table>
    </div>
    <el-dialog v-model="visible" title="钉钉配置" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="Webhook"><el-input v-model="form.webhookUrl"/></el-form-item>
        <el-form-item label="Secret"><el-input v-model="form.secret"/></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { dingtalkApi } from '@/api/dingtalk'
const loading=ref(false); const configs=ref<any[]>([]); const logs=ref<any[]>([]); const visible=ref(false); const form=reactive<any>({})
async function load(){loading.value=true;try{configs.value=(await dingtalkApi.listConfigs()).data||[]; const res=await dingtalkApi.logs({page:0,size:50});logs.value=res.data?.content||[]}finally{loading.value=false}}
function edit(row?:any){Object.assign(form,row||{name:'',webhookUrl:'',secret:'',enabled:true});visible.value=true}
async function save(){await dingtalkApi.saveConfig(form);ElMessage.success('已保存');visible.value=false;load()}
async function test(id:number){await dingtalkApi.test(id);ElMessage.success('已发送测试')}
async function remove(id:number){await ElMessageBox.confirm('确认删除？');await dingtalkApi.removeConfig(id);load()}
onMounted(load)
</script>
<style scoped>h3{margin:0 0 12px;font-size:15px}</style>
