<template>
  <div>
    <PageHeader title="套餐同步" subtitle="对比上游套餐与本地商品并应用">
      <template #actions><el-button @click="$router.push('/products')">返回产品</el-button></template>
    </PageHeader>
    <div class="page-card">
      <div class="filter-bar">
        <el-select v-model="configId" placeholder="选择通道" style="width:240px">
          <el-option v-for="c in configs" :key="c.id" :label="`${c.name} (${c.environment})`" :value="c.id" />
        </el-select>
        <el-button type="primary" :disabled="!configId" @click="loadPreview">同步预览</el-button>
        <el-button type="success" :disabled="!selected.length" @click="apply">应用选中</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" stripe @selection-change="(v:any[])=>selected=v">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="externalPlanId" label="外部ID" />
        <el-table-column prop="externalName" label="外部名称" />
        <el-table-column prop="localProductId" label="本地产品" width="100" />
        <el-table-column prop="diff" label="差异" min-width="160" />
      </el-table>
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { supplierApi } from '@/api/supplier'
const configs=ref<any[]>([]); const configId=ref<number>(); const rows=ref<any[]>([]); const selected=ref<any[]>([]); const loading=ref(false)
async function loadPreview(){if(!configId.value)return;loading.value=true;try{const res=await supplierApi.syncPreview(configId.value);rows.value=res.data?.items||res.data||[]}finally{loading.value=false}}
async function apply(){await supplierApi.syncApply({configId:configId.value,items:selected.value});ElMessage.success('已应用');loadPreview()}
onMounted(async()=>{configs.value=(await supplierApi.listConfigs()).data||[]; if(configs.value[0]) configId.value=configs.value[0].id})
</script>
