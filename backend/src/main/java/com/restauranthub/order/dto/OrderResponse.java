package com.restauranthub.order.dto;

import com.restauranthub.order.Order;
import com.restauranthub.order.OrderStatus;
import com.restauranthub.order.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload representing a complete customer order receipt.
 */
public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal total,
        String customerName,
        String customerEmail,
        String customerPhone,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String deliveryInstructions,
        Integer estimatedDeliveryMinutes,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse fromEntity(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems() != null
                ? order.getItems().stream().map(OrderItemResponse::fromEntity).toList()
                : List.of();

        return new OrderResponse(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getStatus(),
                order.getPaymentMethod(),
                order.getSubtotal(),
                order.getDeliveryFee(),
                order.getTotal(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                order.getAddressLine1(),
                order.getAddressLine2(),
                order.getCity(),
                order.getState(),
                order.getPostalCode(),
                order.getDeliveryInstructions(),
                order.getEstimatedDeliveryMinutes(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}
