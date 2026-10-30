<template>
  <div class="refund-policy-page">
    <SiteHeader />

    <div class="page-hero">
      <div class="hero-bg"></div>
      <div class="container">
        <div class="breadcrumb">
          <router-link to="/">{{ $t('orderDetail.home') }}</router-link>
          <span class="sep">›</span>
          <span>{{ $t('footer.refundPolicy') }}</span>
        </div>
        <h1>{{ $t('refundPolicy.title') }}</h1>
        <p class="last-updated">{{ $t('refundPolicy.lastUpdated') }}: {{ $t('refundPolicy.lastUpdatedDate') }}</p>
      </div>
    </div>

    <div class="container main-content">
      <div class="policy-card">
        <!-- Core Principle -->
        <section class="important-notice">
          <p>{{ $t('refundPolicy.coreNotice') }}</p>
        </section>

        <!-- Section 1 -->
        <section class="policy-section">
          <h2>{{ $t('refundPolicy.section1Title') }}</h2>
          <p>{{ $t('refundPolicy.section1Content') }}</p>
          <div class="warning-box">
            <p>{{ $t('refundPolicy.section1Warning') }}</p>
          </div>
        </section>

        <!-- Section 2 -->
        <section class="policy-section">
          <h2>{{ $t('refundPolicy.section2Title') }}</h2>
          <p>{{ $t('refundPolicy.section2Intro') }}</p>
          <ul class="policy-list">
            <li>
              <strong>{{ $t('refundPolicy.section2Item1Title') }}</strong>
              {{ $t('refundPolicy.section2Item1Desc') }}
            </li>
            <li>
              <strong>{{ $t('refundPolicy.section2Item2Title') }}</strong>
              {{ $t('refundPolicy.section2Item2Desc') }}
            </li>
          </ul>
        </section>

        <!-- Section 3 -->
        <section class="policy-section">
          <h2>{{ $t('refundPolicy.section3Title') }}</h2>
          <ul class="policy-list">
            <li>{{ $t('refundPolicy.section3Item1') }}</li>
            <li>{{ $t('refundPolicy.section3Item2') }}</li>
            <li>{{ $t('refundPolicy.section3Item3') }}</li>
            <li>{{ $t('refundPolicy.section3Item4') }}</li>
            <li>{{ $t('refundPolicy.section3Item5') }}</li>
            <li>{{ $t('refundPolicy.section3Item6') }}</li>
            <li>{{ $t('refundPolicy.section3Item7') }}</li>
          </ul>
        </section>

        <!-- Section 4 -->
        <section class="policy-section">
          <h2>{{ $t('refundPolicy.section4Title') }}</h2>
          <p>{{ $t('refundPolicy.section4Content') }}</p>
          <div class="contact-info">
            <router-link to="/ticket/new" class="ticket-link">{{ $t('refundPolicy.submitTicket') }}</router-link>
          </div>
        </section>

        <!-- Footer Links -->
        <section class="footer-links-section">
          <div class="links-group">
            <router-link to="/privacy" class="footer-link">{{ $t('footer.privacy') }}</router-link>
            <span class="separator">•</span>
            <router-link to="/terms" class="footer-link">{{ $t('footer.terms') }}</router-link>
            <span class="separator">•</span>
            <router-link to="/disclaimer" class="footer-link">{{ $t('footer.disclaimer') }}</router-link>
          </div>
        </section>
      </div>
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
.refund-policy-page {
  min-height: 100vh;
  background: var(--bg-primary);
}

.page-hero {
  position: relative;
  padding: 80px 0 50px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-subtle);
  overflow: hidden;
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

.page-hero .container {
  position: relative;
  z-index: 1;
  padding-left: 20px;
  padding-right: 20px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.breadcrumb a {
  color: var(--accent-primary);
  text-decoration: none;
  transition: color 0.2s;
}

.breadcrumb a:hover {
  color: var(--text-primary);
}

.sep {
  color: var(--text-muted);
}

.page-hero h1 {
  font-size: clamp(28px, 5vw, 42px);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.last-updated {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

.main-content {
  padding: 60px 20px 80px;
}

.policy-card {
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: 12px;
  padding: 48px 40px;
  max-width: 900px;
  margin: 0 auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.important-notice {
  background: rgba(64, 158, 255, 0.08);
  border-left: 4px solid var(--accent-primary);
  padding: 20px 24px;
  border-radius: 8px;
  margin-bottom: 32px;
}

.important-notice p {
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.7;
  margin: 0;
  font-weight: 500;
}

.warning-box {
  background: rgba(255, 77, 79, 0.08);
  border-left: 4px solid #ff4d4f;
  padding: 16px 20px;
  border-radius: 8px;
  margin-top: 12px;
}

.warning-box p {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.7;
  margin: 0;
  font-weight: 500;
}

.policy-section {
  margin-bottom: 32px;
}

.policy-section:last-child {
  margin-bottom: 24px;
}

.policy-section h2 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px;
  line-height: 1.4;
}

.policy-section p {
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.7;
  margin: 0 0 12px;
}

.policy-section p:last-child {
  margin-bottom: 0;
}

.policy-list {
  padding-left: 20px;
  margin: 12px 0 0;
}

.policy-list li {
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.7;
  margin-bottom: 10px;
}

.policy-list li strong {
  color: var(--text-primary);
  font-weight: 600;
}

.contact-info {
  background: var(--bg-primary);
  padding: 16px 20px;
  border-radius: 8px;
  margin-top: 12px;
}

.ticket-link {
  display: inline-block;
  padding: 10px 20px;
  background: var(--accent-primary);
  color: #fff;
  text-decoration: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  transition: opacity 0.2s;
}

.ticket-link:hover {
  opacity: 0.85;
}

.footer-links-section {
  padding-top: 24px;
  border-top: 1px solid var(--border-subtle);
  margin-top: 16px;
}

.links-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.footer-link {
  color: var(--accent-primary);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.footer-link:hover {
  color: var(--text-primary);
}

.separator {
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .policy-card {
    padding: 28px 20px;
  }
}
</style>
