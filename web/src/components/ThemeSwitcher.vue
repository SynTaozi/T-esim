<template>
  <div class="theme-switcher">
    <!-- 触发按钮 -->
    <button
      class="theme-btn"
      :title="`当前主题: ${currentTheme?.name}`"
      @click="open = !open"
    >
      <span class="theme-btn-emoji">{{ currentTheme?.emoji }}</span>
      <svg class="theme-btn-icon" viewBox="0 0 16 16" fill="none">
        <circle cx="8" cy="8" r="3" fill="currentColor" />
        <path d="M8 1v2M8 13v2M1 8h2M13 8h2M3.05 3.05l1.41 1.41M11.54 11.54l1.41 1.41M3.05 12.95l1.41-1.41M11.54 4.46l1.41-1.41" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
    </button>

    <!-- 主题面板 -->
    <Transition name="panel">
      <div v-if="open" class="theme-panel" @click.stop>
        <div class="panel-header">
          <span class="panel-title">🎨 切换主题</span>
          <button class="close-btn" @click="open = false">✕</button>
        </div>
        <div class="theme-grid">
          <button
            v-for="theme in themes"
            :key="theme.id"
            class="theme-card"
            :class="{ active: currentThemeId === theme.id }"
            @click="selectTheme(theme.id)"
          >
            <div class="theme-preview" :style="getPreviewStyle(theme)">
              <div class="preview-header" :style="{ background: theme.vars['--accent-primary'] }"></div>
              <div class="preview-body">
                <div class="preview-line" :style="{ background: theme.vars['--accent-primary'], width: '60%' }"></div>
                <div class="preview-line" :style="{ background: theme.vars['--text-muted'], width: '40%' }"></div>
                <div class="preview-dot" :style="{ background: theme.vars['--accent-secondary'] }"></div>
              </div>
            </div>
            <div class="theme-info">
              <span class="theme-emoji">{{ theme.emoji }}</span>
              <div class="theme-text">
                <div class="theme-name">{{ theme.name }}</div>
                <div class="theme-desc">{{ theme.description }}</div>
              </div>
            </div>
            <div v-if="currentThemeId === theme.id" class="active-mark">✓</div>
          </button>
        </div>
      </div>
    </Transition>

    <!-- 遮罩 -->
    <div v-if="open" class="panel-overlay" @click="open = false" />
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { themes, useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const open = ref(false)

const currentThemeId = computed(() => themeStore.currentThemeId)
const currentTheme = computed(() => themes.find(t => t.id === currentThemeId.value))

function selectTheme(id: string) {
  themeStore.applyTheme(id)
  open.value = false
}

function getPreviewStyle(theme: (typeof themes)[0]) {
  return {
    background: theme.vars['--bg-primary'],
    border: `1px solid ${theme.vars['--border-default']}`,
    borderRadius: theme.vars['--radius-md'],
  }
}

function handleKey(e: KeyboardEvent) {
  if (e.key === 'Escape') open.value = false
}

onMounted(() => window.addEventListener('keydown', handleKey))
onBeforeUnmount(() => window.removeEventListener('keydown', handleKey))
</script>

<style scoped>
.theme-switcher {
  position: relative;
  z-index: 200;
}

/* 触发按钮 */
.theme-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 34px;
  padding: 0 10px;
  border-radius: 980px;
  border: 1px solid var(--border-default);
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  white-space: nowrap;
}

.theme-btn:hover {
  border-color: var(--accent-primary);
  color: var(--accent-primary);
  box-shadow: 0 0 0 3px var(--accent-muted);
}

.theme-btn-emoji {
  font-size: 15px;
  line-height: 1;
}

.theme-btn-icon {
  width: 14px;
  height: 14px;
}

/* 面板遮罩 */
.panel-overlay {
  position: fixed;
  inset: 0;
  z-index: 199;
}

/* 主题面板 */
.theme-panel {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  width: 340px;
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  z-index: 300;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-default);
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-muted);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.close-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

/* 主题格子 */
.theme-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 12px;
  max-height: 460px;
  overflow-y: auto;
}

.theme-grid::-webkit-scrollbar {
  width: 4px;
}
.theme-grid::-webkit-scrollbar-thumb {
  background: var(--border-default);
  border-radius: 2px;
}

/* 单个主题卡 */
.theme-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  border-radius: var(--radius-md);
  border: 1.5px solid var(--border-default);
  background: var(--bg-primary);
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.theme-card:hover {
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 2px var(--accent-muted);
  transform: translateY(-1px);
}

.theme-card.active {
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 2px var(--accent-muted);
}

/* 预览小图 */
.theme-preview {
  height: 52px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.preview-header {
  height: 8px;
  border-radius: 2px 2px 0 0;
  opacity: 0.9;
}

.preview-body {
  flex: 1;
  padding: 5px 6px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: relative;
}

.preview-line {
  height: 4px;
  border-radius: 2px;
  opacity: 0.6;
}

.preview-dot {
  position: absolute;
  bottom: 5px;
  right: 6px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  opacity: 0.8;
}

/* 主题信息 */
.theme-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.theme-emoji {
  font-size: 16px;
  line-height: 1;
  flex-shrink: 0;
}

.theme-text {
  flex: 1;
  min-width: 0;
}

.theme-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.theme-desc {
  font-size: 10px;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 1px;
}

/* 选中标记 */
.active-mark {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 18px;
  height: 18px;
  background: var(--accent-primary);
  color: white;
  border-radius: 50%;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

/* 动画 */
.panel-enter-active,
.panel-leave-active {
  transition: all 0.2s ease;
}

.panel-enter-from,
.panel-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.97);
}
</style>
