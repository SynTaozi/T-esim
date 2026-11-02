<template>
  <div>
    <PageHeader title="优惠券" subtitle="券码与使用记录">
      <template #actions><el-button type="primary" @click="edit()">新建</el-button></template>
    </PageHeader>
    <el-tabs v-model="tab">
      <el-tab-pane label="优惠券" name="list">
        <div class="page-card">
          <el-table :data="rows" v-loading="loading" stripe>
            <el-table-column prop="code" label="券码" width="140" />
            <el-table-column prop="discountAmount" label="减免" width="90" />
            <el-table-column prop="minOrderAmount" label="门槛" width="90" />
            <el-table-column prop="status" label="状态" width="100"><template #default="{row}"><StatusTag :status="row.status"/></template></el-table-column>
            <el-table-column prop="expiresAt" label="过期时间" min-width="160" />
            <el-table-column label="操作" width="160"><template #default="{row}"><el-button link type="primary" @click="edit(row)">编辑</el-button><el-button link type="danger" @click="disable(row.id)">停用</el-button></template></el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="使用记录" name="usage">
        <div class="page-card">
          <el-table :data="usages" stripe>
            <el-table-column prop="couponCode" label="券码" />
            <el-table-column prop="memberId" label="用户" width="90" />
            <el-table-column prop="orderNo" label="订单" />
            <el-table-column prop="discountAmount" label="减免" width="90" />
            <el-table-column prop="usedAt" label="使用时间" min-width="160" />
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="visible" title="优惠券" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="券码"><el-input v-model="form.code"/></el-form-item>
        <el-form-item label="减免金额"><el-input-number v-model="form.discountAmount" :min="0" :precision="2"/></el-form-item>
        <el-form-item label="最低消费"><el-input-number v-model="form.minOrderAmount" :min="0" :precision="2"/></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { couponApi } from '@/api/cms'
const tab=ref('list'); const loading=ref(false); const rows=ref<any[]>([]); const usages=ref<any[]>([]); const visible=ref(false); const form=reactive<any>({})
async function load(){loading.value=true;try{const res=await couponApi.page({page:0,size:100});rows.value=res.data?.content||[]}finally{loading.value=false}}
async function loadUsages(){const res=await couponApi.usages({page:0,size:100});usages.value=res.data?.content||[]}
function edit(row?:any){Object.assign(form,row||{code:'',discountAmount:5,minOrderAmount:10,remark:'',status:'ACTIVE'});visible.value=true}
async function save(){await couponApi.save(form);ElMessage.success('已保存');visible.value=false;load()}
async function disable(id:number){await couponApi.disable(id);ElMessage.success('已停用');load()}
watch(tab,(v)=>{if(v==='usage')loadUsages()})
onMounted(load)
</script>
