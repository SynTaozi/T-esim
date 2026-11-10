package com.tesim.service;

import com.tesim.entity.SalesOrder;
import com.tesim.entity.SalesOrderPayment;
import com.tesim.entity.Ticket;

/**
 * 业务钉钉通知（支付成功 / 新工单 / 测试），异步真实调用 Webhook。
 */
public interface DingtalkNotifyService {

    void notifyOrderPaid(SalesOrder order, SalesOrderPayment payment);

    void notifyNewTicket(Ticket ticket, String userEmail);

    /** 指定配置测试发送；失败抛 BizException */
    void testSend(Long configId);
}
