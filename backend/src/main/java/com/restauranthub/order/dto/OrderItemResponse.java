package com.restauranthub.order.dto;

import com.restauranthub.order.OrderItem;
import java.math.BigDecimal;

/**
 * Response payload representing an itemized line item in an order receipt.
 * Reflects historical snapshots rather than volatile live menu state.
 */
public record OrderItemResponse(
        Long id,
        Long foodId,
        String foodName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
    public static OrderItemResponse fromEntity(OrderItem entity) {
        return new OrderItemResponse(
                entity.getId(),
                entity.getFood() != null ? entity.getFood().getId() : null,
                entity.getFoodName(),
                entity.getUnitPrice(),
                entity.getQuantity(),
                entity.getLineTotal()
        );
    }
}
