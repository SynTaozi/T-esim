package com.tesim.service;

import com.tesim.dto.admin.AdminOrderShippingRequest;

import java.util.Map;

public interface OrderFulfillmentAppService {

    /** 手动履约：填写物流/激活信息后标记已发货 */
    Map<String, Object> manualFulfill(Long orderId, AdminOrderShippingRequest req);
}
