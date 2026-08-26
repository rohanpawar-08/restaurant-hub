package com.restauranthub.settings.dto;

import com.restauranthub.settings.RestaurantSettings;
import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Public, customer-safe view of restaurant configuration and branding.
 * Contains no administrative or internal secrets.
 */
public record RestaurantSettingsResponse(
        Long id,
        String restaurantName,
        String tagline,
        String phone,
        String email,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String pinCode,
        String currencyCode,
        String currencySymbol,
        BigDecimal deliveryFee,
        BigDecimal freeDeliveryThreshold,
        Integer estimatedDeliveryMinutes,
        String gstin,
        String fssaiNumber,
        LocalTime openingTime,
        LocalTime closingTime,
        Boolean acceptingOrders,
        String logoUrl,
        String heroImageUrl,
        String primaryColor,
        String secondaryColor
) {
    public static RestaurantSettingsResponse fromEntity(RestaurantSettings entity) {
        if (entity == null) {
            return null;
        }
        return new RestaurantSettingsResponse(
                entity.getId(),
                entity.getRestaurantName(),
                entity.getTagline(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddressLine1(),
                entity.getAddressLine2(),
                entity.getCity(),
                entity.getState(),
                entity.getPinCode(),
                entity.getCurrencyCode(),
                entity.getCurrencySymbol(),
                entity.getDeliveryFee(),
                entity.getFreeDeliveryThreshold(),
                entity.getEstimatedDeliveryMinutes(),
                entity.getGstin(),
                entity.getFssaiNumber(),
                entity.getOpeningTime(),
                entity.getClosingTime(),
                entity.getAcceptingOrders(),
                entity.getLogoUrl(),
                entity.getHeroImageUrl(),
                entity.getPrimaryColor(),
                entity.getSecondaryColor()
        );
    }
}
