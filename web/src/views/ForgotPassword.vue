<template>
  <AuthShell
    :eyebrow="$t('forgotPassword.formEyebrow')"
    :title="$t('forgotPassword.title')"
    :intro="$t('forgotPassword.subtitle')"
    title-id="forgot-password-title"
  >
    <form class="auth-form" @submit.prevent="handleSubmit" aria-labelledby="forgot-password-title">
      <AuthFormField :label="$t('forgotPassword.email')" :error="errors.email" field-id="fp-email">
        <template #default="{ inputId }">
          <div class="input-affix" :class="{ 'input-affix--error': !!errors.email }">
            <span class="input-affix__prefix" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4-8 5L4 8V6l8 5 8-5v2z" />
              </svg>
            </span>
            <input
              :id="inputId"
              v-model="form.email"
              type="email"
              class="form-input"
              :placeholder="$t('forgotPassword.emailPlaceholder')"
              autocomplete="email"
              @input="errors.email = ''"
            />
          </div>
        </template>
      </AuthFormField>

      <AuthFormField :label="$t('forgotPassword.verificationCode')" :error="errors.verificationCode" field-id="fp-code">
        <template #default="{ inputId }">
          <div class="verification-input-group">
            <input
              :id="inputId"
              v-model="form.verificationCode"
              type="text"
              inputmode="numeric"
              maxlength="6"
              class="form-input-plain"
              :placeholder="$t('forgotPassword.verificationCodePlaceholder')"
              autocomplete="one-time-code"
              @input="errors.verificationCode = ''"
            />
            <button
              type="button"
              class="send-code-btn"
              :class="{ 'send-code-btn--loading': sendingCode }"
              :disabled="sendCodeDisabled || sendingCode || !form.email"
              @click="sendVerificationCode"
            >
              <span v-if="sendingCode" class="send-code-spinner" aria-hidden="true" />
              <span>{{ sendCodeButtonText }}</span>
            </button>
          </div>
          <div class="verification-help">
            {{ $t('forgotPassword.noCode') }}
            <button
              type="button"
              class="resend-btn"
              :disabled="resendDisabled || sendingCode"
              @click="sendVerificationCode"
            >
              {{ resendButtonText }}
            </button>
          </div>
        </template>
      </AuthFormField>

      <div class="form-group-grid">
        <AuthFormField :label="$t('forgotPassword.newPassword')" :error="errors.newPassword" field-id="fp-password">
          <template #default="{ inputId }">
            <input
              :id="inputId"
              v-model="form.newPassword"
              type="password"
              class="form-input-plain"
              :placeholder="$t('forgotPassword.newPasswordPlaceholder')"
              autocomplete="new-password"
              @input="errors.newPassword = ''"
            />
          </template>
        </AuthFormField>
        <AuthFormField :label="$t('forgotPassword.confirmPassword')" :error="errors.confirmPassword" field-id="fp-confirm">
          <template #default="{ inputId }">
            <input
              :id="inputId"
              v-model="form.confirmPassword"
              type="password"
              class="form-input-plain"
              :placeholder="$t('forgotPassword.confirmPasswordPlaceholder')"
              autocomplete="new-password"
              @input="errors.confirmPassword = ''"
            />
          </template>
        </AuthFormField>
      </div>

      <button type="submit" class="submit-btn" :disabled="loading">
        <span v-if="!loading">{{ $t('forgotPassword.submitBtn') }}</span>
        <span v-else class="loading-spinner">
          <span class="spinner" />
          {{ $t('forgotPassword.submitting') }}
        </span>
      </button>
    </form>

    <template #switch>
      {{ $t('forgotPassword.rememberPassword') }}
      <router-link to="/login">{{ $t('forgotPassword.backToLogin') }}</router-link>
    </template>
  </AuthShell>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { authApi } from '@/api/auth'
import AuthShell from '@/components/auth/AuthShell.vue'
import AuthFormField from '@/components/auth/AuthFormField.vue'
import { ElMessage } from 'element-plus'

const { t } = useI18n({ useScope: 'global' })
const router = useRouter()

const loading = ref(false)
const sendingCode = ref(false)

const sendCodeDisabled = ref(false)
const resendDisabled = ref(true)
const sendCodeCountdown = ref(60)
const resendCountdown = ref(0)
let sendCodeTimer: ReturnType<typeof setInterval> | null = null
let resendTimer: ReturnType<typeof setInterval> | null = null

const form = reactive({
  email: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

const errors = reactive({
  email: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

const sendCodeButtonText = computed(() => {
  if (sendingCode.value) return t('forgotPassword.sendingCode')
  if (sendCodeCountdown.value < 60 && sendCodeCountdown.value > 0) {
    return t('forgotPassword.resendIn', { n: sendCodeCountdown.value })
  }
  return t('forgotPassword.sendCode')
})

const resendButtonText = computed(() => {
  if (resendCountdown.value > 0) return t('forgotPassword.resendIn', { n: resendCountdown.value })
  return t('forgotPassword.resend')
})

function validateEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

function startCodeCountdown() {
  sendCodeDisabled.value = true
  sendCodeCountdown.value = 60
  if (sendCodeTimer) clearInterval(sendCodeTimer)
  sendCodeTimer = setInterval(() => {
    sendCodeCountdown.value--
    if (sendCodeCountdown.value <= 0) {
      clearInterval(sendCodeTimer!)
      sendCodeDisabled.value = false
      sendCodeCountdown.value = 60
    }
  }, 1000)

  resendCountdown.value = 5
  resendDisabled.value = true
  if (resendTimer) clearInterval(resendTimer)
  resendTimer = setInterval(() => {
    resendCountdown.value--
    if (resendCountdown.value <= 0) {
      clearInterval(resendTimer!)
      resendDisabled.value = false
    }
  }, 1000)
}

async function sendVerificationCode() {
  if (sendingCode.value || sendCodeDisabled.value) return
  if (!form.email) {
    errors.email = t('forgotPassword.inputEmail')
    return
  }
  if (!validateEmail(form.email)) {
    errors.email = t('forgotPassword.validEmail')
    return
  }

  sendingCode.value = true
  try {
    const res: any = await authApi.sendResetCode(form.email.trim())
    if (res.code === 200) {
      ElMessage.success(t('forgotPassword.codeSent'))
      startCodeCountdown()
    } else {
      ElMessage.error(res.message || t('forgotPassword.sendCodeFailed'))
    }
  } catch (error: any) {
    ElMessage.error(error.message || t('forgotPassword.sendCodeFailed'))
  } finally {
    sendingCode.value = false
  }
}

function validateForm(): boolean {
  let ok = true
  errors.email = ''
  errors.verificationCode = ''
  errors.newPassword = ''
  errors.confirmPassword = ''

  if (!form.email) {
    errors.email = t('forgotPassword.inputEmail')
    ok = false
  } else if (!validateEmail(form.email)) {
    errors.email = t('forgotPassword.validEmail')
    ok = false
  }

  if (!form.verificationCode) {
    errors.verificationCode = t('forgotPassword.inputVerificationCode')
    ok = false
  } else if (form.verificationCode.length !== 6) {
    errors.verificationCode = t('forgotPassword.verificationCodeLength')
    ok = false
  }

  if (!form.newPassword) {
    errors.newPassword = t('forgotPassword.inputPassword')
    ok = false
  } else if (form.newPassword.length < 6) {
    errors.newPassword = t('forgotPassword.passwordMinLength')
    ok = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = t('forgotPassword.inputConfirmPassword')
    ok = false
  } else if (form.newPassword !== form.confirmPassword) {
    errors.confirmPassword = t('forgotPassword.passwordMismatch')
    ok = false
  }

  return ok
}

async function handleSubmit() {
  if (!validateForm()) return
  loading.value = true
  try {
    const res: any = await authApi.resetPassword({
      email: form.email.trim(),
      verificationCode: form.verificationCode.trim(),
      newPassword: form.newPassword
    })
    if (res.code === 200) {
      ElMessage.success(t('forgotPassword.resetSuccess'))
      router.push('/login')
    } else {
      ElMessage.error(res.message || t('forgotPassword.resetFailed'))
    }
  } catch (error: any) {
    ElMessage.error(error.message || t('forgotPassword.resetFailed'))
  } finally {
    loading.value = false
  }
}
</script>
