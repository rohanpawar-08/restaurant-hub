package com.restauranthub.settings;

import com.restauranthub.settings.dto.RestaurantSettingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST Controller exposing customer-safe restaurant settings and branding.
 */
@Tag(name = "Restaurant Settings", description = "Public restaurant branding, contact details, operating hours, and policies")
@RestController
@RequestMapping("/api/v1/settings")
public class RestaurantSettingsController {

    private final RestaurantSettingsService settingsService;

    public RestaurantSettingsController(RestaurantSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * Retrieves the current active restaurant configuration.
     * Publicly accessible without authentication.
     */
    @Operation(summary = "Get public restaurant settings", description = "Retrieves public restaurant profile, branding, operational hours, delivery rules, and order acceptance status")
    @ApiResponse(responseCode = "200", description = "Settings retrieved successfully")
    @GetMapping
    public ResponseEntity<RestaurantSettingsResponse> getSettings() {
        return ResponseEntity.ok(settingsService.getSettingsResponse());
    }
}
