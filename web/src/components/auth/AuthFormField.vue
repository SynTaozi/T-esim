<template>
  <div class="auth-field" :class="{ 'auth-field--error': !!error }">
    <label v-if="label" :for="inputId" class="auth-field__label">{{ label }}</label>
    <slot :input-id="inputId" :has-error="!!error" />
    <Transition name="auth-field-msg">
      <div v-if="error" class="auth-field__message" role="alert">
        <span class="auth-field__icon" aria-hidden="true">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <circle cx="8" cy="8" r="7" stroke="currentColor" stroke-width="1.5" />
            <path d="M8 4.5v4.25M8 11h.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
          </svg>
        </span>
        <span class="auth-field__text">{{ error }}</span>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label?: string
  error?: string
  fieldId?: string
}>()

const inputId = computed(() => props.fieldId || `field-${Math.random().toString(36).slice(2, 9)}`)
</script>

<style scoped>
.auth-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.auth-field__label {
  display: block;
  font-size: 14px;
  line-height: 20px;
  font-weight: 600;
  color: #191b25;
}

.auth-field__message {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  font-size: 13px;
  line-height: 1.45;
}

.auth-field__icon {
  flex-shrink: 0;
  margin-top: 1px;
  display: flex;
}

.auth-field__text {
  flex: 1;
}

.auth-field--error :deep(.form-input) {
  border-color: #f87171;
  background-color: #fffbfb;
  box-shadow: 0 0 0 3px rgba(248, 113, 113, 0.12);
}

.auth-field--error :deep(.form-input:focus) {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15);
}

.auth-field-msg-enter-active,
.auth-field-msg-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.auth-field-msg-enter-from,
.auth-field-msg-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
