<template>
  <div class="admin-layout" :class="{ collapsed }">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="brand">
          <div class="brand-mark">T</div>
          <div v-show="!collapsed" class="brand-text">
            <strong>T-esim</strong>
            <span>Admin Console</span>
          </div>
        </div>
        <button class="collapse-btn" type="button" @click="collapsed = !collapsed">
          <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
        </button>
      </div>

      <el-scrollbar class="sidebar-scroll">
        <el-menu
          :default-active="currentRoute"
          :collapse="collapsed"
          router
          class="sidebar-menu"
        >
          <div v-show="!collapsed" class="nav-group">概览</div>
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>

          <div v-show="!collapsed" class="nav-group">业务</div>
          <el-sub-menu index="business">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>业务管理</span>
            </template>
            <el-menu-item index="/products">产品管理</el-menu-item>
            <el-menu-item index="/product-categories">产品分类</el-menu-item>
            <el-menu-item index="/orders">订单管理</el-menu-item>
            <el-menu-item index="/tickets">工单管理</el-menu-item>
          </el-sub-menu>

          <div v-show="!collapsed" class="nav-group">配置</div>
          <el-sub-menu index="config">
            <template #title>
              <el-icon><DocumentCopy /></el-icon>
              <span>配置管理</span>
            </template>
            <el-menu-item index="/payment-methods">支付方式</el-menu-item>
            <el-menu-item index="/card-platform">发卡平台</el-menu-item>
            <el-menu-item index="/faq">常见问题</el-menu-item>
            <el-menu-item index="/tutorials">教程管理</el-menu-item>
            <el-menu-item index="/app-download-qrcodes">App下载二维码</el-menu-item>
            <el-menu-item index="/coupons">优惠券</el-menu-item>
          </el-sub-menu>

          <div v-show="!collapsed" class="nav-group">邮件</div>
          <el-sub-menu index="email">
            <template #title>
              <el-icon><Message /></el-icon>
              <span>邮件管理</span>
            </template>
            <el-menu-item index="/email-accounts">邮件账户</el-menu-item>
            <el-menu-item index="/email-templates">邮件模板</el-menu-item>
            <el-menu-item index="/email-send-logs">发送日志</el-menu-item>
          </el-sub-menu>

          <div v-show="!collapsed" class="nav-group">系统</div>
          <el-sub-menu index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/users">用户管理</el-menu-item>
            <el-menu-item index="/admin-account">管理员账号</el-menu-item>
            <el-menu-item index="/dingtalk">钉钉通知</el-menu-item>
            <el-menu-item index="/settings">系统设置</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </aside>

    <div class="main">
      <header class="top-header">
        <div>
          <div class="breadcrumb">控制台 / {{ pageTitle }}</div>
          <h2>{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <span class="username">{{ authStore.user?.username || '管理员' }}</span>
          <el-button type="danger" plain size="small" @click="handleLogout">退出</el-button>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  HomeFilled,
  Goods,
  DocumentCopy,
  Message,
  Setting,
  Fold,
  Expand
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const collapsed = ref(false)

const currentRoute = computed(() => route.path)

const pageTitleMap: Record<string, string> = {
  '/': '仪表盘',
  '/products': '产品管理',
  '/products/create': '添加产品',
  '/products/plan-sync': '套餐同步',
  '/product-categories': '产品分类',
  '/product-subcategories': '产品分类',
  '/orders': '订单管理',
  '/tickets': '工单管理',
  '/payment-methods': '支付方式',
  '/faq': '常见问题管理',
  '/tutorials': '教程管理',
  '/app-download-qrcodes': 'App下载二维码',
  '/coupons': '优惠券管理',
  '/email-accounts': '邮件账户管理',
  '/email-templates': '邮件模板管理',
  '/email-send-logs': '邮件发送日志',
  '/card-platform': '发卡平台对接',
  '/card-platform/bulk-mapping': '批量商品映射',
  '/dingtalk': '钉钉通知',
  '/users': '用户管理',
  '/admin-account': '管理员账号',
  '/settings': '系统设置'
}

const pageTitle = computed(() => {
  if (route.path.startsWith('/products/edit')) return '编辑产品'
  return pageTitleMap[route.path] || '管理后台'
})

function handleLogout() {
  authStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--admin-bg);
}

.sidebar {
  width: var(--admin-sidebar-width);
  background: var(--admin-sidebar-bg);
  border-right: 1px solid var(--admin-border);
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease;
  flex-shrink: 0;
}

.admin-layout.collapsed .sidebar {
  width: var(--admin-sidebar-collapsed);
}

.sidebar-header {
  height: var(--admin-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px 0 16px;
  border-bottom: 1px solid var(--admin-border);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.brand-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--admin-primary);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
  flex-shrink: 0;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.brand-text strong {
  font-size: 15px;
  color: var(--admin-text);
}

.brand-text span {
  font-size: 11px;
  color: var(--admin-text-muted);
}

.collapse-btn {
  border: none;
  background: transparent;
  color: var(--admin-text-secondary);
  cursor: pointer;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: grid;
  place-items: center;
}

.collapse-btn:hover {
  background: var(--admin-primary-soft);
  color: var(--admin-primary);
}

.sidebar-scroll {
  flex: 1;
}

.nav-group {
  padding: 16px 20px 6px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--admin-text-muted);
  text-transform: uppercase;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
  padding-bottom: 16px;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 100%;
}

.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.top-header {
  height: var(--admin-header-height);
  background: #fff;
  border-bottom: 1px solid var(--admin-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.breadcrumb {
  font-size: 12px;
  color: var(--admin-text-muted);
  margin-bottom: 2px;
}

.top-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 650;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-size: 14px;
  color: var(--admin-text-secondary);
}

.content {
  flex: 1;
  padding: 20px 24px 32px;
  overflow: auto;
}
</style>
