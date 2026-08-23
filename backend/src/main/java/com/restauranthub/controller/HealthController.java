package com.restauranthub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HealthController provides a simple endpoint to check the running status
 * and availability of the RestaurantHub backend service.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * Strongly typed response record representing the health check response payload.
     * In Java 21, records automatically provide constructor, getters, equals, hashCode, and toString.
     */
    public record HealthResponse(String status, String application) {}

    /**
     * Handles HTTP GET requests sent to /api/v1/health.
     *
     * @return HealthResponse containing the current status ("UP") and application name.
     */
    @GetMapping("/health")
    public HealthResponse getHealth() {
        return new HealthResponse("UP", "RestaurantHub API");
    }
}
