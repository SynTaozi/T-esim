package com.tesim.service;

import com.tesim.dto.coupon.ValidateCouponRequest;
import com.tesim.dto.coupon.ValidateCouponVO;
import com.tesim.entity.Coupon;
import com.tesim.entity.CouponUsage;
import com.tesim.entity.SalesOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CouponAppService {

    ValidateCouponVO validate(String code, BigDecimal orderAmount, Long productId);



    ValidateCouponVO validateRequest(ValidateCouponRequest req);



    void reserve(Long couponId, String code, SalesOrder order, Long memberId, BigDecimal discount);


}
