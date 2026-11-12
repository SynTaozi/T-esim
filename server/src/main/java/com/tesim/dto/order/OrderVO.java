package com.tesim.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long memberId;
    /** true when order was placed without a member account */
    private Boolean guest;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private BigDecimal payableAmount;
    private BigDecimal goodsAmount;
    private BigDecimal shippingAmount;
    private BigDecimal discountAmount;
    private String currency;
    /** Front status: 0 pending, 1 paid, 2 shipped, 3 completed, 4 cancelled */
    private Integer status;
    private String statusText;
    /** Backend status string e.g. PENDING_PAYMENT */
    private String rawStatus;
    private String email;
    private String buyerEmail;
    private String shippingAddress;
    private String remark;
    private String customerRemark;
    private String paymentMethod;
    private String trackingNumber;
    private String shippingCarrier;
    private String trackingQrCode;
    private String shippingPhone;
    private String shippingRemark;
    private String shippedAt;
    private String customerNoticeSentAt;
    private String customerNoticeSubject;
    private String customerNoticeContent;
    private String orderType;
    private Long renewalOrderId;
    private String couponCode;
    private List<OrderItemVO> items;
    private String createdAt;
    private String updatedAt;
    private String paymentId;
    private String thirdPartyOrderNo;
    private BigDecimal usdtAmount;
    private String usdtAddress;
    private Long paymentExpireTime;
    private String paymentStatus;
    private String transactionId;
}
