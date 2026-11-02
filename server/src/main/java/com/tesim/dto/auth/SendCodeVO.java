package com.tesim.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendCodeVO {
    private boolean sent;
    /** Only present in non-prod for local debugging. */
    private String devCode;
}
