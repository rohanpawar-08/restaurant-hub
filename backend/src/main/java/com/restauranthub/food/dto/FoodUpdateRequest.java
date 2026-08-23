package com.restauranthub.food.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) for updating an existing Food item.
 * Supports full entity field updates via PUT.
 */
public record FoodUpdateRequest(

        @NotBlank(message = "Food name is required and cannot be blank")
        @Size(min = 2, max = 150, message = "Food name must be between 2 and 150 characters")
        String name,

        @NotBlank(message = "Food description is required and cannot be blank")
        @Size(min = 5, max = 1000, message = "Food description must be between 5 and 1000 characters")
        String description,

        @NotNull(message = "Price is required and cannot be null")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        @NotNull(message = "Rating is required and cannot be null")
        @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
        @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
        BigDecimal rating,

        String image,

        @NotNull(message = "Veg flag is required")
        Boolean veg,

        @NotNull(message = "Popular flag is required")
        Boolean popular,

        @NotNull(message = "Available flag is required")
        Boolean available,

        @NotNull(message = "Category ID is required")
        @Positive(message = "Category ID must be a positive number")
        Long categoryId
) {
}
