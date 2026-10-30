<template>
  <Teleport to="body">
    <Transition name="purchase-notice">
      <div
        v-if="modelValue"
        class="purchase-notice-mask"
        @click.self="emit('update:modelValue', false)"
      >
        <div class="purchase-notice-dialog" role="dialog" aria-modal="true" :aria-label="$t('checkout.purchaseNoticeTitle')">
          <div class="purchase-notice-header">
            <div class="purchase-notice-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <circle cx="12" cy="12" r="9" />
                <path d="M12 8v5" stroke-linecap="round" />
                <circle cx="12" cy="16.2" r="0.9" fill="currentColor" stroke="none" />
              </svg>
            </div>
            <div>
              <h3 class="purchase-notice-title">{{ $t('checkout.purchaseNoticeTitle') }}</h3>
              <p class="purchase-notice-subtitle">{{ $t('checkout.purchaseNoticeSubtitle') }}</p>
            </div>
          </div>

          <ul class="purchase-notice-list">
            <li v-for="(item, idx) in noticeItems" :key="idx">
              <span class="purchase-notice-bullet">{{ idx + 1 }}</span>
              <span class="purchase-notice-text">{{ item }}</span>
            </li>
          </ul>

          <p class="purchase-notice-footer">{{ $t('checkout.purchaseNoticeFooter') }}</p>

          <div class="purchase-notice-actions">
            <button type="button" class="purchase-notice-cancel" @click="emit('update:modelValue', false)">
              {{ $t('common.cancel') }}
            </button>
            <button type="button" class="purchase-notice-confirm" @click="onConfirm">
              {{ $t('checkout.purchaseNoticeConfirm') }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
}>()

const { t } = useI18n({ useScope: 'global' })

const noticeItems = computed(() => [
  t('checkout.purchaseNoticeItem1'),
  t('checkout.purchaseNoticeItem2'),
  t('checkout.purchaseNoticeItem3'),
  t('checkout.purchaseNoticeItem4')
])

function onConfirm() {
  emit('update:modelValue', false)
  emit('confirm')
}
</script>

<style scoped>
.purchase-notice-mask {
  position: fixed;
  inset: 0;
  z-index: 4000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.48);
  backdrop-filter: blur(6px);
}

.purchase-notice-dialog {
  position: relative;
  width: min(640px, 100%);
  max-height: min(86vh, 720px);
  overflow: auto;
  border-radius: 18px;
  background: #fff;
  box-shadow:
    0 24px 60px rgba(15, 23, 42, 0.22),
    0 0 0 1px rgba(195, 197, 217, 0.35);
  padding: 28px 28px 22px;
}

.purchase-notice-header {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.purchase-notice-icon {
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

.purchase-notice-icon svg {
  width: 22px;
  height: 22px;
}

.purchase-notice-title {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 750;
  line-height: 1.35;
  color: #131b2e;
  font-family: 'Manrope', system-ui, sans-serif;
}

.purchase-notice-subtitle {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: #717785;
}

.purchase-notice-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.purchase-notice-list li {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f7f8fc;
  border: 1px solid rgba(195, 197, 217, 0.45);
}

.purchase-notice-bullet {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  margin-top: 1px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: #003ec7;
}

.purchase-notice-text {
  flex: 1;
  font-size: 14px;
  line-height: 1.65;
  color: #2a3142;
}

.purchase-notice-footer {
  margin: 16px 0 20px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.6;
  color: #434656;
  background: color-mix(in srgb, #003ec7 6%, #fff);
  border: 1px dashed color-mix(in srgb, #003ec7 22%, transparent);
}

.purchase-notice-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.purchase-notice-cancel,
.purchase-notice-confirm {
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

.purchase-notice-cancel {
  color: #434656;
  background: #eef0f6;
}

.purchase-notice-cancel:hover {
  background: #e4e7f0;
}

.purchase-notice-confirm {
  color: #fff;
  background: linear-gradient(135deg, #003ec7, #2958e0);
  box-shadow: 0 8px 18px color-mix(in srgb, #003ec7 28%, transparent);
}

.purchase-notice-confirm:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px color-mix(in srgb, #003ec7 34%, transparent);
}

.purchase-notice-enter-active,
.purchase-notice-leave-active {
  transition: opacity 0.22s ease;
}
.purchase-notice-enter-active .purchase-notice-dialog,
.purchase-notice-leave-active .purchase-notice-dialog {
  transition: transform 0.28s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.22s ease;
}
.purchase-notice-enter-from,
.purchase-notice-leave-to {
  opacity: 0;
}
.purchase-notice-enter-from .purchase-notice-dialog,
.purchase-notice-leave-to .purchase-notice-dialog {
  opacity: 0;
  transform: translateY(12px) scale(0.97);
}

@media (max-width: 520px) {
  .purchase-notice-dialog {
    padding: 22px 18px 18px;
  }
  .purchase-notice-actions {
    flex-direction: column-reverse;
  }
  .purchase-notice-cancel,
  .purchase-notice-confirm {
    width: 100%;
  }
}
</style>
