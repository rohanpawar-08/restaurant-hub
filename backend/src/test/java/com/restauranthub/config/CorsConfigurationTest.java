package com.restauranthub.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying the central CORS configuration across API endpoints.
 */
@SpringBootTest
class CorsConfigurationTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    @DisplayName("Should allow CORS preflight requests from allowed origin (http://localhost:4200)")
    void shouldAllowCorsPreflightFromAllowedOrigin() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(options("/api/v1/categories")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    @Test
    @DisplayName("Should include CORS allow origin header on GET requests from allowed origin")
    void shouldIncludeCorsHeadersOnGetRequest() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(get("/api/v1/health")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    }

    @Test
    @DisplayName("Should reject CORS preflight requests from unauthorized origin")
    void shouldRejectCorsHeadersFromUnauthorizedOrigin() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(options("/api/v1/health")
                        .header(HttpHeaders.ORIGIN, "http://unauthorized-domain.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}


