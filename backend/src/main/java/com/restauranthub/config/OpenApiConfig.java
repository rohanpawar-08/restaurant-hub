package com.restauranthub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Centralized OpenAPI 3.0 / Swagger Documentation Configuration for RestaurantHub API.
 *
 * Documents the complete REST API surface, session-based cookie authentication architecture (JSESSIONID),
 * and Spring Security Single Page Application (SPA) Double Submit Cookie anti-CSRF workflow.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI restaurantHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RestaurantHub API")
                        .description("""
                                # RestaurantHub REST API Documentation
                                
                                RestaurantHub is a production-grade restaurant ordering and administration platform.
                                
                                ## Authentication & Security Architecture
                                - **Session Management**: Cookie-based `HttpSession` (`JSESSIONID`). Authenticate via `POST /api/v1/auth/login` to establish a session.
                                - **CSRF Protection**: Spring Security SPA Double Submit Cookie architecture. Clients retrieve an anti-CSRF token from `GET /api/v1/auth/csrf` (which issues the `XSRF-TOKEN` cookie) and must provide the token in the `X-XSRF-TOKEN` HTTP header on all state-changing mutations (`POST`, `PUT`, `PATCH`, `DELETE`).
                                - **Role-Based Access Control**:
                                  - **Public**: Health check, menu categories, dishes, restaurant settings, registration, login, and CSRF token endpoints.
                                  - **Customer**: Order placement, authenticated profile (`/api/v1/auth/me`), and personal order history (`/api/v1/orders`).
                                  - **Administrator**: Metric summaries (`/api/v1/admin/dashboard`), order status transitions (`/api/v1/admin/orders`), category/food management, branding updates, and media uploads.
                                
                                > **Note on Swagger UI Interactivity**: Swagger UI transmits session cookies automatically on the same origin, but does not natively inject custom `X-XSRF-TOKEN` headers for mutating requests without manual header entry.
                                """)
                        .version("v1")
                        .contact(new Contact()
                                .name("RestaurantHub Engineering")
                                .url("https://github.com/rohanpawar-08/restaurant-hub"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .tags(List.of(
                        new Tag().name("Health").description("Service availability and health check"),
                        new Tag().name("Authentication").description("Customer registration, login, profile inspection, and CSRF token endpoints"),
                        new Tag().name("Restaurant Settings").description("Public restaurant branding, contact details, operating hours, and policies"),
                        new Tag().name("Categories").description("Menu categories (public read, admin write)"),
                        new Tag().name("Foods").description("Menu dishes and dietary information (public read, admin write)"),
                        new Tag().name("Customer Orders").description("Customer order placement and order history"),
                        new Tag().name("Admin Dashboard").description("Administrative metrics and operational summary"),
                        new Tag().name("Admin Orders").description("Administrative order fulfillment and status workflow"),
                        new Tag().name("Admin Settings").description("Administrative restaurant profile and branding updates"),
                        new Tag().name("Admin Media").description("Administrative image uploads and storage provider status")
                ));
    }
}
