package com.tesim.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminAccountUpdateRequest {
    private String username;

    @NotBlank
    private String currentPassword;

    private String newPassword;
}
