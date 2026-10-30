<template>
  <div class="page-shell">
    <SiteHeader />
    <main class="page-shell-main" :class="{ 'page-shell-main--flush': flush }">
      <div class="page-shell-container" :class="{ 'page-shell-container--wide': wide }">
        <slot />
      </div>
    </main>
    <SiteFooter v-if="showFooter" />
  </div>
</template>

<script setup lang="ts">
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'

withDefaults(
  defineProps<{
    showFooter?: boolean
    wide?: boolean
    flush?: boolean
  }>(),
  {
    showFooter: true,
    wide: false,
    flush: false
  }
)
</script>

<style scoped>
.page-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary, #fff);
}

.page-shell-main {
  flex: 1;
  width: 100%;
}

.page-shell-main--flush .page-shell-container {
  max-width: none;
  padding-left: 0;
  padding-right: 0;
}

.page-shell-container {
  width: 100%;
  max-width: var(--content-max-width, 1200px);
  margin: 0 auto;
  padding: 1.5rem clamp(1rem, 3vw, 2rem) 3rem;
  box-sizing: border-box;
}

.page-shell-container--wide {
  max-width: var(--content-max-width-wide, 1400px);
}
</style>
