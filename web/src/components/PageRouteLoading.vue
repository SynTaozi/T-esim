<template>
  <div class="route-loading" aria-live="polite" aria-busy="true">
    <div class="route-loading__bar" />
    <div class="route-loading__spinner-wrap">
      <span class="route-loading__spinner" />
    </div>
  </div>
</template>

<style scoped>
.route-loading {
  position: fixed;
  inset: 0;
  z-index: 9998;
  pointer-events: none;
}

.route-loading__bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #0052ff, #0071e3, #0052ff);
  background-size: 200% 100%;
  animation: route-bar 0.9s ease-in-out infinite;
  z-index: 9999;
}

.route-loading__spinner-wrap {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 82, 255, 0.12);
}

.route-loading__spinner {
  width: 22px;
  height: 22px;
  border: 2px solid rgba(0, 82, 255, 0.2);
  border-top-color: #0052ff;
  border-radius: 50%;
  animation: route-spin 0.65s linear infinite;
}

@keyframes route-bar {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}

@keyframes route-spin {
  to { transform: rotate(360deg); }
}
</style>
