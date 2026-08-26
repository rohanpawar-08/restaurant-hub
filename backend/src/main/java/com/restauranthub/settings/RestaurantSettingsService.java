package com.restauranthub.settings;

import com.restauranthub.settings.dto.RestaurantSettingsResponse;
import com.restauranthub.settings.dto.UpdateRestaurantSettingsRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service governing retrieval and administrative modification of restaurant settings.
 */
@Service
public class RestaurantSettingsService {

    private final RestaurantSettingsRepository settingsRepository;

    public RestaurantSettingsService(RestaurantSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    /**
     * Retrieves the authoritative RestaurantSettings entity.
     * Guarantees a valid entity is returned (creates safe default if none exists).
     */
    @Transactional(readOnly = true)
    public RestaurantSettings getActiveSettings() {
        return settingsRepository.findActiveSettings().orElseGet(this::createDefaultSettings);
    }

    /**
     * Retrieves public customer-safe response DTO.
     */
    @Transactional(readOnly = true)
    public RestaurantSettingsResponse getSettingsResponse() {
        return RestaurantSettingsResponse.fromEntity(getActiveSettings());
    }

    /**
     * Updates restaurant settings based on validated administrator payload.
     */
    @Transactional
    public RestaurantSettingsResponse updateSettings(UpdateRestaurantSettingsRequest request) {
        RestaurantSettings settings = settingsRepository.findActiveSettings().orElseGet(RestaurantSettings::new);

        settings.setRestaurantName(request.restaurantName().trim());
        settings.setTagline(request.tagline() != null ? request.tagline().trim() : null);
        settings.setPhone(request.phone().trim());
        settings.setEmail(request.email().trim().toLowerCase());
        settings.setAddressLine1(request.addressLine1().trim());
        settings.setAddressLine2(request.addressLine2() != null ? request.addressLine2().trim() : null);
        settings.setCity(request.city().trim());
        settings.setState(request.state().trim());
        settings.setPinCode(request.pinCode().trim());
        settings.setCurrencyCode(request.currencyCode().trim().toUpperCase());
        settings.setCurrencySymbol(request.currencySymbol().trim());
        settings.setDeliveryFee(request.deliveryFee());
        settings.setFreeDeliveryThreshold(request.freeDeliveryThreshold());
        settings.setEstimatedDeliveryMinutes(request.estimatedDeliveryMinutes());
        settings.setGstin(request.gstin() != null && !request.gstin().isBlank() ? request.gstin().trim().toUpperCase() : null);
        settings.setFssaiNumber(request.fssaiNumber() != null && !request.fssaiNumber().isBlank() ? request.fssaiNumber().trim() : null);
        settings.setOpeningTime(request.openingTime());
        settings.setClosingTime(request.closingTime());
        settings.setAcceptingOrders(request.acceptingOrders());
        settings.setLogoUrl(request.logoUrl() != null && !request.logoUrl().isBlank() ? request.logoUrl().trim() : null);
        settings.setHeroImageUrl(request.heroImageUrl() != null && !request.heroImageUrl().isBlank() ? request.heroImageUrl().trim() : null);
        settings.setPrimaryColor(request.primaryColor() != null && !request.primaryColor().isBlank() ? request.primaryColor().trim() : "#FF6B00");
        settings.setSecondaryColor(request.secondaryColor() != null && !request.secondaryColor().isBlank() ? request.secondaryColor().trim() : "#1E293B");
        settings.setUpdatedAt(LocalDateTime.now());

        RestaurantSettings saved = settingsRepository.save(settings);
        return RestaurantSettingsResponse.fromEntity(saved);
    }

    private RestaurantSettings createDefaultSettings() {
        RestaurantSettings defaultSettings = new RestaurantSettings();
        defaultSettings.setRestaurantName("RestaurantHub");
        defaultSettings.setTagline("Fresh food, delivered with care");
        defaultSettings.setPhone("9876543210");
        defaultSettings.setEmail("contact@restauranthub.com");
        defaultSettings.setAddressLine1("123 Gourmet Boulevard");
        defaultSettings.setAddressLine2("Near City Center");
        defaultSettings.setCity("Mumbai");
        defaultSettings.setState("Maharashtra");
        defaultSettings.setPinCode("400001");
        defaultSettings.setCurrencyCode("INR");
        defaultSettings.setCurrencySymbol("₹");
        defaultSettings.setDeliveryFee(new BigDecimal("40.00"));
        defaultSettings.setFreeDeliveryThreshold(new BigDecimal("500.00"));
        defaultSettings.setEstimatedDeliveryMinutes(35);
        defaultSettings.setAcceptingOrders(true);
        defaultSettings.setPrimaryColor("#FF6B00");
        defaultSettings.setSecondaryColor("#1E293B");
        return settingsRepository.save(defaultSettings);
    }
}
