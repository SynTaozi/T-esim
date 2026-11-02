<template>
  <div>
    <PageHeader title="工单管理" subtitle="客服回复与关闭" />
    <div class="page-card">
      <div class="filter-bar">
        <el-select v-model="query.status" clearable placeholder="状态" style="width:140px" @change="search">
          <el-option label="OPEN" value="OPEN" /><el-option label="PENDING" value="PENDING" /><el-option label="CLOSED" value="CLOSED" />
        </el-select>
      </div>
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="ticketNo" label="工单号" min-width="140" />
        <el-table-column prop="subject" label="主题" min-width="180" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column prop="status" label="状态" width="100"><template #default="{row}"><StatusTag :status="row.status"/></template></el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{row}">
            <el-button link type="primary" @click="open(row)">处理</el-button>
            <el-button link @click="close(row.id)">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><el-pagination background layout="total, prev, pager, next" :total="total" :page-size="query.size" :current-page="query.page+1" @current-change="(p:number)=>{query.page=p-1;load()}" /></div>
    </div>
    <el-drawer v-model="visible" title="工单详情" size="480px">
      <template v-if="detail">
        <h3>{{ detail.subject }}</h3>
        <div v-for="m in detail.messages||[]" :key="m.id" class="msg">
          <div class="meta">{{ m.senderType }} · {{ m.createdAt }}</div>
          <div>{{ m.content }}</div>
        </div>
        <el-input v-model="reply" type="textarea" :rows="4" placeholder="回复内容" />
        <el-button type="primary" style="margin-top:8px" @click="send">发送回复</el-button>
      </template>
    </el-drawer>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { ticketApi } from '@/api/ticket'
const loading=ref(false); const rows=ref<any[]>([]); const total=ref(0); const visible=ref(false); const detail=ref<any>(null); const reply=ref('')
const query=reactive({page:0,size:20,status:''})
async function load(){loading.value=true;try{const res=await ticketApi.page({...query});rows.value=res.data?.content||[];total.value=res.data?.totalElements||0}finally{loading.value=false}}
function search(){query.page=0;load()}
async function open(row:any){const res=await ticketApi.get(row.id);detail.value=res.data;reply.value='';visible.value=true}
async function send(){if(!detail.value)return;await ticketApi.reply(detail.value.id,reply.value);ElMessage.success('已回复');open(detail.value);load()}
async function close(id:number){await ticketApi.close(id);ElMessage.success('已关闭');load()}
onMounted(load)
</script>
<style scoped>.msg{padding:10px;border:1px solid var(--admin-border);border-radius:8px;margin-bottom:8px}.meta{color:var(--admin-muted);font-size:12px;margin-bottom:4px}</style>
