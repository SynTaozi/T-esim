<template>
  <div class="ppi">
    <div class="ppi-split">
      <section class="ppi-card">
        <div class="ppi-card-h">
          <h2>{{ $t('profile.personalInfo') }}</h2>
          <p>{{ $t('profile.phoneVerification') }}</p>
        </div>
        <div class="ppi-group">
          <div class="ppi-row">
            <span class="ppi-label">{{ $t('profile.username') }}</span>
            <span class="ppi-value">{{ form.username }}</span>
          </div>
          <div class="ppi-row">
            <span class="ppi-label">Email</span>
            <span class="ppi-value">{{ form.email }}</span>
          </div>
          <div class="ppi-row">
            <span class="ppi-label">{{ $t('profile.phone') }}</span>
            <input
              class="ppi-input"
              type="tel"
              :value="form.phone"
              placeholder="+86 138 0000 0000"
              @input="(e) => (form as any).phone = (e.target as HTMLInputElement).value"
            />
          </div>
        </div>
        <button class="ppi-save" type="button" @click="handleSave">{{ $t('profile.saveChanges') }}</button>
      </section>

      <section class="ppi-card ppi-secure">
        <h3>{{ $t('profile.accountSecurity') }}</h3>
        <p>{{ $t('profile.accountSecurityDesc') }}</p>
        <a href="#" @click.prevent="$emit('openPassword')">
          {{ $t('profile.updateSecuritySettings') }} →
        </a>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
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
.ppi {
  --blue: #0071e3;
  --text: #1d1d1f;
  --muted: #6e6e73;
  --line: #d2d2d7;
  --fill: #f5f5f7;
  --shadow: 0 1px 2px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(0, 0, 0, 0.04);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.ppi-split {
  display: grid;
  grid-template-columns: 1.3fr 0.9fr;
  gap: 12px;
}

.ppi-card {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 18px;
}

.ppi-card-h {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.ppi-card-h h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.ppi-card-h p {
  margin: 0;
  color: var(--muted);
  font-size: 12px;
}

.ppi-group {
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.ppi-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 48px;
  padding: 12px 14px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.ppi-row:last-child {
  border-bottom: 0;
}

.ppi-label {
  font-size: 14px;
  color: var(--text);
  flex-shrink: 0;
}

.ppi-value {
  font-size: 14px;
  color: var(--muted);
  text-align: right;
  word-break: break-all;
}

.ppi-input {
  border: 0;
  outline: none;
  background: transparent;
  text-align: right;
  width: min(220px, 48vw);
  font: 400 14px inherit;
  color: var(--text);
}

.ppi-save {
  margin-top: 14px;
  height: 34px;
  padding: 0 16px;
  border: 0;
  border-radius: 980px;
  background: var(--blue);
  color: #fff;
  font: 600 13px inherit;
  cursor: pointer;
}

.ppi-save:hover {
  background: #0077ed;
}

.ppi-secure {
  background: linear-gradient(165deg, #1d1d1f, #2c2c2e);
  color: #fff;
  border: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.ppi-secure h3 {
  margin: 0 0 8px;
  font-size: 15px;
}

.ppi-secure p {
  margin: 0 0 16px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  line-height: 1.55;
}

.ppi-secure a {
  color: #6cb3ff;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
}

@media (max-width: 860px) {
  .ppi-split {
    grid-template-columns: 1fr;
  }
}
</style>
