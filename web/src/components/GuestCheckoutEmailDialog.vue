<template>
  <Teleport to="body">
    <Transition name="guest-checkout">
      <div
        v-if="modelValue"
        class="guest-checkout-mask"
        @click.self="emit('update:modelValue', false)"
      >
        <div class="guest-checkout-dialog" role="dialog" aria-modal="true" :aria-label="$t('checkout.guestCheckoutTitle')">
          <div class="guest-checkout-header">
            <div class="guest-checkout-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <rect x="4" y="10" width="16" height="10" rx="2" />
                <path d="M8 10V8a4 4 0 0 1 8 0v2" stroke-linecap="round" />
                <circle cx="12" cy="15" r="1.2" fill="currentColor" stroke="none" />
              </svg>
            </div>
            <div>
              <h3 class="guest-checkout-title">{{ $t('checkout.guestCheckoutTitle') }}</h3>
              <p class="guest-checkout-subtitle">{{ $t('checkout.guestCheckoutSubtitle') }}</p>
            </div>
          </div>

          <p class="guest-checkout-desc">{{ $t('checkout.guestCheckoutDesc') }}</p>

          <label class="guest-checkout-label" for="guest-checkout-email">
            {{ $t('checkout.email') }} <span class="required">*</span>
          </label>
          <el-input
            id="guest-checkout-email"
            v-model="localEmail"
            type="email"
            clearable
            :placeholder="$t('checkout.emailPlaceholder')"
            @keyup.enter="onConfirm"
          />
          <p class="guest-checkout-hint">{{ $t('checkout.guestCheckoutHint') }}</p>

          <div class="guest-checkout-actions">
            <button type="button" class="guest-checkout-cancel" @click="emit('update:modelValue', false)">
              {{ $t('common.cancel') }}
            </button>
            <button type="button" class="guest-checkout-confirm" @click="onConfirm">
              {{ $t('checkout.guestCheckoutConfirm') }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [email: string]
}>()

const { t } = useI18n({ useScope: 'global' })
const localEmail = ref('')

watch(
  () => props.modelValue,
  (open) => {
    if (open) localEmail.value = ''
  }
)

function onConfirm() {
  const email = localEmail.value.trim()
  if (!email) {
    ElMessage.warning(t('checkout.emailRequired'))
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.warning(t('checkout.emailInvalid'))
    return
  }
  emit('update:modelValue', false)
  emit('confirm', email)
}
</script>

<style scoped>
.guest-checkout-mask {
  position: fixed;
  inset: 0;
  z-index: 4100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.48);
  backdrop-filter: blur(6px);
}

.guest-checkout-dialog {
  width: min(480px, 100%);
  border-radius: 18px;
  background: #fff;
  box-shadow:
    0 24px 60px rgba(15, 23, 42, 0.22),
    0 0 0 1px rgba(195, 197, 217, 0.35);
  padding: 28px 28px 22px;
}

.guest-checkout-header {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  margin-bottom: 14px;
}

.guest-checkout-icon {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #003ec7;
  background: color-mix(in srgb, #003ec7 10%, #f2f3ff);
}

.guest-checkout-icon svg {
  width: 22px;
  height: 22px;
}

.guest-checkout-title {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 750;
  line-height: 1.35;
  color: #131b2e;
  font-family: 'Manrope', system-ui, sans-serif;
}

.guest-checkout-subtitle {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: #717785;
}

.guest-checkout-desc {
  margin: 0 0 16px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.65;
  color: #434656;
  background: color-mix(in srgb, #003ec7 6%, #fff);
  border: 1px dashed color-mix(in srgb, #003ec7 22%, transparent);
}

.guest-checkout-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 650;
  color: #2a3142;
}

.guest-checkout-label .required {
  color: #e11d48;
}

.guest-checkout-hint {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #94a3b8;
}

.guest-checkout-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.guest-checkout-cancel,
.guest-checkout-confirm {
  min-width: 108px;
  height: 42px;
  padding: 0 18px;
  border-radius: 10px;
  border: none;
  font-size: 14px;
  font-weight: 650;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

.guest-checkout-cancel {
  color: #434656;
  background: #eef0f6;
}

.guest-checkout-cancel:hover {
  background: #e4e7f0;
}

.guest-checkout-confirm {
  color: #fff;
  background: linear-gradient(135deg, #003ec7, #2958e0);
  box-shadow: 0 8px 18px color-mix(in srgb, #003ec7 28%, transparent);
}

.guest-checkout-confirm:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px color-mix(in srgb, #003ec7 34%, transparent);
}

.guest-checkout-enter-active,
.guest-checkout-leave-active {
  transition: opacity 0.22s ease;
}
.guest-checkout-enter-active .guest-checkout-dialog,
.guest-checkout-leave-active .guest-checkout-dialog {
  transition: transform 0.28s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.22s ease;
}
.guest-checkout-enter-from,
.guest-checkout-leave-to {
  opacity: 0;
}
.guest-checkout-enter-from .guest-checkout-dialog,
.guest-checkout-leave-to .guest-checkout-dialog {
  opacity: 0;
  transform: translateY(12px) scale(0.97);
}

@media (max-width: 520px) {
  .guest-checkout-dialog {
    padding: 22px 18px 18px;
  }
  .guest-checkout-actions {
    flex-direction: column-reverse;
  }
  .guest-checkout-cancel,
  .guest-checkout-confirm {
    width: 100%;
  }
}
</style>
