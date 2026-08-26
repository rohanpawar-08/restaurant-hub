package com.restauranthub.admin;

import com.restauranthub.settings.RestaurantSettingsService;
import com.restauranthub.settings.dto.RestaurantSettingsResponse;
import com.restauranthub.settings.dto.UpdateRestaurantSettingsRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative REST Controller for viewing and updating restaurant settings.
 * Requires ROLE_ADMIN and valid CSRF token.
 */
@RestController
@RequestMapping("/api/v1/admin/settings")
public class AdminRestaurantSettingsController {

    private final RestaurantSettingsService settingsService;

    public AdminRestaurantSettingsController(RestaurantSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * Retrieves current settings for admin form prefilling.
     */
    @GetMapping
    public ResponseEntity<RestaurantSettingsResponse> getAdminSettings() {
        return ResponseEntity.ok(settingsService.getSettingsResponse());
    }

    /**
     * Updates restaurant configuration, branding, delivery rules, and order acceptance status.
     */
    @PutMapping
    public ResponseEntity<RestaurantSettingsResponse> updateSettings(
            @Valid @RequestBody UpdateRestaurantSettingsRequest request
    ) {
        RestaurantSettingsResponse updated = settingsService.updateSettings(request);
        return ResponseEntity.ok(updated);
    }
}
