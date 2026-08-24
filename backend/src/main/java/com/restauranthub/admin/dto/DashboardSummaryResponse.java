package com.restauranthub.admin.dto;

/**
 * Aggregated administrative metrics for the restaurant management dashboard.
 * Focuses on operational counts and inventory status without speculative revenue figures.
 */
public record DashboardSummaryResponse(
        long totalOrders,
        long confirmedOrders,
        long preparingOrders,
        long readyOrders,
        long outForDeliveryOrders,
        long totalCustomers,
        long totalFoods,
        long activeFoods
) {
}
