package com.restauranthub.admin;

import com.restauranthub.common.exception.ErrorResponse;
import com.restauranthub.settings.RestaurantSettingsService;
import com.restauranthub.settings.dto.RestaurantSettingsResponse;
import com.restauranthub.settings.dto.UpdateRestaurantSettingsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin Settings", description = "Administrative restaurant profile and branding updates")
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
    @Operation(summary = "Get admin restaurant settings", description = "Retrieves full configuration settings for administrative management (Requires ROLE_ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Settings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<RestaurantSettingsResponse> getAdminSettings() {
        return ResponseEntity.ok(settingsService.getSettingsResponse());
    }

    /**
     * Updates restaurant configuration, branding, delivery rules, and order acceptance status.
     */
    @Operation(summary = "Update restaurant settings", description = "Updates restaurant profile, colors, delivery thresholds, contact info, and operational hours (Requires ROLE_ADMIN and CSRF token)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Settings updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation errors on request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN and CSRF)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<RestaurantSettingsResponse> updateSettings(
            @Valid @RequestBody UpdateRestaurantSettingsRequest request
    ) {
        RestaurantSettingsResponse updated = settingsService.updateSettings(request);
        return ResponseEntity.ok(updated);
    }
}
