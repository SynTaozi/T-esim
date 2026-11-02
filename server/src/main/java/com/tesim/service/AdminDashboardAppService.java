package com.tesim.service;

import com.tesim.dto.admin.DashboardOverviewVO;
import com.tesim.dto.admin.DashboardStatsVO;
import com.tesim.entity.Member;
import com.tesim.entity.Product;
import com.tesim.entity.SalesOrder;
import com.tesim.entity.Ticket;
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

public interface AdminDashboardAppService {

    DashboardStatsVO stats();



    DashboardOverviewVO overview();


}
