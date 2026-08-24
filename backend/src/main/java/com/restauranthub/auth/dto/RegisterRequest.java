package com.restauranthub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for customer registration requests.
 * Explicitly does not accept role to prevent unauthorized privilege escalation.
 */
public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 150, message = "Full name must be between 3 and 150 characters")
        String fullName,

        @NotBlank(message = "Email address is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Please enter a valid 10-digit mobile number")
        String phone,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
        String password
) {
}
