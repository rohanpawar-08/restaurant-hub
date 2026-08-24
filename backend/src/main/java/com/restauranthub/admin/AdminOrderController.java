package com.restauranthub.admin;

import com.restauranthub.order.OrderStatus;
import com.restauranthub.order.dto.OrderResponse;
import com.restauranthub.order.dto.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing administrative order management endpoints.
 *
 * Base Route: /api/v1/admin/orders
 * Protected by Spring Security with ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    /**
     * GET /api/v1/admin/orders - Retrieves all restaurant orders, optionally filtered by status.
     *
     * @param status optional OrderStatus filter
     * @return 200 OK with list of OrderResponse DTOs
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(required = false) OrderStatus status
    ) {
        List<OrderResponse> orders = adminOrderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/v1/admin/orders/{id} - Retrieves full details for a specific order.
     *
     * @param id order ID from URL path
     * @return 200 OK with OrderResponse DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse order = adminOrderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * PATCH /api/v1/admin/orders/{id}/status - Advances or cancels an order's lifecycle status.
     * Requires anti-CSRF token verification.
     *
     * @param id      order ID from URL path
     * @param request validated payload with target status
     * @return 200 OK with updated OrderResponse DTO
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderResponse updated = adminOrderService.updateOrderStatus(id, request.status());
        return ResponseEntity.ok(updated);
    }
}
