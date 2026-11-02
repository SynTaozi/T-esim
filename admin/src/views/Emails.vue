<template>
  <div>
    <PageHeader title="收发件" :subtitle="`账户 #${accountId||'-'}`">
      <template #actions>
        <el-button @click="$router.push('/email-accounts')">返回账户</el-button>
        <el-button type="primary" @click="composeVisible=true">写邮件</el-button>
      </template>
    </PageHeader>
    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="subject" label="主题" min-width="200" />
        <el-table-column prop="from" label="发件人" min-width="160" />
        <el-table-column prop="receivedAt" label="时间" width="170" />
        <el-table-column prop="seen" label="已读" width="80"><template #default="{row}">{{ row.seen ? '是' : '否' }}</template></el-table-column>
      </el-table>
      <div class="pager"><el-pagination background layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page+1" @current-change="(p:number)=>{page=p-1;load()}"/></div>
    </div>
    <el-dialog v-model="composeVisible" title="发送邮件" width="640px">
      <el-form :model="compose" label-width="70px">
        <el-form-item label="收件人"><el-input v-model="compose.to"/></el-form-item>
        <el-form-item label="主题"><el-input v-model="compose.subject"/></el-form-item>
        <el-form-item label="正文"><el-input v-model="compose.content" type="textarea" :rows="8"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="composeVisible=false">取消</el-button><el-button type="primary" @click="send">发送</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { emailAccountApi } from '@/api/email'
const route=useRoute(); const accountId=computed(()=>Number(route.query.accountId||0))
const loading=ref(false); const rows=ref<any[]>([]); const total=ref(0); const page=ref(0); const size=20
const composeVisible=ref(false); const compose=reactive({to:'',subject:'',content:''})
async function load(){if(!accountId.value)return;loading.value=true;try{const res=await emailAccountApi.listEmails(accountId.value,{page:page.value,size});rows.value=res.data?.content||[];total.value=res.data?.totalElements||0}finally{loading.value=false}}
async function send(){await emailAccountApi.send(accountId.value,compose);ElMessage.success('已发送');composeVisible.value=false}
onMounted(load)
</script>
