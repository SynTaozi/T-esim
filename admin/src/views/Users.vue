<template>
  <div>
    <PageHeader title="用户管理" subtitle="会员账号状态管理" />
    <div class="page-card">
      <div class="filter-bar">
        <el-input v-model="query.keyword" clearable placeholder="用户名/邮箱" style="width:220px" @keyup.enter="search" />
        <el-select v-model="query.status" clearable placeholder="状态" style="width:140px">
          <el-option label="ACTIVE" value="ACTIVE" /><el-option label="INACTIVE" value="INACTIVE" /><el-option label="BANNED" value="BANNED" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="role" label="角色" width="90" />
        <el-table-column prop="status" label="状态" width="110"><template #default="{row}"><StatusTag :status="row.status"/></template></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button link type="primary" @click="setStatus(row,'ACTIVE')">启用</el-button>
            <el-button link type="danger" @click="setStatus(row,'INACTIVE')">停用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><el-pagination background layout="total, prev, pager, next" :total="total" :page-size="query.size" :current-page="query.page+1" @current-change="(p:number)=>{query.page=p-1;load()}" /></div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { memberApi } from '@/api/member'
const loading=ref(false); const rows=ref<any[]>([]); const total=ref(0)
const query=reactive({page:0,size:20,keyword:'',status:''})
async function load(){loading.value=true;try{const res=await memberApi.page({...query});rows.value=res.data?.content||[];total.value=res.data?.totalElements||0}finally{loading.value=false}}
function search(){query.page=0;load()}
async function setStatus(row:any,status:string){await memberApi.updateStatus(row.id,status);ElMessage.success('已更新');load()}
onMounted(load)
</script>
