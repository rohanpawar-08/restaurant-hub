package com.restauranthub.config;

import com.restauranthub.auth.AuthService;
import com.restauranthub.auth.dto.RegisterRequest;
import com.restauranthub.auth.dto.UserResponse;
import com.restauranthub.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityConfigTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/auth/csrf should return 200 OK and populate XSRF-TOKEN cookie")
    void shouldReturnCsrfTokenAndSetCookie() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register WITHOUT CSRF should be rejected with 403 Forbidden")
    void shouldRejectRegisterWithoutCsrf() throws Exception {
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
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register WITH valid CSRF should succeed with 201 Created")
    void shouldAcceptRegisterWithCsrf() throws Exception {
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("rohan@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login WITHOUT CSRF should be rejected with 403 Forbidden")
    void shouldRejectLoginWithoutCsrf() throws Exception {
        String json = """
                {
                    "email": "rohan@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login WITH valid CSRF should succeed with 200 OK")
    void shouldAcceptLoginWithCsrf() throws Exception {
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "rohan@example.com", roles = {"CUSTOMER"})
    @DisplayName("POST /api/v1/auth/logout WITHOUT CSRF should be rejected with 403 Forbidden")
    void shouldRejectLogoutWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "rohan@example.com", roles = {"CUSTOMER"})
    @DisplayName("POST /api/v1/auth/logout WITH valid CSRF should succeed with 204 No Content")
    void shouldAcceptLogoutWithCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Public GET /api/v1/health should succeed without CSRF")
    void shouldAllowPublicHealthWithoutCsrf() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public GET /api/v1/categories should succeed without CSRF")
    void shouldAllowPublicCategoriesWithoutCsrf() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public GET /api/v1/foods should succeed without CSRF")
    void shouldAllowPublicFoodsWithoutCsrf() throws Exception {
        mockMvc.perform(get("/api/v1/foods"))
                .andExpect(status().isOk());
    }
}
