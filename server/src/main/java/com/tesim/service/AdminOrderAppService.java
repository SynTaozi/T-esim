package com.tesim.service;

import com.tesim.common.PageResult;
import com.tesim.dto.admin.AdminCustomerNoticeRequest;
import com.tesim.dto.admin.AdminOrderShippingRequest;
import com.tesim.dto.order.OrderVO;

import java.util.Map;

public interface AdminOrderAppService {

    PageResult<OrderVO> page(String status, Integer page, Integer size, String orderNo,
                             String email, String thirdPartyOrderNo, Boolean excludeCancelled);

    OrderVO detail(Long id);

    OrderVO updateStatus(Long id, String status);

    OrderVO updateShipping(Long id, AdminOrderShippingRequest req);

    Map<String, Object> resendNotification(Long id);

    OrderVO sendCustomerNotice(Long id, AdminCustomerNoticeRequest req);

    /** 手动履约发货 */
    Map<String, Object> manualFulfill(Long id, AdminOrderShippingRequest req);
}
