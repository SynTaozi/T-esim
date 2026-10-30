<template>
  <div class="home-highlights" aria-label="Highlights">
    <div class="home-highlights__grid">
      <article
        v-for="(item, index) in items"
        :key="index"
        class="home-highlights__item"
      >
        <div class="home-highlights__icon" aria-hidden="true">
          <svg
            v-if="index === 0"
            width="48"
            height="48"
            viewBox="0 0 48 48"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <circle cx="24" cy="24" r="14" stroke="#292b2e" stroke-width="2" />
            <path d="M24 10v28M10 24h28" stroke="#292b2e" stroke-width="2" />
            <path
              d="M14 16c4 2 8 2 12 0s8-2 12 0M14 32c4-2 8-2 12 0s8 2 12 0"
              stroke="#292b2e"
              stroke-width="2"
              stroke-linecap="round"
            />
          </svg>
          <svg
            v-else-if="index === 1"
            width="48"
            height="48"
            viewBox="0 0 48 48"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M8 30c4-8 8-8 12 0s8 8 12 0 8-8 12 0"
              stroke="#292b2e"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path
              d="M14 22c1.5-4 3-4 5 0"
              stroke="#292b2e"
              stroke-width="2"
              stroke-linecap="round"
            />
            <path
              d="M28 18l5-5 3 3-5 5"
              stroke="#292b2e"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path d="M30 14v-3M34 12v-3" stroke="#292b2e" stroke-width="2" stroke-linecap="round" />
          </svg>
          <svg
            v-else-if="index === 2"
            width="48"
            height="48"
            viewBox="0 0 48 48"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect
              x="16"
              y="8"
              width="16"
              height="32"
              rx="3"
              stroke="#292b2e"
              stroke-width="2"
            />
            <path d="M22 36h4" stroke="#292b2e" stroke-width="2" stroke-linecap="round" />
            <path
              d="M30 12l2-2M34 16l2-2M27 21l3-3"
              stroke="#292b2e"
              stroke-width="1.5"
              stroke-linecap="round"
            />
          </svg>
          <svg
            v-else
            width="48"
            height="48"
            viewBox="0 0 48 48"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <circle cx="24" cy="24" r="14" stroke="#292b2e" stroke-width="2" />
            <path
              d="M16 26c2.5 4 5.5 5 8 5s5.5-1 8-5"
              stroke="#292b2e"
              stroke-width="2"
              stroke-linecap="round"
            />
            <path d="M18 20h2M28 20h2" stroke="#292b2e" stroke-width="2.5" stroke-linecap="round" />
          </svg>
        </div>
        <h3 class="home-highlights__title">{{ item.title }}</h3>
        <p class="home-highlights__desc">{{ item.desc }}</p>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

interface HighlightItem {
  title: string
  desc: string
}

const { tm } = useI18n({ useScope: 'global' })

const items = computed(() => {
  const raw = tm('home.highlights.items')
  if (!Array.isArray(raw)) return [] as HighlightItem[]
  return raw as HighlightItem[]
})
</script>

<style scoped>
.home-highlights {
  margin-top: 48px;
}

.home-highlights__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px 24px;
}

.home-highlights__item {
  text-align: center;
  padding: 0 8px;
}

.home-highlights__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  width: 48px;
  height: 48px;
}

.home-highlights__title {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.35;
  color: #292b2e;
}

.home-highlights__desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.55;
  color: #56575a;
}

@media (max-width: 900px) {
  .home-highlights__grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 36px 20px;
  }
}

@media (max-width: 480px) {
  .home-highlights__grid {
    grid-template-columns: 1fr;
    max-width: 320px;
    margin: 0 auto;
  }
}
</style>
