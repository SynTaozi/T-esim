package com.tesim.dto.admin;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminSiteDisplayRequest {
    private String loginBannerUrl;
    private String registerBannerUrl;
    private String homeHeroImageUrl;
    private BigDecimal defaultShippingFee;
}
