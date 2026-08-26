package com.restauranthub.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=prod");


    @Test
    @DisplayName("Production profile MUST configure Secure=true for HTTPS session cookies")
    void shouldConfigureSecureCookieInProduction() {
        contextRunner.run(context -> {
            Environment env = context.getEnvironment();
            assertThat(env.getProperty("server.servlet.session.cookie.secure")).isEqualTo("true");
        });
    }

    @Test
    @DisplayName("Production profile MUST configure HttpOnly=true and SameSite=Lax")
    void shouldConfigureHttpOnlyAndSameSiteInProduction() {
        contextRunner.run(context -> {
            Environment env = context.getEnvironment();
            assertThat(env.getProperty("server.servlet.session.cookie.http-only")).isEqualTo("true");
            assertThat(env.getProperty("server.servlet.session.cookie.same-site")).isEqualTo("lax");
        });
    }

    @Test
    @DisplayName("Production profile should configure session timeout to 30 minutes")
    void shouldConfigureSessionTimeoutInProduction() {
        contextRunner.run(context -> {
            Environment env = context.getEnvironment();
            assertThat(env.getProperty("server.servlet.session.timeout")).isEqualTo("30m");
        });
    }

    @Test
    @DisplayName("Production profile should configure forwarded-headers strategy to framework")
    void shouldConfigureForwardHeadersStrategyInProduction() {
        contextRunner.run(context -> {
            Environment env = context.getEnvironment();
            assertThat(env.getProperty("server.forward-headers-strategy")).isEqualTo("framework");
        });
    }

    @Test
    @DisplayName("Production profile MUST retain ddl-auto=validate and Flyway enabled")
    void shouldValidateSchemaAndEnableFlywayInProduction() {
        contextRunner.run(context -> {
            Environment env = context.getEnvironment();
            assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
            assertThat(env.getProperty("spring.flyway.enabled")).isEqualTo("true");
        });
    }

    @Test
    @DisplayName("Production profile should disable SQL verbosity")
    void shouldDisableShowSqlInProduction() {
        contextRunner.run(context -> {
            Environment env = context.getEnvironment();
            assertThat(env.getProperty("spring.jpa.show-sql")).isEqualTo("false");
        });
    }

    @Test
    @DisplayName("Production profile should default CORS to empty (fail-closed)")
    void shouldDefaultCorsToEmptyInProduction() {
        contextRunner.run(context -> {
            Environment env = context.getEnvironment();
            assertThat(env.getProperty("app.cors.allowed-origins")).isNullOrEmpty();
        });
    }

    @Test
    @DisplayName("WebMvcConfig should instantiate with fail-closed empty CORS list when origins property is empty")
    void shouldCreateWebMvcConfigWithEmptyCorsWhenOriginsEmpty() {
        WebMvcConfig config = new WebMvcConfig(new String[]{""}, "uploads");
        assertThat(config.getAllowedOrigins()).isEmpty();
    }

    @Test
    @DisplayName("WebMvcConfig should properly parse and trim valid origins when provided in production")
    void shouldCreateWebMvcConfigWithConfiguredOrigins() {
        WebMvcConfig config = new WebMvcConfig(
                new String[]{" https://restaurant.example.com ", " https://admin.example.com "},
                "/var/data/uploads"
        );
        assertThat(config.getAllowedOrigins()).containsExactly(
                "https://restaurant.example.com",
                "https://admin.example.com"
        );
    }
}
