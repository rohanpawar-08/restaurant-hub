package com.restauranthub.settings;

import com.restauranthub.settings.dto.RestaurantSettingsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST Controller exposing customer-safe restaurant settings and branding.
 */
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
    @GetMapping
    public ResponseEntity<RestaurantSettingsResponse> getSettings() {
        return ResponseEntity.ok(settingsService.getSettingsResponse());
    }
}
