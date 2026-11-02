<template>
  <div>
    <PageHeader title="批量映射" subtitle="预览并批量应用供应商套餐映射">
      <template #actions><el-button @click="$router.push('/card-platform')">返回</el-button></template>
    </PageHeader>
    <div class="page-card">
      <div class="filter-bar">
        <el-select v-model="configId" placeholder="选择通道" style="width:240px">
          <el-option v-for="c in configs" :key="c.id" :label="`${c.name} (${c.environment})`" :value="c.id" />
        </el-select>
        <el-button type="primary" :disabled="!configId" @click="preview">预览</el-button>
        <el-button type="success" :disabled="!previewRows.length" @click="apply">一键应用</el-button>
      </div>
      <el-table :data="previewRows" v-loading="loading" stripe>
        <el-table-column prop="externalPlanId" label="外部套餐" />
        <el-table-column prop="externalName" label="名称" />
        <el-table-column prop="productId" label="本地产品" width="100" />
        <el-table-column prop="action" label="动作" width="100" />
      </el-table>
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { supplierApi } from '@/api/supplier'
const configs=ref<any[]>([]); const configId=ref<number>(); const previewRows=ref<any[]>([]); const loading=ref(false)
async function preview(){if(!configId.value)return;loading.value=true;try{const res=await supplierApi.bulkPreview(configId.value);previewRows.value=res.data?.items||res.data||[]}finally{loading.value=false}}
async function apply(){await supplierApi.bulkApply({configId:configId.value,items:previewRows.value});ElMessage.success('已应用');preview()}
onMounted(async()=>{configs.value=(await supplierApi.listConfigs()).data||[]; if(configs.value[0]) configId.value=configs.value[0].id})
</script>
