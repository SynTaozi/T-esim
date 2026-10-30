<template>
  <div class="auth-page">
    <div class="auth-grid" aria-hidden="true" />
    <div class="auth-orb auth-orb--primary" aria-hidden="true" />
    <div class="auth-orb auth-orb--secondary" aria-hidden="true" />

    <div class="auth-lang">
      <LangSwitch />
    </div>

    <div class="auth-stage" :class="{ 'auth-stage--tall': size === 'tall' }">
      <aside class="auth-context">
        <p class="auth-context__label">{{ $t('login.contextLabel') }}</p>
        <h1>{{ $t('login.brandTitle') }}</h1>
        <p class="auth-context__lead">{{ $t('login.brandSubtitle') }}</p>

        <ul class="auth-features">
          <li v-for="item in featureItems" :key="item.title">
            <svg v-if="item.icon === 'globe'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true">
              <circle cx="12" cy="12" r="9" />
              <path d="M3 12h18M12 3a15 15 0 0 1 4 9 15 15 0 0 1-4 9 15 15 0 0 1-4-9 15 15 0 0 1 4-9z" />
            </svg>
            <svg v-else-if="item.icon === 'bolt'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true">
              <path d="M13 2 4 14h7l-1 8 9-12h-7l1-8z" stroke-linejoin="round" />
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <path d="M9 12l2 2 4-4" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <span>
              <strong>{{ item.title }}</strong>
              <small>{{ item.desc }}</small>
            </span>
          </li>
        </ul>
      </aside>

      <section class="auth-form-column">
        <div class="auth-card">
          <div class="auth-brand">
            <span class="auth-brand__mark" aria-hidden="true">
              <img src="/logo.png" alt="" />
            </span>
            <span class="auth-brand__text">
              <strong>TESIM</strong>
              <small>{{ $t('login.brandTagline') }}</small>
            </span>
          </div>

          <span v-if="eyebrow" class="auth-eyebrow">{{ eyebrow }}</span>
          <h2 :id="titleId">{{ title }}</h2>
          <p v-if="intro" class="auth-intro">{{ intro }}</p>

          <slot />

          <p v-if="$slots.switch" class="auth-switch">
            <slot name="switch" />
          </p>
        </div>
      </section>
    </div>

    <footer class="auth-footer">
      <p>@TESIM 2026, Powered by Tesim LLC</p>
      <nav>
        <router-link to="/terms">{{ $t('footer.terms') }}</router-link>
        <router-link to="/privacy">{{ $t('footer.privacy') }}</router-link>
        <router-link to="/refund-policy">{{ $t('footer.refundPolicy') }}</router-link>
        <router-link to="/disclaimer">{{ $t('footer.disclaimer') }}</router-link>
      </nav>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import LangSwitch from '@/components/LangSwitch.vue'

withDefaults(defineProps<{
  eyebrow?: string
  title: string
  intro?: string
  titleId?: string
  size?: 'default' | 'tall'
}>(), {
  size: 'default'
})

const { t } = useI18n({ useScope: 'global' })

const featureItems = computed(() => [
  { icon: 'globe', title: t('login.feature1Title'), desc: t('login.feature1Desc') },
  { icon: 'bolt', title: t('login.feature2Title'), desc: t('login.feature2Desc') },
  { icon: 'shield', title: t('login.feature3Title'), desc: t('login.feature3Desc') }
])
</script>

<style scoped>
.auth-page {
  --auth-bg: #eef3f9;
  --auth-accent: #0b63ce;
  --auth-accent-strong: #084fa7;
  --auth-text: #172033;
  --auth-muted: #68758a;
  --auth-border: #d8e1ee;

  isolation: isolate;
  position: relative;
  box-sizing: border-box;
  height: 100svh;
  min-height: 100svh;
  max-height: 100svh;
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  place-items: center;
  padding: clamp(16px, 3vw, 36px) 24px 12px;
  background: var(--auth-bg);
  overflow: hidden;
}

.auth-grid {
  position: absolute;
  inset: 0;
  z-index: -3;
  background-image:
    linear-gradient(rgba(33, 104, 219, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(33, 104, 219, 0.04) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: linear-gradient(#000 0%, transparent 84%);
  -webkit-mask-image: linear-gradient(#000 0%, transparent 84%);
}

.auth-orb {
  position: absolute;
  z-index: -2;
  aspect-ratio: 1;
  width: min(52vw, 700px);
  border-radius: 50%;
  pointer-events: none;
  filter: blur(2px);
}

.auth-orb--primary {
  top: -38%;
  left: -14%;
  background: radial-gradient(circle, rgba(30, 95, 211, 0.18), transparent 68%);
}

.auth-orb--secondary {
  right: -18%;
  bottom: -42%;
  background: radial-gradient(circle, rgba(15, 159, 110, 0.13), transparent 68%);
}

.auth-lang {
  position: fixed;
  z-index: 20;
  top: 22px;
  right: 22px;
  display: flex;
  padding: 4px;
  border-radius: 10px;
  border: 1px solid #dbe3ef;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(12px);
  box-shadow: 0 8px 24px rgba(16, 29, 53, 0.08);
}

.auth-lang :deep(.lang-toggle) {
  border: none;
  box-shadow: none;
}

.auth-stage {
  position: relative;
  z-index: 1;
  width: min(100%, 1040px);
  height: min(640px, calc(100svh - 88px));
  min-width: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(420px, 0.88fr);
  grid-template-rows: minmax(0, 1fr);
  border: 1px solid var(--auth-border);
  border-radius: 22px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(18px);
  box-shadow: 0 32px 90px rgba(31, 50, 81, 0.17);
  align-self: center;
}

.auth-stage--tall {
  height: min(820px, calc(100svh - 88px));
}

.auth-context {
  position: relative;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  height: 100%;
  padding: clamp(36px, 5vw, 64px);
  color: #fff;
  background:
    radial-gradient(circle at 86% 12%, rgba(72, 137, 255, 0.3), transparent 18rem),
    linear-gradient(145deg, #101d35, #14294a 62%, #123868);
  overflow: hidden;
}

.auth-context::after {
  content: '';
  position: absolute;
  right: -110px;
  bottom: -160px;
  width: 380px;
  height: 380px;
  border-radius: 50%;
  border: 1px solid rgba(116, 167, 255, 0.17);
  box-shadow:
    0 0 0 48px rgba(116, 167, 255, 0.05),
    0 0 0 96px rgba(116, 167, 255, 0.03);
  pointer-events: none;
}

.auth-context__label {
  margin: 0 0 26px;
  color: #78aaff;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.16em;
}

.auth-context h1 {
  max-width: 520px;
  margin: 0;
  color: #fff;
  font-size: clamp(32px, 4vw, 48px);
  font-weight: 700;
  line-height: 1.16;
  letter-spacing: -0.03em;
}

.auth-context__lead {
  max-width: 480px;
  margin: 18px 0 0;
  color: #b8c5d9;
  font-size: 15px;
  line-height: 1.7;
}

.auth-features {
  z-index: 1;
  position: relative;
  list-style: none;
  margin: 38px 0 0;
  padding: 0;
  display: grid;
  gap: 18px;
}

.auth-features li {
  display: flex;
  align-items: center;
  gap: 13px;
}

.auth-features svg {
  flex: 0 0 22px;
  width: 22px;
  height: 22px;
  color: #6fa5ff;
}

.auth-features span {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.auth-features strong {
  color: #fff;
  font-size: 14px;
  font-weight: 650;
  line-height: 1.35;
}

.auth-features small {
  color: #9daec5;
  font-size: 12px;
  line-height: 1.5;
}

.auth-form-column {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  height: 100%;
  overflow: hidden;
  background: #fff;
}

.auth-card {
  box-sizing: border-box;
  width: 100%;
  min-width: 0;
  min-height: 100%;
  padding: clamp(30px, 5vw, 52px);
  display: flex;
  flex-direction: column;
}

.auth-brand {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  margin-bottom: 28px;
}

.auth-brand__mark {
  flex: 0 0 44px;
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 13px;
  border: 1px solid #dbe4f0;
  background: #fff;
  box-shadow: 0 10px 26px rgba(31, 68, 124, 0.12);
  overflow: hidden;
}

.auth-brand__mark img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.auth-brand__text {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.auth-brand__text strong {
  color: var(--auth-text);
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.1;
}

.auth-brand__text small {
  color: var(--auth-muted);
  font-size: 12px;
  line-height: 1.3;
}

.auth-eyebrow {
  display: block;
  margin-bottom: 8px;
  color: #1e5fd3;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.15em;
}

.auth-card h2 {
  margin: 0 0 8px;
  color: var(--auth-text);
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.035em;
  line-height: 1.2;
}

.auth-intro {
  margin: 0;
  color: var(--auth-muted);
  font-size: 14px;
  line-height: 1.65;
}

.auth-switch {
  margin: auto 0 0;
  padding-top: 22px;
  text-align: center;
  color: #68758a;
  font-size: 13px;
}

.auth-switch :deep(a) {
  margin-left: 6px;
  color: #1556b8;
  font-weight: 650;
  text-decoration: none;
}

.auth-switch :deep(a:hover) {
  text-decoration: underline;
}

.auth-footer {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 7px;
  padding: 10px 0 0;
  text-align: center;
  color: #7b879a;
  font-size: 12px;
}

.auth-footer p {
  margin: 0;
}

.auth-footer nav {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px 16px;
}

.auth-footer a {
  color: #667389;
  text-decoration: none;
}

.auth-footer a:hover {
  color: var(--auth-accent);
}

@media (max-width: 960px) {
  .auth-page {
    height: auto;
    min-height: 100svh;
    max-height: none;
    padding: 12px;
    place-items: start center;
    overflow-y: auto;
  }

  .auth-lang {
    top: 12px;
    right: 12px;
  }

  .auth-stage,
  .auth-stage--tall {
    width: min(100%, 500px);
    height: auto;
    max-height: none;
    grid-template-columns: 1fr;
    border-radius: 16px;
    margin-top: 56px;
  }

  .auth-context {
    display: none;
  }

  .auth-form-column {
    height: auto;
    overflow: visible;
  }

  .auth-card {
    min-height: 0;
    padding: 28px 22px;
  }

  .auth-switch {
    margin-top: 22px;
  }
}
</style>

<!-- Shared form utilities for auth pages -->
<style>
.auth-page {
  --auth-accent: #0b63ce;
  --auth-accent-strong: #084fa7;
  --auth-text: #172033;
}

.auth-page .auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
}

.auth-page .auth-form .auth-field__label {
  color: var(--auth-text);
  font-size: 14px;
  font-weight: 600;
}

.auth-page .input-affix {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  min-height: 44px;
  padding: 0 12px;
  border: 1px solid #c5cedc;
  border-radius: 8px;
  background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.auth-page .input-affix:focus-within {
  border-color: var(--auth-accent);
  box-shadow: 0 0 0 3px rgba(11, 99, 206, 0.12);
}

.auth-page .input-affix--error {
  border-color: #f87171;
  background: #fffbfb;
  box-shadow: 0 0 0 3px rgba(248, 113, 113, 0.12);
}

.auth-page .input-affix__prefix,
.auth-page .input-affix__suffix {
  flex-shrink: 0;
  display: inline-flex;
  color: #8992a4;
}

.auth-page .input-affix__prefix svg,
.auth-page .input-affix__suffix svg {
  width: 16px;
  height: 16px;
}

.auth-page .input-affix__suffix {
  margin: 0;
  padding: 4px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 6px;
}

.auth-page .input-affix__suffix:hover {
  color: var(--auth-accent);
}

.auth-page .form-input {
  flex: 1;
  min-width: 0;
  width: 100%;
  padding: 10px 0;
  border: none;
  outline: none;
  background: transparent;
  color: var(--auth-text);
  font-size: 14px;
  line-height: 22px;
}

.auth-page .form-input::placeholder {
  color: #8992a4;
}

.auth-page .form-input-plain {
  width: 100%;
  min-height: 44px;
  padding: 10px 12px;
  border: 1px solid #c5cedc;
  border-radius: 8px;
  background: #fff;
  color: var(--auth-text);
  font-size: 14px;
  line-height: 22px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.auth-page .form-input-plain:focus {
  border-color: var(--auth-accent);
  box-shadow: 0 0 0 3px rgba(11, 99, 206, 0.12);
}

.auth-page .form-input-plain::placeholder {
  color: #8992a4;
}

.auth-page .auth-field--error .form-input-plain {
  border-color: #f87171;
  background: #fffbfb;
  box-shadow: 0 0 0 3px rgba(248, 113, 113, 0.12);
}

.auth-page .login-form-meta,
.auth-page .auth-form-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: -3px 0 0;
  color: #7b879a;
  font-size: 12px;
}

.auth-page .secure-check {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.auth-page .secure-check input {
  width: 14px;
  height: 14px;
  margin: 0;
  accent-color: var(--auth-accent);
  cursor: pointer;
}

.auth-page .secure-check--error {
  color: #c92929;
}

.auth-page .login-form-meta a,
.auth-page .auth-form-meta a {
  color: #1556b8;
  font-weight: 650;
  text-decoration: none;
}

.auth-page .login-form-meta a:hover,
.auth-page .auth-form-meta a:hover {
  text-decoration: underline;
}

.auth-page .terms-note,
.auth-page .terms-hint {
  margin: -6px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #7b879a;
}

.auth-page .terms-hint {
  color: #c92929;
}

.auth-page .terms-note a,
.auth-page .terms-hint a {
  color: #1556b8;
  font-weight: 650;
  text-decoration: none;
}

.auth-page .terms-note a:hover,
.auth-page .terms-hint a:hover {
  text-decoration: underline;
}

.auth-page .submit-btn {
  width: 100%;
  min-height: 44px;
  margin-top: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 18px;
  border: none;
  border-radius: 8px;
  background: var(--auth-accent);
  color: #fff;
  font-size: 15px;
  font-weight: 650;
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(11, 99, 206, 0.22);
  transition: background 0.2s;
}

.auth-page .submit-btn:hover:not(:disabled) {
  background: var(--auth-accent-strong);
}

.auth-page .submit-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.auth-page .loading-spinner {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.auth-page .spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: auth-spin 0.6s linear infinite;
}

@keyframes auth-spin {
  to { transform: rotate(360deg); }
}

.auth-page .verification-input-group {
  display: flex;
  gap: 10px;
  align-items: stretch;
}

.auth-page .verification-input-group .form-input-plain {
  flex: 1;
}

.auth-page .send-code-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 110px;
  padding: 0 14px;
  border: none;
  border-radius: 8px;
  background: var(--auth-accent);
  color: #fff;
  font-size: 13px;
  font-weight: 650;
  white-space: nowrap;
  cursor: pointer;
}

.auth-page .send-code-btn:hover:not(:disabled) {
  background: var(--auth-accent-strong);
}

.auth-page .send-code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.auth-page .send-code-btn--loading {
  opacity: 0.85;
  cursor: wait;
}

.auth-page .send-code-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: auth-spin 0.65s linear infinite;
  flex-shrink: 0;
}

.auth-page .verification-help {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #68758a;
}

.auth-page .resend-btn {
  padding: 0;
  border: none;
  background: none;
  color: #1556b8;
  font-size: 12px;
  font-weight: 650;
  cursor: pointer;
}

.auth-page .resend-btn:hover:not(:disabled) {
  text-decoration: underline;
}

.auth-page .resend-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.auth-page .form-group-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.auth-page .form-group-grid .auth-field {
  min-width: 0;
}

.auth-page .referral-notice {
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid rgba(11, 99, 206, 0.22);
  background: rgba(11, 99, 206, 0.08);
  color: #1556b8;
  font-size: 13px;
  line-height: 1.45;
}

.auth-page .terms-consent {
  padding: 8px 0;
}

.auth-page .terms-consent__input:checked + .terms-consent__box {
  background: var(--auth-accent);
  border-color: var(--auth-accent);
}

.auth-page .terms-consent__row:hover .terms-consent__box {
  border-color: var(--auth-accent);
}

.auth-page .terms-consent__text .policy-link {
  color: #1556b8;
  font-weight: 650;
}

@media (max-width: 640px) {
  .auth-page .form-group-grid {
    grid-template-columns: 1fr;
  }
}
</style>
