package com.restauranthub.order.dto;

import com.restauranthub.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for Admin order status updates.
 * Accepts only status transition commands; rejects client monetary modifications or user manipulation.
 */
public record UpdateOrderStatusRequest(
        @NotNull(message = "Order status is required.")
        OrderStatus status
) {
}
