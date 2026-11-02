package com.tesim.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String role;
    private String status;
}
