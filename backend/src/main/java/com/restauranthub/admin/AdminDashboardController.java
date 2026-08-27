package com.restauranthub.admin;

import com.restauranthub.admin.dto.DashboardSummaryResponse;
import com.restauranthub.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing operational metrics for the administrative dashboard.
 *
 * Base Route: /api/v1/admin/dashboard
 * Protected by Spring Security with ROLE_ADMIN.
 */
@Tag(name = "Admin Dashboard", description = "Administrative metrics and operational summary")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    /**
     * GET /api/v1/admin/dashboard/summary - Retrieves aggregate metrics for dashboard summary cards.
     *
     * @return 200 OK with DashboardSummaryResponse
     */
    @Operation(summary = "Get dashboard metrics summary", description = "Retrieves live operational metrics including order counts, revenue, and active menu items (Requires ROLE_ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse summary = adminDashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
}
