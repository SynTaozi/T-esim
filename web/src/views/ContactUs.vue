<template>
  <div class="contact-page">
    <SiteHeader />

    <section class="hero">
      <div class="container center">
        <div class="breadcrumb">
          <router-link to="/">{{ $t('orderDetail.home') }}</router-link>
          <span class="sep">›</span>
          <span>{{ $t('nav.contactUs') }}</span>
        </div>
        <p class="brand">Tesim</p>
        <h1>{{ $t('contact.title') }}</h1>
        <p class="intro">{{ $t('contact.subtitle') }}</p>
      </div>
    </section>

    <section class="channels-band">
      <div class="container channels">
        <a
          v-for="item in channels"
          :key="item.title"
          class="channel"
          :href="item.href"
          :target="item.external ? '_blank' : undefined"
          :rel="item.external ? 'noopener noreferrer' : undefined"
        >
          <span class="material-symbols-outlined">{{ item.icon }}</span>
          <strong>{{ $t(item.title) }}</strong>
          <span class="meta">{{ item.meta }}</span>
        </a>
      </div>
    </section>

    <section class="block">
      <div class="container center-copy">
        <h2>{{ $t('contact.submitTicket') }}</h2>
        <p>{{ $t('contact.submitTicketDesc') }}</p>
        <router-link v-if="authStore.isLoggedIn" to="/ticket/new" class="btn">{{ $t('contact.submitTicket') }}</router-link>
        <router-link v-else to="/login" class="btn">{{ $t('nav.login') }}</router-link>
      </div>
    </section>

    <section id="company" class="block soft">
      <div class="container">
        <div class="center">
          <h2>{{ $t('contact.companyInfo') }}</h2>
        </div>
        <div class="legal-strip">
          <div>
            <span>{{ $t('contact.legalName') }}</span>
            <strong>{{ $t('contact.legalNameValue') }}</strong>
          </div>
          <div>
            <span>{{ $t('contact.registration') }}</span>
            <strong>{{ $t('contact.registrationValue') }}</strong>
          </div>
          <div class="wide">
            <span>{{ $t('contact.address') }}</span>
            <strong>{{ $t('contact.addressValue') }}</strong>
          </div>
        </div>
      </div>
    </section>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const { t } = useI18n({ useScope: 'global' })

const channels = computed(() => [
  {
    icon: 'group',
    title: 'contact.qqGroup',
    meta: t('contact.qqJoin'),
    href: 'https://qm.qq.com/q/y7fG8uG4xy',
    external: true
  },
  {
    icon: 'mail',
    title: 'contact.emailLabel',
    meta: 'support@tesim.com',
    href: 'mailto:support@tesim.com',
    external: false
  },
  {
    icon: 'call',
    title: 'contact.phoneLabel',
    meta: '+1 213-343-8884',
    href: 'tel:+12133438884',
    external: false
  },
  {
    icon: 'location_on',
    title: 'contact.addressLabel',
    meta: t('contact.addressValue'),
    href: '#company',
    external: false
  },
  {
    icon: 'send',
    title: 'contact.telegramGroup',
    meta: t('contact.telegramJoin'),
    href: 'https://t.me/+t7btxMNFgUhiYjMx',
    external: true
  }
])

onMounted(() => {
  window.scrollTo(0, 0)
})
</script>

<style scoped>
.contact-page {
  min-height: 100vh;
  background: #fff;
  font-family: var(--font-body, Manrope, system-ui, sans-serif);
  color: #1a1b1f;
}

.center {
  text-align: center;
}

.hero {
  padding: 48px 0 28px;
}

.breadcrumb {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  color: #86868b;
  font-size: 14px;
  margin-bottom: 18px;
}

.breadcrumb a {
  color: #0059b5;
  text-decoration: none;
}

.sep {
  color: #86868b;
}

.brand {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #0059b5;
}

h1 {
  margin: 0 auto;
  max-width: 14ch;
  font-size: clamp(32px, 5vw, 48px);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.12;
}

.intro {
  margin: 14px auto 0;
  max-width: 36em;
  color: #414753;
  line-height: 1.7;
  font-size: 16px;
}

.channels-band {
  border-top: 1px solid #eef0f4;
  border-bottom: 1px solid #eef0f4;
  background: #f8fafc;
}

.channels {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  padding: 28px 0;
}

.channel {
  text-decoration: none;
  color: inherit;
  text-align: center;
  padding: 14px 10px;
  border-radius: 12px;
  transition: background 0.15s ease;
}

.channel:hover {
  background: #fff;
}

.channel .material-symbols-outlined {
  display: block;
  margin: 0 auto 8px;
  font-size: 24px;
  color: #0059b5;
}

.channel strong {
  display: block;
  font-size: 13px;
  font-weight: 750;
  margin-bottom: 4px;
}

.channel .meta {
  display: block;
  font-size: 12px;
  color: #86868b;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
}

.block {
  padding: 56px 0;
}

.block.soft {
  background: #f5f7fb;
}

.center-copy {
  max-width: 560px;
  margin: 0 auto;
  text-align: center;
}

.center-copy h2 {
  margin: 0 0 10px;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.center-copy p {
  margin: 0 0 20px;
  color: #414753;
  line-height: 1.7;
  font-size: 15px;
}

.btn {
  display: inline-flex;
  align-items: center;
  min-height: 44px;
  padding: 0 22px;
  border-radius: 999px;
  background: #0071e3;
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  text-decoration: none;
}

.legal-strip {
  margin-top: 24px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  max-width: 820px;
  margin-left: auto;
  margin-right: auto;
}

.legal-strip > div {
  padding-top: 14px;
  border-top: 2px solid #0059b5;
}

.legal-strip span {
  display: block;
  font-size: 12px;
  color: #86868b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 6px;
}

.legal-strip strong {
  display: block;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.5;
}

.legal-strip .wide {
  grid-column: 1 / -1;
}

@media (max-width: 900px) {
  .channels {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 560px) {
  .channels {
    grid-template-columns: 1fr;
  }

  .legal-strip {
    grid-template-columns: 1fr;
  }
}
</style>
