package com.tesim.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMemberVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String role;
    private String status;
    private String createdAt;
}
