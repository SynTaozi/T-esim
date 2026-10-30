<template>
  <div class="terms-consent" :class="{ 'terms-consent--error': !!error }">
    <label class="terms-consent__row">
      <input
        type="checkbox"
        class="terms-consent__input"
        :checked="modelValue"
        @change="onToggle"
      />
      <span class="terms-consent__box" aria-hidden="true">
        <svg v-if="modelValue" class="terms-consent__check" viewBox="0 0 16 16" fill="none">
          <path
            d="M3.5 8.5L6.5 11.5L12.5 4.5"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </span>
      <span class="terms-consent__text">
        <slot />
      </span>
    </label>
    <Transition name="terms-hint">
      <p v-if="error" class="terms-consent__hint" role="alert">
        <span class="terms-consent__hint-icon" aria-hidden="true">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <circle cx="8" cy="8" r="7" stroke="currentColor" stroke-width="1.5" />
            <path d="M8 4.5v4.25M8 11h.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
          </svg>
        </span>
        <span>{{ error }}</span>
      </p>
    </Transition>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: boolean
  error?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

function onToggle(e: Event) {
  const checked = (e.target as HTMLInputElement).checked
  emit('update:modelValue', checked)
}
</script>

<style scoped>
.terms-consent {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid transparent;
  background: transparent;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
}

.terms-consent--error {
  border-color: #fecaca;
  background: #fffbfb;
  box-shadow: 0 0 0 3px rgba(248, 113, 113, 0.1);
}

.terms-consent__row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  cursor: pointer;
  margin: 0;
}

.terms-consent__input {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.terms-consent__box {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  margin-top: 1px;
  border-radius: 5px;
  border: 1.5px solid #c3c5d9;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
}

.terms-consent__row:hover .terms-consent__box {
  border-color: #0052ff;
}

.terms-consent--error .terms-consent__box {
  border-color: #f87171;
  box-shadow: 0 0 0 2px rgba(248, 113, 113, 0.15);
}

.terms-consent__input:focus-visible + .terms-consent__box {
  outline: 2px solid #0052ff;
  outline-offset: 2px;
}

.terms-consent__input:checked + .terms-consent__box {
  background: #0052ff;
  border-color: #0052ff;
}

.terms-consent__check {
  width: 12px;
  height: 12px;
}

.terms-consent__text {
  font-size: 14px;
  line-height: 1.5;
  color: #434656;
  flex: 1;
  min-width: 0;
}

.terms-consent__text :deep(.policy-link) {
  color: #0052ff;
  font-weight: 600;
  text-decoration: none;
}

.terms-consent__text :deep(.policy-link:hover) {
  text-decoration: underline;
}

.terms-consent__hint {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin: 0;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
  font-size: 13px;
  line-height: 1.45;
}

.terms-consent__hint-icon {
  flex-shrink: 0;
  display: flex;
  margin-top: 1px;
}

.terms-hint-enter-active,
.terms-hint-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.terms-hint-enter-from,
.terms-hint-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
