import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { i18n } from './i18n'
import './styles/main.css'
import './styles/site-chrome.css'
import './styles/theme-home-enhanced.css'
import { useThemeStore } from '@/stores/theme'
import { useSiteConfigStore } from '@/stores/siteConfig'

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(i18n)

async function waitForFonts(maxMs = 1800) {
  const timeout = new Promise<void>(resolve => setTimeout(resolve, maxMs))
  const loads: Promise<unknown>[] = []
  if (document.fonts?.load) {
    // 优先等待图标字体，否则 ligature 会以英文原文闪现
    loads.push(document.fonts.load('24px "Material Symbols Outlined"').catch(() => undefined))
  }
  if (document.fonts?.ready) {
    loads.push(document.fonts.ready)
  }
  if (loads.length) {
    await Promise.race([Promise.all(loads), timeout])
  } else {
    await timeout
  }
}

async function bootstrap() {
  // 启动时立即应用本地缓存主题，避免首屏样式闪烁
  useThemeStore()

  // 展示配置异步加载：Hero 等未就绪前保持为空，不阻塞首屏、不闪默认图
  void useSiteConfigStore().load()

  try {
    await waitForFonts()
  } catch {
    // 超时也继续挂载，避免白屏过久
  }

  document.documentElement.classList.add('ms-icons-ready')
  document.documentElement.classList.add('styles-ready')
  app.mount('#app')
}

bootstrap()
