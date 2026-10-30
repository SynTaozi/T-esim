import { defineStore } from 'pinia'
import { ref } from 'vue'
import { siteDisplayApi } from '@/api/siteDisplay'
import {
  DEFAULT_HOME_HERO_IMAGE_URL,
  DEFAULT_LOGIN_BANNER_URL,
  DEFAULT_REGISTER_BANNER_URL
} from '@/constants/siteDisplayDefaults'

/**
 * 展示图由后端启动时下载到本地缓存，再通过 /api/public/site-display 返回同源地址。
 * 前端只使用接口返回的 URL，不再直连 R2。
 * 快递费必须等接口加载完成后再用于下单计价，禁止写死 3.5。
 */
export const useSiteConfigStore = defineStore('siteConfig', () => {
  const defaultShippingFee = ref<number | null>(null)
  const homeHeroImageUrl = ref(DEFAULT_HOME_HERO_IMAGE_URL)
  const loginBannerUrl = ref(DEFAULT_LOGIN_BANNER_URL)
  const registerBannerUrl = ref(DEFAULT_REGISTER_BANNER_URL)
  const loaded = ref(false)
  let loadingPromise: Promise<void> | null = null

  async function load() {
    if (loaded.value) return
    if (loadingPromise) return loadingPromise

    loadingPromise = (async () => {
      try {
        const res: any = await siteDisplayApi.getConfig()
        const data = res?.data
        if (!data) return

        const fee = data.defaultShippingFee
        if (fee != null && !Number.isNaN(Number(fee))) {
          defaultShippingFee.value = Number(fee)
        }
        if (data.homeHeroImageUrl) {
          homeHeroImageUrl.value = data.homeHeroImageUrl
        }
        if (data.loginBannerUrl) {
          loginBannerUrl.value = data.loginBannerUrl
        }
        if (data.registerBannerUrl) {
          registerBannerUrl.value = data.registerBannerUrl
        }
      } catch {
        // keep defaults except shipping fee stays null until loaded
      } finally {
        loaded.value = true
        loadingPromise = null
      }
    })()

    return loadingPromise
  }

  /** 下单前调用：确保已拿到后台配置的快递费 */
  async function ensureShippingFee(): Promise<number> {
    if (defaultShippingFee.value == null || Number.isNaN(Number(defaultShippingFee.value))) {
      loaded.value = false
      loadingPromise = null
    }
    await load()
    if (defaultShippingFee.value == null || Number.isNaN(Number(defaultShippingFee.value))) {
      throw new Error('快递费配置加载失败，请刷新页面后重试')
    }
    return Number(defaultShippingFee.value)
  }

  function formatShippingFee(): string {
    if (defaultShippingFee.value == null) return '—'
    return `$${Number(defaultShippingFee.value).toFixed(2)} USD`
  }

  return {
    defaultShippingFee,
    homeHeroImageUrl,
    loginBannerUrl,
    registerBannerUrl,
    loaded,
    load,
    ensureShippingFee,
    formatShippingFee
  }
})
