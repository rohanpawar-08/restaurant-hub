package com.restauranthub.auth.dto;

/**
 * Safe public representation of anti-CSRF token parameters for SPA initialization.
 */
public record CsrfResponse(
        String headerName,
        String parameterName,
        String token
) {
}
