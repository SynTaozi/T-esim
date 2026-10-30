<template>
  <section class="home-pay-card-section">
    <div class="container">
      <p class="section-lead">{{ $t('home.payAsYouGo.lead') }}</p>
      <div class="pay-card-wrap">
        <div class="pay-card">
          <div class="pay-card-visual" aria-hidden="true">
            <img class="pay-card-shadow" :src="HOME_PAY_CARD_SHADOW" alt="" />
          </div>
          <div class="pay-card-main">
            <div class="pay-card-head">
              <div>
                <p class="pay-card-badge">{{ $t('home.payAsYouGo.badge') }}</p>
                <p class="pay-card-countries">{{ $t('home.payAsYouGo.countries') }}</p>
              </div>
              <router-link to="/products" class="pay-card-cta">{{ $t('home.payAsYouGo.cta') }}</router-link>
            </div>
            <ul class="pay-card-features">
              <li v-for="(feat, i) in featureList" :key="i">
                <span class="feat-icon" aria-hidden="true">✓</span>
                <span>{{ feat }}</span>
              </li>
            </ul>
            <router-link to="/products" class="pay-card-learn">
              {{ $t('home.payAsYouGo.learnMore') }}
              <svg viewBox="0 0 20 20" fill="currentColor" width="16" height="16"><path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"/></svg>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { HOME_PAY_CARD_SHADOW } from '@/constants/homeReferenceAssets'

const { tm } = useI18n({ useScope: 'global' })

const featureList = computed(() => {
  const raw = tm('home.payAsYouGo.features')
  return Array.isArray(raw) ? raw : []
})
</script>

<style scoped>
.home-pay-card-section {
  padding: 48px 0 56px;
}

.section-lead {
  text-align: center;
  max-width: 640px;
  margin: 0 auto 28px;
  font-size: 16px;
  line-height: 1.6;
  color: var(--on-surface-variant, #6e6e73);
}

.pay-card-wrap {
  display: flex;
  justify-content: center;
}

.pay-card {
  display: grid;
  grid-template-columns: 1fr;
  width: 100%;
  max-width: 880px;
  background: linear-gradient(145deg, #cfe8f5 0%, #b8dff0 50%, #a8d4ec 100%);
  border-radius: 20px;
  padding: 8px;
  position: relative;
  overflow: hidden;
}

@media (min-width: 768px) {
  .pay-card {
    grid-template-columns: 1fr 1.1fr;
    min-height: 280px;
  }
}

.pay-card-visual {
  display: none;
}

@media (min-width: 768px) {
  .pay-card-visual {
    display: flex;
    align-items: flex-end;
    justify-content: center;
    padding: 16px;
  }
  .pay-card-shadow {
    max-width: 200px;
    width: 100%;
    opacity: 0.9;
  }
}

.pay-card-main {
  background: #fff;
  border-radius: 16px;
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.pay-card-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.pay-card-badge {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: -0.02em;
}

.pay-card-countries {
  margin: 0;
  font-size: 14px;
  color: #6e6e73;
}

.pay-card-cta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 28px;
  background: #1d1d1f;
  color: #fff;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.2s, transform 0.2s;
  white-space: nowrap;
}

.pay-card-cta:hover {
  background: #0059b5;
  transform: scale(1.02);
}

.pay-card-features {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 10px;
}

.pay-card-features li {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 14px;
  line-height: 1.45;
  color: #4a4a4a;
}

.feat-icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 89, 181, 0.1);
  color: #0059b5;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pay-card-learn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #0059b5;
  text-decoration: none;
}

.pay-card-learn:hover {
  text-decoration: underline;
}
</style>
