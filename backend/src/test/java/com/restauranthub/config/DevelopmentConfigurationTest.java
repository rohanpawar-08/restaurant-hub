package com.restauranthub.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
class DevelopmentConfigurationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private WebMvcConfig webMvcConfig;

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean sessionCookieSecure;

    @Value("${server.servlet.session.cookie.http-only:true}")
    private boolean sessionCookieHttpOnly;

    @Value("${server.servlet.session.cookie.same-site:lax}")
    private String sessionCookieSameSite;

    @Value("${spring.jpa.hibernate.ddl-auto:}")
    private String ddlAuto;

    @Value("${spring.flyway.enabled:false}")
    private boolean flywayEnabled;

    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    @Test
    @DisplayName("Development profile should NOT enforce Secure cookie over HTTP localhost")
    void shouldNotEnforceSecureCookieInDevelopment() {
        assertFalse(sessionCookieSecure, "Development session cookie must allow unencrypted HTTP for localhost");
    }

    @Test
    @DisplayName("Development profile should retain HttpOnly=true and SameSite=Lax session cookie")
    void shouldRetainHttpOnlyAndSameSiteInDevelopment() {
        assertTrue(sessionCookieHttpOnly, "Development session cookie must retain HttpOnly for XSS defense");
        assertEquals("lax", sessionCookieSameSite.toLowerCase(), "Development session cookie should have SameSite=Lax");
    }

    @Test
    @DisplayName("Development profile should use ddl-auto=validate and Flyway enabled")
    void shouldValidateSchemaAndEnableFlyway() {
        assertEquals("validate", ddlAuto, "JPA schema validation must be active in development");
        assertTrue(flywayEnabled, "Flyway migrations must be enabled in development");
    }

    @Test
    @DisplayName("Development profile should default CORS to localhost:4200")
    void shouldAllowLocalhostCorsInDevelopment() {
        assertTrue(webMvcConfig.getAllowedOrigins().contains("http://localhost:4200"),
                "Development profile should allow Angular dev origin http://localhost:4200");
    }

    @Test
    @DisplayName("Development profile should enable SQL logging for developer visibility")
    void shouldEnableShowSqlInDevelopment() {
        assertTrue(showSql, "Development profile should enable show-sql for debugging queries");
    }
}
