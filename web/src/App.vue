<template>
  <div id="app">
    <router-view v-slot="{ Component, route }">
      <component :is="Component" :key="route.path" />
    </router-view>
    <PageRouteLoading v-if="navigationStore.isRouteLoading" />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { useNavigationStore } from '@/stores/navigation'
import { themeApi } from '@/api/theme'
import PageRouteLoading from '@/components/PageRouteLoading.vue'

const themeStore = useThemeStore()
const navigationStore = useNavigationStore()

/**
 * 应用启动时从后端获取主题配置
 */
onMounted(async () => {
  try {
    console.log('🎨 正在从后端获取主题配置...')
    const response = await themeApi.getCurrentTheme()
    console.log('📡 API 响应:', response)
    
    // response 是 ApiResponse 对象: { code, message, data: {...} }
    if (response && response.data) {
      const themeData = response.data
      console.log('📝 主题数据:', themeData)
      
      if (themeData.themeId) {
        console.log(`🎯 应用主题: ${themeData.themeId} (${themeData.themeName})`)
        themeStore.applyTheme(themeData.themeId)
        console.log('✅ 主题已应用')
      } else {
        console.warn('⚠️ 主题数据中没有 themeId')
      }
    } else {
      console.warn('⚠️ API 响应中没有 data 字段:', response)
    }
  } catch (error: any) {
    console.error('❌ 获取主题配置失败:', error)
    console.log('💾 使用本地存储或默认主题')
  }
})
</script>

<style>
/* 全局基础样式 - 苹果风格主题 */
:root {
  /* Header背景 */
  --header-bg: #FFFFFF;
  
  /* 背景色 - 全站白底 */
  --bg-deepest: #FFFFFF;
  --bg-deep: #FFFFFF;
  --bg-primary: #FFFFFF;
  --bg-elevated: #FFFFFF;
  --bg-card: #FFFFFF;
  --bg-hover: #F5F5F7;
  
  /* 主色调 - 深灰/苹果蓝 */
  --accent-primary: #1D1D1D;
  --accent-secondary: #86868B;
  --accent-hover: #0071E3;
  --accent-muted: rgba(0, 113, 227, 0.08);
  
  /* 功能色 - 苹果风格 */
  --accent-emerald: #34C759;
  --accent-rose: #FF3B30;
  --accent-cyan: #00B4D8;
  --accent-violet: #0071E3;
  --accent-amber: #FF9500;
  --accent-pink: #FF2D55;
  --accent-success: #34C759;
  --accent-warning: #FF9500;
  --accent-danger: #FF3B30;
  --accent-info: #0071E3;
  
  /* 文字色 - 深灰 */
  --text-primary: #1D1D1D;
  --text-secondary: #86868B;
  --text-muted: #A1A1A6;
  
  /* 边框 */
  --border-subtle: rgba(0, 0, 0, 0.04);
  --border-default: #F5F5F7;
  --border-hover: #E5E5E7;
  
  /* 阴影 */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.1);
  --shadow-glow: 0 0 30px rgba(0, 113, 227, 0.1);
  
  /* 字体 & 按钮圆角 */
  --font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Helvetica Neue', sans-serif;
  --btn-radius: 980px;
  
  /* Material Design 3 系统变量（Home.vue 需要） */
  --primary:             #0059b5;
  --primary-container:   #0071e3;
  --on-primary:          #ffffff;
  --inverse-primary:     #abc7ff;
  --primary-fixed:       #d7e2ff;
  --primary-fixed-dim:   #abc7ff;
  --on-primary-fixed:    #001b3f;
  --secondary:           #5f5e60;
  --surface:             #ffffff;
  --surface-dim:         #dad9df;
  --surface-bright:      #ffffff;
  --surface-container-lowest:  #ffffff;
  --surface-container-low:     #f4f3f8;
  --surface-container:         #eeedf3;
  --surface-container-high:    #e9e7ed;
  --surface-container-highest: #e3e2e7;
  --on-surface:          #1a1b1f;
  --on-surface-variant:  #414753;
  --inverse-surface:     #2f3034;
  --inverse-on-surface:  #f1f0f5;
  --outline:             #717785;
  --outline-variant:     #c1c6d6;
  --surface-tint:        #005cbb;
  --surface-variant:     #e3e2e7;
  --background:          #ffffff;
  --on-background:       #1a1b1f;
  --error:               #ba1a1a;
  --on-error:            #ffffff;
  
  /* 字体系列 */
  --font-headline: 'Space Grotesk', system-ui, sans-serif;
  --font-body:     'Manrope', system-ui, sans-serif;
  
  /* 圆角尺寸 */
  --radius-sm:   0.25rem;
  --radius:      0.5rem;
  --radius-md:   0.75rem;
  --radius-lg:   1rem;
  --radius-xl:   1.5rem;
  --radius-full: 9999px;
  
  /* Material Design 3 阴影 */
  --shadow-xs:  0 1px 2px rgba(0,0,0,0.05);
  --shadow-md:  0 4px 12px rgba(0,0,0,0.08);
  --shadow-lg:  0 8px 30px rgba(0,0,0,0.10);
  --shadow-xl:  0 20px 50px rgba(0,89,181,0.12);
  
  /* FAQ.vue 所需的变量 */
  --border: #F5F5F7;
  --bg-white: #FFFFFF;
  --bg-light: #F5F5F7;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: var(--font-family);
  background: #ffffff;
  color: var(--text-primary);
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  transition: background 0.3s ease, color 0.3s ease;
}

/* 全站白底：关闭顶部光晕 */
body::before {
  display: none;
}

/* 链接默认样式 */
a {
  color: inherit;
  text-decoration: none;
}

/* Element Plus 商务主题覆盖 */
.el-button--primary {
  --el-button-bg-color: var(--accent-primary);
  --el-button-border-color: var(--accent-primary);
  --el-button-hover-bg-color: var(--accent-hover);
  --el-button-hover-border-color: var(--accent-hover);
  --el-button-active-bg-color: #0a2540;
  --el-button-active-border-color: #0a2540;
  --el-button-text-color: #fff;
  border: none !important;
  font-weight: 500;
}

.el-button--default {
  --el-button-bg-color: var(--bg-primary);
  --el-button-border-color: var(--border-default);
  --el-button-text-color: var(--text-primary);
  --el-button-hover-bg-color: var(--bg-hover);
  --el-button-hover-border-color: var(--accent-primary);
  --el-button-hover-text-color: var(--accent-primary);
}

.el-button--success {
  --el-button-bg-color: var(--accent-success);
  --el-button-border-color: var(--accent-success);
  --el-button-hover-bg-color: #047857;
  --el-button-hover-border-color: #047857;
}

.el-button--warning {
  --el-button-bg-color: var(--accent-warning);
  --el-button-border-color: var(--accent-warning);
  --el-button-hover-bg-color: #b45309;
  --el-button-hover-border-color: #b45309;
}

.el-button--danger {
  --el-button-bg-color: var(--accent-danger);
  --el-button-border-color: var(--accent-danger);
  --el-button-hover-bg-color: #b91c1c;
  --el-button-hover-border-color: #b91c1c;
}

.el-input {
  --el-input-bg-color: var(--bg-primary);
  --el-input-border-color: var(--border-default);
  --el-input-text-color: var(--text-primary);
  --el-input-placeholder-color: var(--text-muted);
  --el-input-focus-border-color: var(--accent-primary);
}

.el-input__wrapper {
  background: var(--bg-primary) !important;
  box-shadow: none !important;
  border: 1px solid var(--border-default) !important;
  border-radius: var(--radius-md) !important;
}

.el-input__wrapper:hover {
  border-color: var(--border-hover) !important;
}

.el-input__wrapper.is-focus {
  border-color: var(--accent-primary) !important;
  box-shadow: 0 0 0 3px rgba(15, 52, 96, 0.1) !important;
}

.el-select {
  --el-select-input-focus-border-color: var(--accent-primary);
}

.el-select .el-input__wrapper {
  background: var(--bg-primary) !important;
}

.el-select-dropdown {
  background: var(--bg-card) !important;
  border: 1px solid var(--border-default) !important;
  border-radius: var(--radius-md) !important;
  box-shadow: var(--shadow-lg) !important;
}

.el-select-dropdown__item {
  color: var(--text-secondary) !important;
  border-radius: var(--radius-sm);
}

.el-select-dropdown__item.hover,
.el-select-dropdown__item:hover {
  background: var(--bg-hover) !important;
}

.el-select-dropdown__item.selected {
  color: var(--accent-primary) !important;
  font-weight: 600;
}

.el-dialog {
  --el-dialog-bg-color: var(--bg-card);
  --el-dialog-title-font-size: 18px;
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--border-default);
  box-shadow: var(--shadow-lg);
}

.el-dialog__header {
  border-bottom: 1px solid var(--border-subtle);
  padding: 20px 24px !important;
}

.el-dialog__title {
  color: var(--text-primary) !important;
  font-weight: 600;
}

.el-dialog__body {
  padding: 24px !important;
  color: var(--text-secondary);
}

.el-dialog__footer {
  border-top: 1px solid var(--border-subtle);
  padding: 16px 24px !important;
}

.el-message-box {
  --el-messagebox-title-color: var(--text-primary);
  --el-messagebox-content-color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-md);
}

.el-tag {
  --el-tag-bg-color: var(--accent-muted);
  --el-tag-border-color: transparent;
  --el-tag-text-color: var(--accent-primary);
  border-radius: var(--radius-sm);
  font-weight: 500;
}

.el-tag--success {
  --el-tag-bg-color: #dcfce7;
  --el-tag-text-color: #166534;
}

.el-tag--warning {
  --el-tag-bg-color: #fef3c7;
  --el-tag-text-color: #92400e;
}

.el-tag--danger {
  --el-tag-bg-color: #fee2e2;
  --el-tag-text-color: #991b1b;
}

.el-tag--info {
  --el-tag-bg-color: #e0f2fe;
  --el-tag-text-color: #075985;
}

.el-pagination {
  --el-pagination-bg-color: transparent;
  --el-pagination-text-color: var(--text-secondary);
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: var(--accent-primary);
}

.el-pagination .el-pager li {
  background: var(--bg-primary) !important;
  color: var(--text-secondary) !important;
  border: 1px solid var(--border-default);
  border-radius: var(--radius-sm);
  font-weight: 500;
}

.el-pagination .el-pager li:hover {
  color: var(--accent-primary) !important;
  border-color: var(--accent-primary);
}

.el-pagination .el-pager li.is-active {
  background: var(--accent-primary) !important;
  color: #fff !important;
  border-color: var(--accent-primary);
}

.el-pagination .btn-prev,
.el-pagination .btn-next {
  background: var(--bg-primary) !important;
  color: var(--text-secondary) !important;
  border: 1px solid var(--border-default);
  border-radius: var(--radius-sm);
}

.el-pagination .btn-prev:hover,
.el-pagination .btn-next:hover {
  color: var(--accent-primary) !important;
  border-color: var(--accent-primary);
}

.el-form-item__label {
  color: var(--text-secondary) !important;
  font-weight: 500;
}

.el-message {
  --el-message-bg-color: var(--bg-card);
  --el-message-border-color: var(--border-default);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
}

.el-message__content {
  color: var(--text-primary);
}

.el-empty__description {
  color: var(--text-muted) !important;
}

.el-card {
  --el-card-bg-color: var(--bg-card);
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--border-default);
  box-shadow: var(--shadow-sm);
}

.el-card:hover {
  box-shadow: var(--shadow-md);
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: var(--border-default);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--border-hover);
}

/* 通用容器 — 宽度统一由 main.css --site-content-max-width 控制 */
.container {
  max-width: var(--site-content-max-width, 1300px);
  margin: 0 auto;
  padding-left: var(--site-content-padding-x, 24px);
  padding-right: var(--site-content-padding-x, 24px);
}

/* 霓虹主题特效 */
.theme-neon .el-button--primary,
.theme-neon .btn-primary {
  text-shadow: 0 0 10px currentColor;
  box-shadow: 0 0 15px var(--accent-primary), inset 0 0 15px rgba(0,255,136,0.05);
}

.theme-neon *:focus {
  outline: 1px solid var(--accent-primary);
  outline-offset: 2px;
}

/* 黄金主题特效 */
.theme-gold .el-button--primary,
.theme-gold .btn-primary {
  background: linear-gradient(135deg, #F59E0B, #D97706) !important;
  box-shadow: 0 4px 15px rgba(245,158,11,0.4);
}

/* 紫色主题特效 */
.theme-violet .el-button--primary,
.theme-violet .btn-primary {
  background: linear-gradient(135deg, #A78BFA, #7C3AED) !important;
  box-shadow: 0 4px 15px rgba(124,58,237,0.4);
}

/* 按钮圆角跟随主题 */
.el-button {
  border-radius: var(--btn-radius) !important;
}
</style>
