package com.restauranthub.auth;

import com.restauranthub.auth.dto.LoginRequest;
import com.restauranthub.auth.dto.RegisterRequest;
import com.restauranthub.auth.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing Customer Authentication endpoints under `/api/v1/auth`.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new customer account.
     *
     * @param request validated customer registration payload
     * @return HTTP 201 Created with safe UserResponse details
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates customer credentials and initiates an HttpOnly session.
     *
     * @param request     login credentials (email + password)
     * @param httpRequest servlet request
     * @param httpResponse servlet response
     * @return HTTP 200 OK with authenticated user profile
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        UserResponse response = authService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the profile of the currently authenticated customer.
     *
     * @param authentication active security principal
     * @return HTTP 200 OK with authenticated customer profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UserResponse response = authService.getCurrentUser(authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * Exposes anti-CSRF token parameters for SPA initialization and sets the XSRF-TOKEN cookie.
     *
     * @param httpRequest  servlet request containing CsrfToken attribute
     * @param httpResponse servlet response for cookie persistence
     * @return HTTP 200 OK with CSRF header and token details
     */
    @GetMapping("/csrf")
    public ResponseEntity<com.restauranthub.auth.dto.CsrfResponse> getCsrfToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        org.springframework.security.web.csrf.CsrfToken csrfToken =
                (org.springframework.security.web.csrf.CsrfToken) httpRequest.getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
        if (csrfToken == null) {
            return ResponseEntity.noContent().build();
        }

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("XSRF-TOKEN", csrfToken.getToken());
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        httpResponse.addCookie(cookie);

        return ResponseEntity.ok(new com.restauranthub.auth.dto.CsrfResponse(
                "X-XSRF-TOKEN",
                csrfToken.getParameterName(),
                csrfToken.getToken()
        ));
    }

    /**
     * Logs out the user by terminating the server session.
     *
     * @param httpRequest servlet request
     * @param httpResponse servlet response
     * @return HTTP 204 No Content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.logout(httpRequest, httpResponse);
        return ResponseEntity.noContent().build();
    }
}
