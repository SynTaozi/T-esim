<template>
  <div class="contact-page">
    <SiteHeader />

    <!-- Hero -->
    <div class="page-hero">
      <div class="hero-bg"></div>
      <div class="container hero-content">
        <div class="breadcrumb">
          <router-link to="/">{{ $t('orderDetail.home') }}</router-link>
          <span class="sep">›</span>
          <span>{{ $t('nav.contactUs') }}</span>
        </div>
        <h1>{{ $t('contact.title') }}</h1>
        <p class="hero-subtitle">{{ $t('contact.subtitle') }}</p>
      </div>
    </div>

    <div class="container main-content">

      <!-- Contact Cards -->
      <section class="contact-section">
        <div class="contact-grid">
          <!-- QQ Group -->
          <div class="contact-card">
            <div class="contact-icon-wrap">🐧</div>
            <h3>{{ $t('contact.qqGroup') }}</h3>
            <div class="contact-values">
              <span>{{ $t('contact.qqGroupId') }}</span>
              <a
                href="https://qm.qq.com/q/y7fG8uG4xy"
                target="_blank"
                rel="noopener noreferrer"
              >
                {{ $t('contact.qqJoin') }}
              </a>
            </div>
          </div>
          <!-- Email -->
          <div class="contact-card">
            <div class="contact-icon-wrap">📧</div>
            <h3>{{ $t('contact.emailLabel') }}</h3>
            <div class="contact-values">
              <a href="mailto:support@tesim.com">support@tesim.com</a>
              <a href="mailto:service@tesim.com">service@tesim.com</a>
            </div>
          </div>
          <!-- Phone -->
          <div class="contact-card">
            <div class="contact-icon-wrap">📞</div>
            <h3>{{ $t('contact.phoneLabel') }}</h3>
            <div class="contact-values">
              <a href="tel:+12133438884">+1 213-343-8884</a>
            </div>
          </div>
          <!-- Address -->
          <div class="contact-card">
            <div class="contact-icon-wrap">📍</div>
            <h3>{{ $t('contact.addressLabel') }}</h3>
            <div class="contact-values">
              <span>{{ $t('contact.addressValue') }}</span>
            </div>
          </div>
          <!-- Telegram Group -->
          <div class="contact-card">
            <div class="contact-icon-wrap">✈️</div>
            <h3>{{ $t('contact.telegramGroup') }}</h3>
            <div class="contact-values">
              <a
                href="https://t.me/+t7btxMNFgUhiYjMx"
                target="_blank"
                rel="noopener noreferrer"
              >
                {{ $t('contact.telegramJoin') }}
              </a>
            </div>
          </div>
        </div>
      </section>

      <!-- Support Ticket CTA -->
      <section class="ticket-section">
        <div class="ticket-card">
          <div class="ticket-icon">🎫</div>
          <div class="ticket-info">
            <h2>{{ $t('contact.submitTicket') }}</h2>
            <p>{{ $t('contact.submitTicketDesc') }}</p>
          </div>
          <div class="ticket-action">
            <router-link v-if="authStore.isLoggedIn" to="/ticket/new" class="btn-primary">
              {{ $t('contact.submitTicket') }}
            </router-link>
            <router-link v-else to="/login" class="btn-primary">
              {{ $t('nav.login') }}
            </router-link>
          </div>
        </div>
      </section>

      <!-- Company Info -->
      <section class="company-section">
        <div class="section-header">
          <h2>{{ $t('contact.companyInfo') }}</h2>
        </div>
        <div class="company-card">
          <div class="company-grid">
            <div class="info-item">
              <span class="info-label">{{ $t('contact.legalName') }}</span>
              <span class="info-value">{{ $t('contact.legalNameValue') }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ $t('contact.registration') }}</span>
              <span class="info-value">{{ $t('contact.registrationValue') }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">{{ $t('contact.address') }}</span>
              <span class="info-value">{{ $t('contact.addressValue') }}</span>
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
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n({ useScope: 'global' })
const authStore = useAuthStore()

onMounted(() => {
  window.scrollTo(0, 0)
})
</script>

<style scoped>
.contact-page {
  min-height: 100vh;
  background: var(--bg-primary);
}

/* Hero — 与 Refund Policy 等页面一致 */
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

.hero-content {
  position: relative;
  z-index: 1;
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
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  max-width: 640px;
  line-height: 1.6;
}

/* Main */
.main-content {
  padding: 40px 20px 80px;
}

/* Contact Cards */
.contact-section {
  margin-bottom: 40px;
}

.contact-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.contact-card {
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  transition: border-color 0.2s;
}

.contact-card:hover {
  border-color: var(--accent-primary);
}

.contact-icon-wrap {
  font-size: 32px;
  margin-bottom: 12px;
}

.contact-card h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0 0 12px;
}

.contact-values {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.contact-values a,
.contact-values span {
  font-size: 14px;
  color: var(--text-primary);
  text-decoration: none;
  line-height: 1.5;
}

.contact-values a {
  color: var(--accent-primary);
}

.contact-values a:hover {
  text-decoration: underline;
}

/* Ticket CTA */
.ticket-section {
  margin-bottom: 40px;
}

.ticket-card {
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: 12px;
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.ticket-icon {
  font-size: 48px;
  flex-shrink: 0;
}

.ticket-info {
  flex: 1;
}

.ticket-info h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.ticket-info p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
}

.ticket-action {
  flex-shrink: 0;
}

.btn-primary {
  display: inline-block;
  background: var(--accent-primary);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  padding: 12px 24px;
  border-radius: 8px;
  text-decoration: none;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-primary:hover {
  background: var(--accent-hover);
  transform: translateY(-1px);
}

/* Company Info */
.company-section {
  margin-bottom: 40px;
}

.section-header {
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.company-card {
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: 12px;
  padding: 32px;
}

.company-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-value {
  font-size: 15px;
  color: var(--text-primary);
  line-height: 1.6;
}

@media (max-width: 1100px) {
  .contact-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .contact-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .ticket-card {
    flex-direction: column;
    text-align: center;
  }

  .company-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .contact-grid {
    grid-template-columns: 1fr;
  }
}
</style>
