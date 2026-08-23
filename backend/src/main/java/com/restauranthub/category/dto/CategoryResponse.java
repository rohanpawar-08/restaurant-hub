package com.restauranthub.category.dto;

import com.restauranthub.category.Category;

/**
 * Data Transfer Object (DTO) for returning Category data in API responses.
 *
 * Exposing a dedicated response DTO instead of the raw JPA Entity:
 * 1. Hides internal entity/database concerns (e.g. lazy-loading proxies, persistence context state).
 * 2. Establishes an immutable, versionable public API contract for client apps like Angular.
 * 3. Prevents accidental leakage of internal fields or circular relationship references.
 */
public record CategoryResponse(
        Long id,
        String name,
        String slug,
        Boolean active
) {

    /**
     * Explicit mapping helper to construct a CategoryResponse from a Category JPA entity.
     *
     * @param category the persisted Category entity
     * @return a clean, immutable CategoryResponse DTO
     */
    public static CategoryResponse fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getActive()
        );
    }
}
