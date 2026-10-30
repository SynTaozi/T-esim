<template>
  <div class="disclaimer-page">
    <SiteHeader />

    <div class="page-hero">
      <div class="hero-bg"></div>
      <div class="container">
        <div class="breadcrumb">
          <router-link to="/">{{ $t('orderDetail.home') }}</router-link>
          <span class="sep">›</span>
          <span>{{ $t('footer.disclaimer') }}</span>
        </div>
        <h1>{{ $t('disclaimer.title') }}</h1>
        <p class="last-updated">{{ $t('disclaimer.lastUpdated') }}: {{ $t('disclaimer.lastUpdatedDate') }}</p>
      </div>
    </div>

    <div class="container main-content">
      <div class="policy-card">
        <!-- Important Notice -->
        <section class="important-notice">
          <p>{{ $t('disclaimer.importantNotice') }}</p>
        </section>

        <!-- Section 1 -->
        <section class="policy-section">
          <h2>{{ $t('disclaimer.section1Title') }}</h2>
          <p>{{ $t('disclaimer.section1Content') }}</p>
        </section>

        <!-- Section 2 -->
        <section class="policy-section">
          <h2>{{ $t('disclaimer.section2Title') }}</h2>
          <p>{{ $t('disclaimer.section2Content') }}</p>
          <p>{{ $t('disclaimer.section2Content2') }}</p>
        </section>

        <!-- Section 3 -->
        <section class="policy-section">
          <h2>{{ $t('disclaimer.section3Title') }}</h2>
          <p>{{ $t('disclaimer.section3Content') }}</p>
        </section>

        <!-- Section 4 -->
        <section class="policy-section">
          <h2>{{ $t('disclaimer.section4Title') }}</h2>
          <p>{{ $t('disclaimer.section4Content') }}</p>
        </section>

        <!-- Section 5 -->
        <section class="policy-section">
          <h2>{{ $t('disclaimer.section5Title') }}</h2>
          <p>{{ $t('disclaimer.section5Content') }}</p>
        </section>

        <!-- Section 6 -->
        <section class="policy-section">
          <h2>{{ $t('disclaimer.section6Title') }}</h2>
          <p>{{ $t('disclaimer.section6Content') }}</p>
        </section>

        <!-- Section 7 -->
        <section class="policy-section">
          <h2>{{ $t('disclaimer.section7Title') }}</h2>
          <p>{{ $t('disclaimer.section7Content') }}</p>
        </section>

        <!-- Section 8 -->
        <section class="policy-section">
          <h2>{{ $t('disclaimer.section8Title') }}</h2>
          <p>{{ $t('disclaimer.section8Content') }}</p>
          <div class="contact-info">
            <p>{{ $t('disclaimer.section8Email', { email: 'support@tesim.com' }) }}</p>
            <p>{{ $t('disclaimer.section8Address') }}</p>
          </div>
        </section>

        <!-- Footer Links -->
        <section class="footer-links-section">
          <div class="links-group">
            <router-link to="/privacy" class="footer-link">{{ $t('disclaimer.privacyPolicy') }}</router-link>
            <span class="separator">•</span>
            <router-link to="/terms" class="footer-link">{{ $t('disclaimer.termsOfService') }}</router-link>
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
.disclaimer-page {
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
  background: rgba(255, 193, 7, 0.1);
  border-left: 4px solid #ffc107;
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

.contact-info {
  background: var(--bg-primary);
  padding: 16px 20px;
  border-radius: 8px;
  margin-top: 12px;
}

.contact-info p {
  margin: 6px 0;
  color: var(--text-primary);
  font-size: 15px;
}

.footer-links-section {
  border-top: 1px solid var(--border-default);
  padding-top: 24px;
  margin-top: 32px;
  text-align: center;
}

.links-group {
  display: flex;
  justify-content: center;
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
  text-decoration: underline;
}

.separator {
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .policy-card {
    padding: 32px 24px;
  }

  .important-notice {
    padding: 16px 20px;
  }

  .policy-section {
    margin-bottom: 24px;
  }

  .policy-section h2 {
    font-size: 16px;
  }

  .policy-section p {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .page-hero {
    padding: 60px 0 40px;
  }

  .policy-card {
    padding: 24px 16px;
  }

  .policy-section {
    margin-bottom: 20px;
  }

  .links-group {
    gap: 8px;
  }
}
</style>
