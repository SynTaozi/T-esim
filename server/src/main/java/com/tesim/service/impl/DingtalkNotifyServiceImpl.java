package com.tesim.service.impl;

import com.tesim.common.BizException;
import com.tesim.entity.SalesOrder;
import com.tesim.entity.SalesOrderPayment;
import com.tesim.entity.SysDingtalkConfig;
import com.tesim.entity.Ticket;
import com.tesim.service.DingtalkNotifyService;
import com.tesim.service.SysDingtalkConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class DingtalkNotifyServiceImpl implements DingtalkNotifyService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysDingtalkConfigService configService;
    private final DingtalkSendExecutor sendExecutor;

    @Override
    public void notifyOrderPaid(SalesOrder order, SalesOrderPayment payment) {
        if (order == null) {
            return;
        }
        String title = "新订单支付成功";
        String text = buildPaymentMarkdown(order, payment);
        scheduleAfterCommit(() -> sendExecutor.sendMarkdownAsync("ORDER_PAID", title, text));
    }

    @Override
    public void notifyNewTicket(Ticket ticket, String userEmail) {
        if (ticket == null) {
            return;
        }
        String title = "新工单提交";
        String text = buildTicketMarkdown(ticket.getTicketNo(), ticket.getSubject(), userEmail);
        scheduleAfterCommit(() -> sendExecutor.sendMarkdownAsync("NEW_TICKET", title, text));
    }

    @Override
    public void testSend(Long configId) {
        SysDingtalkConfig config = configService.getById(configId);
        if (config == null) {
            throw new BizException("钉钉配置不存在");
        }
        if (!StringUtils.hasText(config.getWebhookUrl())) {
            throw new BizException("Webhook 未配置");
        }
        // 机器人安全设置关键词为「新」，测试文案必须包含该字
        String content = "【Tesim】新通知测试：钉钉机器人连通正常，此消息由管理员手动触发。";
        try {
            sendExecutor.sendTextSync(config, "TEST", "新通知测试", content);
        } catch (Exception e) {
            sendExecutor.saveFailure(config.getId(), "TEST", "测试消息", content, e.getMessage());
            throw new BizException("测试发送失败：" + e.getMessage());
        }
    }

    private void scheduleAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private String buildPaymentMarkdown(SalesOrder order, SalesOrderPayment payment) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 新订单支付成功\n\n");
        sb.append("- **订单号：** ").append(nullToDash(order.getOrderNo())).append("\n");
        sb.append("- **用户邮箱：** ").append(nullToDash(order.getBuyerEmail())).append("\n");
        BigDecimal amount = order.getPaidAmount() != null ? order.getPaidAmount() : order.getPayableAmount();
        if (amount != null) {
            sb.append("- **支付金额：** $").append(amount).append("\n");
        }
        if (payment != null && StringUtils.hasText(payment.getChannelCode())) {
            sb.append("- **支付方式：** ").append(payment.getChannelCode()).append("\n");
        }
        if (order.getCreatedAt() != null) {
            sb.append("- **下单时间：** ").append(FMT.format(order.getCreatedAt())).append("\n");
        }
        sb.append("\n> 请及时登录后台处理订单。");
        return sb.toString();
    }

    private String buildTicketMarkdown(String ticketNo, String subject, String userEmail) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 有新工单提交\n\n");
        sb.append("- **工单号：** ").append(nullToDash(ticketNo)).append("\n");
        sb.append("- **主题：** ").append(nullToDash(subject)).append("\n");
        sb.append("- **用户：** ").append(nullToDash(userEmail)).append("\n");
        sb.append("\n> 请及时登录后台处理工单。");
        return sb.toString();
    }

    private static String nullToDash(String v) {
        return StringUtils.hasText(v) ? v.trim() : "—";
    }
}
