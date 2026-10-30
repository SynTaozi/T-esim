<template>
  <section id="cases" class="home-cases">
    <div class="container">
      <header class="home-cases__header">
        <h2 class="home-cases__title">{{ $t('home.testimonials.title') }}</h2>
        <svg class="home-cases__squiggle" viewBox="0 0 120 12" aria-hidden="true">
          <path
            d="M2 8 C 20 2, 40 10, 60 6 S 100 2, 118 8"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
        <p class="home-cases__subtitle">{{ $t('home.testimonials.subtitle') }}</p>
      </header>

      <div class="home-cases__viewport">
        <div :key="activeIndex" class="home-cases__track">
          <article
            v-for="(item, index) in pairItems"
            :key="`${activeIndex}-${index}`"
            class="home-cases__card"
          >
            <p class="home-cases__quote-mark" aria-hidden="true">"</p>
            <h3 class="home-cases__headline">{{ item.headline }}</h3>
            <p class="home-cases__author">{{ item.author }}</p>
            <p class="home-cases__body">{{ item.body }}</p>
            <router-link to="/contact" class="home-cases__cta">
              {{ $t('home.testimonials.cta') }}
            </router-link>
          </article>
        </div>
      </div>

      <div class="home-cases__dots" role="tablist" :aria-label="$t('home.testimonials.title')">
        <button
          v-for="(_, index) in testimonials"
          :key="index"
          type="button"
          class="home-cases__dot"
          :class="{ 'home-cases__dot--active': index === activeIndex }"
          :aria-label="`Slide ${index + 1}`"
          :aria-selected="index === activeIndex"
          @click="activeIndex = index"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

interface CaseItem {
  headline: string
  author: string
  body: string
}

const { tm } = useI18n({ useScope: 'global' })

const activeIndex = ref(0)
let autoplayTimer: ReturnType<typeof setInterval> | null = null

const testimonials = computed(() => {
  const raw = tm('home.testimonials.items')
  if (!Array.isArray(raw)) return [] as CaseItem[]
  return raw as CaseItem[]
})

const pairItems = computed(() => {
  const items = testimonials.value
  if (!items.length) return []
  const n = items.length
  const i = activeIndex.value % n
  const second = items[(i + 1) % n]
  if (i === n - 1 && n > 1) return [items[i]]
  return second ? [items[i], second] : [items[i]]
})

function nextSlide() {
  const n = testimonials.value.length
  if (n <= 1) return
  activeIndex.value = (activeIndex.value + 1) % n
}

onMounted(() => {
  autoplayTimer = setInterval(nextSlide, 6000)
})

onUnmounted(() => {
  if (autoplayTimer) clearInterval(autoplayTimer)
})
</script>

<style scoped>
.home-cases {
  padding: 72px 16px 80px;
  background: #f6f7f8;
}

.home-cases__header {
  max-width: 820px;
  margin: 0 auto 48px;
  text-align: center;
}

.home-cases__title {
  margin: 0;
  font-size: clamp(28px, 4vw, 36px);
  font-weight: 500;
  line-height: 1.25;
  color: #292b2e;
}

.home-cases__squiggle {
  display: block;
  width: 88px;
  height: 12px;
  margin: 10px auto 16px;
  color: #292b2e;
}

.home-cases__subtitle {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: #56575a;
}

.home-cases__viewport {
  overflow: hidden;
  max-width: var(--site-content-max-width, 1300px);
  margin: 0 auto;
}

.home-cases__track {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  animation: home-cases-fade 0.45s ease;
}

@keyframes home-cases-fade {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.home-cases__card {
  display: flex;
  flex-direction: column;
  min-height: 320px;
  padding: 32px 28px 28px;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 4px 24px rgba(41, 43, 46, 0.06);
}

.home-cases__quote-mark {
  margin: 0 0 12px;
  font-size: 40px;
  line-height: 1;
  font-family: Georgia, 'Times New Roman', serif;
  color: #e12b57;
  font-weight: 700;
}

.home-cases__headline {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.45;
  color: #292b2e;
}

.home-cases__author {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #292b2e;
}

.home-cases__body {
  margin: 0;
  flex: 1;
  font-size: 14px;
  line-height: 1.65;
  color: #56575a;
}

.home-cases__cta {
  align-self: flex-start;
  margin-top: 24px;
  padding: 12px 22px;
  border-radius: 12px;
  background: linear-gradient(104.52deg, #e12b57 0%, #c8094e 100%);
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  transition: filter 0.2s, transform 0.15s;
}

.home-cases__cta:hover {
  filter: brightness(1.05);
  transform: translateY(-1px);
  color: #ffffff;
}

.home-cases__dots {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 36px;
}

.home-cases__dot {
  width: 8px;
  height: 8px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: #ccd0d7;
  cursor: pointer;
  transition: width 0.2s, height 0.2s, background 0.2s;
}

.home-cases__dot--active {
  width: 10px;
  height: 10px;
  background: #292b2e;
}

@media (max-width: 900px) {
  .home-cases__track {
    grid-template-columns: 1fr;
    max-width: 520px;
  }

  .home-cases__card {
    min-height: 0;
  }
}
</style>
