<template>
  <AuthShell
    size="tall"
    :eyebrow="$t('register.formEyebrow')"
    :title="$t('register.createAccount')"
    :intro="$t('register.registerSubtitle')"
    title-id="register-title"
  >
    <form class="auth-form" @submit.prevent="handleRegister" aria-labelledby="register-title">
      <AuthFormField :label="$t('register.username')" :error="errors.username" field-id="reg-username">
        <template #default="{ inputId }">
          <div class="input-affix" :class="{ 'input-affix--error': !!errors.username }">
            <span class="input-affix__prefix" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 12c2.7 0 4.8-2.1 4.8-4.8S14.7 2.4 12 2.4 7.2 4.5 7.2 7.2 9.3 12 12 12zm0 2.4c-3.2 0-9.6 1.6-9.6 4.8V22h19.2v-2.8c0-3.2-6.4-4.8-9.6-4.8z" />
              </svg>
            </span>
            <input
              :id="inputId"
              v-model="form.username"
              type="text"
              class="form-input"
              :placeholder="$t('register.usernamePlaceholder')"
              autocomplete="username"
              @input="errors.username = ''"
            />
          </div>
        </template>
      </AuthFormField>

      <AuthFormField :label="$t('register.email')" :error="errors.email" field-id="reg-email">
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
              :placeholder="$t('register.emailPlaceholder')"
              autocomplete="email"
              @input="errors.email = ''"
            />
          </div>
        </template>
      </AuthFormField>

      <AuthFormField :label="$t('register.verificationCode')" :error="errors.verificationCode" field-id="reg-code">
        <template #default="{ inputId }">
          <div class="verification-input-group">
            <input
              :id="inputId"
              v-model="form.verificationCode"
              type="text"
              inputmode="numeric"
              maxlength="6"
              class="form-input-plain"
              :placeholder="$t('register.verificationCodePlaceholder')"
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
            {{ $t('register.noCode') }}
            <button
              type="button"
              class="resend-btn"
              :disabled="resendDisabled || sendingCode"
              @click="resendVerificationCode"
            >
              {{ resendButtonText }}
            </button>
          </div>
        </template>
      </AuthFormField>

      <div class="form-group-grid">
        <AuthFormField :label="$t('register.password')" :error="errors.password" field-id="reg-password">
          <template #default="{ inputId }">
            <input
              :id="inputId"
              v-model="form.password"
              type="password"
              class="form-input-plain"
              :placeholder="$t('register.passwordPlaceholder')"
              autocomplete="new-password"
              @input="errors.password = ''"
            />
          </template>
        </AuthFormField>
        <AuthFormField :label="$t('register.confirmPassword')" :error="errors.confirmPassword" field-id="reg-confirm-password">
          <template #default="{ inputId }">
            <input
              :id="inputId"
              v-model="form.confirmPassword"
              type="password"
              class="form-input-plain"
              :placeholder="$t('register.confirmPasswordPlaceholder')"
              autocomplete="new-password"
              @input="errors.confirmPassword = ''"
            />
          </template>
        </AuthFormField>
      </div>

      <AuthTermsConsent v-model="form.agreeTerms" :error="termsError">
        {{ $t('register.agreeTerms') }}
        <router-link to="/terms" class="policy-link" @click.stop>{{ $t('footer.terms') }}</router-link>
        {{ $t('register.and') }}
        <router-link to="/privacy" class="policy-link" @click.stop>{{ $t('footer.privacy') }}</router-link>
      </AuthTermsConsent>

      <div v-if="referralCode" class="referral-notice">
        {{ $t('register.referralTip') }}
      </div>

      <button type="submit" class="submit-btn" :disabled="loading">
        <span v-if="!loading">{{ $t('register.registerBtn') }}</span>
        <span v-else class="loading-spinner">
          <span class="spinner" />
          {{ $t('register.registering') }}
        </span>
      </button>
    </form>

    <template #switch>
      {{ $t('register.hasAccount') }}
      <router-link to="/login">{{ $t('register.loginNow') }}</router-link>
    </template>
  </AuthShell>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'
import AuthShell from '@/components/auth/AuthShell.vue'
import AuthFormField from '@/components/auth/AuthFormField.vue'
import AuthTermsConsent from '@/components/auth/AuthTermsConsent.vue'
import { ElMessage } from 'element-plus'

const { t } = useI18n({ useScope: 'global' })
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const referralCode = ref('')
const termsError = ref('')
const sendingCode = ref(false)

const sendCodeDisabled = ref(false)
const resendDisabled = ref(true)
const sendCodeCountdown = ref(60)
const resendCountdown = ref(0)
let sendCodeTimer: ReturnType<typeof setInterval> | null = null
let resendTimer: ReturnType<typeof setInterval> | null = null

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  verificationCode: '',
  agreeTerms: false
})

const errors = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  verificationCode: ''
})

const sendCodeButtonText = computed(() => {
  if (sendingCode.value) {
    return t('register.sendingCode')
  }
  if (sendCodeCountdown.value < 60 && sendCodeCountdown.value > 0) {
    return t('register.resendIn', { n: sendCodeCountdown.value })
  }
  return t('register.sendCode')
})

watch(() => form.agreeTerms, (checked) => {
  if (checked) termsError.value = ''
})

const resendButtonText = computed(() => {
  if (resendCountdown.value > 0) {
    return t('register.resendIn', { n: resendCountdown.value })
  }
  return t('register.resend')
})

function validateEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

function validateUsername(username: string): boolean {
  return username.length >= 3 && username.length <= 20
}

function validatePassword(password: string): boolean {
  return password.length >= 6
}

async function sendVerificationCode() {
  if (sendingCode.value || sendCodeDisabled.value) {
    return
  }

  if (!form.email) {
    errors.email = t('register.inputEmail')
    return
  }

  if (!validateEmail(form.email)) {
    errors.email = t('register.validEmail')
    return
  }

  sendingCode.value = true
  try {
    const res: any = await authApi.sendRegisterCode(form.email)
    if (res.code === 200) {
      ElMessage.success(t('register.codeSent'))
      startCodeCountdown()
    } else {
      ElMessage.error(res.message || t('register.sendCodeFailed'))
    }
  } catch (error: any) {
    ElMessage.error(error.message || t('register.sendCodeFailed'))
  } finally {
    sendingCode.value = false
  }
}

async function resendVerificationCode() {
  await sendVerificationCode()
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
      resendCountdown.value = 0
    }
  }, 1000)
}

function validateForm(): boolean {
  let isValid = true

  errors.username = ''
  errors.email = ''
  errors.password = ''
  errors.confirmPassword = ''
  errors.verificationCode = ''
  termsError.value = ''

  if (!form.username) {
    errors.username = t('register.inputUsername')
    isValid = false
  } else if (!validateUsername(form.username)) {
    errors.username = t('register.usernameLength')
    isValid = false
  }

  if (!form.email) {
    errors.email = t('register.inputEmail')
    isValid = false
  } else if (!validateEmail(form.email)) {
    errors.email = t('register.validEmail')
    isValid = false
  }

  if (!form.verificationCode) {
    errors.verificationCode = t('register.inputVerificationCode')
    isValid = false
  } else if (form.verificationCode.length !== 6) {
    errors.verificationCode = t('register.verificationCodeLength')
    isValid = false
  }

  if (!form.password) {
    errors.password = t('register.inputPassword')
    isValid = false
  } else if (!validatePassword(form.password)) {
    errors.password = t('register.passwordLength')
    isValid = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = t('register.confirmPwd')
    isValid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = t('register.pwdMismatch')
    isValid = false
  }

  if (!form.agreeTerms) {
    termsError.value = t('auth.agreeTermsRequired')
    isValid = false
  }

  return isValid
}

async function handleRegister() {
  if (!validateForm()) {
    return
  }

  loading.value = true
  try {
    await authStore.register({
      username: form.username,
      email: form.email,
      password: form.password,
      verificationCode: form.verificationCode,
      phone: '',
      referralCode: referralCode.value || undefined
    })

    ElMessage.success(t('register.registerSuccess'))

    const redirect = route.query.redirect as string
    if (redirect) {
      router.push(redirect)
    } else {
      router.push('/')
    }
  } catch (error: any) {
    ElMessage.error(error.message || t('register.registerFailed'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const ref = route.query.ref as string
  if (ref) {
    referralCode.value = ref.toUpperCase()
  }
})
</script>
