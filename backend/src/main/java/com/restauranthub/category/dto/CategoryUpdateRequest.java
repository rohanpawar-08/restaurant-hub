package com.restauranthub.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) for updating an existing Category.
 * Encapsulates the fields that the client is permitted to update.
 */
public record CategoryUpdateRequest(

        @NotBlank(message = "Category name is required and cannot be blank")
        @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Category slug is required and cannot be blank")
        @Size(min = 2, max = 100, message = "Category slug must be between 2 and 100 characters")
        @Pattern(
                regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                message = "Category slug must contain only lowercase letters, numbers, and hyphens (e.g. 'beverages', 'main-course')"
        )
        String slug,

        @NotNull(message = "Active status is required and cannot be null")
        Boolean active
) {
}
