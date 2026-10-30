<template>
  <footer class="site-footer">
    <FooterMountainWave />
    <div class="footer-inner container">
      <div class="footer-grid">
        <div class="footer-brand">
          <div class="brand-logo">
            <span class="logo-icon"><img src="/logo.png" alt="Tesim logo" /></span>
            <span>TESIM</span>
          </div>
          <p class="brand-desc">{{ $t('footer.description') }}</p>
          <div class="footer-contact-info">
            <div class="contact-row">
              <span class="contact-icon">&#128231;</span>
              <a href="mailto:support@tesim.com">support@tesim.com</a>
            </div>
            <div class="contact-row">
              <span class="contact-icon">&#128222;</span>
              <a href="tel:+12133438884">{{ $t('footer.phone') }}</a>
            </div>
            <div class="contact-row">
              <span class="contact-icon">&#128205;</span>
              <span>{{ $t('footer.address') }}</span>
            </div>
          </div>
        </div>
        <div class="footer-links">
          <h4>{{ $t('footer.company') }}</h4>
          <router-link to="/contact">{{ $t('footer.support') }}</router-link>
          <router-link to="/faq">{{ $t('footer.faq') }}</router-link>
          <router-link to="/tutorials">{{ $t('footer.tutorials') }}</router-link>
          <router-link to="/tickets">{{ $t('footer.tickets') }}</router-link>
          <div v-if="downloadItems.length" class="download-block">
            <p class="download-title">{{ $t('footer.download') }}</p>
            <div class="download-platforms">
              <div
                v-for="item in downloadItems"
                :key="item.platform"
                class="download-platform"
              >
                <img
                  v-if="item.qrImageUrl"
                  :src="item.qrImageUrl"
                  class="download-qr"
                  :alt="displayName(item)"
                />
                <canvas
                  v-else
                  :ref="(el) => setQrCanvas(item.platform, el)"
                  class="download-qr"
                  :aria-label="displayName(item)"
                />
                <a
                  :href="item.downloadUrl"
                  :target="item.platform === 'ANDROID' ? undefined : '_blank'"
                  :rel="item.platform === 'ANDROID' ? undefined : 'noopener noreferrer'"
                  class="platform-label"
                >
                  <svg v-if="item.platform === 'IOS'" class="platform-icon" viewBox="0 0 24 24" aria-hidden="true">
                    <path fill="currentColor" d="M17.05 20.28c-.98.95-2.05.8-3.08.35-1.09-.46-2.09-.48-3.24 0-1.44.62-2.2.44-3.06-.35C2.79 15.25 3.51 7.59 9.05 7.31c1.35.07 2.29.74 3.08.8 1.18-.24 2.31-.93 3.57-.84 1.51.12 2.65.72 3.4 1.8-3.12 1.87-2.38 5.98.48 7.13-.57 1.5-1.31 2.99-2.54 4.09l.01-.01zM12.03 7.25c-.15-2.23 1.66-4.07 3.74-4.25.29 2.58-2.34 4.5-3.74 4.25z"/>
                  </svg>
                  <svg v-else class="platform-icon" viewBox="0 0 24 24" aria-hidden="true">
                    <path fill="currentColor" d="M17.6 9.48l1.84-3.18c.16-.31-.04-.69-.37-.69h-.01c-.15 0-.29.08-.37.21l-1.87 3.24c-1.28-.6-2.71-.93-4.22-.93s-2.94.33-4.22.93L6.03 5.82c-.08-.13-.22-.21-.37-.21h-.01c-.33 0-.53.38-.37.69L7.12 9.48C4.43 11.02 2.57 13.81 2.57 17.07h18.86c0-3.26-1.86-6.05-4.83-7.59zM8.75 15.25c-.41 0-.75-.34-.75-.75s.34-.75.75-.75.75.34.75.75-.34.75-.75.75zm6.5 0c-.41 0-.75-.34-.75-.75s.34-.75.75-.75.75.34.75.75-.34.75-.75.75z"/>
                  </svg>
                  {{ displayName(item) }}
                </a>
              </div>
            </div>
          </div>
        </div>
        <div class="footer-links">
          <h4>{{ $t('footer.business') }}</h4>
          <a href="https://moccards.com/" target="_blank" rel="noopener noreferrer">
            <span>{{ $t('footer.mocCards') }}</span>
            <span class="biz-desc">{{ $t('footer.mocCardsDesc') }}</span>
          </a>
          <a href="https://mocstack.com/" target="_blank" rel="noopener noreferrer">
            <span>{{ $t('footer.mocStack') }}</span>
            <span class="biz-desc">{{ $t('footer.mocStackDesc') }}</span>
          </a>
          <a href="https://mocdocs.com/" target="_blank" rel="noopener noreferrer">
            <span>{{ $t('footer.mocDocs') }}</span>
            <span class="biz-desc">{{ $t('footer.mocDocsDesc') }}</span>
          </a>
          <a href="https://tellme.tesim.com/" target="_blank" rel="noopener noreferrer">
            <span>{{ $t('footer.tellme') }}</span>
            <span class="biz-desc">{{ $t('footer.tellmeDesc') }}</span>
          </a>
          <a href="https://tesim.com/" target="_blank" rel="noopener noreferrer">
            <span>{{ $t('footer.tesim') }}</span>
            <span class="biz-desc">{{ $t('footer.tesimDesc') }}</span>
          </a>
        </div>
        <div class="footer-links">
          <h4>{{ $t('footer.legal') }}</h4>
          <router-link to="/privacy">{{ $t('footer.privacy') }}</router-link>
          <router-link to="/terms">{{ $t('footer.terms') }}</router-link>
          <router-link to="/disclaimer">{{ $t('footer.disclaimer') }}</router-link>
          <router-link to="/refund-policy">{{ $t('footer.refundPolicy') }}</router-link>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2026 Tesim&trade; LLC. {{ $t('footer.copyright') }}</p>
        <p class="footer-description">{{ $t('footer.registered') }} &middot; {{ $t('footer.platform') }}</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import QRCode from 'qrcode'
import FooterMountainWave from '@/components/FooterMountainWave.vue'
import { appDownloadQrcodeApi, type AppDownloadQrcode } from '@/api/appDownloadQrcode'

const { locale } = useI18n({ useScope: 'global' })

const FALLBACK_ITEMS: AppDownloadQrcode[] = [
  {
    id: 0,
    platform: 'IOS',
    name: 'iOS 下载',
    nameEn: 'Download for iOS',
    downloadUrl: 'https://apps.apple.com/app/id6775102608'
  },
  {
    id: 0,
    platform: 'ANDROID',
    name: 'Android 下载',
    nameEn: 'Download for Android',
    downloadUrl: 'https://www.tesim.com/file/TesimTools-1.0.1.apk'
  }
]

const QR_DISPLAY_SIZE = 68
const FOOTER_BG = '#f8f8fb'
const downloadItems = ref<AppDownloadQrcode[]>([])
const qrCanvasMap = new Map<string, HTMLCanvasElement>()

function displayName(item: AppDownloadQrcode) {
  if (locale.value === 'zh') return item.name
  return item.nameEn || item.name
}

function setQrCanvas(platform: string, el: unknown) {
  if (el instanceof HTMLCanvasElement) {
    qrCanvasMap.set(platform, el)
  } else {
    qrCanvasMap.delete(platform)
  }
}

async function renderQr(canvas: HTMLCanvasElement | undefined, url: string) {
  if (!canvas || !url) return
  const dpr = window.devicePixelRatio || 1
  await QRCode.toCanvas(canvas, url, {
    width: QR_DISPLAY_SIZE * dpr,
    margin: 0,
    color: { dark: '#3a3a3c', light: FOOTER_BG }
  })
  canvas.style.width = `${QR_DISPLAY_SIZE}px`
  canvas.style.height = `${QR_DISPLAY_SIZE}px`
}

async function renderGeneratedQrs() {
  await nextTick()
  const tasks = downloadItems.value
    .filter((item) => !item.qrImageUrl)
    .map((item) => renderQr(qrCanvasMap.get(item.platform), item.downloadUrl))
  try {
    await Promise.all(tasks)
  } catch (err) {
    console.error('QR Code generation failed:', err)
  }
}

onMounted(async () => {
  try {
    const list = await appDownloadQrcodeApi.listEnabled()
    downloadItems.value = list?.length ? list : FALLBACK_ITEMS
  } catch {
    downloadItems.value = FALLBACK_ITEMS
  }
  await renderGeneratedQrs()
})
</script>

<style scoped>
/* Footer — 背景/分隔由 site-chrome.css + main.css 统一 */
.site-footer {
  padding: 0;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 32px;
  margin-bottom: 48px;
}

@media (min-width: 768px) {
  .footer-grid { grid-template-columns: 2fr 1fr 1fr 1fr; }
}

.footer-brand .brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: var(--font-headline);
  font-size: 20px;
  font-weight: 700;
  color: var(--on-surface);
  margin-bottom: 16px;
}

.footer-brand .logo-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.footer-brand .logo-icon img { width: 100%; height: 100%; object-fit: contain; }

.brand-desc {
  font-size: 15px;
  line-height: 1.6;
  color: var(--on-surface-variant);
  margin: 0 0 20px;
  text-align: left;
}

.footer-contact-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.contact-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--on-surface-variant);
}

.contact-row a {
  color: var(--on-surface-variant);
  text-decoration: none;
  transition: color 0.2s;
}

.contact-row a:hover { color: #0059b5; }

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-links h4 {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--on-background);
  margin: 0 0 8px;
}

.footer-links a {
  font-size: 15px;
  color: var(--on-surface-variant);
  text-decoration: none;
  transition: color 0.2s;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.footer-links a:hover { color: #0059b5; text-decoration: underline; }

.biz-desc {
  font-size: 12px;
  color: var(--outline);
}

.download-block {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  width: 100%;
}

.download-title {
  margin: 0;
  font-size: 15px;
  line-height: 1.4;
  color: var(--on-surface-variant);
}

.download-platforms {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.download-platform {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.download-block .platform-label {
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--on-surface-variant);
  text-decoration: none;
  transition: color 0.2s;
}

.download-block .platform-label:hover {
  color: #0059b5;
  text-decoration: underline;
}

.platform-icon {
  width: 13px;
  height: 13px;
  flex-shrink: 0;
}

.download-qr {
  display: block;
  flex-shrink: 0;
  width: 68px;
  height: 68px;
  object-fit: contain;
}

.footer-bottom {
  border-top: 1px solid rgba(0, 0, 0, 0.04);
  padding-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

@media (min-width: 768px) {
  .footer-bottom { flex-direction: row; justify-content: space-between; align-items: center; }
}

.footer p {
  color: var(--on-surface-variant);
  font-size: 14px;
  margin: 0;
}

.footer-description {
  font-size: 13px;
  color: var(--outline);
}
</style>
