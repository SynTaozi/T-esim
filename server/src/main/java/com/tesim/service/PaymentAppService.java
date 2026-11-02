package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.dto.payment.*;
import com.tesim.entity.SalesOrder;
import com.tesim.entity.SalesOrderPayment;
import com.tesim.security.SecurityUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.List;
import java.util.Map;

public interface PaymentAppService {

    PaymentOrderVO createUsdt(CreateUsdtPaymentRequest req);



    void usdtCallback(UsdtCallbackRequest req);



    void simulatePay(SimulatePayRequest req);



    PaymentOrderVO queryByPaymentId(String paymentId);



    PaymentOrderVO queryByOrderNo(String orderNo);



    UsdtAddressVO usdtAddress();



    CheckoutRedirectVO stripeCheckoutSession(StripeCheckoutRequest req);



    StripeConfirmVO confirmStripe(String sessionId);



    void handleStripeWebhook(String payload, String signatureHeader);


}
