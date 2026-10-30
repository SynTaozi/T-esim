<template>
  <section class="home-how-it-works">
    <div class="container">
      <h2 class="section-title">{{ $t('home.howItWorks.title') }}</h2>
      <div class="steps-row">
        <article v-for="(step, i) in steps" :key="i" class="step-card">
          <div class="step-copy">
            <h3>{{ step.title }}</h3>
            <p>{{ step.desc }}</p>
          </div>
          <div class="step-illus">
            <img :src="HOME_STEP_IMAGES.desktop[i]" :alt="step.title" loading="lazy" />
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { HOME_STEP_IMAGES } from '@/constants/homeReferenceAssets'

const { tm } = useI18n({ useScope: 'global' })

const steps = computed(() => {
  const raw = tm('home.howItWorks.steps')
  if (!Array.isArray(raw)) return []
  return raw as { title: string; desc: string }[]
})

</script>

<style scoped>
.home-how-it-works {
  padding: 54px 16px 100px;
  background: #e8e8e8;
}

.section-title {
  margin: 0 auto 40px;
  max-width: var(--site-content-max-width, 1300px);
  text-align: center;
  font-size: 28px;
  font-weight: 500;
  line-height: 1.3;
  color: #1d1d1f;
}

.steps-row {
  display: flex;
  flex-flow: row wrap;
  justify-content: center;
  align-items: stretch;
  gap: 24px;
  max-width: var(--site-content-max-width, 1300px);
  margin: 0 auto;
}

.step-card {
  flex: 1 1 280px;
  max-width: 384px;
  background: #fff;
  border-radius: 16px;
  padding: 32px 28px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.step-copy {
  width: 100%;
  margin-bottom: 20px;
}

.step-copy h3 {
  margin: 0 0 10px;
  font-size: 20px;
  font-weight: 500;
  line-height: 1.35;
  color: #1d1d1f;
}

.step-copy p {
  margin: 0;
  font-size: 16px;
  line-height: 24px;
  color: #4a4a4a;
}

.step-illus {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  width: 100%;
  margin-top: auto;
}

.step-illus img {
  width: 170px;
  max-width: 100%;
  height: auto;
  display: block;
  object-fit: contain;
}

@media (max-width: 900px) {
  .steps-row {
    flex-direction: column;
    align-items: center;
  }

  .step-card {
    width: 100%;
    max-width: 400px;
  }
}
</style>
