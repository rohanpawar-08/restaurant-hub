package com.restauranthub.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload representing a single food item selection during checkout.
 * Intentionally contains only foodId and quantity; monetary totals are calculated server-side.
 */
public record OrderItemRequest(
        @NotNull(message = "Food ID is required")
        Long foodId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be at least 1")
        Integer quantity
) {
}
