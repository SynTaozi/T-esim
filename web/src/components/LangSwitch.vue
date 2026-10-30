<template>
  <button
    type="button"
    class="lang-toggle"
    @click="toggleLocale"
    :title="locale === 'zh' ? '切换至英文' : 'Switch to Chinese'"
    :aria-label="locale === 'zh' ? '切换至英文' : 'Switch to Chinese'"
  >
    <NavLocaleFlag :target-locale="locale === 'zh' ? 'en' : 'zh'" />
  </button>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { toggleLocale as _toggleLocale } from '@/i18n'
import NavLocaleFlag from '@/components/NavLocaleFlag.vue'

const { locale } = useI18n({ useScope: 'global' })

function toggleLocale() {
  _toggleLocale()
}
</script>

<style scoped>
.lang-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  padding: 0;
  border-radius: 50%;
  background: transparent;
  border: 1px solid #d2d2d7;
  color: #6e6e73;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.lang-toggle:hover {
  border-color: #0071e3;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.12);
  transform: scale(1.04);
}
</style>
