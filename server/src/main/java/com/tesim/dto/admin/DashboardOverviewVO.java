package com.tesim.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardOverviewVO {
    private List<DayPoint> last7Days;
    private List<RecentOrder> recentOrders;
    private Map<String, Long> orderStatusCounts;

    @Data
    @Builder
    public static class DayPoint {
        private String date;
        private long orders;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    public static class RecentOrder {
        private Long id;
        private String orderNo;
        private String status;
        private BigDecimal payableAmount;
        private String buyerEmail;
        private String createdAt;
    }
}
