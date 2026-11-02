package com.tesim.dto.cms;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SiteDisplayVO {
    private String loginBannerUrl;
    private String registerBannerUrl;
    private String homeHeroImageUrl;
    private BigDecimal defaultShippingFee;
}
