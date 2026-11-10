package com.tesim.service;

import com.tesim.common.BizException;
import com.tesim.common.PageResult;
import com.tesim.dto.coupon.ValidateCouponVO;
import com.tesim.dto.order.CreateOrderRequest;
import com.tesim.dto.order.OrderItemVO;
import com.tesim.dto.order.OrderVO;
import com.tesim.entity.*;
import com.tesim.security.SecurityUtils;
import com.tesim.util.LocaleHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Map;

public interface OrderAppService {

    OrderVO create(CreateOrderRequest req);



    PageResult<OrderVO> guestLookup(String email, Integer page, Integer pageSize);



    List<OrderVO> listMine();



    PageResult<OrderVO> paginated(int page, int size);



    PageResult<OrderVO> myOrders(Integer page, Integer pageSize, Integer status);



    OrderVO getById(Long id);



    OrderVO getByNo(String orderNo);



    OrderVO cancel(Long id);



    OrderVO pay(Long id);



    void markPaid(SalesOrder order, SalesOrderPayment payment, String transactionId);

    /** Reserved: auto member_subscription creation currently disabled. */
    void ensureSubscriptions(SalesOrder order);

    SalesOrder requireOwned(Long id);



    SalesOrder findByOrderNo(String orderNo);



    SalesOrderPayment latestPayment(Long orderId);



    OrderVO toFrontOrder(SalesOrder order, boolean withItems);


}
