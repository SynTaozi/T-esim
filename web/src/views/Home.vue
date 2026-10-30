<template>
  <div class="home-page home-page--enhanced">
    <!-- Navigation -->
    <SiteHeader />

    <main v-loading="pageLoading" element-loading-background="rgba(255, 255, 255, 0.85)">
      <!-- Hero Section -->
      <section class="hero">
        <div class="hero-signal-deco" aria-hidden="true">
          <span class="hero-signal-grid" />
          <span class="hero-signal-orb hero-signal-orb--1" />
          <span class="hero-signal-orb hero-signal-orb--2" />
        </div>
        <div class="container hero-inner">
          <div class="hero-left">
            <span class="hero-label">{{ $t('hero.newEsim') }}</span>
            <h1 class="hero-title">
              {{ $t('hero.title') }}
              <span class="hero-title-accent">200+ {{ $t('hero.coverage') }}</span>
            </h1>
            <p class="hero-desc">{{ $t('hero.desc') }}</p>
            <div class="search-bar">
              <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35" stroke-linecap="round"/></svg>
              <input
                v-model="searchQuery"
                type="text"
                class="search-input"
                :placeholder="$t('hero.searchPlaceholder') || 'Where are you going?'"
                @keyup.enter="handleSearch"
              />
              <router-link to="/products" class="search-btn">{{ $t('hero.browseProducts') }}</router-link>
            </div>
            <div class="social-proof">
              <div class="avatar-stack">
                <img class="avatar" alt="User" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBm0rF8Sfc07qpH2SJXeTKcNMNH9fMXuYG9P87oFwEJhzMGHdp7PO4EKJCgvVmGxBFw865t3Zm6L3m0-roCV38pIUaeSf-xe0CPEK7V63H8g5jYHUYW6N4JHGwjRCWhHeTyFXIrddxWgho2D8fP51cTP0eAvWIYBvbEESZ8MGn8LxsjSFAt5AKa7DsJiAcF4GnZ3nOCC3ctFxxkoH4TQuL9UPtrtpmTNPUpSEZytiEiSsKuqa96ZZ7j64o3caUF8H2It_iO9w1RP5g" />
                <img class="avatar" alt="User" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDYGL8ydSPMwn5TRMT1tlbzoVpyGk-p-GE07i1uJxU4w9Q99AqQlIaslqwkhn0NP7QxFA9FpjfsUqJcm2Iw8sL1EgAKeln2Ahx3RCzoZqAggpdyjUZKzTI90IeMKYLRsD_ih8YtorlcffR3E5r_Ftr4W74IAXoYMJxQ_WxIKgRLdo2KSYpThnAkMGCR9wpJkuNlD8gmUc5soyEq_bPKU0EvTf9ZI0PznFtWAkshXNjtJAlKT0rh9_SOm7HuTba28ZxpKIHBztsOALs" />
                <img class="avatar" alt="User" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDIhO6qqw5FRfptHJdY9_TTI48wNXDj-7bqvmDtMW5anAbDRxzUuaUJWykAZEslYK3My7u-3lDi5un4LO86CuDhPPJxA4Tgdcl3aUwMsyoSaVcLTs2GB2Upg5nU8_hIB7tn9lThixzgxGemJSFdvZsZ0htsNcL2WiWIYbLkY-oMkYfm1NjTAKGNrVRvO2otSij7zxq7aiAmIKRo-chA8RiFgqaGBkngdVdycBuFLiXDbCeWbzenSrUbL7-3P6FZ_qmuX5vxQTN0EN8" />
              </div>
              <span class="social-text">{{ $t('hero.trustedBy') || 'Trusted by 1M+ travelers worldwide' }}</span>
            </div>
          </div>
          <div class="hero-right">
            <div class="hero-visual" :class="{ 'hero-visual--empty': !heroImageUrl }">
              <img
                v-if="heroImageUrl"
                class="hero-photo"
                :src="heroImageUrl"
                alt="Traveler using phone"
              />
              <template v-if="heroImageUrl">
                <div class="hero-photo-overlay"></div>
                <div class="float-5g">
                  <div class="float-5g-icon">5G</div>
                  <div class="float-5g-text">
                    <p class="float-5g-title">5G Network Ready</p>
                    <p class="float-5g-sub">Instant activation</p>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>
      </section>

      <HomeProductMarquee v-if="marqueeProducts.length" :products="marqueeProducts" />

      <div class="site-section-divider" aria-hidden="true" />

      <!-- Trending Destinations (Bento Grid) -->
      <section class="destinations">
        <div class="container">
          <div class="destinations-header">
            <div>
              <h2 class="section-title">{{ $t('regions.title') }}</h2>
              <p class="section-subtitle">{{ $t('regions.subtitle') }}</p>
            </div>
            <router-link to="/products" class="view-all-link view-all-desktop">
              {{ $t('featured.viewAll') }}
              <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/></svg>
            </router-link>
          </div>
          <div class="bento-grid">
            <!-- 第一个产品（大图卡片） -->
            <router-link to="/products" class="bento-card bento-large bento-usa">
              <img v-if="products.length > 0 && getProductImage(products[0])" class="bento-bg-img" :src="getProductImage(products[0])" :alt="getProductName(products[0])" />
              <div class="bento-overlay"></div>
              <div class="bento-content">
                <div class="bento-footer">
                  <div>
                    <h3 class="bento-country">{{ products.length > 0 ? getProductName(products[0]) : 'United States' }}</h3>
                    <p class="bento-price">{{ products.length > 0 ? 'From ' + getProductPrice(products[0]) : 'From $4.50' }} &middot; Local Plan</p>
                  </div>
                  <div class="bento-arrow-btn">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M5 12h14M12 5l7 7-7 7" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </div>
                </div>
              </div>
            </router-link>
            <!-- 第二个产品（小图卡片） -->
            <router-link to="/products" class="bento-card bento-photo bento-japan">
              <img v-if="products.length > 1 && getProductImage(products[1])" class="bento-bg-img" :src="getProductImage(products[1])" :alt="getProductName(products[1])" />
              <div class="bento-overlay"></div>
              <div class="bento-content">
                <div class="bento-footer">
                  <div>
                    <h3 class="bento-country">{{ products.length > 1 ? getProductName(products[1]) : 'Japan' }}</h3>
                    <p class="bento-price">{{ products.length > 1 ? 'From ' + getProductPrice(products[1]) : 'From $5.00' }}</p>
                  </div>
                </div>
              </div>
            </router-link>
            <!-- 第三个卡片（信息卡，写死文案，中英文） -->
            <div class="bento-card bento-info" @click="$router.push('/products')">
              <div>
                <span class="plan-chip plan-chip--regional">{{ $t('home.bentoThird.planChip') }}</span>
                <h3 class="bento-info-title">{{ $t('home.bentoThird.title') }}</h3>
                <p class="bento-info-desc">{{ $t('home.bentoThird.desc') }}</p>
                <div class="flag-row">
                  <span role="img" aria-label="France">&#127467;&#127479;</span>
                  <span role="img" aria-label="Germany">&#127465;&#127466;</span>
                  <span role="img" aria-label="Italy">&#127470;&#127481;</span>
                  <span role="img" aria-label="Spain">&#127466;&#127480;</span>
                  <span class="flag-more">{{ $t('home.bentoThird.moreCountries') }}</span>
                </div>
              </div>
              <div class="bento-info-footer">
                <span class="bento-info-price">{{ $t('home.bentoThird.price') }}</span>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M5 12h14M12 5l7 7-7 7" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </div>
            </div>
          </div>
          <router-link to="/products" class="view-all-mobile">
            {{ $t('regions.viewAll') || 'View All Destinations' }}
          </router-link>
          <HomeFeatureHighlights />
        </div>
      </section>

      <div class="site-section-divider" aria-hidden="true" />

      <HomeHowItWorks />
      <HomeTestimonials />
      <HomeFooterCta />

      <div class="site-section-divider" aria-hidden="true" />

      <section class="ecosystem-section">
        <div class="container">
          <div class="section-header">
            <h2 class="section-title">{{ $t('ecosystem.title') }}</h2>
            <p class="section-subtitle">{{ $t('ecosystem.subtitle') }}</p>
          </div>
          <div class="ecosystem-grid">
            <a href="https://www.mocstack.com" target="_blank" rel="noopener noreferrer" class="eco-card">
              <div class="eco-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <rect x="2" y="3" width="20" height="5" rx="1.5"/>
                  <rect x="2" y="10" width="20" height="5" rx="1.5"/>
                  <rect x="2" y="17" width="20" height="4" rx="1.5"/>
                </svg>
              </div>
              <span class="eco-category">{{ $t('ecosystem.stackCategory') }}</span>
              <h3>MocStack</h3>
              <p>{{ $t('ecosystem.stackDesc') }}</p>
              <span class="eco-link">
                {{ $t('ecosystem.visit') }} MocStack
                <svg viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M2 12L12 2M12 2H7M12 2v5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </a>
            <a href="https://moccards.com/" target="_blank" rel="noopener noreferrer" class="eco-card eco-card--featured">
              <div class="eco-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <rect x="2" y="5" width="20" height="14" rx="2"/>
                  <path d="M2 10h20"/>
                  <path d="M6 15h4"/>
                </svg>
              </div>
              <span class="eco-category">{{ $t('ecosystem.cardsCategory') }}</span>
              <h3>MocCards</h3>
              <p>{{ $t('ecosystem.cardsDesc') }}</p>
              <span class="eco-link">
                {{ $t('ecosystem.visit') }} MocCards
                <svg viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M2 12L12 2M12 2H7M12 2v5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </a>
            <a href="https://mocdocs.com/" target="_blank" rel="noopener noreferrer" class="eco-card">
              <div class="eco-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <path d="M14 2v6h6"/>
                  <path d="M8 13h8M8 17h8M8 9h2"/>
                </svg>
              </div>
              <span class="eco-category">{{ $t('ecosystem.docsCategory') }}</span>
              <h3>MocDocs</h3>
              <p>{{ $t('ecosystem.docsDesc') }}</p>
              <span class="eco-link">
                {{ $t('ecosystem.visit') }} MocDocs
                <svg viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M2 12L12 2M12 2H7M12 2v5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </a>
          </div>
        </div>
      </section>

      <!-- Footer -->
      <SiteFooter />
    </main>

    <!-- Mobile Bottom Navigation -->
    <nav class="bottom-nav" aria-label="Mobile navigation">
      <router-link to="/" class="bottom-nav-item" :class="{ 'bottom-nav-active': $route.path === '/' }">
        <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
        <span>{{ $t('nav.home') }}</span>
      </router-link>
      <router-link to="/products" class="bottom-nav-item" :class="{ 'bottom-nav-active': $route.path.startsWith('/product') }">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22"><rect x="5" y="2" width="14" height="20" rx="2"/><path d="M12 18h.01"/></svg>
        <span>{{ $t('nav.products') }}</span>
      </router-link>
      <router-link to="/tickets" class="bottom-nav-item" :class="{ 'bottom-nav-active': $route.path.startsWith('/ticket') }">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        <span>{{ $t('nav.tickets') }}</span>
      </router-link>
      <router-link to="/profile" class="bottom-nav-item" :class="{ 'bottom-nav-active': $route.path === '/profile' }">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        <span>{{ $t('nav.profile') || 'Profile' }}</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import HomeProductMarquee from '@/components/home/HomeProductMarquee.vue'
import HomeHowItWorks from '@/components/home/HomeHowItWorks.vue'
import HomeTestimonials from '@/components/home/HomeTestimonials.vue'
import HomeFooterCta from '@/components/home/HomeFooterCta.vue'
import HomeFeatureHighlights from '@/components/home/HomeFeatureHighlights.vue'
import { productApi, type Product } from '@/api/product'
import { useSiteConfigStore } from '@/stores/siteConfig'

const router = useRouter()
const route = useRoute()
const { locale } = useI18n()
const siteConfig = useSiteConfigStore()
const searchQuery = ref('')
const products = ref<Product[]>([])
const marqueeProducts = ref<Product[]>([])
const pageLoading = ref(true)
/** 启动时 main.ts 已拉取过展示配置，避免先闪默认图再换成后台图 */
const heroImageUrl = computed(() => siteConfig.homeHeroImageUrl)

onMounted(async () => {
  try {
    const [productRes, marqueeRes] = await Promise.all([
      productApi.getProducts(undefined, 0, 2, 1),
      productApi.getProducts(undefined, 0, 16, 1)
    ])
    products.value = parseProductList(productRes)
    marqueeProducts.value = parseProductList(marqueeRes)
  } catch (e) {
    // 静默失败，保持空数组
  } finally {
    pageLoading.value = false
  }
})

function parseProductList(res: { code?: number; data?: unknown } | null): Product[] {
  if (res?.code !== 200 || !res.data) return []
  const data = res.data as Record<string, unknown>
  if (Array.isArray(data)) return data as Product[]
  if (Array.isArray(data.content)) return data.content as Product[]
  if (Array.isArray(data.products)) return data.products as Product[]
  return []
}

function getProductName(p: Product): string {
  return locale.value === 'zh' ? p.name : (p.nameEn || p.name)
}

function getProductPrice(p: Product): string {
  return p.price != null ? `$${p.price}` : ''
}

function getProductImage(p: Product): string {
  return p.displayImageUrl || p.imageUrl || ''
}

function handleSearch() {
  const q = searchQuery.value.trim()
  if (q) {
    router.push(`/products?search=${encodeURIComponent(q)}`)
  } else {
    router.push('/products')
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@600;700&family=Manrope:wght@400;500;700&display=swap');

/* ============================================================
   BASE
   ============================================================ */
.home-page {
  /* 首页内容区略窄于个人中心/商品页，大屏占比更克制 */
  --site-content-max-width: var(--site-home-max-width, 1080px);
  min-height: 100vh;
  background: var(--background);
  color: var(--on-background);
  font-family: var(--font-body);
  padding-bottom: 64px; /* bottom nav on mobile */
}

@media (min-width: 768px) {
  .home-page { padding-bottom: 0; }
}

/* ============================================================
   HERO SECTION
   ============================================================ */
.hero {
  padding: 80px 0;
  overflow: hidden;
  position: relative;
}

.hero-inner {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  align-items: center;
}

/* Left side */
.hero-left {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-label {
  display: inline-block;
  color: #0059b5;
  font-family: var(--font-body);
  font-size: 14px;
  font-weight: 700;
  line-height: 20px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-title {
  font-family: var(--font-headline);
  font-size: 48px;
  font-weight: 700;
  line-height: 1.167;
  letter-spacing: -0.02em;
  color: var(--on-background);
  margin: 0;
}

.hero-title-accent {
  color: #0059b5;
}

.hero-desc {
  font-size: 18px;
  line-height: 1.556;
  color: var(--on-surface-variant);
  max-width: 480px;
  margin: 0;
}

/* Search Bar */
.search-bar {
  margin-top: 8px;
  background: var(--surface-container-lowest);
  padding: 8px;
  border-radius: var(--radius-full);
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  border: 1px solid var(--surface-variant);
  max-width: 480px;
}

.search-icon {
  width: 20px;
  height: 20px;
  margin: 0 16px;
  color: var(--outline);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  font-family: var(--font-body);
  color: var(--on-background);
  padding: 8px;
}

.search-input::placeholder {
  color: var(--outline);
}

.search-btn {
  background: #0059b5;
  color: var(--on-primary);
  border: none;
  border-radius: var(--radius-full);
  padding: 12px 24px;
  font-family: var(--font-body);
  font-size: 14px;
  font-weight: 700;
  line-height: 20px;
  cursor: pointer;
  text-decoration: none;
  white-space: nowrap;
  transition: transform 0.2s, background 0.2s;
  display: inline-block;
}

.search-btn:hover {
  background: #0071e3;
  transform: scale(1.02);
}

/* Social Proof */
.social-proof {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.avatar-stack {
  display: flex;
  margin: 0 -8px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  border: 2px solid var(--surface-container-lowest);
  margin-left: 0;
  margin-right: -8px;
  object-fit: cover;
  display: block;
}

.avatar:first-child { margin-left: 0; }
.avatar:last-child { margin-right: 0; }

.social-text {
  font-size: 12px;
  font-weight: 500;
  line-height: 16px;
  color: var(--on-surface-variant);
}

/* Right side: Hero Visual */
.hero-right {
  display: flex;
  justify-content: flex-end;
}

.hero-visual {
  position: relative;
  width: 100%;
  max-width: 520px;
  margin-left: auto;
  aspect-ratio: 4 / 5;
  border-radius: 32px;
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}

.hero-visual--empty {
  box-shadow: none;
  background: transparent;
}

.hero-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.hero-photo-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.4) 0%, transparent 60%);
}

/* Center card */
.visual-card-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255,255,255,0.12);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255,255,255,0.25);
  border-radius: 24px;
  padding: 24px;
  width: 82%;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}

.vc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.vc-logo {
  font-family: var(--font-headline);
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.02em;
}

.vc-chip {
  background: rgba(255,255,255,0.2);
  color: #ffffff;
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: var(--radius-full);
  border: 1px solid rgba(255,255,255,0.3);
}

.vc-sim {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255,255,255,0.1);
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 16px;
}

.vc-flag { font-size: 24px; }

.vc-sim-label {
  font-size: 13px;
  font-weight: 700;
  color: #ffffff;
}

.vc-sim-status {
  font-size: 11px;
  color: rgba(255,255,255,0.75);
  margin-top: 2px;
}

.vc-data {
  margin-bottom: 16px;
}

.vc-data-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: rgba(255,255,255,0.85);
  margin-bottom: 8px;
}

.vc-progress-bar {
  height: 6px;
  background: rgba(255,255,255,0.2);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.vc-progress-fill {
  height: 100%;
  width: 57%;
  background: linear-gradient(90deg, #ffffff 0%, rgba(255,255,255,0.7) 100%);
  border-radius: var(--radius-full);
}

.vc-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.vc-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.vc-stat-n {
  font-family: var(--font-headline);
  font-size: 16px;
  font-weight: 700;
  color: #ffffff;
}

.vc-stat-l {
  font-size: 10px;
  color: rgba(255,255,255,0.7);
  margin-top: 2px;
  text-align: center;
}

.vc-stat-divider {
  width: 1px;
  height: 28px;
  background: rgba(255,255,255,0.2);
}

/* Floating 5G Badge */
.float-5g {
  position: absolute;
  bottom: 32px;
  left: 32px;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 12px 16px;
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(255,255,255,0.5);
  display: flex;
  align-items: center;
  gap: 12px;
}

.float-5g-icon {
  width: 44px;
  height: 44px;
  background: #0071e3;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-family: var(--font-headline);
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.float-5g-icon svg { width: 28px; height: 28px; fill: #ffffff; }

.float-5g-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--on-background);
  margin: 0;
  line-height: 1.4;
}

.float-5g-sub {
  font-size: 11px;
  color: var(--on-surface-variant);
  margin: 0;
  line-height: 1.4;
}

/* ============================================================
   DESTINATIONS (BENTO GRID)
   ============================================================ */
.destinations {
  padding: 80px 0;
  background: var(--background);
}

.destinations-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
}

.section-title {
  font-family: var(--font-headline);
  font-size: 32px;
  font-weight: 700;
  line-height: 1.25;
  letter-spacing: -0.01em;
  color: var(--on-background);
  margin: 0 0 8px 0;
}

.section-subtitle {
  font-size: 16px;
  line-height: 1.5;
  color: var(--on-surface-variant);
  margin: 0;
}

.view-all-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 700;
  color: #0059b5;
  text-decoration: none;
  transition: text-decoration 0.2s;
}

.view-all-link:hover { text-decoration: underline; }

.view-all-desktop { display: none; }
@media (min-width: 768px) { .view-all-desktop { display: flex; } }

/* Bento Grid */
.bento-grid {
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: auto;
  gap: 16px;
}

@media (min-width: 768px) {
  .bento-grid {
    grid-template-columns: repeat(3, 1fr);
    grid-auto-rows: 250px;
  }
}

.bento-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.3s, transform 0.3s;
  text-decoration: none;
  display: block;
  min-height: 200px;
}

.bento-card:hover {
  box-shadow: var(--shadow-xl);
  transform: translateY(-2px);
}

/* Large card spans 2 cols & 2 rows */
.bento-large {
  background: linear-gradient(135deg, #1a3c6e 0%, #0059b5 50%, #0071e3 100%);
  min-height: 300px;
}

@media (min-width: 768px) {
  .bento-large {
    grid-column: span 2;
    grid-row: span 2;
    min-height: unset;
  }
}

/* Photo card for Japan */
.bento-photo {
  background: linear-gradient(135deg, #b91c4c 0%, #dc2626 40%, #f97316 100%);
  min-height: 200px;
}

/* Image-based background placeholders */
.bento-usa { background: linear-gradient(150deg, #0f2d5e 0%, #1a4f9f 40%, #2d6fd1 70%, #4a8fe8 100%); }
.bento-japan { background: linear-gradient(145deg, #7f1d1d 0%, #b91c4c 50%, #e11d48 80%, #fb7185 100%); }

/* Bento card background image */
.bento-bg-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.bento-card:hover .bento-bg-img {
  transform: scale(1.05);
}

.bento-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.75) 0%, rgba(0,0,0,0.15) 50%, transparent 100%);
}

.bento-content {
  position: absolute;
  inset: 0;
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.bento-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.bento-country {
  font-family: var(--font-headline);
  font-size: 22px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 4px 0;
}

.bento-price {
  font-size: 14px;
  color: rgba(255,255,255,0.8);
  margin: 0;
}

.bento-arrow-btn {
  width: 40px;
  height: 40px;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(8px);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  flex-shrink: 0;
  transition: background 0.2s;
}

.bento-card:hover .bento-arrow-btn {
  background: #0059b5;
}

/* Info card (Europe) */
.bento-info {
  background: var(--surface-container-lowest);
  border: 1px solid var(--outline-variant);
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  cursor: pointer;
}

.bento-info-title {
  font-family: var(--font-headline);
  font-size: 22px;
  font-weight: 600;
  color: var(--on-background);
  margin: 8px 0;
}

.bento-info-desc {
  font-size: 16px;
  color: var(--on-surface-variant);
  margin: 0 0 16px;
}

.flag-row {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 22px;
}

.flag-more {
  font-size: 16px;
  color: var(--outline);
}

.bento-info-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--surface-variant);
  padding-top: 16px;
  margin-top: 8px;
  color: var(--on-surface-variant);
}

.bento-info-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--on-background);
}

/* Plan chips */
.plan-chip {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  line-height: 16px;
}

.plan-chip--bestseller {
  background: rgba(0,89,181,0.9);
  color: #ffffff;
  border: 1px solid rgba(255,255,255,0.2);
  backdrop-filter: blur(4px);
}

.plan-chip--regional {
  background: rgba(0,92,187,0.1);
  color: #0059b5;
}

/* Mobile view all */
.view-all-mobile {
  display: block;
  text-align: center;
  margin-top: 16px;
  padding: 12px;
  border: 1px solid #0059b5;
  color: #0059b5;
  border-radius: var(--radius-full);
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
  transition: background 0.2s;
}

.view-all-mobile:hover { background: rgba(0,89,181,0.06); }

@media (min-width: 768px) { .view-all-mobile { display: none; } }

/* ============================================================
   FEATURES SECTION
   ============================================================ */
.features-section {
  padding: 80px 0;
  background: var(--background);
}

.features-section .section-title { text-align: center; }
.features-section .section-subtitle {
  text-align: center;
  margin-bottom: 48px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

@media (min-width: 768px) {
  .features-grid { grid-template-columns: repeat(4, 1fr); }
}

.feature-card {
  background: var(--surface-container-lowest);
  border: 1px solid var(--outline-variant);
  border-radius: var(--radius-xl);
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: box-shadow 0.25s, transform 0.25s;
}

.feature-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.feature-icon {
  font-size: 32px;
  line-height: 1;
}

.feature-card h3 {
  font-family: var(--font-headline);
  font-size: 18px;
  font-weight: 600;
  color: var(--on-background);
  margin: 0;
}

.feature-card p {
  font-size: 14px;
  line-height: 1.6;
  color: var(--on-surface-variant);
  margin: 0;
}

/* ============================================================
   ECOSYSTEM SECTION (综合数字服务)
   ============================================================ */
.ecosystem-section {
  padding: 80px 0;
  background: var(--background);
}

.section-header {
  margin-bottom: 40px;
}

.ecosystem-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

@media (min-width: 768px) {
  .ecosystem-grid { grid-template-columns: repeat(3, 1fr); }
}

.eco-card {
  background: var(--surface-container-lowest);
  border: 1px solid var(--outline-variant);
  border-radius: var(--radius-xl);
  padding: 32px 28px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-decoration: none;
  transition: box-shadow 0.25s, transform 0.25s, border-color 0.25s;
  box-shadow: var(--shadow-md);
}

.eco-card:hover {
  box-shadow: var(--shadow-lg);
  border-color: #0059b5;
  transform: translateY(-3px);
}

.eco-card--featured {
  background: linear-gradient(145deg, #0059b5 0%, #0071e3 100%);
  border-color: transparent;
}

.eco-card--featured .eco-category,
.eco-card--featured h3,
.eco-card--featured p,
.eco-card--featured .eco-link {
  color: rgba(255,255,255,0.9) !important;
}

.eco-card--featured .eco-icon {
  background: rgba(255,255,255,0.15);
  border-color: rgba(255,255,255,0.25);
  color: #ffffff;
}

.eco-icon {
  width: 52px;
  height: 52px;
  background: rgba(0,89,181,0.08);
  border: 1px solid var(--primary-fixed);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0059b5;
  margin-bottom: 8px;
}

.eco-icon svg { width: 26px; height: 26px; }

.eco-category {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #0059b5;
}

.eco-card h3 {
  font-family: var(--font-headline);
  font-size: 20px;
  font-weight: 600;
  color: var(--on-background);
  margin: 0;
}

.eco-card p {
  font-size: 14px;
  line-height: 1.6;
  color: var(--on-surface-variant);
  margin: 0;
  flex: 1;
}

.eco-link {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #0059b5;
  margin-top: 8px;
}

.eco-link svg { width: 14px; height: 14px; }

/* ============================================================
   MOBILE BOTTOM NAVIGATION
   ============================================================ */
.bottom-nav {
  display: flex;
  justify-content: space-around;
  align-items: center;
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 64px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--outline-variant);
  box-shadow: 0 -4px 12px rgba(0,0,0,0.05);
  z-index: 50;
}

@media (min-width: 768px) { .bottom-nav { display: none; } }

.bottom-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 25%;
  padding: 8px 0;
  color: var(--on-surface-variant);
  text-decoration: none;
  font-size: 11px;
  font-weight: 500;
  gap: 4px;
  border-radius: var(--radius);
  transition: color 0.2s;
}

.bottom-nav-item:hover,
.bottom-nav-active {
  color: #0059b5 !important;
  font-weight: 700;
}

.bottom-nav-item svg { transition: fill 0.2s, stroke 0.2s; }

/* ============================================================
   RESPONSIVE
   ============================================================ */
@media (max-width: 1023px) {
  .hero-inner {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  .hero-title { font-size: 36px; }
  .hero-right {
    order: -1;
    justify-content: center;
  }
  .hero-visual {
    max-width: 100%;
    margin-left: 0;
    aspect-ratio: 16/9;
  }
  .visual-card-center { width: 75%; }
}

@media (max-width: 767px) {
  .hero { padding: 40px 0; }
  .hero-title { font-size: 30px; }
  .destinations { padding: 48px 0; }
  .features-section { padding: 48px 0; }
  .ecosystem-section { padding: 48px 0; }
  .section-title { font-size: 26px; }
}
</style>

