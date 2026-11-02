package com.tesim.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;
    @NotBlank
    @Size(min = 6, max = 64)
    private String password;
    @NotBlank
    @Email
    private String email;
    private String phone;
    private String referralCode;
    @NotBlank
    private String verificationCode;
}
