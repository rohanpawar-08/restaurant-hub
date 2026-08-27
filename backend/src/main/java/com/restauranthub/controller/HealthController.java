package com.restauranthub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HealthController provides a simple endpoint to check the running status
 * and availability of the RestaurantHub backend service.
 */
@Tag(name = "Health", description = "Service availability and health check")
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
    @Operation(summary = "Check backend service health", description = "Public health endpoint returning application availability status")
    @ApiResponse(responseCode = "200", description = "Service is operational")
    @GetMapping("/health")
    public HealthResponse getHealth() {
        return new HealthResponse("UP", "RestaurantHub API");
    }
}
