package com.tesim.dto.admin;

import lombok.Data;

import java.util.List;

@Data
public class IdListRequest {
    private List<Long> ids;
    private Integer status;
}
