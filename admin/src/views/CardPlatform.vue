<template>
  <div>
    <PageHeader title="发卡平台" subtitle="供应商通道与商品映射">
      <template #actions>
        <el-button @click="$router.push('/card-platform/bulk-mapping')">批量映射</el-button>
        <el-button type="primary" @click="editConfig()">新建通道</el-button>
      </template>
    </PageHeader>
    <el-tabs v-model="tab">
      <el-tab-pane label="通道配置" name="config">
        <div class="page-card">
          <el-table :data="configs" v-loading="loading" stripe>
            <el-table-column prop="code" label="编码" width="120" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="environment" label="环境" width="90" />
            <el-table-column prop="baseUrl" label="Base URL" min-width="180" show-overflow-tooltip />
            <el-table-column label="启用" width="80"><template #default="{row}"><StatusTag :status="row.enabled ? 'ACTIVE' : 'DISABLED'"/></template></el-table-column>
            <el-table-column label="操作" width="260">
              <template #default="{row}">
                <el-button link type="primary" @click="editConfig(row)">编辑</el-button>
                <el-button link @click="test(row.id)">测试</el-button>
                <el-button link @click="switchEnv(row)">切环境</el-button>
                <el-button link type="danger" @click="removeConfig(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="商品映射" name="map">
        <div class="page-card">
          <div class="table-toolbar"><el-button type="primary" @click="editMap()">新建映射</el-button></div>
          <el-table :data="mappings" stripe>
            <el-table-column prop="channelId" label="通道" width="80" />
            <el-table-column prop="productId" label="产品ID" width="90" />
            <el-table-column prop="skuId" label="SKU" width="90" />
            <el-table-column prop="externalPlanId" label="外部套餐ID" min-width="140" />
            <el-table-column prop="externalName" label="外部名称" min-width="140" />
            <el-table-column label="自动履约" width="90"><template #default="{row}">{{ row.autoFulfill ? '是' : '否' }}</template></el-table-column>
            <el-table-column label="操作" width="160"><template #default="{row}"><el-button link type="primary" @click="editMap(row)">编辑</el-button><el-button link type="danger" @click="removeMap(row.id)">删除</el-button></template></el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="cfgVisible" title="通道配置" width="620px">
      <el-form :model="cfgForm" label-width="100px">
        <el-form-item label="编码"><el-input v-model="cfgForm.code"/></el-form-item>
        <el-form-item label="名称"><el-input v-model="cfgForm.name"/></el-form-item>
        <el-form-item label="环境"><el-select v-model="cfgForm.environment" style="width:100%"><el-option label="TEST" value="TEST"/><el-option label="PROD" value="PROD"/></el-select></el-form-item>
        <el-form-item label="Base URL"><el-input v-model="cfgForm.baseUrl"/></el-form-item>
        <el-form-item label="凭证 JSON"><el-input v-model="cfgForm.credentialJson" type="textarea" :rows="4"/></el-form-item>
        <el-form-item label="回调 URL"><el-input v-model="cfgForm.callbackUrl"/></el-form-item>
        <el-form-item label="启用"><el-switch v-model="cfgForm.enabled"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="cfgVisible=false">取消</el-button><el-button type="primary" @click="saveConfig">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="mapVisible" title="商品映射" width="560px">
      <el-form :model="mapForm" label-width="110px">
        <el-form-item label="通道ID"><el-input-number v-model="mapForm.channelId" :min="1"/></el-form-item>
        <el-form-item label="产品ID"><el-input-number v-model="mapForm.productId" :min="1"/></el-form-item>
        <el-form-item label="SKU ID"><el-input-number v-model="mapForm.skuId" :min="0"/></el-form-item>
        <el-form-item label="外部套餐ID"><el-input v-model="mapForm.externalPlanId"/></el-form-item>
        <el-form-item label="外部名称"><el-input v-model="mapForm.externalName"/></el-form-item>
        <el-form-item label="自动履约"><el-switch v-model="mapForm.autoFulfill"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="mapVisible=false">取消</el-button><el-button type="primary" @click="saveMap">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { supplierApi } from '@/api/supplier'
const tab=ref('config'); const loading=ref(false); const configs=ref<any[]>([]); const mappings=ref<any[]>([])
const cfgVisible=ref(false); const mapVisible=ref(false); const cfgForm=reactive<any>({}); const mapForm=reactive<any>({})
async function load(){loading.value=true;try{configs.value=(await supplierApi.listConfigs()).data||[];mappings.value=(await supplierApi.listMappings()).data||[]}finally{loading.value=false}}
function editConfig(row?:any){Object.assign(cfgForm,row||{code:'TALK2ALL',name:'Talk2All',environment:'TEST',baseUrl:'',credentialJson:'{}',callbackUrl:'',enabled:false});cfgVisible.value=true}
async function saveConfig(){await supplierApi.saveConfig(cfgForm);ElMessage.success('已保存');cfgVisible.value=false;load()}
async function test(id:number){await supplierApi.test(id);ElMessage.success('连接测试完成')}
async function switchEnv(row:any){const env=row.environment==='PROD'?'TEST':'PROD';await supplierApi.switchEnv(row.id,env);ElMessage.success('已切换');load()}
async function removeConfig(id:number){await ElMessageBox.confirm('确认删除？');await supplierApi.removeConfig(id);load()}
function editMap(row?:any){Object.assign(mapForm,row||{channelId:configs.value[0]?.id,productId:1,skuId:null,externalPlanId:'',externalName:'',autoFulfill:false});mapVisible.value=true}
async function saveMap(){await supplierApi.saveMapping(mapForm);ElMessage.success('已保存');mapVisible.value=false;load()}
async function removeMap(id:number){await ElMessageBox.confirm('确认删除？');await supplierApi.removeMapping(id);load()}
onMounted(load)
</script>
