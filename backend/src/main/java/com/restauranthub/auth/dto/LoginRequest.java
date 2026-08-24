package com.restauranthub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for customer login requests.
 */
public record LoginRequest(
        @NotBlank(message = "Email address is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}
