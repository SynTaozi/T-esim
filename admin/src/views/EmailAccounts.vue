<template>
  <div>
    <PageHeader title="邮件账户" subtitle="SMTP / IMAP 账户">
      <template #actions><el-button type="primary" @click="edit()">新建</el-button></template>
    </PageHeader>
    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="emailAddress" label="邮箱" min-width="180" />
        <el-table-column prop="smtpHost" label="SMTP" min-width="140" />
        <el-table-column label="启用" width="80"><template #default="{row}"><StatusTag :status="row.enabled ? 'ACTIVE' : 'DISABLED'"/></template></el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{row}">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link @click="test(row.id)">测试</el-button>
            <el-button link @click="$router.push({path:'/emails',query:{accountId:row.id}})">收发件</el-button>
            <el-button link type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="visible" title="邮件账户" width="640px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.emailAddress"/></el-form-item>
        <el-form-item label="SMTP主机"><el-input v-model="form.smtpHost"/></el-form-item>
        <el-form-item label="SMTP端口"><el-input-number v-model="form.smtpPort" :min="1"/></el-form-item>
        <el-form-item label="IMAP主机"><el-input v-model="form.imapHost"/></el-form-item>
        <el-form-item label="IMAP端口"><el-input-number v-model="form.imapPort" :min="1"/></el-form-item>
        <el-form-item label="用户名"><el-input v-model="form.username"/></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password/></el-form-item>
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
import { emailAccountApi } from '@/api/email'
const loading=ref(false); const rows=ref<any[]>([]); const visible=ref(false); const form=reactive<any>({})
async function load(){loading.value=true;try{rows.value=(await emailAccountApi.list()).data||[]}finally{loading.value=false}}
function edit(row?:any){Object.assign(form,row||{name:'',emailAddress:'',smtpHost:'',smtpPort:465,imapHost:'',imapPort:993,username:'',password:'',enabled:true});visible.value=true}
async function save(){await emailAccountApi.save(form);ElMessage.success('已保存');visible.value=false;load()}
async function test(id:number){await emailAccountApi.test(id);ElMessage.success('测试通过')}
async function remove(id:number){await ElMessageBox.confirm('确认删除？');await emailAccountApi.remove(id);load()}
onMounted(load)
</script>
