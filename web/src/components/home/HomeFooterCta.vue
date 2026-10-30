<template>
  <section class="home-footer-cta" aria-labelledby="home-footer-cta-title">
    <div class="home-footer-cta__divider" aria-hidden="true" />
    <div class="container home-footer-cta__inner">
      <h2 id="home-footer-cta-title" class="home-footer-cta__title">
        {{ $t('home.footerCta.title') }}
      </h2>
      <router-link to="/contact" class="home-footer-cta__btn">
        {{ $t('home.footerCta.contact') }}
      </router-link>
    </div>
  </section>
</template>

<style scoped>
.home-footer-cta {
  position: relative;
  z-index: 2;
  background: #ffffff;
  /* 底部留白：避免 SiteFooter 波浪负边距压住本区域 */
  padding: 0 16px 24px;
  margin-bottom: 28px;
}

.home-footer-cta__divider {
  height: 1px;
  max-width: 100%;
  margin: 0 auto 20px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    #e4e6ea 12%,
    #e4e6ea 88%,
    transparent 100%
  );
}

.home-footer-cta__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  max-width: var(--site-content-max-width, 1300px);
  min-height: 44px;
  overflow: visible;
}

.home-footer-cta__title {
  margin: 0;
  flex: 1;
  min-width: 200px;
  font-size: clamp(16px, 2.2vw, 22px);
  font-weight: 500;
  line-height: 1.4;
  color: #292b2e;
}

.home-footer-cta__btn {
  flex-shrink: 0;
  padding: 10px 20px;
  border-radius: 10px;
  background: linear-gradient(104.52deg, #e12b57 0%, #c8094e 100%);
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: filter 0.2s, transform 0.15s;
}

.home-footer-cta__btn:hover {
  filter: brightness(1.05);
  transform: translateY(-1px);
  color: #ffffff;
}

@media (max-width: 768px) {
  .home-footer-cta__inner {
    flex-direction: column;
    align-items: flex-start;
  }

  .home-footer-cta__btn {
    width: 100%;
    text-align: center;
  }
}
</style>
