package com.tesim.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsVO {
    private long todayOrders;
    private long yesterdayOrders;
    private double todayOrdersChangePct;
    private BigDecimal todayRevenue;
    private BigDecimal yesterdayRevenue;
    private double todayRevenueChangePct;
    private long pendingOrders;
    private long awaitingShipment;
    private long paidToday;
    private long totalUsers;
    private long todayNewUsers;
    private long totalProducts;
    private BigDecimal totalRevenue;
    private long openTickets;
}
