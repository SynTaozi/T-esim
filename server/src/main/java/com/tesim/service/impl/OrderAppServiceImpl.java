package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.OrderAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.coupon.ValidateCouponVO;
import com.tesim.dto.order.CreateOrderRequest;
import com.tesim.dto.order.OrderItemVO;
import com.tesim.dto.order.OrderVO;
import com.tesim.entity.*;
import com.tesim.security.SecurityUtils;
import com.tesim.util.LocaleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderAppServiceImpl implements OrderAppService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SalesOrderService salesOrderService;
    private final SalesOrderItemService salesOrderItemService;
    private final SalesOrderAddressService salesOrderAddressService;
    private final SalesOrderPaymentService salesOrderPaymentService;
    private final SalesOrderShipmentService salesOrderShipmentService;
    private final SalesOrderNoticeService salesOrderNoticeService;
    private final SalesOrderEsimService salesOrderEsimService;
    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final I18nProductService i18nProductService;
    private final CmsSiteSettingService siteSettingService;
    private final CouponAppService couponAppService;
    private final OrderPaymentExpireService orderPaymentExpireService;
    private final DingtalkNotifyService dingtalkNotifyService;

    @Transactional
    public OrderVO create(CreateOrderRequest req) {
        Long memberId = SecurityUtils.memberIdOrNull();
        Long productId = req.getProductId();
        if (productId == null) {
            throw new BizException("缺少商品");
        }
        Product product = productService.getById(productId);
        if (product == null || Boolean.TRUE.equals(product.getDeleted()) || !"ON_SALE".equals(product.getStatus())) {
            throw new BizException("商品不可售");
        }
        if (req.getRenewalOrderId() != null || "RENEWAL".equalsIgnoreCase(req.getOrderType())) {
            if (memberId == null) {
                throw new BizException("续费订单请先登录");
            }
        }

        ProductSku sku = resolveSku(productId, req.getSkuId(), req.getPeriodValue());
        BigDecimal unitPrice = sku != null && sku.getSalePrice() != null
                ? sku.getSalePrice()
                : BigDecimal.ZERO;
        if (req.getTotalAmount() != null) {
            unitPrice = req.getTotalAmount();
        }

        BigDecimal shipping = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(product.getIsPhysical()) || StringUtils.hasText(req.getShippingAddress())) {
            CmsSiteSetting setting = siteSettingService.getOne(new LambdaQueryWrapper<CmsSiteSetting>()
                    .eq(CmsSiteSetting::getCode, "default").last("LIMIT 1"));
            if (setting != null && setting.getDefaultShippingFee() != null) {
                shipping = setting.getDefaultShippingFee();
            }
        }

        String couponCode = req.getCouponCode();
        BigDecimal discount = BigDecimal.ZERO;
        Long couponId = null;
        if (StringUtils.hasText(couponCode)) {
            ValidateCouponVO validated = couponAppService.validate(couponCode, unitPrice, productId);
            if (Boolean.FALSE.equals(validated.getValid())) {
                throw new BizException(StringUtils.hasText(validated.getMessage()) ? validated.getMessage() : "优惠券不可用");
            }
            discount = validated.getDiscountAmount() != null ? validated.getDiscountAmount() : BigDecimal.ZERO;
            couponId = validated.getCouponId();
            couponCode = validated.getCode();
        }

        BigDecimal goods = unitPrice;
        BigDecimal payable = goods.add(shipping).subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        String email = normalizeEmail(req.getEmail());
        if (!StringUtils.hasText(email)) {
            throw new BizException("缺少邮箱");
        }
        if (!isValidEmail(email)) {
            throw new BizException("邮箱格式不正确");
        }
        String paymentMethod = normalizePayMethod(req.getPaymentMethod());
        if ("BALANCE".equals(paymentMethod) && memberId == null) {
            throw new BizException("余额支付请先登录");
        }
        String locale = StringUtils.hasText(req.getLanguage())
                ? LocaleHelper.normalize(req.getLanguage())
                : LocaleHelper.current();
        String orderType = StringUtils.hasText(req.getOrderType()) ? req.getOrderType() : "PURCHASE";
        Long renewalOf = req.getRenewalOrderId();

        SalesOrder order = new SalesOrder();
        order.setOrderNo(genOrderNo());
        order.setMemberId(memberId);
        order.setOrderType(orderType);
        order.setStatus("PENDING_PAYMENT");
        order.setCurrency("USD");
        order.setGoodsAmount(goods);
        order.setShippingAmount(shipping);
        order.setDiscountAmount(discount);
        order.setPayableAmount(payable);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setCouponId(couponId);
        order.setCouponCode(StringUtils.hasText(couponCode) ? couponCode : null);
        order.setLocale(locale);
        order.setBuyerEmail(email);
        order.setBuyerRemark(firstNonBlank(req.getCustomerRemark(), req.getRemark()));
        order.setSellerRemark(req.getRemark());
        order.setRenewalOfOrderId(renewalOf);
        order.setDeleted(false);
        salesOrderService.save(order);

        I18nProduct zh = i18nProductService.getOne(new LambdaQueryWrapper<I18nProduct>()
                .eq(I18nProduct::getProductId, productId).eq(I18nProduct::getLocale, "zh"));
        I18nProduct en = i18nProductService.getOne(new LambdaQueryWrapper<I18nProduct>()
                .eq(I18nProduct::getProductId, productId).eq(I18nProduct::getLocale, "en"));

        SalesOrderItem item = new SalesOrderItem();
        item.setOrderId(order.getId());
        item.setProductId(productId);
        item.setSkuId(sku != null ? sku.getId() : null);
        item.setProductKind(product.getProductKind());
        item.setSkuCode(sku != null ? sku.getSkuCode() : null);
        item.setTitleZh(zh != null ? zh.getName() : (StringUtils.hasText(req.getProductName())
                ? req.getProductName() : product.getSpuCode()));
        item.setTitleEn(en != null ? en.getName() : item.getTitleZh());
        item.setMonths(sku != null ? sku.getMonths() : null);
        item.setValidityDays(sku != null && sku.getDurationDays() != null
                ? sku.getDurationDays() : product.getBaseValidityDays());
        item.setQuantity(1);
        item.setUnitPrice(unitPrice);
        item.setPricePerMonth(sku != null ? sku.getPricePerMonth() : null);
        item.setLineAmount(unitPrice);
        salesOrderItemService.save(item);

        saveAddressIfPresent(order.getId(), email, req.getShippingAddress());

        SalesOrderPayment payment = new SalesOrderPayment();
        payment.setOrderId(order.getId());
        payment.setChannelCode(paymentMethod);
        payment.setPayStatus("PENDING");
        payment.setAmount(payable);
        payment.setCurrency("USD");
        payment.setExpireAt(LocalDateTime.now().plusMinutes(30));
        salesOrderPaymentService.save(payment);
        orderPaymentExpireService.schedule(order.getId());

        if (couponId != null) {
            couponAppService.reserve(couponId, couponCode, order, memberId, discount);
        }

        return toFrontOrder(order, true);
    }

    public List<OrderVO> listMine() {
        Long memberId = SecurityUtils.requireMemberId();
        List<SalesOrder> orders = salesOrderService.list(new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getMemberId, memberId)
                .orderByDesc(SalesOrder::getCreatedAt));
        return orders.stream().map(o -> toFrontOrder(o, true)).toList();
    }

    public PageResult<OrderVO> paginated(int page, int size) {
        Long memberId = SecurityUtils.requireMemberId();
        Page<SalesOrder> result = salesOrderService.page(new Page<>(page + 1L, size),
                new LambdaQueryWrapper<SalesOrder>()
                        .eq(SalesOrder::getMemberId, memberId)
                        .orderByDesc(SalesOrder::getCreatedAt));
        return PageResult.of(result, o -> toFrontOrder(o, true));
    }

    /**
     * Frontend my-orders uses 1-based {@code page}; null/&lt;1 treated as 1.
     * MyBatis-Plus {@code Page.current} stays 1-based; {@link PageResult#getNumber()} is 0-based.
     */
    public PageResult<OrderVO> myOrders(Integer page, Integer pageSize, Integer status) {
        Long memberId = SecurityUtils.requireMemberId();
        int p = page == null || page < 1 ? 1 : page;
        int s = pageSize == null || pageSize < 1 ? 10 : pageSize;
        LambdaQueryWrapper<SalesOrder> qw = new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getMemberId, memberId)
                .orderByDesc(SalesOrder::getCreatedAt);
        if (status != null) {
            qw.eq(SalesOrder::getStatus, fromFrontStatus(status));
        }
        Page<SalesOrder> result = salesOrderService.page(new Page<>(p, s), qw);
        return PageResult.of(result, o -> toFrontOrder(o, true));
    }

    public OrderVO getById(Long id) {
        SalesOrder order = requireOwned(id);
        return toFrontOrder(order, true);
    }

    public OrderVO getByNo(String orderNo) {
        SalesOrder order = salesOrderService.getOne(new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getOrderNo, orderNo));
        if (order == null || !Objects.equals(order.getMemberId(), SecurityUtils.requireMemberId())) {
            throw new BizException("订单不存在");
        }
        return toFrontOrder(order, true);
    }

    @Transactional
    public OrderVO cancel(Long id) {
        SalesOrder order = requireOwned(id);
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BizException("当前状态不可取消");
        }
        order.setStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        salesOrderService.updateById(order);
        SalesOrderPayment pay = latestPayment(order.getId());
        if (pay != null && "PENDING".equals(pay.getPayStatus())) {
            pay.setPayStatus("EXPIRED");
            salesOrderPaymentService.updateById(pay);
        }
        orderPaymentExpireService.clear(order.getId());
        return toFrontOrder(order, true);
    }

    @Transactional
    public OrderVO pay(Long id) {
        SalesOrder order = requireOwned(id);
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BizException("订单不可支付");
        }
        markPaid(order, latestPayment(order.getId()), null);
        return toFrontOrder(order, true);
    }

    @Transactional
    public void markPaid(SalesOrder order, SalesOrderPayment payment, String transactionId) {
        order.setStatus("PAID");
        order.setPaidAmount(order.getPayableAmount());
        order.setPaidAt(LocalDateTime.now());
        salesOrderService.updateById(order);
        if (payment != null) {
            payment.setPayStatus("SUCCESS");
            payment.setPaidAt(LocalDateTime.now());
            if (StringUtils.hasText(transactionId)) {
                payment.setTransactionId(transactionId);
            }
            salesOrderPaymentService.updateById(payment);
        }
        orderPaymentExpireService.clear(order.getId());
        ensureSubscriptions(order);
        dingtalkNotifyService.notifyOrderPaid(order, payment);
    }

    @Override
    public void ensureSubscriptions(SalesOrder order) {
        // 暂不自动创建 member_subscription（无真实号码/号池，手动发货阶段用不到）
    }

    public SalesOrder requireOwned(Long id) {
        SalesOrder order = salesOrderService.getById(id);
        if (order == null || !Objects.equals(order.getMemberId(), SecurityUtils.requireMemberId())) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    public SalesOrder findByOrderNo(String orderNo) {
        return salesOrderService.getOne(new LambdaQueryWrapper<SalesOrder>().eq(SalesOrder::getOrderNo, orderNo));
    }

    public SalesOrderPayment latestPayment(Long orderId) {
        return salesOrderPaymentService.getOne(new LambdaQueryWrapper<SalesOrderPayment>()
                .eq(SalesOrderPayment::getOrderId, orderId)
                .orderByDesc(SalesOrderPayment::getId)
                .last("LIMIT 1"));
    }

    public OrderVO toFrontOrder(SalesOrder order, boolean withItems) {
        List<SalesOrderItem> items = withItems
                ? salesOrderItemService.list(new LambdaQueryWrapper<SalesOrderItem>().eq(SalesOrderItem::getOrderId, order.getId()))
                : List.of();
        SalesOrderPayment payment = latestPayment(order.getId());
        SalesOrderShipment shipment = salesOrderShipmentService.getOne(new LambdaQueryWrapper<SalesOrderShipment>()
                .eq(SalesOrderShipment::getOrderId, order.getId()).last("LIMIT 1"));
        SalesOrderAddress address = salesOrderAddressService.getOne(new LambdaQueryWrapper<SalesOrderAddress>()
                .eq(SalesOrderAddress::getOrderId, order.getId()).last("LIMIT 1"));
        SalesOrderNotice notice = salesOrderNoticeService.getOne(new LambdaQueryWrapper<SalesOrderNotice>()
                .eq(SalesOrderNotice::getOrderId, order.getId())
                .orderByDesc(SalesOrderNotice::getId).last("LIMIT 1"));
        SalesOrderEsim esim = salesOrderEsimService.getOne(new LambdaQueryWrapper<SalesOrderEsim>()
                .eq(SalesOrderEsim::getOrderId, order.getId())
                .orderByDesc(SalesOrderEsim::getId)
                .last("LIMIT 1"));
        String qr = esim != null && StringUtils.hasText(esim.getQrPayload())
                ? esim.getQrPayload()
                : (esim != null ? esim.getActivationCode() : null);

        int status = toFrontStatus(order.getStatus());
        OrderVO.OrderVOBuilder builder = OrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .memberId(order.getMemberId())
                .guest(order.getMemberId() == null)
                .totalAmount(order.getPayableAmount())
                .finalAmount(order.getPayableAmount())
                .payableAmount(order.getPayableAmount())
                .goodsAmount(order.getGoodsAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .currency(order.getCurrency())
                .status(status)
                .statusText(statusText(status))
                .rawStatus(order.getStatus())
                .email(order.getBuyerEmail())
                .buyerEmail(order.getBuyerEmail())
                .shippingAddress(formatAddress(address))
                .remark(order.getBuyerRemark())
                .customerRemark(order.getBuyerRemark())
                .paymentMethod(payment != null ? payment.getChannelCode() : null)
                .trackingNumber(shipment != null ? shipment.getTrackingNo() : null)
                .shippingCarrier(shipment != null ? shipment.getCarrierCode() : null)
                .trackingQrCode(qr)
                .shippingPhone(shipment != null ? shipment.getShippingPhone() : null)
                .shippingRemark(shipment != null ? shipment.getShippingRemark() : null)
                .shippedAt(shipment != null && shipment.getShippedAt() != null ? format(shipment.getShippedAt()) : null)
                .orderType(order.getOrderType())
                .renewalOrderId(order.getRenewalOfOrderId())
                .couponCode(order.getCouponCode())
                .createdAt(format(order.getCreatedAt()))
                .updatedAt(format(order.getUpdatedAt()));
        if (notice != null) {
            builder.customerNoticeSentAt(notice.getSentAt() != null ? format(notice.getSentAt()) : null)
                    .customerNoticeSubject(notice.getSubject())
                    .customerNoticeContent(sanitizeCustomerNotice(notice.getContent()));
        }
        if (payment != null) {
            builder.paymentId("PAY" + payment.getId())
                    .thirdPartyOrderNo(payment.getThirdPartyTradeNo())
                    .usdtAmount(payment.getCryptoAmount())
                    .usdtAddress(payment.getCryptoAddress())
                    .paymentExpireTime(payment.getExpireAt() != null
                            ? payment.getExpireAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                    .paymentStatus(payment.getPayStatus())
                    .transactionId(payment.getTransactionId());
        }
        List<OrderItemVO> itemVos = items.stream().map(it -> OrderItemVO.builder()
                .id(it.getId())
                .productId(it.getProductId())
                .productName(LocaleHelper.pick(it.getTitleZh(), it.getTitleEn()))
                .nameEn(it.getTitleEn())
                .nameZh(it.getTitleZh())
                .price(it.getUnitPrice())
                .quantity(it.getQuantity())
                .subtotal(it.getLineAmount())
                .itemType(it.getProductKind())
                .productType(it.getProductKind())
                .validityDays(it.getValidityDays())
                .months(it.getMonths())
                .build()).toList();
        return builder.items(itemVos).build();
    }

    private ProductSku resolveSku(Long productId, Long skuId, Integer periodValue) {
        if (skuId != null) {
            ProductSku sku = productSkuService.getById(skuId);
            if (sku != null && Objects.equals(sku.getProductId(), productId)) {
                return sku;
            }
        }
        Integer months = periodValue;
        if (months != null && months > 0) {
            ProductSku byMonths = productSkuService.getOne(new LambdaQueryWrapper<ProductSku>()
                    .eq(ProductSku::getProductId, productId)
                    .eq(ProductSku::getMonths, months)
                    .eq(ProductSku::getStatus, 1)
                    .last("LIMIT 1"));
            if (byMonths != null) {
                return byMonths;
            }
        }
        return productSkuService.getOne(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId)
                .eq(ProductSku::getStatus, 1)
                .orderByDesc(ProductSku::getIsDefault)
                .orderByAsc(ProductSku::getSortOrder)
                .last("LIMIT 1"));
    }

    private void saveAddressIfPresent(Long orderId, String email, String raw) {
        if (!StringUtils.hasText(raw)) {
            return;
        }
        SalesOrderAddress addr = new SalesOrderAddress();
        addr.setOrderId(orderId);
        addr.setAddressType("SHIPPING");
        addr.setEmail(email);
        addr.setReceiverName("-");
        addr.setPhone("-");
        addr.setAddressLine(raw);
        try {
            Map<String, Object> map = MAPPER.readValue(raw, new TypeReference<>() {});
            addr.setReceiverName(firstNonBlank(str(map.get("name")), str(map.get("receiverName")), "-"));
            addr.setPhone(firstNonBlank(str(map.get("phone")), str(map.get("mobile")), "-"));
            addr.setCountry(str(map.get("country")));
            addr.setProvince(str(map.get("province")));
            addr.setCity(str(map.get("city")));
            addr.setPostalCode(str(map.get("postalCode")));
            String line = firstNonBlank(str(map.get("address")), str(map.get("addressLine")), str(map.get("detail")));
            if (StringUtils.hasText(line)) {
                addr.setAddressLine(line);
            }
        } catch (Exception ignored) {
            // keep raw as address line
        }
        salesOrderAddressService.save(addr);
    }

    private static String formatAddress(SalesOrderAddress a) {
        if (a == null) {
            return null;
        }
        return String.join(" ",
                Objects.toString(a.getCountry(), ""),
                Objects.toString(a.getProvince(), ""),
                Objects.toString(a.getCity(), ""),
                Objects.toString(a.getAddressLine(), ""),
                Objects.toString(a.getPostalCode(), "")).trim();
    }

    /** 用户端展示时去掉内部履约话术 */
    private static String sanitizeCustomerNotice(String content) {
        if (!StringUtils.hasText(content)) {
            return content;
        }
        String cleaned = content
                .replace("手动履约完成；", "")
                .replace("手动履约完成", "")
                .replace("；已录入激活码/二维码", "")
                .replace("已录入激活码/二维码；", "")
                .replace("已录入激活码/二维码", "")
                .replaceAll("^；+", "")
                .replaceAll("；{2,}", "；")
                .trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static int toFrontStatus(String status) {
        if (status == null) {
            return 0;
        }
        return switch (status) {
            case "PAID", "FULFILLING" -> 1;
            case "SHIPPED" -> 2;
            case "COMPLETED" -> 3;
            case "CANCELLED" -> 4;
            default -> 0;
        };
    }

    private static String fromFrontStatus(int status) {
        return switch (status) {
            case 1 -> "PAID";
            case 2 -> "SHIPPED";
            case 3 -> "COMPLETED";
            case 4 -> "CANCELLED";
            default -> "PENDING_PAYMENT";
        };
    }

    private static String statusText(int status) {
        return switch (status) {
            case 1 -> "Paid";
            case 2 -> "Shipped";
            case 3 -> "Completed";
            case 4 -> "Cancelled";
            default -> "Pending Payment";
        };
    }

    private static String normalizePayMethod(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "USDT";
        }
        String v = raw.trim().toUpperCase();
        return switch (v) {
            case "WECHAT" -> "WECHAT";
            case "ALIPAY" -> "ALIPAY";
            case "BANKCARD", "BANK_CARD", "CARD" -> "BANK_CARD";
            case "BALANCE" -> "BALANCE";
            case "STRIPE" -> "STRIPE";
            default -> "USDT";
        };
    }

    private static String normalizeEmail(String email) {
        return StringUtils.hasText(email) ? email.trim().toLowerCase() : null;
    }

    private static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public PageResult<OrderVO> guestLookup(String email, Integer page, Integer pageSize) {
        String em = normalizeEmail(email);
        if (!StringUtils.hasText(em)) {
            throw new BizException("请输入下单邮箱");
        }
        if (!isValidEmail(em)) {
            throw new BizException("邮箱格式不正确");
        }
        int p = page == null || page < 1 ? 1 : page;
        int s = pageSize == null || pageSize < 1 ? 5 : Math.min(pageSize, 20);
        Page<SalesOrder> result = salesOrderService.page(new Page<>(p, s),
                new LambdaQueryWrapper<SalesOrder>()
                        .eq(SalesOrder::getBuyerEmail, em)
                        .eq(SalesOrder::getDeleted, false)
                        .orderByDesc(SalesOrder::getCreatedAt));
        return PageResult.of(result, o -> toFrontOrder(o, true));
    }

    private static String genOrderNo() {
        return "T" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    private static String format(LocalDateTime t) {
        return t == null ? null : FMT.format(t);
    }

    private static String str(Object v) {
        return v == null ? null : String.valueOf(v).trim();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return null;
    }
}
