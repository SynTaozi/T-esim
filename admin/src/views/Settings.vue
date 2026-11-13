<template>
  <div>
    <PageHeader title="系统设置" subtitle="主题与站点展示" />
    <div class="page-card" style="margin-bottom:16px">
      <h3>前台主题</h3>
      <el-form :model="theme" label-width="120px" style="max-width:560px">
        <el-form-item label="主题编码"><el-input v-model="theme.code"/></el-form-item>
        <el-form-item label="主色"><el-input v-model="theme.primaryColor"/></el-form-item>
        <el-form-item label="启用"><el-switch v-model="theme.enabled"/></el-form-item>
        <el-form-item><el-button type="primary" @click="saveTheme">保存主题</el-button></el-form-item>
      </el-form>
    </div>
    <div class="page-card">
      <h3>站点展示</h3>
      <el-form :model="site" label-width="140px" style="max-width:640px">
        <el-form-item label="登录页 Banner"><el-input v-model="site.loginBannerUrl" /></el-form-item>
        <el-form-item label="注册页 Banner"><el-input v-model="site.registerBannerUrl" /></el-form-item>
        <el-form-item label="首页 Hero"><el-input v-model="site.homeHeroImageUrl" /></el-form-item>
        <el-form-item label="默认运费 (USD)">
          <el-input-number
            v-model="site.defaultShippingFee"
            :min="0"
            :precision="2"
            :step="0.5"
          />
          <div class="field-tip">购买实体设备时整单收取一次，前台结算会读取该金额</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveSite">保存站点配置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { siteApi, themeApi } from '@/api/cms'

const theme = reactive<any>({ code: 'default', primaryColor: '#2563eb', enabled: true })
const site = reactive<any>({
  loginBannerUrl: '',
  registerBannerUrl: '',
  homeHeroImageUrl: '',
  defaultShippingFee: 0
})

async function load() {
  Object.assign(theme, (await themeApi.get()).data || {})
  const siteData = (await siteApi.get()).data || {}
  site.loginBannerUrl = siteData.loginBannerUrl || ''
  site.registerBannerUrl = siteData.registerBannerUrl || ''
  site.homeHeroImageUrl = siteData.homeHeroImageUrl || ''
  site.defaultShippingFee =
    siteData.defaultShippingFee == null ? 0 : Number(siteData.defaultShippingFee)
}

async function saveTheme() {
  await themeApi.save(theme)
  ElMessage.success('主题已保存')
}

async function saveSite() {
  await siteApi.save({
    loginBannerUrl: site.loginBannerUrl,
    registerBannerUrl: site.registerBannerUrl,
    homeHeroImageUrl: site.homeHeroImageUrl,
    defaultShippingFee: Number(site.defaultShippingFee ?? 0)
  })
  ElMessage.success('站点配置已保存')
  await load()
}

onMounted(load)
</script>
<style scoped>
h3 {
  margin: 0 0 16px;
  font-size: 15px;
}
.field-tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--admin-muted, #64748b);
  line-height: 1.4;
}
</style>
