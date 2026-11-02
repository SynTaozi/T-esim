package com.tesim.service.impl;

import com.tesim.service.*;

import com.tesim.service.AdminDashboardAppService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tesim.dto.admin.DashboardOverviewVO;
import com.tesim.dto.admin.DashboardStatsVO;
import com.tesim.entity.Member;
import com.tesim.entity.Product;
import com.tesim.entity.SalesOrder;
import com.tesim.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardAppServiceImpl implements AdminDashboardAppService {

    private final SalesOrderService salesOrderService;
    private final MemberService memberService;
    private final ProductService productService;
    private final TicketService ticketService;

    public DashboardStatsVO stats() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long todayOrders = countOrdersBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        long yesterdayOrders = countOrdersBetween(yesterday.atStartOfDay(), today.atStartOfDay());
        BigDecimal todayRevenue = sumPaidBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        BigDecimal yesterdayRevenue = sumPaidBetween(yesterday.atStartOfDay(), today.atStartOfDay());

        long pendingOrders = salesOrderService.count(new LambdaQueryWrapper<SalesOrder>()
                .in(SalesOrder::getStatus, List.of("PENDING_PAYMENT", "PAID", "PROCESSING"))
                .eq(SalesOrder::getDeleted, false));
        long awaitingShipment = salesOrderService.count(new LambdaQueryWrapper<SalesOrder>()
                .in(SalesOrder::getStatus, List.of("PAID", "PROCESSING"))
                .eq(SalesOrder::getDeleted, false));
        long paidToday = salesOrderService.count(new LambdaQueryWrapper<SalesOrder>()
                .ge(SalesOrder::getPaidAt, today.atStartOfDay())
                .lt(SalesOrder::getPaidAt, today.plusDays(1).atStartOfDay())
                .eq(SalesOrder::getDeleted, false));

        long totalUsers = memberService.count(new LambdaQueryWrapper<Member>().eq(Member::getDeleted, false));
        long todayNewUsers = memberService.count(new LambdaQueryWrapper<Member>()
                .ge(Member::getCreatedAt, today.atStartOfDay())
                .eq(Member::getDeleted, false));
        long totalProducts = productService.count(new LambdaQueryWrapper<Product>().eq(Product::getDeleted, false));
        BigDecimal totalRevenue = sumPaidBetween(LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now().plusDays(1));
        long openTickets = ticketService.count(new LambdaQueryWrapper<Ticket>()
                .in(Ticket::getStatus, List.of("OPEN", "PENDING", "IN_PROGRESS")));

        return DashboardStatsVO.builder()
                .todayOrders(todayOrders)
                .yesterdayOrders(yesterdayOrders)
                .todayOrdersChangePct(pct(todayOrders, yesterdayOrders))
                .todayRevenue(todayRevenue)
                .yesterdayRevenue(yesterdayRevenue)
                .todayRevenueChangePct(pct(todayRevenue, yesterdayRevenue))
                .pendingOrders(pendingOrders)
                .awaitingShipment(awaitingShipment)
                .paidToday(paidToday)
                .totalUsers(totalUsers)
                .todayNewUsers(todayNewUsers)
                .totalProducts(totalProducts)
                .totalRevenue(totalRevenue)
                .openTickets(openTickets)
                .build();
    }

    public DashboardOverviewVO overview() {
        LocalDate today = LocalDate.now();
        List<DashboardOverviewVO.DayPoint> last7 = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime end = d.plusDays(1).atStartOfDay();
            last7.add(DashboardOverviewVO.DayPoint.builder()
                    .date(d.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .orders(countOrdersBetween(start, end))
                    .revenue(sumPaidBetween(start, end))
                    .build());
        }

        List<SalesOrder> recent = salesOrderService.list(new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getDeleted, false)
                .orderByDesc(SalesOrder::getCreatedAt)
                .last("LIMIT 10"));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<DashboardOverviewVO.RecentOrder> recentOrders = recent.stream()
                .map(o -> DashboardOverviewVO.RecentOrder.builder()
                        .id(o.getId())
                        .orderNo(o.getOrderNo())
                        .status(o.getStatus())
                        .payableAmount(o.getPayableAmount())
                        .buyerEmail(o.getBuyerEmail())
                        .createdAt(o.getCreatedAt() == null ? null : o.getCreatedAt().format(fmt))
                        .build())
                .toList();

        List<SalesOrder> all = salesOrderService.list(new LambdaQueryWrapper<SalesOrder>()
                .eq(SalesOrder::getDeleted, false)
                .select(SalesOrder::getStatus));
        Map<String, Long> statusCounts = all.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus() == null ? "UNKNOWN" : o.getStatus(), Collectors.counting()));

        return DashboardOverviewVO.builder()
                .last7Days(last7)
                .recentOrders(recentOrders)
                .orderStatusCounts(statusCounts == null ? new HashMap<>() : statusCounts)
                .build();
    }

    private long countOrdersBetween(LocalDateTime start, LocalDateTime end) {
        return salesOrderService.count(new LambdaQueryWrapper<SalesOrder>()
                .ge(SalesOrder::getCreatedAt, start)
                .lt(SalesOrder::getCreatedAt, end)
                .eq(SalesOrder::getDeleted, false));
    }

    private BigDecimal sumPaidBetween(LocalDateTime start, LocalDateTime end) {
        List<SalesOrder> list = salesOrderService.list(new LambdaQueryWrapper<SalesOrder>()
                .ge(SalesOrder::getPaidAt, start)
                .lt(SalesOrder::getPaidAt, end)
                .eq(SalesOrder::getDeleted, false)
                .isNotNull(SalesOrder::getPaidAt));
        return list.stream()
                .map(o -> o.getPaidAmount() != null ? o.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private double pct(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100D : 0D;
        }
        return BigDecimal.valueOf(current - previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previous), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double pct(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100D : 0D;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
