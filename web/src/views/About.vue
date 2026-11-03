<template>
  <div class="about-page">
    <SiteHeader />

    <section class="hero">
      <div class="container center">
        <div class="breadcrumb">
          <router-link to="/">{{ $t('orderDetail.home') }}</router-link>
          <span class="sep">›</span>
          <span>{{ $t('nav.about') }}</span>
        </div>
        <p class="brand">Tesim</p>
        <h1>{{ $t('about.subtitle') }}</h1>
        <p class="platform">{{ $t('about.platformTitle') }}</p>
        <p class="intro">{{ $t('about.intro') }}</p>
      </div>
    </section>

    <section class="stats">
      <div class="container stats-row">
        <div>
          <strong>200+</strong>
          <span>{{ $t('about.countriesServed') }}</span>
        </div>
        <div>
          <strong>{{ $t('about.hours247') }}</strong>
          <span>{{ $t('about.globalAccess') }}</span>
        </div>
        <div>
          <strong>Global</strong>
          <span>{{ $t('about.globalCoverage') }}</span>
        </div>
        <div>
          <strong>US</strong>
          <span>{{ $t('about.wyomingReg') }}</span>
        </div>
      </div>
    </section>

    <section class="block">
      <div class="container center-copy">
        <h2>{{ $t('about.whoWeAre') }}</h2>
        <p class="sub">{{ $t('about.whoWeAreSubtitle') }}</p>
        <p>{{ $t('about.whoWeAreContent1') }}</p>
        <p>{{ $t('about.whoWeAreContent2') }}</p>
        <p>{{ $t('about.whoWeAreContent3') }}</p>
        <router-link to="/contact" class="text-link">{{ $t('about.contactUs') }} →</router-link>
      </div>
    </section>

    <section class="block soft">
      <div class="container">
        <div class="center">
          <h2>{{ $t('about.businessScope') }}</h2>
          <p class="sub">{{ $t('about.ourServiceDomains') }}</p>
        </div>
        <div class="services">
          <article v-for="s in services" :key="s.title">
            <h3>{{ $t(s.title) }}</h3>
            <p>{{ $t(s.desc) }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="block">
      <div class="container">
        <div class="center">
          <h2>{{ $t('about.legalEntity') }}</h2>
          <p class="sub">{{ $t('about.companyDetails') }}</p>
        </div>
        <div class="legal-strip">
          <div>
            <span>{{ $t('about.legalName') }}</span>
            <strong>{{ $t('about.legalNameValue') }}</strong>
          </div>
          <div>
            <span>{{ $t('about.stateOfIncorporation') }}</span>
            <strong>{{ $t('about.stateValue') }}</strong>
          </div>
          <div>
            <span>{{ $t('about.phone') }}</span>
            <strong><a href="tel:+12133438884">+1 213-343-8884</a></strong>
          </div>
          <div>
            <span>{{ $t('about.email') }}</span>
            <strong><a href="mailto:support@tesim.com">support@tesim.com</a></strong>
          </div>
          <div class="wide">
            <span>{{ $t('about.registeredAddress') }}</span>
            <strong>{{ $t('about.addressValue') }}</strong>
          </div>
        </div>
      </div>
    </section>

    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'

const services = [
  { title: 'about.service1Title', desc: 'about.service1Desc' },
  { title: 'about.service2Title', desc: 'about.service2Desc' },
  { title: 'about.service3Title', desc: 'about.service3Desc' },
  { title: 'about.service4Title', desc: 'about.service4Desc' }
]

onMounted(() => {
  window.scrollTo(0, 0)
})
</script>

<style scoped>
.about-page {
  min-height: 100vh;
  background: #fff;
  font-family: var(--font-body, Manrope, system-ui, sans-serif);
  color: #1a1b1f;
}

.center {
  text-align: center;
}

.hero {
  padding: 56px 0 36px;
}

.breadcrumb {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  color: #86868b;
  font-size: 14px;
  margin-bottom: 20px;
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
  max-width: 16ch;
  font-size: clamp(32px, 5vw, 48px);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.12;
}

.platform {
  margin: 12px 0 0;
  color: #0059b5;
  font-weight: 700;
  font-size: 18px;
}

.intro {
  margin: 16px auto 0;
  max-width: 40em;
  color: #414753;
  line-height: 1.7;
  font-size: 16px;
}

.stats {
  border-top: 1px solid #eef0f4;
  border-bottom: 1px solid #eef0f4;
  background: #f8fafc;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 28px 0;
  text-align: center;
}

.stats-row strong {
  display: block;
  font-size: 26px;
  font-weight: 800;
  color: #0059b5;
  margin-bottom: 6px;
}

.stats-row span {
  font-size: 13px;
  color: #86868b;
}

.block {
  padding: 56px 0;
}

.block.soft {
  background: #f5f7fb;
}

.center-copy {
  max-width: 720px;
  margin: 0 auto;
  text-align: center;
}

h2 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.sub {
  margin: 0 0 18px;
  color: #86868b;
}

.center-copy p {
  margin: 0 0 14px;
  color: #414753;
  line-height: 1.75;
  font-size: 15px;
}

.text-link {
  display: inline-block;
  margin-top: 8px;
  color: #0059b5;
  font-weight: 700;
  text-decoration: none;
}

.services {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px 28px;
  margin-top: 28px;
}

.services article {
  padding-top: 18px;
  border-top: 2px solid #0059b5;
}

.services h3 {
  margin: 0 0 8px;
  font-size: 17px;
}

.services p {
  margin: 0;
  color: #414753;
  font-size: 14px;
  line-height: 1.6;
}

.legal-strip {
  margin-top: 24px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.legal-strip > div {
  background: #fff;
  border: 1px solid #e8ebf0;
  border-radius: 14px;
  padding: 16px 18px;
}

.legal-strip span {
  display: block;
  font-size: 12px;
  color: #86868b;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.legal-strip strong {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
}

.legal-strip a {
  color: #0059b5;
  text-decoration: none;
}

.legal-strip .wide {
  grid-column: 1 / -1;
}

@media (max-width: 800px) {
  .stats-row {
    grid-template-columns: 1fr 1fr;
  }

  .services,
  .legal-strip {
    grid-template-columns: 1fr;
  }
}
</style>
