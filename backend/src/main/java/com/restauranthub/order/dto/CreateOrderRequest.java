package com.restauranthub.order.dto;

import com.restauranthub.order.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request payload for creating a customer order during checkout.
 * Monetary totals, user IDs, and initial statuses are excluded to prevent client manipulation.
 */
public record CreateOrderRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 150, message = "Full name must be between 3 and 150 characters")
        String customerName,

        @NotBlank(message = "Email address is required")
        @Email(message = "Please provide a valid email address")
        String customerEmail,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Please enter a valid 10-digit mobile number")
        String customerPhone,

        @NotBlank(message = "Address line 1 is required")
        @Size(min = 5, max = 255, message = "Address line 1 must be between 5 and 255 characters")
        String addressLine1,

        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
        String city,

        @NotBlank(message = "State is required")
        @Size(min = 2, max = 100, message = "State must be between 2 and 100 characters")
        String state,

        @NotBlank(message = "Postal code is required")
        @Pattern(regexp = "^\\d{6}$", message = "Please enter a valid 6-digit postal code")
        String postalCode,

        String deliveryInstructions,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items
) {
}
