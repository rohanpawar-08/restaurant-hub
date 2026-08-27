package com.restauranthub.admin;

import com.restauranthub.common.exception.ErrorResponse;
import com.restauranthub.order.OrderStatus;
import com.restauranthub.order.dto.OrderResponse;
import com.restauranthub.order.dto.UpdateOrderStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin Orders", description = "Administrative order fulfillment and status workflow")
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
    @Operation(summary = "List all orders", description = "Retrieves all restaurant orders with optional status filter (Requires ROLE_ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @Parameter(description = "Filter by order status") @RequestParam(required = false) OrderStatus status
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
    @Operation(summary = "Get order details", description = "Retrieves complete order and line item details for administrative management (Requires ROLE_ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@Parameter(description = "Order ID") @PathVariable Long id) {
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
    @Operation(summary = "Update order status", description = "Advances order through lifecycle states (CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED) (Requires ROLE_ADMIN and CSRF)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition or request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN and CSRF)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderResponse updated = adminOrderService.updateOrderStatus(id, request.status());
        return ResponseEntity.ok(updated);
    }
}
