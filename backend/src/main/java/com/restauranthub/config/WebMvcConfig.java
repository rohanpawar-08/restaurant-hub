package com.restauranthub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Centralized Spring MVC Configuration for Cross-Origin Resource Sharing (CORS).
 *
 * Why centralized CORS configuration is preferred over `@CrossOrigin` on individual controllers:
 * 1. Single Source of Truth: Security headers, allowed methods, and origin whitelists are maintained in one place.
 * 2. Environment-Driven: Origin URLs can be configured dynamically via properties/environment variables
 *    (e.g., local dev at http://localhost:4200 vs production domain) without touching Java code.
 * 3. Consistent Policy Enforcement: Guarantees uniform headers and options handling across all /api/** endpoints.
 * 4. Safer Credential Handling: Explicit origin whitelists avoid the security risk of wildcard origins (*)
 *    when sending authentication headers, cookies, or credentials.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebMvcConfig(@Value("${app.cors.allowed-origins:http://localhost:4200}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
