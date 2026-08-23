package com.restauranthub.food.dto;

import com.restauranthub.category.Category;
import com.restauranthub.food.Food;
import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) representing a Food item in API responses.
 *
 * Why we separate Food Entity from FoodResponse:
 * 1. Hides JPA entity internals (proxies, persistence context state).
 * 2. Flattens Category data (categoryId, categoryName, categorySlug) to avoid circular references.
 * 3. Provides an immutable, versionable public API contract for client applications like Angular.
 */
public record FoodResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal rating,
        String image,
        Boolean veg,
        Boolean popular,
        Boolean available,
        Long categoryId,
        String categoryName,
        String categorySlug
) {

    /**
     * Explicit mapping helper to convert a Food JPA Entity into a FoodResponse DTO.
     *
     * @param food persisted Food entity
     * @return clean, immutable FoodResponse
     */
    public static FoodResponse fromEntity(Food food) {
        if (food == null) {
            return null;
        }

        Category category = food.getCategory();
        Long categoryId = (category != null) ? category.getId() : null;
        String categoryName = (category != null) ? category.getName() : null;
        String categorySlug = (category != null) ? category.getSlug() : null;

        return new FoodResponse(
                food.getId(),
                food.getName(),
                food.getDescription(),
                food.getPrice(),
                food.getRating(),
                food.getImage(),
                food.getVeg(),
                food.getPopular(),
                food.getAvailable(),
                categoryId,
                categoryName,
                categorySlug
        );
    }
}
