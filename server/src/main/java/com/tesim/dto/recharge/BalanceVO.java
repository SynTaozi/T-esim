package com.tesim.dto.recharge;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BalanceVO {
    private BigDecimal balance;
}
