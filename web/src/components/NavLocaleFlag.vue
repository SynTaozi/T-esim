<template>
  <!-- 当前中文 → 显示美国旗（切换英文）；当前英文 → 显示中国旗（切换中文） -->
  <svg
    v-if="showUs"
    class="locale-flag"
    viewBox="0 0 24 24"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <defs>
      <clipPath :id="clipId + '-us'">
        <circle cx="12" cy="12" r="12" />
      </clipPath>
    </defs>
    <g :clip-path="'url(#' + clipId + '-us)'">
      <rect width="24" height="24" fill="#b22234" />
      <path
        fill="#fff"
        d="M0 2.3h24V4.6H0zm0 4.6h24v2.3H0zm0 4.6h24v2.3H0zm0 4.6h24v2.3H0zm0 4.6h24V24H0z"
      />
      <rect width="9.6" height="6.9" fill="#3c3b6e" />
    </g>
  </svg>
  <svg
    v-else
    class="locale-flag"
    viewBox="0 0 24 24"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <defs>
      <clipPath :id="clipId + '-cn'">
        <circle cx="12" cy="12" r="12" />
      </clipPath>
    </defs>
    <g :clip-path="'url(#' + clipId + '-cn)'">
      <rect width="24" height="24" fill="#de2910" />
      <path
        fill="#ffde00"
        d="M7.2 5.2l.95 2.92h3.07l-2.48 1.8.95 2.92L7.2 11.04 5.25 12.84l.95-2.92-2.48-1.8h3.07L7.2 5.2zm8.2 1.1l.35 1.08h1.14l-.92.67.35 1.08-.92-.67-.92.67.35-1.08-.92-.67h1.14l.35-1.08zm1.6 5.2l.35 1.08h1.14l-.92.67.35 1.08-.92-.67-.92.67.35-1.08-.92-.67h1.14l.35-1.08zm-2.4 3.4l.35 1.08h1.14l-.92.67.35 1.08-.92-.67-.92.67.35-1.08-.92-.67h1.14l.35-1.08zm-4.8 0l.35 1.08h1.14l-.92.67.35 1.08-.92-.67-.92.67.35-1.08-.92-.67h1.14l.35-1.08z"
      />
    </g>
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  /** 点击后将切换到的语言：展示目标语言国旗 */
  targetLocale: 'zh' | 'en'
}>()

const clipId = `flag-${Math.random().toString(36).slice(2, 9)}`
const showUs = computed(() => props.targetLocale === 'en')
</script>

<style scoped>
.locale-flag {
  width: 22px;
  height: 22px;
  display: block;
  border-radius: 50%;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08);
}
</style>
