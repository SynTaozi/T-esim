<template>
  <div class="products-page">
    <SiteHeader />

    <!-- 左侧悬浮二级分类：有足够边距时浮在内容左侧，否则改用顶部横排 -->
    <Transition name="subcat-rail">
      <aside
        v-if="isPhoneCategory && subCategories.length && subcategoryRailMode === 'float'"
        ref="subcategoryRailRef"
        class="subcategory-rail"
        :class="{
          'subcategory-rail--open': subCategoryExpanded,
          'subcategory-rail--hidden': !subcategoryRailVisible
        }"
        :style="{ left: subcategoryRailLeft }"
      >
        <button
          type="button"
          class="subcategory-rail__handle"
          :aria-expanded="subCategoryExpanded"
          @click="subCategoryExpanded = !subCategoryExpanded"
        >
          <span class="subcategory-rail__handle-bar"></span>
          <span class="subcategory-rail__handle-text">{{ $t('phoneNumbers.subCategoryFilter') }}</span>
          <el-icon class="subcategory-rail__handle-icon"><ArrowRight /></el-icon>
        </button>

        <nav class="subcategory-rail__panel" aria-label="subcategory filter">
          <button
            type="button"
            class="subcategory-rail__item"
            :class="{ 'subcategory-rail__item--active': !selectedSubCategoryId }"
            :style="{ '--i': 0 }"
            @click="selectSubCategory(0)"
          >
            <span class="subcategory-rail__dot"></span>
            {{ $t('phoneNumbers.allSubCategories') }}
          </button>
          <button
            v-for="(sub, idx) in subCategories"
            :key="sub.id"
            type="button"
            class="subcategory-rail__item"
            :class="{ 'subcategory-rail__item--active': selectedSubCategoryId === sub.id }"
            :style="{ '--i': idx + 1 }"
            @click="selectSubCategory(sub.id)"
          >
            <span class="subcategory-rail__dot"></span>
            {{ subCategoryLabel(sub) }}
          </button>
        </nav>
      </aside>
    </Transition>

    <main class="page-main">
      <!-- Hero Section -->
      <section class="hero-section">
        <h1 class="hero-title">
          {{ $t('phoneNumbers.exploreDestinations') }}
          <span class="hero-accent">{{ $t('phoneNumbers.simplified') || 'Simplified.' }}</span>
        </h1>
        <p class="hero-subtitle">{{ $t('phoneNumbers.stayConnected') }}</p>
      </section>

      <!-- Controls: Region Tabs + Filter Bar -->
      <div class="controls-block">
        <!-- Region Tab Bar -->
        <div class="region-tab-bar">
          <button :class="['region-tab', { 'region-tab--active': !selectedRegion }]" @click="selectedRegion = ''">
            {{ $t('regions.all') }}
          </button>
          <button
            v-for="region in regions"
            :key="region"
            :class="['region-tab', { 'region-tab--active': selectedRegion === region }]"
            @click="selectedRegion = region">
            {{ getRegionName(region) }}
          </button>
        </div>
        <!-- Filter Bar -->
        <div class="filter-bar">
          <div class="type-tabs">
            <button
              v-for="cat in frontCategories"
              :key="cat.id"
              :class="['type-tab', { 'type-tab--active': selectedItemType === cat.id }]"
              @click="handleItemTypeChange(cat.id)"
            >
              <el-icon class="type-tab-icon">
                <Iphone v-if="cat.code === 'PHONE'" />
                <Connection v-else />
              </el-icon>
              {{ categoryLabel(cat) }}
            </button>
          </div>
          <!-- Search -->
          <div class="search-wrap">
            <el-icon class="filter-search-icon"><Search /></el-icon>
            <el-input
              v-model="searchKeyword"
              :placeholder="$t('common.search')"
              clearable
              @input="handleSearch"
              class="filter-search-input" />
          </div>
          <!-- Sort -->
          <div class="sort-wrap">
            <span class="sort-label">{{ $t('phoneNumbers.sortBy') }}</span>
            <el-select v-model="sortBy" class="sort-select" style="width:160px">
              <el-option :value="'default'" :label="$t('phoneNumbers.default')" />
              <el-option :value="'price_asc'" :label="$t('phoneNumbers.priceAsc')" />
              <el-option :value="'price_desc'" :label="$t('phoneNumbers.priceDesc')" />
            </el-select>
          </div>
        </div>
        <!-- 窄屏 / 边距不足时：顶部横排展示二级分类 -->
        <div v-if="isPhoneCategory && subCategories.length && subcategoryRailMode === 'inline'" class="subcategory-inline">
          <button
            type="button"
            class="subcategory-inline__item"
            :class="{ 'subcategory-inline__item--active': !selectedSubCategoryId }"
            @click="selectSubCategory(0)"
          >{{ $t('phoneNumbers.allSubCategories') }}</button>
          <button
            v-for="sub in subCategories"
            :key="sub.id"
            type="button"
            class="subcategory-inline__item"
            :class="{ 'subcategory-inline__item--active': selectedSubCategoryId === sub.id }"
            @click="selectSubCategory(sub.id)"
          >{{ subCategoryLabel(sub) }}</button>
        </div>
      </div>
      

      <!-- Product Grid -->
      <div v-loading="loading" class="product-grid">
        <router-link
          v-for="(product, idx) in products"
          :key="product.id"
          :to="'/products/' + product.id"
          class="product-card">
          <!-- Image -->
          <div class="card-img-wrap">
            <img
              v-if="(product as any).displayImageUrl || product.imageUrl"
              :src="(product as any).displayImageUrl || product.imageUrl"
              :alt="getProductName(product)"
              class="card-img" />
            <div v-else class="card-img-placeholder">{{ getProductFlag(product) }}</div>
            <div v-if="idx === 0" class="card-badge">Best Seller</div>
          </div>
          <!-- Body -->
          <div class="card-body">
            <div class="card-title-row">
              <h3 class="card-name">{{ getProductName(product) }}</h3>
              <el-icon class="card-wifi-icon"><Connection /></el-icon>
            </div>
            <div class="card-country-tags" v-if="getProductCountries(product).length">
              <span
                v-for="(country, cIdx) in getCardCountries(product).visible"
                :key="cIdx"
                class="card-country-tag"
              >{{ country }}</span>
              <span v-if="getCardCountries(product).extra > 0" class="card-country-tag card-country-more">
                +{{ getCardCountries(product).extra }}
              </span>
            </div>
            <div class="card-meta-row" v-if="product.dataLimit || product.validityDays">
              <span v-if="product.dataLimit" class="card-data">{{ product.dataLimit }}</span>
              <span v-if="product.dataLimit && product.validityDays" class="card-dot">•</span>
              <span v-if="product.validityDays" class="card-days">{{ product.validityDays }} {{ $t('footer.days') }}</span>
            </div>
            <div class="card-support-list" v-else>
              <div class="card-support-line" :class="getSmsSupport(product) === 1 ? 'is-on' : 'is-off'">
                <span class="card-support-label">{{ $t('planInfo.smsSupport') }}</span>
                <span class="card-support-val">{{ supportLabel(getSmsSupport(product)) }}</span>
              </div>
              <div class="card-support-line" :class="getDataSupport(product) === 1 ? 'is-on' : 'is-off'">
                <span class="card-support-label">{{ $t('planInfo.dataSupport') }}</span>
                <span class="card-support-val">{{ supportLabel(getDataSupport(product)) }}</span>
              </div>
              <div class="card-support-line" :class="getVoiceSupport(product) === 1 ? 'is-on' : 'is-off'">
                <span class="card-support-label">{{ $t('planInfo.voiceSupport') }}</span>
                <span class="card-support-val">{{ supportLabel(getVoiceSupport(product)) }}</span>
              </div>
            </div>
            <div class="card-footer">
              <div class="card-price-block">
                <span class="card-from">{{ $t('checkout.packageFee') }}</span>
                <span class="card-price">${{ product.price }}</span>
              </div>
              <span class="card-select-btn">
                {{ $t('phoneNumbers.viewPlans') }}
              </span>
            </div>
          </div>
        </router-link>
      </div>

      <!-- Empty -->
      <el-empty v-if="!loading && products.length === 0" :description="$t('common.noData')" />

      <!-- Pagination -->
      <div class="pagination-wrap" v-if="total > 0 && totalPages > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
          class="products-pagination"
          background />
      </div>
    </main>

    <!-- Order Dialog -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="$t('phoneNumbers.checkout')"
      width="min(1000px, calc(100vw - 24px))"
      class="order-dialog"
      :show-close="true">
      <div v-if="selectedProduct" class="order-content" style="max-height: 65vh; overflow-y: auto;">
        <!-- Product Info -->
        <div class="order-product">
          <div class="product-header">
            <span class="product-flag">{{ getProductFlag(selectedProduct) }}</span>
            <div class="product-info">
              <h3>{{ getProductName(selectedProduct) }}</h3>
              <p
                v-if="selectedProduct.dataLimit || selectedProduct.validityDays"
                class="product-type"
              >{{ [
                selectedProduct.dataLimit || null,
                selectedProduct.validityDays ? selectedProduct.validityDays + ' ' + $t('footer.days') : null
              ].filter(Boolean).join(' / ') }}</p>
              <div v-else class="product-support-list">
                <div class="product-support-line">
                  <span>{{ $t('planInfo.smsSupport') }}</span>
                  <span>{{ supportLabel(getSmsSupport(selectedProduct)) }}</span>
                </div>
                <div class="product-support-line">
                  <span>{{ $t('planInfo.dataSupport') }}</span>
                  <span>{{ supportLabel(getDataSupport(selectedProduct)) }}</span>
                </div>
                <div class="product-support-line">
                  <span>{{ $t('planInfo.voiceSupport') }}</span>
                  <span>{{ supportLabel(getVoiceSupport(selectedProduct)) }}</span>
                </div>
              </div>
            </div>
            <div class="product-price">${{ selectedProduct.price }} <span>USD</span></div>
          </div>
          <!-- Plan Tags -->
          <div class="plan-info-tags" v-if="getPlanType(selectedProduct) != null || getSmsSupport(selectedProduct) != null || getTopupType(selectedProduct) != null">
            <el-tag v-if="getPlanType(selectedProduct) != null" size="small" :type="getPlanTypeTagType(getPlanType(selectedProduct))" class="plan-tag">
              {{ $t('planInfo.planType') }}: {{ getPlanTypeLabel(getPlanType(selectedProduct)) }}
            </el-tag>
            <el-tag v-if="getSmsSupport(selectedProduct) != null" size="small" :type="getSmsSupport(selectedProduct) === 1 ? 'success' : 'info'" class="plan-tag">
              {{ $t('planInfo.smsSupport') }}: {{ getSmsSupport(selectedProduct) === 1 ? $t('planInfo.smsSupportYes') : $t('planInfo.smsSupportNo') }}
            </el-tag>
            <el-tag v-if="getTopupType(selectedProduct) != null" size="small" :type="getTopupTypeTagType(getTopupType(selectedProduct))" class="plan-tag">
              {{ $t('planInfo.topupType') }}: {{ getTopupTypeLabel(getTopupType(selectedProduct)) }}
            </el-tag>
          </div>
          <div v-if="getProductNote(selectedProduct) || getPhoneAllFeatures(selectedProduct).length" class="dialog-phone-features">
            <div v-if="getProductNote(selectedProduct)" class="dialog-note-block">
              <div class="section-title" style="margin-bottom:8px;">{{ $t('phoneNumbers.productNote') }}</div>
              <p class="dialog-note-text">{{ getProductNote(selectedProduct) }}</p>
            </div>
            <div v-if="getPhoneAllFeatures(selectedProduct).length" class="dialog-specs-block" :class="{ 'has-note': !!getProductNote(selectedProduct) }">
              <div class="section-title" style="margin-bottom:8px;">{{ $t('phoneNumbers.productCharacteristics') }}</div>
              <div class="dialog-features-grid">
                <div v-for="(feat, idx) in getPhoneAllFeatures(selectedProduct)" :key="idx"
                     :class="['dialog-feature-item', feat.startsWith('⚠️') ? 'feat-warning' : feat.startsWith('💰') ? 'feat-renewal' : '']">
                  <span>{{ feat }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Device Selection -->
        <div class="order-section">
          <div class="section-title">
            {{ (selectedProduct as any)?.isPhysicalCard ? '选择实体设备' : $t('phoneNumbers.devices') }}
            <span :class="(selectedProduct as any)?.isPhysicalCard ? 'section-required' : 'section-hint'">
              {{ (selectedProduct as any)?.isPhysicalCard ? '* 实体卡必选' : $t('checkout.oneShipping') }}
            </span>
          </div>
          <div class="device-options">
            <label
              v-for="device in devices"
              :key="device.id"
              class="device-option"
              :class="{ selected: selectedDevices.includes(device.id) }">
              <input type="checkbox" :value="device.id" v-model="selectedDevices" />
              <div class="device-content">
                <div class="device-info">
                  <span class="device-name">{{ locale === 'zh' ? device.name : device.nameEn }}</span>
                  <span class="device-desc">{{ locale === 'zh' ? device.description : device.descriptionEn }}</span>
                </div>
                <span class="device-price">{{ device.isFree ? $t('phoneNumbers.free') : '$' + device.price.toFixed(2) }}</span>
              </div>
            </label>
          </div>
          <div class="shipping-note">
            <svg class="shipping-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
            {{ $t('checkout.shippingNote') }} <strong>{{ siteConfig.formatShippingFee() }}</strong>
          </div>
        </div>

        <!-- Pricing Tiers -->
        <div class="order-section tier-section" v-if="productPricingTiers.length > 0">
          <div class="section-title">
            选择购买周期
            <span class="required-hint">*</span>
          </div>
          <div class="tier-chips">
            <label
              v-for="tier in productPricingTiers"
              :key="tier.id"
              class="tier-chip"
              :class="{ selected: selectedTier?.id === tier.id }">
              <input type="radio" :value="tier" v-model="selectedTier" />
              <div class="tier-chip-content">
                <div class="tier-chip-main">
                  <span class="tier-chip-months">{{ tier.months }}个月</span>
                  <span v-if="tier.label" class="tier-chip-badge">{{ tier.label }}</span>
                </div>
                <div class="tier-chip-prices">
                  <span class="tier-chip-total">¥{{ tier.totalPrice }}</span>
                  <span class="tier-chip-monthly">{{ (tier.totalPrice / tier.months).toFixed(2) }}/月</span>
                </div>
              </div>
            </label>
          </div>
        </div>

        <!-- Shipping Address -->
        <div class="order-section shipping-address-section" v-if="hasDevice">
          <div class="section-title">
            {{ $t('checkout.shippingAddress') }}
            <span class="required-hint">*</span>
          </div>
          <div class="address-form">
            <div class="form-row">
              <div class="form-item">
                <label>{{ $t('checkout.recipientName') }} <span class="required">*</span></label>
                <el-input v-model="shippingAddress.name" :placeholder="$t('checkout.recipientNamePlaceholder')" />
              </div>
              <div class="form-item">
                <label>{{ $t('checkout.contactPhone') }} <span class="required">*</span></label>
                <el-input v-model="shippingAddress.phone" :placeholder="$t('checkout.contactPhonePlaceholder')" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-item">
                <label>{{ $t('checkout.province') }} <span class="required">*</span></label>
                <el-input v-model="shippingAddress.province" :placeholder="$t('checkout.provincePlaceholder')" />
              </div>
              <div class="form-item">
                <label>{{ $t('checkout.city') }} <span class="required">*</span></label>
                <el-input v-model="shippingAddress.city" :placeholder="$t('checkout.cityPlaceholder')" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-item full-width">
                <label>{{ $t('checkout.postalCode') }}</label>
                <el-input v-model="shippingAddress.postalCode" :placeholder="$t('checkout.postalCodePlaceholder')" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-item full-width">
                <label>{{ $t('checkout.detailedAddress') }} <span class="required">*</span></label>
                <el-input v-model="shippingAddress.address" :placeholder="$t('checkout.detailedAddressPlaceholder')" type="textarea" :rows="2" />
              </div>
            </div>
          </div>
        </div>

        <!-- Order remark -->
        <div class="order-section">
          <div class="section-title">
            {{ $t('checkout.orderRemark') }}
            <span class="optional-hint">{{ $t('checkout.optional') }}</span>
          </div>
          <el-input
            v-model="customerRemark"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            :placeholder="$t('checkout.orderRemarkPlaceholder')"
          />
        </div>

        <!-- Order Summary -->
        <div class="order-section order-summary">
          <div class="summary-row">
            <span>{{ $t('checkout.packageFee') }}</span>
            <span v-if="selectedTier">${{ selectedTier.totalPrice }} USD <small style="color:#999;font-size:11px;">（{{ selectedTier.months }}个月 × ${{ selectedTier.pricePerMonth }}/月）</small></span>
            <span v-else>${{ selectedProduct.price }} USD</span>
          </div>
          <div class="summary-row" v-if="hasDevice">
            <span>{{ $t('phoneNumbers.deviceFee') }}</span>
            <span>${{ deviceTotal.toFixed(2) }} USD</span>
          </div>
          <div class="summary-row shipping-row" v-if="hasDevice">
            <span>{{ $t('phoneNumbers.expressFee') }}</span>
            <span>{{ siteConfig.formatShippingFee() }}</span>
          </div>
          <div class="summary-row" v-if="appliedDiscount > 0">
            <span>{{ $t('checkout.couponDiscount') }}</span>
            <span class="coupon-discount">-${{ appliedDiscount.toFixed(2) }} USD</span>
          </div>
          <div class="coupon-box">
            <div class="coupon-label">{{ $t('checkout.couponCode') }}</div>
            <div class="coupon-row">
              <el-input
                v-model="couponInput"
                :placeholder="$t('checkout.couponPlaceholder')"
                clearable
                @keyup.enter="applyCoupon"
              />
              <el-button :loading="couponLoading" @click="applyCoupon">{{ $t('checkout.couponApply') }}</el-button>
            </div>
            <div class="coupon-tip" v-if="couponMessage" :class="{ error: !couponApplied }">{{ couponMessage }}</div>
          </div>
          <div class="summary-row total-row" v-if="productPricingTiers.length === 0 || selectedTier">
            <span>{{ $t('phoneNumbers.total') }}</span>
            <span>${{ payablePrice.toFixed(2) }} USD</span>
          </div>
          <div class="summary-row total-row" v-else style="color:#f56c6c;">
            <span>{{ $t('phoneNumbers.total') }}</span>
            <span>请先选择购买周期</span>
          </div>
        </div>

        <!-- Payment Methods -->
        <div class="order-section" v-if="enabledPaymentMethods.length > 0">
          <div class="section-title">{{ $t('phoneNumbers.selectPayment') }}</div>
          <div class="payment-options">
            <label
              v-for="pm in enabledPaymentMethods"
              :key="pm.code"
              class="payment-option"
              :class="{
                selected: paymentMethod === pm.code.toLowerCase(),
                disabled: pm.code === 'BALANCE' && userBalance < payablePrice
              }">
              <input
                type="radio"
                v-model="paymentMethod"
                :value="pm.code.toLowerCase()"
                :disabled="pm.code === 'BALANCE' && userBalance < payablePrice" />
              <div class="payment-content">
                <div class="payment-icon" :class="getPaymentIconClass(pm.code)">
                  <span v-if="pm.code === 'USDT'">₮</span>
                  <svg v-else-if="pm.code === 'WECHAT'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M8.5 11.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm7 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM12 2C6.48 2 2 6.48 2 12c0 2.17.7 4.18 1.88 5.82L2 22l4.18-1.88C7.82 21.3 9.83 22 12 22c5.52 0 10-4.48 10-10S17.52 2 12 2z"/>
                  </svg>
                  <svg v-else-if="pm.code === 'BALANCE'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1.41 16.09V20h-2.67v-1.93c-1.71-.36-3.16-1.46-3.27-3.4h1.96c.1 1.05.82 1.87 2.65 1.87 1.96 0 2.4-.98 2.4-1.59 0-.83-.44-1.61-2.67-2.14-2.48-.6-4.18-1.62-4.18-3.67 0-1.72 1.39-2.84 3.11-3.21V4h2.67v1.95c1.86.45 2.79 1.86 2.85 3.39H14.3c-.05-1.11-.64-1.87-2.22-1.87-1.5 0-2.4.68-2.4 1.64 0 .84.65 1.39 2.67 1.91s4.18 1.39 4.18 3.91c-.01 1.83-1.38 2.83-3.12 3.16z"/>
                  </svg>
                  <svg v-else-if="pm.code === 'CARD' || pm.code === 'BANK_CARD'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z"/>
                  </svg>
                  <span v-else-if="pm.code === 'ALIPAY' || pm.code === 'ANTOM_ALIPAY'" class="alipay-mark">支</span>
                </div>
                <div class="payment-info">
                  <span class="payment-name">{{ getPaymentMethodLabel(pm.code, t) }}</span>
                  <span v-if="pm.code === 'USDT'" class="payment-desc">{{ $t('payment.trc20Note') }}</span>
                  <span v-else-if="pm.code === 'BALANCE'" class="payment-desc balance-desc" :class="{ insufficient: userBalance < payablePrice }">
                    {{ userBalance >= payablePrice ? '' : $t('checkout.balanceInsufficient') }}（${{ userBalance.toFixed(2) }} USD）
                  </span>
                  <span v-else class="payment-desc">{{ getPaymentMethodDesc(pm.code, t, pm.description) }}</span>
                </div>
                <div class="payment-check">
                  <svg v-if="paymentMethod === pm.code.toLowerCase()" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                  </svg>
                </div>
              </div>
            </label>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false" :disabled="placingOrder" size="large">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleBuyNow" :disabled="placingOrder" :loading="placingOrder" size="large" class="btn-submit">
            {{ placingOrder ? $t('checkout.processing') : $t('phoneNumbers.checkout') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Footer -->
    <SiteFooter />

    <PurchaseNoticeDialog
      v-model="purchaseNoticeVisible"
      @confirm="submitOrderAfterNotice"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { productApi, Product } from '@/api/product'
import { productCategoryApi, type ProductCategory } from '@/api/productCategory'
import { productSubcategoryApi, type ProductSubcategory } from '@/api/productSubcategory'
import { pricingTierApi, ProductPricingTier } from '@/api/pricingTier'
import { phoneNumberApi, Device } from '@/api/phoneNumber'
import { orderApi } from '@/api/order'
import { validateCoupon } from '@/api/coupon'
import { resolveCouponTip, resolveCouponTipOrNull } from '@/utils/couponMessage'
import { paymentMethodApi } from '@/api/paymentMethod'
import { getBalance } from '@/api/recharge'
import { ElMessage } from 'element-plus'
import { CircleCheck, Search, Connection, Iphone, ArrowRight } from '@element-plus/icons-vue'
import SiteHeader from '@/components/SiteHeader.vue'
import SiteFooter from '@/components/SiteFooter.vue'
import PurchaseNoticeDialog from '@/components/PurchaseNoticeDialog.vue'
import { getCurrentLocale } from '@/i18n'
import { formatCountriesForDisplay, getCountryFlagEmoji } from '@/utils/countryDisplay'
import { useSiteConfigStore } from '@/stores/siteConfig'
import { navigateToLogin } from '@/utils/authNavigation'
import { navigateByPaymentMethod } from '@/utils/stripeCheckout'
import { getPaymentMethodLabel, getPaymentMethodDesc } from '@/utils/paymentMethodLabel'

const CARD_COUNTRY_MAX = 4

const { locale, t } = useI18n({ useScope: 'global' })
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const siteConfig = useSiteConfigStore()

const phoneProducts = ref<Product[]>([])  // 兼容旧逻辑：手机号卡列表缓存
const dataProducts = ref<Product[]>([])   // 兼容旧逻辑：其他分类列表缓存
const frontCategories = ref<ProductCategory[]>([])
const allProducts = ref<Product[]>([])  // 保存搜索或全部产品
const originProducts = ref<Product[]>([])  // 保存原始的全部产品（用于地区列表）
const regions = ref<string[]>([])
const selectedRegion = ref('')
const selectedItemType = ref(0)  // product_categories.id
const sortBy = ref('default')
const searchKeyword = ref('')  // 搜索关键词
const loadingPhone = ref(true)  // 初始为 true，避免首屏先闪「暂无数据」
const loadingData = ref(true)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const flagColors = ref<Map<number, { r: number; g: number; b: number }>>(new Map())  // 缓存国旗中心颜色
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const selectedCategory = computed(() =>
  frontCategories.value.find(c => c.id === selectedItemType.value)
)
const isPhoneCategory = computed(() => selectedCategory.value?.code === 'PHONE')

const subCategories = ref<ProductSubcategory[]>([])
const selectedSubCategoryId = ref(0)
const subCategoryExpanded = ref(true)
const subcategoryRailRef = ref<HTMLElement | null>(null)
/** float：浮在内容左侧；inline：顶部横排，避免盖住产品 */
const subcategoryRailMode = ref<'float' | 'inline'>('float')
const subcategoryRailLeft = ref('12px')
/** 仅当页脚即将盖住侧栏时隐藏；默认显示 */
const subcategoryRailVisible = ref(true)

function updateSubcategoryRailPosition() {
  if (!isPhoneCategory.value || !subCategories.value.length) {
    subcategoryRailVisible.value = true
    subcategoryRailMode.value = 'inline'
    return
  }

  const pageMain = document.querySelector('.products-page .page-main') as HTMLElement | null
  const contentLeft = pageMain?.getBoundingClientRect().left ?? 0
  const gap = 8
  const minEdge = 12
  // 用实测宽度判断左侧是否放得下；未挂载时按展开态估算
  const rail = subcategoryRailRef.value
  const railWidth = rail?.offsetWidth || (subCategoryExpanded.value ? 196 : 36)
  const canFloat = contentLeft >= railWidth + gap + minEdge

  if (!canFloat) {
    subcategoryRailMode.value = 'inline'
    subcategoryRailVisible.value = true
    return
  }

  // left 对齐内容区左缘，再由 CSS translateX(-100%) 整块贴在外侧
  subcategoryRailMode.value = 'float'
  subcategoryRailLeft.value = `${contentLeft - gap}px`

  const footer = document.querySelector('.site-footer') as HTMLElement | null
  if (!footer) {
    subcategoryRailVisible.value = true
    return
  }
  const footerTop = footer.getBoundingClientRect().top
  const railBottom = rail
    ? rail.getBoundingClientRect().bottom
    : window.innerHeight * 0.5 + 140
  subcategoryRailVisible.value = footerTop > railBottom + 20
}

function subCategoryLabel(sub: ProductSubcategory): string {
  if (locale.value === 'en' && sub.nameEn) return sub.nameEn
  return sub.name
}

async function loadSubCategories(categoryId: number) {
  try {
    subCategories.value = await productSubcategoryApi.listByCategory(categoryId)
  } catch {
    subCategories.value = []
  }
  await nextTick()
  updateSubcategoryRailPosition()
}

function selectSubCategory(id: number) {
  selectedSubCategoryId.value = id
  currentPage.value = 1
  fetchAllProducts()
  nextTick(() => updateSubcategoryRailPosition())
}

function categoryLabel(cat: ProductCategory): string {
  if (locale.value === 'en' && cat.nameEn) return cat.nameEn
  return cat.name
}

// 计算当前显示的产品列表
const products = computed(() => {
  return isPhoneCategory.value ? phoneProducts.value : dataProducts.value
})

// 计算当前的加载状态
const loading = computed(() => {
  return isPhoneCategory.value ? loadingPhone.value : loadingData.value
})

const detailDialogVisible = ref(false)
const selectedProduct = ref<Product | null>(null)

// 订单表单
const esimOption = ref('esim')
const selectedDevices = ref<number[]>([])  // 选中的设备 ID 列表
const paymentMethod = ref('usdt')
const userBalance = ref(0) // 用户余额
const enabledPaymentMethods = ref<any[]>([]) // 开启的支付方式（完整对象）
const placingOrder = ref(false) // 下单防抖标志
const purchaseNoticeVisible = ref(false)
const devices = ref<Device[]>([]) // 从数据库加载的设备列表

// 定价档位
const productPricingTiers = ref<ProductPricingTier[]>([])
const selectedTier = ref<ProductPricingTier | null>(null)

// 收货地址表单
const shippingAddress = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  postalCode: '',
  address: ''
})
const customerRemark = ref('')

const SHIPPING_FEE = computed(() => Number(siteConfig.defaultShippingFee ?? 0))

// 计算设备总价（使用数据库中的价格）
const deviceTotal = computed(() => {
  return selectedDevices.value.reduce((sum, deviceId) => {
    const device = devices.value.find(d => d.id === deviceId)
    return sum + (device?.price || 0)
  }, 0)
})

const hasDevice = computed(() => selectedDevices.value.length > 0)

// 运费：选购任意数量设备，整单只收一次
const shippingFeeAmount = computed(() => {
  if (!hasDevice.value) return 0
  return SHIPPING_FEE.value
})

// 套餐实际价格（选了档位则用档位总价，否则用产品原价）
const packagePrice = computed(() => {
  if (selectedTier.value) return selectedTier.value.totalPrice
  return selectedProduct.value?.price || 0
})

// 订单总价
const orderTotal = computed(() => {
  return packagePrice.value + deviceTotal.value + shippingFeeAmount.value
})

const couponInput = ref('')
const couponLoading = ref(false)
const couponApplied = ref(false)
const couponMessage = ref('')
const appliedCode = ref('')
const appliedDiscount = ref(0)

const payablePrice = computed(() =>
  Math.max(0, Number((orderTotal.value - appliedDiscount.value).toFixed(2)))
)

watch(orderTotal, () => {
  if (couponApplied.value) applyCoupon()
})

async function applyCoupon() {
  const code = couponInput.value.trim()
  if (!code) {
    couponApplied.value = false
    appliedCode.value = ''
    appliedDiscount.value = 0
    couponMessage.value = ''
    return
  }
  couponLoading.value = true
  try {
    const res: any = await validateCoupon(code, orderTotal.value, selectedProduct.value?.id)
    const data = res.data || res
    if (res.code === 200 && data?.valid !== false) {
      couponApplied.value = true
      appliedCode.value = data.code || code.toUpperCase()
      appliedDiscount.value = Number(data.discountAmount || 0)
      couponMessage.value = t('checkout.couponAppliedTip', { amount: appliedDiscount.value.toFixed(2) })
    } else {
      couponApplied.value = false
      appliedDiscount.value = 0
      appliedCode.value = ''
      couponMessage.value = resolveCouponTip(t, {
        messageKey: data?.messageKey,
        messageParam: data?.messageParam,
        message: res.message || data?.message
      })
    }
  } catch (e: any) {
    couponApplied.value = false
    appliedDiscount.value = 0
    appliedCode.value = ''
    couponMessage.value = resolveCouponTip(t, {
      messageKey: e?.response?.data?.data?.messageKey,
      messageParam: e?.response?.data?.data?.messageParam,
      message: e?.message
    })
  } finally {
    couponLoading.value = false
  }
}

const regionNames: Record<string, { zh: string; en: string }> = {
  'Global': { zh: '全球', en: 'Global' },
  'Asia': { zh: '亚洲', en: 'Asia' },
  'Europe': { zh: '欧洲', en: 'Europe' },
  'America': { zh: '美洲', en: 'America' },
  'Oceania': { zh: '大洋洲', en: 'Oceania' },
  'Africa': { zh: '非洲', en: 'Africa' }
}

function getRegionName(region: string): string {
  return regionNames[region]?.[locale.value as 'zh' | 'en'] || region
}

function parseJson(str: string): string[] {
  try {
    return JSON.parse(str)
  } catch {
    return []
  }
}

// 获取产品图标（从适用地区获取第一个国家的国旗）
function getProductFlag(product: Product): string {
  try {
    const countries = JSON.parse(product.countries || '[]')
    if (countries.length > 0) {
      const flag = getCountryFlagEmoji(countries[0])
      if (flag !== '🌐') return flag
    }
  } catch {}

  if (product.dataLimit) {
    const codeMatch = product.dataLimit.match(/[A-Z]{2}/)
    if (codeMatch) {
      const flag = getCountryFlagEmoji(codeMatch[0])
      if (flag !== '🌐') return flag
    }
  }

  return '🌍'
}

// 获取产品覆盖国家（ISO 存库，按语言展示，供标签渲染）
function getProductCountries(product: Product): string[] {
  try {
    const raw = JSON.parse(product.countries || '[]')
    if (!Array.isArray(raw) || raw.length === 0) return []
    return formatCountriesForDisplay(raw, locale.value as 'zh' | 'en')
  } catch {}
  return []
}

/** 产品卡片最多展示 4 个国家，超出显示 +N */
function getCardCountries(product: Product): { visible: string[]; extra: number } {
  const all = getProductCountries(product)
  if (all.length <= CARD_COUNTRY_MAX) {
    return { visible: all, extra: 0 }
  }
  return { visible: all.slice(0, CARD_COUNTRY_MAX), extra: all.length - CARD_COUNTRY_MAX }
}

// 获取产品适用地区显示（详情等场景备用）
function getProductRegions(product: Product): string {
  try {
    const regions = JSON.parse(product.applicableRegions || '[]')
    if (regions.length > 0) {
      return regions.map((r: string) => regionNames[r]?.[locale.value as 'zh' | 'en'] || r).join(', ')
    }
  } catch {}
  return ''
}

// 获取本地化产品名称
function getProductName(product: Product): string {
  if (locale.value === 'en' && product.nameEn) {
    return product.nameEn
  }
  return product.name
}

// 获取支付方式图标样式类
function getPaymentIconClass(code: string): string {
  const classMap: Record<string, string> = {
    'USDT': 'usdt-icon',
    'WECHAT': 'wechat-icon',
    'BALANCE': 'balance-icon',
    'CARD': 'card-icon',
    'BANK_CARD': 'card-icon',
    'ALIPAY': 'alipay-icon',
    'ANTOM_ALIPAY': 'alipay-icon'
  }
  return classMap[code] || 'default-icon'
}

// plan_type 标签颜色
function getPlanTypeTagType(planType: number | undefined): string {
  if (planType === 1) return 'warning'   // 每日流量
  if (planType === 2) return 'danger'    // 体验包
  return 'primary'                        // 固定期限
}

// plan_type 文字
function getPlanTypeLabel(planType: number | undefined): string {
  if (planType === 1) return t('planInfo.planTypeDaily')
  if (planType === 2) return t('planInfo.planTypeTrial')
  return t('planInfo.planTypeFixed')
}

// topup_type 标签颜色
function getTopupTypeTagType(topupType: number | undefined): string {
  if (topupType === 2) return 'success'  // 支持续费+每日重置
  if (topupType === 1) return 'warning'  // 支持续费
  return 'info'                           // 不可续费
}

// topup_type 文字
function getTopupTypeLabel(topupType: number | undefined): string {
  if (topupType === 2) return t('planInfo.topupTypeDaily')
  if (topupType === 1) return t('planInfo.topupTypeSupported')
  return t('planInfo.topupTypeNone')
}

function noteToText(raw?: string): string {
  if (!raw) return ''
  const trimmed = raw.trim()
  if (!trimmed) return ''
  if (trimmed.startsWith('[')) {
    try {
      const parsed = JSON.parse(trimmed)
      if (Array.isArray(parsed)) return parsed.map((item) => String(item).trim()).filter(Boolean).join('\n')
    } catch {
      /* plain text */
    }
  }
  return trimmed
}
function getProductNote(product: Product): string {
  const en = noteToText((product as any).importantLabelsEn)
  const zh = noteToText((product as any).importantLabels)
  return locale.value === 'en' && en ? en : zh
}

// 兼容 camelCase / snake_case 的套餐属性 getter
function getPlanType(product: Product): number | undefined {
  const v = (product as any).planType ?? (product as any).plan_type
  return v != null ? v : undefined
}
function getSmsSupport(product: Product): number | undefined {
  const v = (product as any).smsSupport ?? (product as any).sms_support
  return v != null ? v : undefined
}
function getDataSupport(product: Product): number | undefined {
  const v = (product as any).dataSupport ?? (product as any).data_support
  if (v != null) return v
  // 有流量额度时默认视为支持流量
  return product.dataLimit ? 1 : 0
}
function getVoiceSupport(product: Product): number | undefined {
  const v = (product as any).voiceSupport ?? (product as any).voice_support
  return v != null ? v : 0
}
function supportLabel(value: number | undefined): string {
  return value === 1 ? t('planInfo.smsSupportYes') : t('planInfo.smsSupportNo')
}
function getTopupType(product: Product): number | undefined {
  const v = (product as any).topupType ?? (product as any).topup_type
  return v != null ? v : undefined
}

// 解析手机号卡 features JSON（全部条目，用于弹窗）
function getPhoneAllFeatures(product: Product): string[] {
  try {
    const featuresEn = product.featuresEn
    // 仅当 locale 为 en 且英文特性有实际内容（非空数组）时才使用英文
    const hasEnFeatures = locale.value === 'en' && featuresEn && featuresEn !== '[]' && featuresEn !== ''
    const raw = hasEnFeatures ? featuresEn : product.features
    return JSON.parse(raw || '[]')
  } catch { return [] }
}

// 卡片上只显示关键特性（前6条，排除💰和⚠️等底部备注）
function getPhoneKeyFeatures(product: Product): string[] {
  return getPhoneAllFeatures(product)
    .filter(f => !f.startsWith('💰') && !f.startsWith('⚠️') && !f.startsWith('ℹ️') && !f.startsWith('🛒') && !f.startsWith('📋') && !f.startsWith('🏷️') && !f.startsWith('♻️') && !f.startsWith('🔄'))
    .slice(0, 6)
}

// 提取续费信息（💰前缀条目）
function getPhoneRenewalInfo(product: Product): string {
  const renewal = getPhoneAllFeatures(product).find(f => f.startsWith('💰'))
  return renewal ? renewal.replace('💰 ', '') : ''
}

function getPhoneBottomNotes(product: Product): string[] {
  return getPhoneAllFeatures(product).filter(f =>
    f.startsWith('💰') || f.startsWith('⚠️') ||
    f.startsWith('ℹ️') || f.startsWith('🛒') ||
    f.startsWith('📋') || f.startsWith('🏷️') ||
    f.startsWith('♻️') || f.startsWith('🔄')
  )
}

// 同步版本：用于模板，直接从缓存获取
function getPhoneCardColorSync(product: Product): string {
  if (product.imageUrl && product.id && flagColors.value.has(product.id)) {
    const cached = flagColors.value.get(product.id)!
    return `linear-gradient(135deg, rgba(${cached.r}, ${cached.g}, ${cached.b}, 0.22) 0%, rgba(${cached.r}, ${cached.g}, ${cached.b}, 0.12) 100%)`
  }
  const name = getProductName(product).toLowerCase()
  const nameHash = name.split('').reduce((acc, c) => acc + c.charCodeAt(0), 0)
  const colorSets = [
    { h1: 210, h2: 230 },
    { h1: 140, h2: 160 },
    { h1: 270, h2: 290 },
    { h1: 190, h2: 210 },
    { h1: 25, h2: 40 },
    { h1: 350, h2: 10 },
  ]
  const color = colorSets[nameHash % colorSets.length]
  return `linear-gradient(135deg, hsla(${color.h1}, 60%, 55%, 0.18) 0%, hsla(${color.h2}, 50%, 45%, 0.12) 100%)`
}

// 从图片中心提取RGB颜色
function extractCenterColor(imageUrl: string): Promise<{ r: number; g: number; b: number } | null> {
  return new Promise((resolve) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      const canvas = document.createElement('canvas')
      canvas.width = img.width
      canvas.height = img.height
      const ctx = canvas.getContext('2d')
      if (ctx) {
        ctx.drawImage(img, 0, 0)
        const centerX = Math.floor(img.width / 2)
        const centerY = Math.floor(img.height / 2)
        const pixel = ctx.getImageData(centerX, centerY, 1, 1).data
        resolve({ r: pixel[0], g: pixel[1], b: pixel[2] })
      }
      resolve(null)
    }
    img.onerror = () => resolve(null)
    img.src = imageUrl
  })
}

// 异步提取颜色并存入缓存
async function extractAndCacheFlagColor(product: Product) {
  if (product.imageUrl && product.id && !flagColors.value.has(product.id)) {
    const color = await extractCenterColor(product.imageUrl)
    if (color) {
      flagColors.value.set(product.id, color)
    }
  }
}

// 判断是否是全球产品（有图片且国旗是地球）
function isGlobalProduct(product: Product): boolean {
  return !!product.imageUrl && getProductFlag(product) === '🌍'
}

async function openDetail(product: Product) {
  selectedProduct.value = product
  // 重置表单
  esimOption.value = 'esim'
  selectedDevices.value = []
  paymentMethod.value = 'usdt'
  selectedTier.value = null
  productPricingTiers.value = []
  // 重置收货地址
  shippingAddress.value = {
    name: '',
    phone: '',
    province: '',
    city: '',
    postalCode: '',
    address: ''
  }
  customerRemark.value = ''
  detailDialogVisible.value = true
  // 异步加载定价档位
  try {
    const res: any = await pricingTierApi.getActiveByProduct(product.id)
    if (res.code === 200) {
      productPricingTiers.value = res.data || []
    }
  } catch (e) {
    productPricingTiers.value = []
  }
}

async function handleBuyNow() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning(t('checkout.pleaseLogin'))
    detailDialogVisible.value = false
    navigateToLogin(router, route)
    return
  }

  // 如果有月份定价，必须选择周期
  if (productPricingTiers.value.length > 0 && !selectedTier.value) {
    ElMessage.warning('请选择购买周期')
    return
  }
  
  // 实体卡必须选择设备
  const isPhysical = !!(selectedProduct.value as any)?.isPhysicalCard
  if (isPhysical && selectedDevices.value.length === 0) {
    ElMessage.warning('该产品为实体卡，请选择至少一个配套设备')
    return
  }
  
  // 验证收货地址（选择设备时必填）
  if (hasDevice.value) {
    if (!shippingAddress.value.name) {
      ElMessage.warning(t('checkout.nameRequired') || '请填写收货人姓名')
      return
    }
    if (!shippingAddress.value.phone) {
      ElMessage.warning(t('checkout.phoneRequired') || '请填写联系电话')
      return
    }
    if (!shippingAddress.value.province) {
      ElMessage.warning(t('checkout.provinceRequired') || '请填写省/州')
      return
    }
    if (!shippingAddress.value.city) {
      ElMessage.warning(t('checkout.cityRequired') || '请填写城市')
      return
    }
    if (!shippingAddress.value.address) {
      ElMessage.warning(t('checkout.addressRequired') || '请填写详细地址')
      return
    }
  }
  
  if (!selectedProduct.value) return
  if (placingOrder.value) {
    ElMessage.warning('订单正在处理中，请勿重复点击')
    return
  }
  purchaseNoticeVisible.value = true
}

async function submitOrderAfterNotice() {
  if (!selectedProduct.value) return
  if (placingOrder.value) {
    ElMessage.warning('订单正在处理中，请勿重复点击')
    return
  }
  placingOrder.value = true

  try {
    await siteConfig.ensureShippingFee()
    const paymentMethodUpper = paymentMethod.value.toUpperCase()
    const orderData: any = {
      email: authStore.user?.email || '',
      paymentMethod: paymentMethodUpper,
      productId: selectedProduct.value.id,
      devices: selectedDevices.value.map(id => String(id)),
      totalAmount: orderTotal.value,
      couponCode: appliedCode.value || undefined,
      language: getCurrentLocale(),
      customerRemark: customerRemark.value.trim() || undefined,
      ...(selectedTier.value ? {
        pricingTierId: selectedTier.value.id,
        selectedMonths: selectedTier.value.months
      } : {})
    }
    
    if (hasDevice.value) {
      const addr = shippingAddress.value
      orderData.shippingAddress = `${addr.name} ${addr.phone} ${addr.province} ${addr.city} ${addr.postalCode || ''} ${addr.address}`
    }
    
    const res: any = await orderApi.createOrder(orderData)
    
    if (res.code === 200) {
      detailDialogVisible.value = false
      ElMessage.success(t('checkout.processing'))
      const orderId = res.data.id
      const orderNo = res.data.orderNo || String(orderId)
      await navigateByPaymentMethod({
        paymentMethod: paymentMethodUpper,
        orderId,
        orderNo,
        email: authStore.user?.email,
        checkoutUrl: res.data.checkoutUrl,
        router,
      })
    } else {
      ElMessage.error(resolveCouponTipOrNull(t, { message: res.message }) || res.message || t('checkout.createOrderFailed'))
    }
  } catch (error: any) {
    ElMessage.error(resolveCouponTipOrNull(t, { message: error.message }) || error.message || t('checkout.createOrderFailed'))
  } finally {
    placingOrder.value = false
  }
}

// 获取地区列表
async function initRegions() {
  try {
    // 直接从后端获取地区列表，不需要获取所有产品数据
    const res: any = await productApi.getRegions()
    console.log('getRegions response:', res)
    
    if (res?.code === 200) {
      if (Array.isArray(res.data)) {
        regions.value = res.data
      } else if (res.data?.regions && Array.isArray(res.data.regions)) {
        regions.value = res.data.regions
      } else {
        // 使用默认地区列表
        regions.value = ['Asia', 'Europe', 'America', 'Oceania', 'Africa', 'Global']
      }
    } else {
      // 后端返回错误时使用默认地区列表
      regions.value = ['Asia', 'Europe', 'America', 'Oceania', 'Africa', 'Global']
    }
  } catch (error) {
    console.error('获取地区列表失败', error)
    // 使用默认地区列表确保功能可用
    regions.value = ['Asia', 'Europe', 'America', 'Oceania', 'Africa', 'Global']
  }
}

// 按页获取产品列表（只加载当前激活 tab 的数据）
async function fetchAllProducts() {
  if (!selectedItemType.value) {
    loadingPhone.value = false
    loadingData.value = false
    return
  }
  const isPhone = isPhoneCategory.value
  // 只设置当前 tab 的 loading
  if (isPhone) {
    loadingPhone.value = true
  } else {
    loadingData.value = true
  }
  try {
    const itemTypeParam = selectedItemType.value
    const subParam = isPhoneCategory.value && selectedSubCategoryId.value ? selectedSubCategoryId.value : undefined
    let res: any
    if (searchKeyword.value.trim()) {
      res = await productApi.searchProducts(searchKeyword.value.trim(), currentPage.value - 1, pageSize.value, itemTypeParam, sortBy.value, subParam)
    } else {
      res = await productApi.getProducts(selectedRegion.value || undefined, currentPage.value - 1, pageSize.value, itemTypeParam, sortBy.value, subParam)
    }

    if (res?.code === 200) {
      let rawProducts: any[] = []
      if (Array.isArray(res.data)) {
        rawProducts = res.data
      } else if (res.data?.content && Array.isArray(res.data.content)) {
        rawProducts = res.data.content
      } else if (res.data?.products && Array.isArray(res.data.products)) {
        rawProducts = res.data.products
      }
      total.value = res.data?.total ?? rawProducts.length
      // 正常翻页，每次都替换数据
      if (isPhone) {
        phoneProducts.value = rawProducts
      } else {
        dataProducts.value = rawProducts
      }
      rawProducts.forEach((p: any) => extractAndCacheFlagColor(p))
    } else {
      total.value = 0
      // 只有失败且是第一页时才清空
      if (currentPage.value === 1) {
        if (isPhone) {
          phoneProducts.value = []
        } else {
          dataProducts.value = []
        }
      }
    }
    console.log(`Products loaded [itemType=${itemTypeParam}] page=${currentPage.value} total=${total.value}`)
  } catch (error) {
    console.error('获取产品列表失败', error)
    total.value = 0
    // 只有第一页才清空数据
    if (currentPage.value === 1) {
      if (isPhone) {
        phoneProducts.value = []
      } else {
        dataProducts.value = []
      }
    }
  } finally {
    loadingPhone.value = false
    loadingData.value = false
  }
}

async function fetchRegions() {
  // 区域列表单独通过 initRegions 获取
  await initRegions()
}

async function fetchUserBalance() {
  // 如果已登录，通过 API 获取用户余额
  if (authStore.isLoggedIn) {
    try {
      const res: any = await getBalance()
      // 兼容两种响应格式：res.balance 或 res.data.balance
      userBalance.value = res.balance || res.data?.balance || 0
      console.log('Products - Balance loaded:', userBalance.value)
    } catch (err) {
      console.error('获取余额失败:', err)
      userBalance.value = 0
    }
  }
}

async function fetchEnabledPaymentMethods() {
  try {
    const res: any = await paymentMethodApi.getEnabled()
    if (res && res.length > 0) {
      // 保存完整对象，包含 name 字段
      enabledPaymentMethods.value = res
      // 提取支付方式代码列表
      const codes = res.map((m: any) => m.code)
      // 如果默认支付方式不在开启列表中，切换到第一个开启的
      const defaultMethod = paymentMethod.value.toUpperCase()
      if (!codes.includes(defaultMethod) && codes.length > 0) {
        // 转换为小写用于前端
        paymentMethod.value = codes[0].toLowerCase()
      }
    }
  } catch (error) {
    console.error('获取支付方式失败', error)
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  fetchAllProducts()
}

function handleItemTypeChange(itemType: number) {
  selectedItemType.value = itemType
  selectedSubCategoryId.value = 0
  currentPage.value = 1
  total.value = 0
  const cat = frontCategories.value.find(c => c.id === itemType)
  subCategoryExpanded.value = cat?.code === 'PHONE'
  pageSize.value = cat?.code === 'PHONE' ? 6 : 12
  if (cat?.code === 'PHONE') {
    phoneProducts.value = []
    loadingPhone.value = true
    loadSubCategories(cat.id)
  } else {
    dataProducts.value = []
    loadingData.value = true
    subCategories.value = []
  }
  fetchAllProducts()
}

function getItemTypeLabel(itemType: number): string {
  const cat = frontCategories.value.find(c => c.id === itemType)
  return cat ? categoryLabel(cat) : ''
}

function handleSearch() {
  currentPage.value = 1
  initRegions() // 重新计算地区列表
  fetchAllProducts()
}

watch([selectedRegion, sortBy], () => {
  currentPage.value = 1
  fetchAllProducts()
})

watch(subCategoryExpanded, async () => {
  await nextTick()
  updateSubcategoryRailPosition()
})

watch(() => authStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    fetchUserBalance()
  }
})

onMounted(async () => {
  console.log('Products.vue onMounted started')
  // 从URL读取region参数
  const regionParam = route.query.region as string
  if (regionParam) {
    console.log('Setting region from URL:', regionParam)
    selectedRegion.value = regionParam
  }

  try {
    frontCategories.value = await productCategoryApi.listForStorefront()
  } catch (e) {
    console.error('加载产品分类失败', e)
  }
  // 默认选中后台排序后的第一个分类（与 Tab 展示顺序一致）
  if (frontCategories.value.length) {
    const preferred = frontCategories.value[0]
    selectedItemType.value = preferred.id
    pageSize.value = preferred.code === 'PHONE' ? 6 : 12
    if (preferred.code === 'PHONE') {
      loadSubCategories(preferred.id)
    }
  }
  
  // 先初始化地区列表
  fetchRegions().then(() => {
    console.log('Regions initialized:', regions.value)
    fetchAllProducts()
  })
  
  // 加载设备列表（从数据库）
  try {
    const res: any = await phoneNumberApi.getDevices()
    if (res && res.data && Array.isArray(res.data) && res.data.length > 0) {
      devices.value = res.data.map((d: any) => ({
        ...d,
        price: typeof d.price === 'string' ? parseFloat(d.price) : (d.price || 0),
        shippingFee: typeof d.shippingFee === 'string' ? parseFloat(d.shippingFee) : (d.shippingFee || 0)
      }))
    }
  } catch (e) {
    console.error('加载设备列表失败:', e)
  }
  
  fetchEnabledPaymentMethods()
  if (authStore.isLoggedIn) {
    fetchUserBalance()
  }

  window.addEventListener('scroll', updateSubcategoryRailPosition, { passive: true })
  window.addEventListener('resize', updateSubcategoryRailPosition)
  await nextTick()
  updateSubcategoryRailPosition()
})

onUnmounted(() => {
  window.removeEventListener('scroll', updateSubcategoryRailPosition)
  window.removeEventListener('resize', updateSubcategoryRailPosition)
})
</script>

<style scoped>
/* ================================================
   PRODUCTS PAGE — MD3 Redesign
   Matches new_product.html design language
   ================================================ */

/* Base */
.products-page {
  min-height: 100vh;
  background: var(--background, #ffffff);
  /* ── MD3 color token overrides ──
     Force this page to use the MD3 design-system palette
     regardless of which app theme is active.
  */
  --accent-primary:  #003ec7;
  --text-primary:    #131b2e;
  --text-muted:      #434656;
  --bg-card:         #ffffff;
  --bg-elevated:     #f2f3ff;
  --border-subtle:   #c3c5d9;
}

.page-main {
  max-width: var(--site-content-max-width, 1300px);
  margin: 0 auto;
  padding: 32px var(--site-content-padding-x, 24px) 80px;
  overflow: visible;
}

/* ── HERO ── */
.hero-section {
  margin-bottom: 48px;
}
.hero-title {
  font-size: 48px;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.02em;
  line-height: 56px;
  margin: 0 0 16px;
  font-family: 'Manrope', system-ui, sans-serif;
}
.hero-accent { color: var(--accent-primary); }
.hero-subtitle {
  font-size: 18px;
  font-weight: 400;
  font-family: 'Inter', system-ui, sans-serif;
  color: var(--text-muted);
  max-width: 640px;
  line-height: 28px;
  margin: 0;
}

/* ── CONTROLS ── */
.controls-block {
  margin-bottom: 40px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Region Tab Bar */
.region-tab-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 8px;
  border-bottom: 1px solid color-mix(in srgb, var(--border-subtle) 60%, transparent);
  scrollbar-width: none;
}
.region-tab-bar::-webkit-scrollbar { display: none; }
.region-tab {
  flex-shrink: 0;
  padding: 8px 20px;
  border-radius: 100px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 500;
  font-family: 'Inter', system-ui, sans-serif;
  letter-spacing: 0.01em;
  line-height: 20px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s;
}
.region-tab:hover {
  background: color-mix(in srgb, var(--accent-primary) 8%, transparent);
  color: var(--accent-primary);
}
.region-tab--active {
  background: var(--accent-primary);
  color: #fff;
  font-weight: 700;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

/* ── Left subcategory rail：贴在中间内容区左缘外侧 ── */
.subcategory-rail {
  position: fixed;
  z-index: 40;
  top: 50%;
  left: 12px;
  transform: translate(-100%, -50%);
  display: flex;
  align-items: stretch;
  pointer-events: none;
  opacity: 1;
  visibility: visible;
  transition: opacity 0.25s ease, visibility 0.25s ease, left 0.2s ease;
}
.subcategory-rail--hidden {
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
}
.subcategory-rail--hidden > * {
  pointer-events: none;
}
.subcategory-rail > * {
  pointer-events: auto;
}

.subcategory-rail__handle {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 36px;
  padding: 14px 0;
  border: none;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: color 0.25s ease;
}
.subcategory-rail__handle:hover {
  color: var(--accent-primary);
}
.subcategory-rail__handle-bar {
  width: 2px;
  height: 28px;
  border-radius: 2px;
  background: linear-gradient(to bottom, transparent, var(--accent-primary), transparent);
  opacity: 0.7;
}
.subcategory-rail__handle-text {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  white-space: nowrap;
}
.subcategory-rail__handle-icon {
  font-size: 14px;
  transition: transform 0.35s cubic-bezier(0.34, 1.4, 0.64, 1);
}
.subcategory-rail--open .subcategory-rail__handle-icon {
  transform: rotate(180deg);
}

.subcategory-rail__panel {
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
  max-width: 0;
  opacity: 0;
  padding: 0;
  transform: translateX(-8px);
  transition:
    max-width 0.42s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.32s ease,
    transform 0.42s cubic-bezier(0.22, 1, 0.36, 1),
    padding 0.42s ease;
}
.subcategory-rail--open .subcategory-rail__panel {
  max-width: 160px;
  opacity: 1;
  padding: 0 0 0 10px;
  transform: translateX(0);
}

.subcategory-rail__item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 4px 8px 0;
  border: none;
  background: transparent;
  color: color-mix(in srgb, var(--text-muted) 88%, transparent);
  font-size: 13px;
  font-weight: 500;
  font-family: 'Inter', system-ui, sans-serif;
  text-align: left;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.2s ease, transform 0.2s ease;
}
.subcategory-rail--open .subcategory-rail__item {
  animation: subcat-rail-item-in 0.4s cubic-bezier(0.22, 1, 0.36, 1) backwards;
  animation-delay: calc(var(--i, 0) * 45ms + 60ms);
}
@keyframes subcat-rail-item-in {
  from { opacity: 0; transform: translateX(-10px); }
  to { opacity: 1; transform: translateX(0); }
}
.subcategory-rail__item:hover {
  color: var(--accent-primary);
  transform: translateX(2px);
}
.subcategory-rail__dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.35;
  flex-shrink: 0;
  transition: opacity 0.2s, transform 0.2s, background 0.2s;
}
.subcategory-rail__item--active {
  color: var(--accent-primary);
  font-weight: 700;
}
.subcategory-rail__item--active .subcategory-rail__dot {
  opacity: 1;
  background: var(--accent-primary);
  box-shadow: 0 0 8px color-mix(in srgb, var(--accent-primary) 50%, transparent);
  transform: scale(1.25);
}

.subcat-rail-enter-active {
  transition: opacity 0.35s ease, transform 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}
.subcat-rail-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}
.subcat-rail-enter-from,
.subcat-rail-leave-to {
  opacity: 0;
  transform: translate(-100%, -50%) translateX(-16px);
}

/* 边距不足时由 JS 切到 inline，始终用横排展示 */
.subcategory-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 4px;
}
.subcategory-inline__item {
  padding: 5px 12px;
  border: none;
  border-radius: 100px;
  background: transparent;
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s ease;
}
.subcategory-inline__item:hover {
  color: var(--accent-primary);
}
.subcategory-inline__item--active {
  color: var(--accent-primary);
  font-weight: 700;
  text-decoration: underline;
  text-underline-offset: 4px;
}

/* Type Tabs */
.type-tabs {
  display: flex;
  gap: 4px;
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: 10px;
  padding: 4px;
}
.type-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 7px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 500;
  font-family: 'Inter', system-ui, sans-serif;
  letter-spacing: 0.01em;
  line-height: 20px;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.type-tab:hover { color: var(--accent-primary); }
.type-tab--active {
  background: var(--accent-primary);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 2px 8px color-mix(in srgb, var(--accent-primary) 25%, transparent);
}
.type-tab-icon { font-size: 16px; width: 16px; height: 16px; }

/* Search */
.search-wrap {
  position: relative;
  flex: 1;
  min-width: 160px;
  max-width: 280px;
}
.filter-search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  font-size: 18px;
  width: 18px;
  height: 18px;
  color: var(--text-muted);
  pointer-events: none;
}
.filter-search-input :deep(.el-input__wrapper) {
  padding-left: 38px !important;
  border-radius: 8px !important;
  background: var(--bg-elevated) !important;
  border: 1px solid var(--border-subtle) !important;
  box-shadow: none !important;
}
.filter-search-input :deep(.el-input__wrapper:focus-within) {
  border-color: var(--accent-primary) !important;
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--accent-primary) 15%, transparent) !important;
}

/* Sort */
.sort-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}
.sort-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-muted);
  white-space: nowrap;
}
.sort-select :deep(.el-select__wrapper) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  font-size: 14px !important;
  font-weight: 700 !important;
  color: var(--accent-primary) !important;
}

/* ── PRODUCT GRID ── */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  min-height: 300px;
  margin-bottom: 40px;
}
@media (max-width: 1024px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .product-grid { grid-template-columns: 1fr; gap: 16px; }
}

/* ── PRODUCT CARD ── */
a.product-card {
  text-decoration: none;
  color: inherit;
}
.product-card {
  background: var(--bg-card);
  border: 1px solid rgba(195, 197, 217, 0.3);
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.03);
  transition: transform 0.3s, box-shadow 0.3s;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.1);
}

/* Card Image */
.card-img-wrap {
  position: relative;
  width: 100%;
  height: 192px;
  overflow: hidden;
  border-radius: 12px;
  background: var(--bg-elevated);
  flex-shrink: 0;
  margin-bottom: 24px;
}
.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}
.product-card:hover .card-img { transform: scale(1.04); }
.card-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  background: linear-gradient(135deg,
    color-mix(in srgb, var(--accent-primary) 8%, transparent),
    color-mix(in srgb, var(--accent-primary) 4%, transparent));
}
.card-badge {
  position: absolute;
  top: 14px;
  left: 14px;
  background: #00c1fd;
  color: #004b65;
  padding: 4px 12px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 600;
  font-family: 'Inter', system-ui, sans-serif;
  line-height: 16px;
}

/* Card Body */
.card-body {
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.card-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}
.card-name {
  font-size: 24px;
  font-weight: 600;
  font-family: 'Manrope', system-ui, sans-serif;
  color: var(--text-primary);
  margin: 0;
  line-height: 32px;
}
.card-wifi-icon {
  font-size: 20px;
  width: 20px;
  height: 20px;
  color: var(--accent-primary);
  flex-shrink: 0;
  margin-top: 2px;
}
.card-country-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 48px;
  align-content: flex-start;
}
.card-country-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  font-family: 'Inter', system-ui, sans-serif;
  line-height: 20px;
  color: var(--text-muted);
  background: rgba(0, 89, 181, 0.06);
  border: 1px solid rgba(0, 89, 181, 0.12);
}
.card-country-more {
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.04);
  border-color: var(--border-default);
  font-weight: 600;
}
.card-desc {
  font-size: 16px;
  font-weight: 400;
  font-family: 'Inter', system-ui, sans-serif;
  color: var(--text-muted);
  margin: 0;
  line-height: 24px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 48px;
}
.card-meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-support-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-height: 68px;
}
.card-support-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  font-weight: 500;
  font-family: 'Inter', system-ui, sans-serif;
  line-height: 20px;
  letter-spacing: 0.01em;
}
.card-support-label {
  color: var(--text-muted);
  flex-shrink: 0;
}
.card-support-val {
  text-align: right;
}
.card-support-line.is-on .card-support-val { color: var(--accent-primary); }
.card-support-line.is-off .card-support-val { color: var(--text-muted); }
.card-data {
  font-size: 24px;
  font-weight: 600;
  font-family: 'Manrope', system-ui, sans-serif;
  line-height: 32px;
  color: var(--accent-primary);
}
.card-dot { color: var(--text-muted); font-size: 14px; font-weight: 500; font-family: 'Inter', system-ui, sans-serif; letter-spacing: 0.01em; }
.card-days { font-size: 14px; font-weight: 500; font-family: 'Inter', system-ui, sans-serif; letter-spacing: 0.01em; color: var(--text-muted); line-height: 20px; }
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 14px;
  border-top: 1px solid rgba(195, 197, 217, 0.4);
}
.card-price-block { display: flex; flex-direction: column; }
.card-from { font-size: 12px; font-weight: 600; font-family: 'Inter', system-ui, sans-serif; line-height: 16px; color: var(--text-muted); }
.card-price {
  font-size: 20px;
  font-weight: 600;
  font-family: 'Inter', system-ui, sans-serif;
  line-height: 28px;
  color: var(--text-primary);
}
.card-select-btn {
  padding: 12px 32px;
  border-radius: 8px;
  border: none;
  background: var(--accent-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  font-family: 'Inter', system-ui, sans-serif;
  letter-spacing: 0.01em;
  line-height: 20px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s;
}
.card-select-btn:hover { opacity: 0.9; }
.card-select-btn:active { transform: scale(0.97); }

/* ── PAGINATION ── */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 32px 0;
  border-top: 1px solid var(--border-subtle);
}
.products-pagination :deep(.el-pager li) {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid color-mix(in srgb, var(--border-subtle) 80%, transparent);
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 500;
  margin: 0 2px;
  transition: all 0.15s;
}
.products-pagination :deep(.el-pager li:hover) {
  background: color-mix(in srgb, var(--accent-primary) 8%, transparent);
  color: var(--accent-primary);
  border-color: color-mix(in srgb, var(--accent-primary) 30%, transparent);
}
.products-pagination :deep(.el-pager li.is-active) {
  background: var(--accent-primary) !important;
  color: #fff !important;
  border-color: var(--accent-primary) !important;
  font-weight: 700;
  box-shadow: 0 2px 8px color-mix(in srgb, var(--accent-primary) 30%, transparent);
}
.products-pagination :deep(.btn-prev),
.products-pagination :deep(.btn-next) {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid color-mix(in srgb, var(--border-subtle) 80%, transparent);
  background: transparent;
  color: var(--text-muted);
  margin: 0 2px;
  transition: all 0.15s;
}
.products-pagination :deep(.btn-prev:hover),
.products-pagination :deep(.btn-next:hover) {
  background: color-mix(in srgb, var(--accent-primary) 8%, transparent);
  color: var(--accent-primary);
  border-color: color-mix(in srgb, var(--accent-primary) 30%, transparent);
}

/* ── RESPONSIVE ── */
@media (max-width: 768px) {
  .page-main { padding: 80px 16px 60px; }
  .hero-section { text-align: center; }
  .hero-subtitle { margin: 0 auto; }
  .hero-title { font-size: 36px; line-height: 44px; }
  .hero-subtitle { font-size: 16px; line-height: 24px; }
  .card-name { font-size: 20px; line-height: 28px; }
  .card-data { font-size: 20px; line-height: 28px; }
  .hero-section { margin-bottom: 32px; }
  .controls-block { gap: 16px; }
  .search-wrap { min-width: 120px; max-width: 100%; flex: 1 1 100%; order: 3; }
  .sort-wrap { margin-left: 0; }
  .card-body { padding: 16px; }
  .card-name { font-size: 16px; }
}

/* ==============================
   ORDER DIALOG
============================== */
.order-content { color: var(--text-secondary); }
.order-product {
  background: var(--bg-elevated); border-radius: 14px;
  padding: 16px; margin-bottom: 16px;
  border: 1px solid var(--border-subtle);
}
.product-header { display: flex; align-items: center; gap: 14px; }
.product-flag { font-size: 38px; line-height: 1; flex-shrink: 0; }
.product-info { flex: 1; min-width: 0; }
.product-info h3 { color: var(--text-primary); font-size: 15px; font-weight: 700; margin: 0 0 3px; word-break: break-word; }
.product-type { font-size: 12px; color: var(--text-muted); margin: 0; }
.product-support-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 4px;
}
.product-support-line {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 18px;
}
.product-support-line span:last-child {
  font-weight: 600;
  color: var(--text-primary);
}
.product-price { font-size: 20px; font-weight: 800; color: var(--accent-primary); white-space: nowrap; letter-spacing: -0.02em; }
.product-price span { font-size: 11px; font-weight: 400; color: var(--text-muted); margin-left: 2px; }

.plan-info-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--border-subtle); }
.plan-tag { font-size: 11px; }

.dialog-note-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
}
.dialog-specs-block.has-note {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border-subtle);
}

.dialog-phone-features { margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--border-subtle); }
.dialog-features-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 5px 12px; }
@media (max-width: 520px) { .dialog-features-grid { grid-template-columns: 1fr; } }
.dialog-feature-item { display: flex; align-items: flex-start; gap: 5px; font-size: 12px; color: var(--text-secondary); line-height: 1.5; }
.dialog-feature-item.feat-warning { color: #f59e0b; grid-column: 1 / -1; }
.dialog-feature-item.feat-renewal { color: #16a34a; font-weight: 600; grid-column: 1 / -1; }
.feat-icon-check { width: 13px; height: 13px; color: #16a34a; flex-shrink: 0; margin-top: 2px; }

.order-section { margin-bottom: 14px; }
.section-title { font-size: 13px; font-weight: 600; color: var(--text-primary); margin-bottom: 10px; display: flex; align-items: center; gap: 8px; }
.section-hint { font-size: 12px; font-weight: 400; color: var(--text-muted); }
.section-required { font-size: 12px; font-weight: 500; color: #ef4444; }

.esim-options { display: flex; flex-direction: column; gap: 8px; }
.radio-option {
  display: flex; align-items: flex-start; gap: 10px; padding: 10px 12px;
  background: var(--bg-card); border: 1px solid var(--border-subtle);
  border-radius: 10px; cursor: pointer; transition: border-color 0.15s;
}
.radio-option:hover { border-color: var(--accent-primary); }
.radio-option.selected { border-color: var(--accent-primary); background: color-mix(in srgb, var(--accent-primary) 5%, transparent); }
.radio-option input { display: none; }
.radio-icon {
  width: 16px; height: 16px; border: 2px solid var(--border-default);
  border-radius: 50%; flex-shrink: 0; margin-top: 3px; transition: all 0.15s; position: relative;
}
.radio-option.selected .radio-icon { border-color: var(--accent-primary); }
.radio-option.selected .radio-icon::after { content: ''; position: absolute; top: 2px; left: 2px; width: 8px; height: 8px; background: var(--accent-primary); border-radius: 50%; }
.radio-text { flex: 1; }
.radio-title { display: block; font-size: 13px; font-weight: 500; color: var(--text-primary); margin-bottom: 2px; }
.radio-desc { display: block; font-size: 11px; color: var(--text-muted); line-height: 1.4; }

.device-options { display: flex; flex-direction: column; gap: 6px; }
.device-option {
  display: flex; align-items: stretch; padding: 10px 12px;
  background: var(--bg-card); border: 1px solid var(--border-subtle);
  border-radius: 10px; cursor: pointer; transition: border-color 0.15s; position: relative;
}

/* ===== 月份定价档位选择（横向chip）===== */
.tier-chips { display: flex; flex-wrap: wrap; gap: 10px; }
.tier-chip {
  display: flex; align-items: center; padding: 10px 14px; cursor: pointer;
  background: var(--bg-card, #fff); border: 1.5px solid var(--border-subtle, #e5e7eb);
  border-radius: 12px; transition: border-color 0.15s, background 0.15s, box-shadow 0.15s;
  position: relative;
}
.tier-chip input { display: none; }
.tier-chip:hover { border-color: var(--accent-primary, #3b82f6); box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15); }
.tier-chip.selected {
  border-color: var(--accent-primary, #3b82f6);
  background: color-mix(in srgb, var(--accent-primary, #3b82f6) 10%, transparent);
  box-shadow: 0 2px 12px rgba(59, 130, 246, 0.2);
}
.tier-chip-content { display: flex; flex-direction: column; gap: 4px; width: 100%; }
.tier-chip-main { display: flex; align-items: center; gap: 8px; }
.tier-chip-months { font-size: 14px; font-weight: 600; color: var(--text-primary, #111); min-width: 50px; }
.tier-chip-badge {
  font-size: 10px; padding: 2px 8px; border-radius: 12px;
  background: color-mix(in srgb, var(--accent-primary, #3b82f6) 15%, transparent);
  color: var(--accent-primary, #3b82f6); font-weight: 600;
}
.tier-chip-prices { display: flex; align-items: baseline; gap: 6px; }
.tier-chip-total { font-size: 14px; font-weight: 700; color: var(--accent-primary, #3b82f6); }
.tier-chip-monthly { font-size: 12px; color: var(--text-secondary, #666); font-weight: 500; }
.device-option:hover { border-color: var(--accent-primary); }
.device-option.selected { border-color: var(--accent-primary); background: color-mix(in srgb, var(--accent-primary) 5%, transparent); }
.device-option input { display: none; }
.device-option::before {
  content: ''; width: 18px; height: 18px;
  border: 2px solid var(--border-default); border-radius: 5px;
  flex-shrink: 0; align-self: flex-start; margin-top: 2px; transition: all 0.15s;
}
.device-option.selected::before { background: var(--accent-primary); border-color: var(--accent-primary); }
.device-option.selected::after { content: 'v'; position: absolute; left: 20px; top: 12px; color: #fff; font-size: 11px; font-weight: bold; }
.device-content { display: flex; align-items: flex-start; gap: 10px; width: 100%; margin-left: 8px; }
.device-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.device-name { display: block; font-size: 13px; font-weight: 600; color: var(--text-primary); }
.device-desc { display: block; font-size: 11px; color: var(--text-muted); line-height: 1.5; }
.device-price { font-size: 13px; font-weight: 600; color: var(--accent-primary); white-space: nowrap; align-self: flex-start; }

.shipping-note {
  display: flex; align-items: center; gap: 8px; margin-top: 10px;
  padding: 9px 12px;
  background: color-mix(in srgb, #f59e0b 8%, transparent);
  border: 1px solid color-mix(in srgb, #f59e0b 20%, transparent);
  border-radius: 9px; font-size: 12px; color: var(--text-secondary);
}
.shipping-icon { width: 14px; height: 14px; color: #d97706; flex-shrink: 0; }
.shipping-note strong { color: #d97706; }

.shipping-address-section { background: var(--bg-elevated); border-radius: 12px; padding: 12px 14px; border: 1px solid var(--border-subtle); }
.required-hint { color: #ef4444; font-size: 15px; margin-left: 3px; }
.optional-hint { color: var(--text-muted, #717785); font-size: 12px; font-weight: 400; margin-left: 4px; }
.address-form { display: flex; flex-direction: column; gap: 10px; }
.form-row { display: flex; gap: 10px; }
.form-item { flex: 1; display: flex; flex-direction: column; gap: 5px; }
.form-item label { font-size: 12px; color: var(--text-secondary); font-weight: 500; }
.form-item .required { color: #ef4444; }
.form-item :deep(.el-input__wrapper),
.form-item :deep(.el-textarea__wrapper) { background: var(--bg-card); border: 1px solid var(--border-default); box-shadow: none; border-radius: 8px; }
.form-item :deep(.el-input__wrapper:hover),
.form-item :deep(.el-textarea__wrapper:hover),
.form-item :deep(.el-input__wrapper.is-focus),
.form-item :deep(.el-textarea__wrapper.is-focus) { border-color: var(--accent-primary); box-shadow: 0 0 0 2px color-mix(in srgb, var(--accent-primary) 10%, transparent); }

.order-summary { background: var(--bg-elevated); border-radius: 12px; padding: 12px 14px; border: 1px solid var(--border-subtle); }
.summary-row { display: flex; justify-content: space-between; align-items: center; padding: 7px 0; font-size: 13px; color: var(--text-secondary); border-bottom: 1px dashed var(--border-subtle); }
.summary-row:last-child { border-bottom: none; }
.shipping-row { color: var(--text-muted); font-size: 12px; }
.coupon-discount { color: #16a34a; font-weight: 600; }
.coupon-box { margin: 8px 0; padding-top: 8px; border-top: 1px dashed var(--border-subtle); }
.coupon-label { font-size: 12px; color: var(--text-secondary); margin-bottom: 6px; }
.coupon-row { display: flex; gap: 8px; align-items: stretch; }
.coupon-row :deep(.el-input__wrapper) {
  border-radius: 6px;
  min-height: 40px;
  padding: 4px 12px;
}
.coupon-row :deep(.el-input__inner) {
  height: 32px;
  line-height: 32px;
}
.coupon-row :deep(.el-button) {
  border-radius: 6px;
  height: 40px;
  padding: 0 16px;
}
.coupon-tip { margin-top: 6px; font-size: 12px; color: #16a34a; }
.coupon-tip.error { color: #ef4444; }
.total-row { font-size: 15px; font-weight: 700; color: var(--text-primary); padding-top: 10px; margin-top: 3px; border-top: 2px solid var(--border-default) !important; border-bottom: none !important; }
.total-row span:last-child { color: var(--accent-primary); }

.payment-options { display: flex; flex-direction: column; gap: 7px; }
.payment-option { cursor: pointer; transition: all 0.15s; }
.payment-option.disabled { opacity: 0.5; cursor: not-allowed; }
.payment-option input { display: none; }
.payment-content { display: flex; align-items: center; gap: 10px; padding: 10px 12px; background: var(--bg-card); border: 1px solid var(--border-subtle); border-radius: 10px; transition: border-color 0.15s; }
.payment-option:hover .payment-content { border-color: var(--accent-primary); }
.payment-option.selected .payment-content { border-color: var(--accent-primary); background: color-mix(in srgb, var(--accent-primary) 5%, transparent); }
.payment-icon { width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; border-radius: 8px; font-size: 16px; font-weight: 700; flex-shrink: 0; }
.usdt-icon { background: linear-gradient(135deg, #26a17b, #1a6b4e); color: #fff; }
.wechat-icon { background: #07c160; color: #fff; }
.wechat-icon svg, .balance-icon svg, .card-icon svg { width: 20px; height: 20px; }
.balance-icon { background: var(--accent-primary); color: #fff; }
.card-icon { background: var(--accent-muted); color: var(--accent-primary); border: 1px solid color-mix(in srgb, var(--accent-primary) 30%, transparent); }
.payment-info { flex: 1; min-width: 0; }
.payment-name { display: block; font-size: 13px; font-weight: 600; color: var(--text-primary); }
.payment-desc { display: block; font-size: 11px; color: var(--text-muted); margin-top: 1px; }
.balance-desc.insufficient { color: #ef4444; }
.payment-check { width: 18px; height: 18px; color: var(--accent-primary); flex-shrink: 0; }
.payment-check svg { width: 100%; height: 100%; }

.dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }
.btn-submit { min-width: 130px; font-weight: 600; }

@media (max-width: 768px) {
  .page-body { padding: 20px 0 60px; }
  .product-header { flex-wrap: wrap; gap: 10px; }
  .product-flag { font-size: 32px; }
  .product-info h3 { font-size: 14px; }
  .product-price { width: 100%; font-size: 18px; }
  .device-content { flex-direction: column; gap: 4px; }
  .device-price { align-self: flex-start; }
  .form-row { flex-direction: column; gap: 8px; }
  .dialog-footer { width: 100%; flex-direction: column-reverse; gap: 8px; }
  .dialog-footer .el-button,
  .dialog-footer .btn-submit { width: 100%; margin-left: 0 !important; min-height: 44px; font-size: 15px; }
}
</style>

<!-- 全局样式覆盖 el-dialog 标题 -->
<style scoped>
:deep(.order-dialog .el-dialog__header) {
  border-bottom: 1px solid var(--border-subtle);
  padding: 16px 20px;
  margin: 0;
}

:deep(.order-dialog .el-dialog__title) {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.order-dialog .el-dialog__body) {
  padding: 18px 20px;
}

:deep(.order-dialog .el-dialog__footer) {
  border-top: 1px solid var(--border-subtle);
  padding: 14px 20px;
}

@media (max-width: 768px) {
  :deep(.order-dialog .el-dialog) {
    width: calc(100vw - 12px) !important;
    margin: 2vh auto 0 !important;
    border-radius: 16px !important;
  }
  :deep(.order-dialog .el-dialog__header) { padding: 13px 16px; }
  :deep(.order-dialog .el-dialog__title) { font-size: 15px; }
  :deep(.order-dialog .el-dialog__body) { padding: 12px 14px; }
  :deep(.order-dialog .el-dialog__footer) { padding: 10px 14px; }
}

@media (max-width: 480px) {
  :deep(.order-dialog .el-dialog) {
    width: 100vw !important;
    margin: 0 !important;
    border-radius: 20px 20px 0 0 !important;
    position: fixed !important;
    bottom: 0 !important;
    left: 0 !important;
  }
  :deep(.order-dialog .el-dialog__body) { padding: 10px 12px; }
}
</style>
