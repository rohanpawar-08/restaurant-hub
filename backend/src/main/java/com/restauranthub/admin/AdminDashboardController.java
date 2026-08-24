package com.restauranthub.admin;

import com.restauranthub.admin.dto.DashboardSummaryResponse;
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
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse summary = adminDashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }
}
