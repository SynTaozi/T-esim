<template>
  <div class="page-header">
    <div class="page-header__text">
      <h1>{{ title }}</h1>
      <p v-if="subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="page-header__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header__text h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--admin-text);
}

.page-header__text p {
  margin: 6px 0 0;
  color: var(--admin-text-muted);
  font-size: 13px;
}

.page-header__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
</style>
