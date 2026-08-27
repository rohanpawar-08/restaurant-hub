package com.restauranthub.auth;

import com.restauranthub.auth.dto.CsrfResponse;
import com.restauranthub.auth.dto.LoginRequest;
import com.restauranthub.auth.dto.RegisterRequest;
import com.restauranthub.auth.dto.UserResponse;
import com.restauranthub.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Customer registration, login, profile inspection, and CSRF token endpoints")
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
    @Operation(summary = "Register new customer account", description = "Creates a new customer account with validated credentials and contact details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or validation errors",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email or phone already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(summary = "Login to customer account", description = "Authenticates user credentials and establishes an HTTP session (JSESSIONID cookie)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "400", description = "Validation errors on request fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(summary = "Get current authenticated user profile", description = "Returns profile details of the customer associated with the active session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(summary = "Fetch anti-CSRF token", description = "Generates/retrieves CSRF token, sets XSRF-TOKEN cookie, and returns header name and token value")
    @ApiResponse(responseCode = "200", description = "CSRF token initialized")
    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> getCsrfToken(
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

        return ResponseEntity.ok(new CsrfResponse(
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
    @Operation(summary = "Logout from session", description = "Invalidates the server-side HTTP session and clears authentication cookies")
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.logout(httpRequest, httpResponse);
        return ResponseEntity.noContent().build();
    }
}
