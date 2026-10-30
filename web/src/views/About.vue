<template>
  <div class="about-page">
    <SiteHeader />

    <!-- Hero -->
    <div class="page-hero">
      <div class="hero-bg"></div>
      <div class="container hero-content">
        <div class="breadcrumb">
          <router-link to="/">{{ $t('orderDetail.home') }}</router-link>
          <span class="sep">›</span>
          <span>{{ $t('nav.about') }}</span>
        </div>
        <h1>{{ $t('about.subtitle') }}</h1>
        <p class="hero-subtitle">{{ $t('about.platformTitle') }}</p>
        <p class="hero-intro">{{ $t('about.intro') }}</p>
      </div>
    </div>

    <div class="container main-content">

      <!-- Stats -->
      <section class="stats-section">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-num">120+</div>
            <div class="stat-label">{{ $t('about.countriesServed') }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-num">{{ $t('about.hours247') }}</div>
            <div class="stat-label">{{ $t('about.globalAccess') }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-num">Global</div>
            <div class="stat-label">{{ $t('about.globalCoverage') }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-num">United States</div>
            <div class="stat-label">{{ $t('about.wyomingReg') }}</div>
          </div>
        </div>
      </section>

      <!-- Who We Are -->
      <section class="section">
        <div class="section-header">
          <h2>{{ $t('about.whoWeAre') }}</h2>
          <p class="section-subtitle">{{ $t('about.whoWeAreSubtitle') }}</p>
        </div>
        <div class="who-we-are-content">
          <div class="text-content">
            <p>{{ $t('about.whoWeAreContent1') }}</p>
            <p>{{ $t('about.whoWeAreContent2') }}</p>
            <p>{{ $t('about.whoWeAreContent3') }}</p>
          </div>
          <div class="cta-box">
            <h3>{{ $t('about.ourServices') }}</h3>
            <router-link to="/contact" class="btn-cta">{{ $t('about.contactUs') }}</router-link>
          </div>
        </div>
      </section>

      <!-- Business Scope -->
      <section class="section">
        <div class="section-header">
          <h2>{{ $t('about.businessScope') }}</h2>
          <p class="section-subtitle">{{ $t('about.ourServiceDomains') }}</p>
        </div>
        <div class="services-grid">
          <div class="service-card">
            <div class="service-icon">🏗️</div>
            <h3>{{ $t('about.service1Title') }}</h3>
            <p>{{ $t('about.service1Desc') }}</p>
          </div>
          <div class="service-card">
            <div class="service-icon">☁️</div>
            <h3>{{ $t('about.service2Title') }}</h3>
            <p>{{ $t('about.service2Desc') }}</p>
          </div>
          <div class="service-card">
            <div class="service-icon">📡</div>
            <h3>{{ $t('about.service3Title') }}</h3>
            <p>{{ $t('about.service3Desc') }}</p>
          </div>
          <div class="service-card">
            <div class="service-icon">✈️</div>
            <h3>{{ $t('about.service4Title') }}</h3>
            <p>{{ $t('about.service4Desc') }}</p>
          </div>
        </div>
      </section>

      <!-- Legal Entity -->
      <section class="section">
        <div class="section-header">
          <h2>{{ $t('about.legalEntity') }}</h2>
          <p class="section-subtitle">{{ $t('about.companyDetails') }}</p>
        </div>
        <div class="legal-card">
          <div class="legal-grid">
            <div class="legal-item">
              <span class="label">{{ $t('about.legalName') }}</span>
              <span class="value">{{ $t('about.legalNameValue') }}</span>
            </div>
            <div class="legal-item">
              <span class="label">{{ $t('about.stateOfIncorporation') }}</span>
              <span class="value">{{ $t('about.stateValue') }}</span>
            </div>
            <div class="legal-item">
              <span class="label">{{ $t('about.phone') }}</span>
              <span class="value"><a href="tel:+12133438884">+1 213-343-8884</a></span>
            </div>
            <div class="legal-item">
              <span class="label">{{ $t('about.email') }}</span>
              <span class="value">
                <a href="mailto:support@tesim.com">support@tesim.com</a><br>
                <a href="mailto:service@tesim.com">service@tesim.com</a>
              </span>
            </div>
            <div class="legal-item full-width">
              <span class="label">{{ $t('about.registeredAddress') }}</span>
              <span class="value">{{ $t('about.addressValue') }}</span>
            </div>
          </div>
        </div>
      </section>
    </div>

         <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
const { t } = useI18n({ useScope: 'global' })

onMounted(() => {
  window.scrollTo(0, 0)
})
</script>

<style scoped>
.about-page {
  min-height: 100vh;
}

/* Hero */
.page-hero {
  position: relative;
  background: transparent;
  padding: 40px 0 60px;
  overflow: hidden;
  border-bottom: none;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(ellipse at 30% 50%, var(--accent-muted) 0%, transparent 60%),
    radial-gradient(ellipse at 70% 50%, var(--accent-muted) 0%, transparent 60%);
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.breadcrumb a {
  color: var(--accent-primary);
  text-decoration: none;
}

.sep {
  color: var(--text-muted);
}

.page-hero h1 {
  font-size: clamp(28px, 5vw, 44px);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.hero-subtitle {
  font-size: 18px;
  color: var(--accent-primary);
  font-weight: 600;
  margin: 0 0 16px;
}

.hero-intro {
  font-size: 16px;
  color: var(--text-secondary);
  max-width: 680px;
  line-height: 1.7;
  margin: 0;
}

/* Stats */
.stats-section {
  padding: 40px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: 12px;
  padding: 28px 20px;
  text-align: center;
}

.stat-num {
  font-size: 28px;
  font-weight: 800;
  color: var(--accent-primary);
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-muted);
}

/* Sections */
.main-content {
  padding: 0 20px 60px;
}

.section {
  margin-bottom: 60px;
}

.section-header {
  margin-bottom: 32px;
}

.section-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.section-subtitle {
  font-size: 15px;
  color: var(--text-muted);
  margin: 0;
}

/* Who We Are */
.who-we-are-content {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 32px;
  align-items: start;
}

.text-content p {
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0 0 16px;
  font-size: 15px;
}

.cta-box {
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: 12px;
  padding: 24px;
  text-align: center;
}

.cta-box h3 {
  font-size: 16px;
  color: var(--text-primary);
  margin: 0 0 16px;
}

.btn-cta {
  display: inline-block;
  background: var(--accent-primary);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  padding: 10px 20px;
  border-radius: 8px;
  text-decoration: none;
  transition: all 0.2s;
}

.btn-cta:hover {
  background: var(--accent-hover);
  transform: translateY(-1px);
}

/* Services Grid */
.services-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.service-card {
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: 12px;
  padding: 24px;
  transition: border-color 0.2s;
}

.service-card:hover {
  border-color: var(--accent-primary);
}

.service-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.service-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.service-card p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

/* Legal */
.legal-card {
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: 12px;
  padding: 32px;
}

.legal-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.legal-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.legal-item.full-width {
  grid-column: 1 / -1;
}

.legal-item .label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.legal-item .value {
  font-size: 15px;
  color: var(--text-primary);
  line-height: 1.6;
}

.legal-item .value a {
  color: var(--accent-primary);
  text-decoration: none;
}

.legal-item .value a:hover {
  text-decoration: underline;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .who-we-are-content {
    grid-template-columns: 1fr;
  }

  .services-grid {
    grid-template-columns: 1fr;
  }

  .legal-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
