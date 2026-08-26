package com.restauranthub.settings.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Payload submitted by administrators to update restaurant configuration.
 * Enforces rigorous validations including Indian mobile format, 6-digit PIN,
 * standard GSTIN/FSSAI patterns, monetary bounds, and hex color codes.
 */
public record UpdateRestaurantSettingsRequest(
        @NotBlank(message = "Restaurant name is required")
        @Size(min = 2, max = 150, message = "Restaurant name must be between 2 and 150 characters")
        String restaurantName,

        @Size(max = 255, message = "Tagline cannot exceed 255 characters")
        String tagline,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^(?:\\+91)?[6-9]\\d{9}$",
                message = "Phone must be a valid 10-digit Indian mobile number"
        )
        String phone,

        @NotBlank(message = "Email address is required")
        @Email(message = "Email must be a valid format")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        String email,

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 255, message = "Address line 1 cannot exceed 255 characters")
        String addressLine1,

        @Size(max = 255, message = "Address line 2 cannot exceed 255 characters")
        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 100, message = "State cannot exceed 100 characters")
        String state,

        @NotBlank(message = "PIN code is required")
        @Pattern(
                regexp = "^[1-9][0-9]{5}$",
                message = "PIN code must be a valid 6-digit Indian postal code"
        )
        String pinCode,

        @NotBlank(message = "Currency code is required")
        @Size(max = 10, message = "Currency code cannot exceed 10 characters")
        String currencyCode,

        @NotBlank(message = "Currency symbol is required")
        @Size(max = 10, message = "Currency symbol cannot exceed 10 characters")
        String currencySymbol,

        @NotNull(message = "Delivery fee is required")
        @DecimalMin(value = "0.00", message = "Delivery fee cannot be negative")
        BigDecimal deliveryFee,

        @NotNull(message = "Free delivery threshold is required")
        @DecimalMin(value = "0.00", message = "Free delivery threshold cannot be negative")
        BigDecimal freeDeliveryThreshold,

        @NotNull(message = "Estimated delivery minutes is required")
        @Min(value = 5, message = "Estimated delivery minutes must be at least 5")
        @Max(value = 240, message = "Estimated delivery minutes cannot exceed 240")
        Integer estimatedDeliveryMinutes,

        @Pattern(
                regexp = "^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
                message = "GSTIN must follow standard 15-character Indian format"
        )
        String gstin,

        @Pattern(
                regexp = "^$|^[0-9]{14}$",
                message = "FSSAI number must be a 14-digit numeric code"
        )
        String fssaiNumber,

        LocalTime openingTime,

        LocalTime closingTime,

        @NotNull(message = "Accepting orders flag is required")
        Boolean acceptingOrders,

        @Size(max = 500, message = "Logo URL cannot exceed 500 characters")
        String logoUrl,

        @Size(max = 500, message = "Hero image URL cannot exceed 500 characters")
        String heroImageUrl,

        @Pattern(
                regexp = "^$|^#(?:[0-9a-fA-F]{3}){1,2}$",
                message = "Primary color must be a valid Hex color code (e.g. #FF6B00)"
        )
        String primaryColor,

        @Pattern(
                regexp = "^$|^#(?:[0-9a-fA-F]{3}){1,2}$",
                message = "Secondary color must be a valid Hex color code (e.g. #1E293B)"
        )
        String secondaryColor
) {
}
