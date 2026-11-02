<template>
  <div>
    <PageHeader title="管理员账号" subtitle="修改当前登录管理员的用户名或密码" />
    <div class="page-card" style="max-width:520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="当前密码"><el-input v-model="form.currentPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="form.newPassword" type="password" show-password placeholder="不修改可留空" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="saving" @click="save">保存</el-button></el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
const auth=useAuthStore(); const saving=ref(false)
const form=reactive({username:'',currentPassword:'',newPassword:''})
onMounted(async()=>{const res=await authApi.me();form.username=res.data?.username||''})
async function save(){saving.value=true;try{const res=await authApi.updateAccount(form);auth.updateSession(res.data);ElMessage.success('账号已更新');form.currentPassword='';form.newPassword=''}finally{saving.value=false}}
</script>
