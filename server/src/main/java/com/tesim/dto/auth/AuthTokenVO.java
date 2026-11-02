package com.tesim.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthTokenVO {
    private String token;
    private MemberVO user;
}
