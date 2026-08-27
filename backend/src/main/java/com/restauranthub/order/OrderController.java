package com.restauranthub.order;

import com.restauranthub.common.exception.ErrorResponse;
import com.restauranthub.order.dto.CreateOrderRequest;
import com.restauranthub.order.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing Customer Order management endpoints under `/api/v1/orders`.
 */
@Tag(name = "Customer Orders", description = "Customer order placement and order history")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates a new customer order for the authenticated user.
     *
     * @param request   validated order creation payload
     * @param principal active security principal
     * @return HTTP 201 Created with persisted OrderResponse
     */
    @Operation(summary = "Place order", description = "Creates a new food order for the authenticated customer (Requires Authentication and CSRF token)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid items, store closed, or delivery rule violation",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Missing or invalid CSRF token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Principal principal
    ) {
        if (principal == null) {
            throw new BadCredentialsException("User is not authenticated.");
        }
        OrderResponse response = orderService.createOrder(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the order history for the authenticated customer.
     *
     * @param principal active security principal
     * @return HTTP 200 OK with list of owned orders
     */
    @Operation(summary = "Get my orders", description = "Retrieves chronological order history for the currently logged-in customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(Principal principal) {
        if (principal == null) {
            throw new BadCredentialsException("User is not authenticated.");
        }
        List<OrderResponse> orders = orderService.getCustomerOrders(principal.getName());
        return ResponseEntity.ok(orders);
    }

    /**
     * Retrieves a single owned order by ID.
     *
     * @param id        order primary key
     * @param principal active security principal
     * @return HTTP 200 OK with order receipt details
     */
    @Operation(summary = "Get order by ID", description = "Retrieves details for a specific order owned by the authenticated customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order details found"),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            throw new BadCredentialsException("User is not authenticated.");
        }
        OrderResponse order = orderService.getOrderById(id, principal.getName());
        return ResponseEntity.ok(order);
    }
}
