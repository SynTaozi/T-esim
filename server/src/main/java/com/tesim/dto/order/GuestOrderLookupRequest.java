package com.tesim.dto.order;

import lombok.Data;

@Data
public class GuestOrderLookupRequest {
    private String email;
    /** 1-based page index; default 1 */
    private Integer page;
    /** page size; default 5, max 20 */
    private Integer pageSize;
}
