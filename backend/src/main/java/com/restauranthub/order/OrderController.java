package com.restauranthub.order;

import com.restauranthub.order.dto.CreateOrderRequest;
import com.restauranthub.order.dto.OrderResponse;
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
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            throw new BadCredentialsException("User is not authenticated.");
        }
        OrderResponse order = orderService.getOrderById(id, principal.getName());
        return ResponseEntity.ok(order);
    }
}
