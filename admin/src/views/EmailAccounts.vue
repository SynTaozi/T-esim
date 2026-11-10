<template>
  <div>
    <PageHeader title="邮件账户" subtitle="SMTP 发信账户（业务邮件异步发送）">
      <template #actions><el-button type="primary" @click="edit()">新建</el-button></template>
    </PageHeader>
    <div class="page-card">
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="emailAddress" label="邮箱" min-width="180" />
        <el-table-column prop="purpose" label="用途" width="110" />
        <el-table-column prop="smtpHost" label="SMTP" min-width="140" />
        <el-table-column label="启用" width="80">
          <template #default="{ row }"><StatusTag :status="row.enabled ? 'ACTIVE' : 'DISABLED'" /></template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link @click="test(row.id)">测试</el-button>
            <el-button link type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="visible" title="邮件账户" width="640px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="邮箱" required><el-input v-model="form.emailAddress" /></el-form-item>
        <el-form-item label="用途" required>
          <el-select v-model="form.purpose" style="width: 100%">
            <el-option label="通用 GENERAL" value="GENERAL" />
            <el-option label="发货 SHIPPING" value="SHIPPING" />
            <el-option label="支付 PAYMENT" value="PAYMENT" />
            <el-option label="客服 SUPPORT" value="SUPPORT" />
          </el-select>
        </el-form-item>
        <el-form-item label="SMTP主机" required><el-input v-model="form.smtpHost" /></el-form-item>
        <el-form-item label="SMTP端口" required><el-input-number v-model="form.smtpPort" :min="1" /></el-form-item>
        <el-form-item label="用户名" required><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" :required="!form.id">
          <el-input v-model="form.password" type="password" show-password :placeholder="form.id ? '不改请留空' : ''" />
        </el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { emailAccountApi } from '@/api/email'

const loading = ref(false)
const saving = ref(false)
const rows = ref<any[]>([])
const visible = ref(false)
const form = reactive<any>({})

function blankForm() {
  return {
    name: '',
    emailAddress: '',
    purpose: 'GENERAL',
    smtpHost: '',
    smtpPort: 465,
    username: '',
    password: '',
    enabled: true
  }
}

async function load() {
  loading.value = true
  try {
    rows.value = (await emailAccountApi.list()).data || []
  } finally {
    loading.value = false
  }
}

function edit(row?: any) {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, blankForm(), row || {})
  if (!form.purpose) form.purpose = 'GENERAL'
  // 编辑时不回填密码
  form.password = ''
  visible.value = true
}

async function save() {
  if (!form.name?.trim() || !form.emailAddress?.trim() || !form.username?.trim() || !form.smtpHost?.trim() || !form.smtpPort) {
    ElMessage.warning('请填写名称、邮箱、用户名、SMTP 主机和端口')
    return
  }
  if (!form.purpose) {
    ElMessage.warning('请选择用途')
    return
  }
  if (!form.id && !form.password) {
    ElMessage.warning('请填写密码')
    return
  }
  saving.value = true
  try {
    await emailAccountApi.save(form)
    ElMessage.success('已保存')
    visible.value = false
    await load()
  } catch {
    // 错误由 api 拦截器提示
  } finally {
    saving.value = false
  }
}

async function test(id: number) {
  try {
    await emailAccountApi.test(id)
    ElMessage.success('测试邮件已发送')
  } catch {
    // ignore
  }
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除？')
  await emailAccountApi.remove(id)
  await load()
}

onMounted(load)
</script>
