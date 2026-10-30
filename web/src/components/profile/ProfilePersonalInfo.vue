<template>
  <div class="ppi-bento">

    <!-- Left: Form Card -->
    <div class="ppi-card ppi-form-card">
      <form @submit.prevent>
        <!-- Readonly Fields: username + email (two columns) -->
        <div class="ppi-readonly-row">
          <div class="ppi-field">
            <label class="ppi-label ppi-label--muted" for="ppi-username">{{ $t('profile.username') }}</label>
            <div class="ppi-input-wrap">
              <input
                id="ppi-username"
                class="ppi-input ppi-input--readonly"
                :value="form.username"
                readonly
                type="text"
              />
              <span class="material-symbols-outlined ppi-lock-icon">lock</span>
            </div>
          </div>
          <div class="ppi-field">
            <label class="ppi-label ppi-label--muted" for="ppi-email">Email Address</label>
            <div class="ppi-input-wrap">
              <input
                id="ppi-email"
                class="ppi-input ppi-input--readonly"
                :value="form.email"
                readonly
                type="email"
              />
              <span class="material-symbols-outlined ppi-lock-icon">lock</span>
            </div>
          </div>
        </div>

        <!-- Divider -->
        <div class="ppi-divider"></div>

        <!-- Phone Number (editable) -->
        <div class="ppi-phone-group">
          <label class="ppi-label ppi-label--dark" for="ppi-phone">{{ $t('profile.phone') }}</label>
          <div class="ppi-input-wrap">
            <span class="material-symbols-outlined ppi-phone-icon">phone</span>
            <input
              id="ppi-phone"
              class="ppi-input ppi-input--phone"
              :value="form.phone"
              @input="(e) => (form as any).phone = (e.target as HTMLInputElement).value"
              type="tel"
              placeholder="+86 138 0000 0000"
            />
          </div>
          <p class="ppi-hint">{{ $t('profile.phoneVerification') }}</p>
        </div>

        <!-- Save Button -->
        <div class="ppi-actions">
          <button class="ppi-save-btn" type="button" @click="handleSave">
            <span class="material-symbols-outlined">save</span>
            {{ $t('profile.saveChanges') }}
          </button>
        </div>
      </form>
    </div>

    <!-- Right column -->
    <div class="ppi-right-col">

      <!-- Avatar Card -->
      <div class="ppi-card ppi-avatar-card">
        <div class="ppi-avatar-wrap">
          <div class="ppi-avatar">
            <img 
              src="https://pub-a9d701ee7be14d628bd79fc511b4e290.r2.dev/front/default_avatar.png" 
              alt="User Avatar"
              class="ppi-avatar-img"
            />
          </div>
        </div>
        <h3 class="ppi-display-name">{{ form.username || 'Alex Chen' }}</h3>
        <span class="ppi-verified-badge">
          <span class="material-symbols-outlined ppi-verified-icon">verified</span>
          Verified Account
        </span>
      </div>

      <!-- Security Card -->
      <div class="ppi-card ppi-security-card">
        <h4 class="ppi-security-title">
          <span class="material-symbols-outlined ppi-security-icon">security</span>
          {{ $t('profile.accountSecurity') }}
        </h4>
        <p class="ppi-security-desc">
          {{ $t('profile.accountSecurityDesc') }}
        </p>
        <a class="ppi-security-link" href="#" @click.prevent="$emit('openPassword')">
          {{ $t('profile.updateSecuritySettings') }}
          <span class="material-symbols-outlined ppi-arrow-icon">arrow_forward</span>
        </a>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n({ useScope: 'global' })

interface FormData {
  username: string
  email: string
  phone: string
}

defineProps<{
  form: FormData
}>()

const emit = defineEmits<{
  save: []
  openPassword: []
}>()

function handleSave() {
  emit('save')
}

defineExpose({ handleSave })
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap');

/* ===== BENTO LAYOUT ===== */
.ppi-bento {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

@media (min-width: 1024px) {
  .ppi-bento {
    grid-template-columns: 2fr 1fr;
    align-items: start;
  }
}

/* ===== GLASS CARD ===== */
.ppi-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid #E5E5E7;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  padding: 32px;
}

/* ===== FORM CARD ===== */
.ppi-form-card form {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* Readonly row: 2 columns */
.ppi-readonly-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  padding-bottom: 24px;
}

@media (max-width: 640px) {
  .ppi-readonly-row {
    grid-template-columns: 1fr;
  }
}

.ppi-divider {
  border: none;
  border-top: 1px solid rgba(193, 198, 214, 0.5);
  margin: 0 0 24px;
}

.ppi-phone-group {
  padding-top: 4px;
}

/* Field */
.ppi-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* Labels */
.ppi-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.02em;
  line-height: 1.4;
  margin-bottom: 4px;
}

.ppi-label--muted {
  color: #414753;
}

.ppi-label--dark {
  color: #1a1b1f;
}

/* Input wrap */
.ppi-input-wrap {
  position: relative;
}

/* Inputs */
.ppi-input {
  width: 100%;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 17px;
  font-family: inherit;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.ppi-input--readonly {
  background: #f4f3f8;
  border: 1px solid #c1c6d6;
  color: #1a1b1f;
  cursor: not-allowed;
  padding-right: 44px;
}

.ppi-input--phone {
  background: #ffffff;
  border: 1px solid #c1c6d6;
  color: #1a1b1f;
  padding-left: 40px;
}

.ppi-input--phone:focus {
  border-color: #0071e3;
  box-shadow: 0 0 0 2px rgba(0, 113, 227, 0.15);
}

.ppi-input--phone::placeholder {
  color: #717785;
}

/* Lock icon */
.ppi-lock-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 20px;
  color: #717785;
  pointer-events: none;
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

/* Phone icon */
.ppi-phone-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 20px;
  color: #717785;
  pointer-events: none;
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

/* Hint */
.ppi-hint {
  font-size: 12px;
  font-weight: 600;
  color: #414753;
  margin: 8px 0 0 0;
  line-height: 1.4;
}

/* Actions */
.ppi-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 24px;
}

/* Save button */
.ppi-save-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #0059b5;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.02em;
  cursor: pointer;
  transition: background 0.2s, box-shadow 0.2s;
  font-family: inherit;
}

.ppi-save-btn:hover {
  background: #005cbb;
  box-shadow: 0 4px 12px rgba(0, 89, 181, 0.3);
}

.ppi-save-btn .material-symbols-outlined {
  font-size: 18px;
  font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

/* ===== RIGHT COLUMN ===== */
.ppi-right-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Avatar card */
.ppi-avatar-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 32px 24px;
}

.ppi-avatar-wrap {
  position: relative;
  margin-bottom: 16px;
}

.ppi-avatar {
  width: 128px;
  height: 128px;
  border-radius: 9999px;
  background: #eeedf3;
  border: 4px solid #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.ppi-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 9999px;
}

.ppi-display-name {
  font-size: 32px;
  font-weight: 600;
  color: #1a1b1f;
  margin: 0 0 12px 0;
  line-height: 1.3;
}

.ppi-verified-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #e9e7ed;
  color: #414753;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 9999px;
}

.ppi-verified-icon {
  font-size: 14px;
  color: #414753;
  font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

/* Security card */
.ppi-security-card {
  background: rgba(0, 89, 181, 0.03);
  border-color: rgba(0, 89, 181, 0.2);
}

.ppi-security-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.02em;
  color: #1a1b1f;
  margin: 0 0 12px 0;
}

.ppi-security-icon {
  font-size: 20px;
  color: #0059b5;
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}

.ppi-security-desc {
  font-size: 12px;
  font-weight: 600;
  color: #414753;
  margin: 0 0 16px 0;
  line-height: 1.6;
}

.ppi-security-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #0059b5;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.02em;
  text-decoration: none;
  transition: color 0.2s;
}

.ppi-security-link:hover {
  color: #005cbb;
}

.ppi-arrow-icon {
  font-size: 16px;
  font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
}
</style>
