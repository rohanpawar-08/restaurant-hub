package com.restauranthub.auth;

import com.restauranthub.auth.dto.LoginRequest;
import com.restauranthub.auth.dto.RegisterRequest;
import com.restauranthub.auth.dto.UserResponse;
import com.restauranthub.auth.exception.DuplicateEmailException;
import com.restauranthub.common.exception.GlobalExceptionHandler;
import com.restauranthub.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 201 Created on valid input")
    void shouldRegisterSuccessfully() throws Exception {
        UserResponse response = new UserResponse(
                1L,
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                UserRole.CUSTOMER,
                LocalDateTime.now()
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        String json = """
                {
                    "fullName": "Rohan Pawar",
                    "email": "rohan@example.com",
                    "phone": "9876543210",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Rohan Pawar"))
                .andExpect(jsonPath("$.email").value("rohan@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 400 Bad Request on invalid email and short password")
    void shouldReturn400OnInvalidRegisterRequest() throws Exception {
        String json = """
                {
                    "fullName": "R",
                    "email": "invalid-email",
                    "phone": "12345",
                    "password": "short"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 409 Conflict on duplicate email")
    void shouldReturn409OnDuplicateEmail() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateEmailException("An account with this email address already exists."));

        String json = """
                {
                    "fullName": "Rohan Pawar",
                    "email": "existing@example.com",
                    "phone": "9876543210",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("An account with this email address already exists."));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 200 OK on valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        UserResponse response = new UserResponse(
                1L,
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                UserRole.CUSTOMER,
                LocalDateTime.now()
        );

        when(authService.login(any(), any(), any())).thenReturn(response);

        String json = """
                {
                    "email": "rohan@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("rohan@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 401 Unauthorized on bad credentials")
    void shouldReturn401OnBadCredentials() throws Exception {
        when(authService.login(any(), any(), any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        String json = """
                {
                    "email": "rohan@example.com",
                    "password": "WrongPassword"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password. Please try again."));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me should return 200 OK when authenticated")
    void shouldReturnUserForMeWhenAuthenticated() throws Exception {
        UserResponse response = new UserResponse(
                1L,
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                UserRole.CUSTOMER,
                LocalDateTime.now()
        );

        when(authService.getCurrentUser(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("rohan@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout should return 204 No Content")
    void shouldLogoutSuccessfully() throws Exception {
        doNothing().when(authService).logout(any(), any());

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }
}
