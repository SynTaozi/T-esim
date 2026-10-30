<template>
  <AuthShell
    :eyebrow="$t('login.formEyebrow')"
    :title="$t('login.welcomeBack')"
    :intro="$t('login.loginSubtitle')"
    title-id="login-title"
  >
    <form class="auth-form" @submit.prevent="handleLogin" aria-labelledby="login-title">
      <AuthFormField :label="$t('login.username')" :error="errors.username" field-id="login-username">
        <template #default="{ inputId }">
          <div class="input-affix" :class="{ 'input-affix--error': !!errors.username }">
            <span class="input-affix__prefix" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4-8 5L4 8V6l8 5 8-5v2z" />
              </svg>
            </span>
            <input
              :id="inputId"
              v-model="form.username"
              type="text"
              class="form-input"
              :placeholder="$t('login.usernamePlaceholder')"
              autocomplete="username"
              @input="errors.username = ''"
            />
          </div>
        </template>
      </AuthFormField>

      <AuthFormField :label="$t('login.password')" :error="errors.password" field-id="login-password">
        <template #default="{ inputId }">
          <div class="input-affix" :class="{ 'input-affix--error': !!errors.password }">
            <span class="input-affix__prefix" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9V6zm9 14H6V10h12v10zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z" />
              </svg>
            </span>
            <input
              :id="inputId"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="form-input"
              :placeholder="$t('login.passwordPlaceholder')"
              autocomplete="current-password"
              @input="errors.password = ''"
            />
            <button
              type="button"
              class="input-affix__suffix"
              :aria-label="showPassword ? $t('login.hidePassword') : $t('login.showPassword')"
              :aria-pressed="showPassword"
              @click="showPassword = !showPassword"
            >
              <svg v-if="!showPassword" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 6a9.77 9.77 0 0 1 8.82 5.5A9.77 9.77 0 0 1 12 17a9.77 9.77 0 0 1-8.82-5.5A9.77 9.77 0 0 1 12 6m0-2C7 4 2.73 7.11 1 11.5 2.73 15.89 7 19 12 19s9.27-3.11 11-7.5C21.27 7.11 17 4 12 4zm0 5a2.5 2.5 0 1 1 0 5 2.5 2.5 0 0 1 0-5z" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 6a9.77 9.77 0 0 1 8.82 5.5 9.56 9.56 0 0 1-1.68 2.48l1.46 1.46C22.19 13.88 23 12.75 23 11.5 21.27 7.11 17 4 12 4c-1.3 0-2.54.24-3.68.67l1.62 1.62c.66-.18 1.35-.29 2.06-.29zm-9.5.1L1.11 4.7l1.41-1.41 18.19 18.19-1.41 1.41-3.1-3.1A10.2 10.2 0 0 1 12 19c-5 0-9.27-3.11-11-7.5.8-2.03 2.2-3.77 3.97-5.07L2.5 6.1zm5.8 5.8A2.5 2.5 0 0 0 12 14.5c.3 0 .58-.05.84-.14l-3.04-3.04z" />
              </svg>
            </button>
          </div>
        </template>
      </AuthFormField>

      <div class="auth-form-meta">
        <label class="secure-check" :class="{ 'secure-check--error': !!termsError }">
          <input v-model="form.agreeTerms" type="checkbox" />
          <span>{{ $t('login.secureSession') }}</span>
        </label>
        <router-link to="/forgot-password">{{ $t('login.forgotPassword') }}</router-link>
      </div>
      <p v-if="termsError" class="terms-hint" role="alert">
        {{ termsError }}
        <router-link to="/terms">{{ $t('footer.terms') }}</router-link>
        {{ $t('login.and') }}
        <router-link to="/privacy">{{ $t('footer.privacy') }}</router-link>
      </p>
      <p v-else class="terms-note">
        {{ $t('login.agreeTerms') }}
        <router-link to="/terms">{{ $t('footer.terms') }}</router-link>
        {{ $t('login.and') }}
        <router-link to="/privacy">{{ $t('footer.privacy') }}</router-link>
      </p>

      <button type="submit" class="submit-btn" :disabled="loading">
        <span v-if="!loading">{{ $t('login.loginBtn') }}</span>
        <span v-else class="loading-spinner">
          <span class="spinner" />
          {{ $t('login.loggingIn') }}
        </span>
      </button>
    </form>

    <template #switch>
      {{ $t('login.noAccount') }}
      <router-link to="/register">{{ $t('login.registerNow') }}</router-link>
    </template>
  </AuthShell>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import AuthShell from '@/components/auth/AuthShell.vue'
import AuthFormField from '@/components/auth/AuthFormField.vue'
import { ElMessage } from 'element-plus'
import { resolvePostLoginRedirect } from '@/utils/authNavigation'

const { t } = useI18n({ useScope: 'global' })
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const showPassword = ref(false)

const form = reactive({
  username: '',
  password: '',
  agreeTerms: false
})

const errors = reactive({
  username: '',
  password: ''
})

const termsError = ref('')

watch(() => form.agreeTerms, (checked) => {
  if (checked) termsError.value = ''
})

function validatePassword(password: string): boolean {
  return password.length >= 6
}

function validateForm(): boolean {
  let isValid = true

  errors.username = ''
  errors.password = ''
  termsError.value = ''

  if (!form.username) {
    errors.username = t('login.inputUsername')
    isValid = false
  }

  if (!form.password) {
    errors.password = t('login.inputPassword')
    isValid = false
  } else if (!validatePassword(form.password)) {
    errors.password = t('login.passwordMinLength')
    isValid = false
  }

  if (!form.agreeTerms) {
    termsError.value = t('auth.agreeTermsRequired')
    isValid = false
  }

  return isValid
}

async function handleLogin() {
  if (!validateForm()) {
    return
  }

  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success(t('login.loginSuccess'))
    const redirect = resolvePostLoginRedirect(route.query.redirect)
    router.push(redirect)
  } catch (error: any) {
    ElMessage.error(error.message || t('login.loginFailed'))
  } finally {
    loading.value = false
  }
}
</script>
