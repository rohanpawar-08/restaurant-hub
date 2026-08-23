package com.restauranthub.category;

import com.restauranthub.category.dto.CategoryCreateRequest;
import com.restauranthub.category.dto.CategoryResponse;
import com.restauranthub.category.dto.CategoryUpdateRequest;
import com.restauranthub.category.exception.CategoryNotFoundException;
import com.restauranthub.category.exception.DuplicateCategorySlugException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer implementing all business logic and transaction management for Categories.
 *
 * Key Concepts:
 * 1. @Service: Marks this class as a Spring-managed service component in the application context.
 * 2. Constructor Injection: Dependencies are injected via the constructor, ensuring immutability (final fields)
 *    and making unit testing straightforward without relying on reflection or Spring test runners.
 * 3. @Transactional: Manages database transaction boundaries automatically. Methods with readOnly = true optimize
 *    read operations in Hibernate/MySQL, while write methods commit or rollback on exception.
 * 4. Separation of Concerns: Keeps HTTP handling (request mapping, response codes) out of business logic.
 */
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Constructor injection: Spring automatically discovers this constructor
     * and provides the CategoryRepository bean at runtime.
     *
     * @param categoryRepository repository for database operations
     */
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates a new category based on the validated request payload.
     *
     * Business Rules:
     * - The slug must be globally unique across all categories.
     * - A new category is always initialized with active = true by default.
     *
     * @param request validated creation DTO
     * @return CategoryResponse representing the newly persisted category
     * @throws DuplicateCategorySlugException if the slug is already in use
     */
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new DuplicateCategorySlugException(request.slug());
        }

        // Explicit mapping: DTO -> JPA Entity
        Category category = new Category(request.name(), request.slug(), true);
        Category savedCategory = categoryRepository.save(category);

        // Explicit mapping: JPA Entity -> Response DTO
        return CategoryResponse.fromEntity(savedCategory);
    }

    /**
     * Retrieves all categories from the database.
     *
     * @return List of CategoryResponse DTOs
     */
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves a single category by its unique identifier.
     *
     * @param id category ID
     * @return CategoryResponse DTO
     * @throws CategoryNotFoundException if no category matches the provided ID
     */
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryResponse::fromEntity)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    /**
     * Updates an existing category with new details.
     *
     * Business Rules:
     * - The target category must exist.
     * - If the slug is modified, the new slug must not collide with any other existing category.
     *
     * @param id category ID to update
     * @param request validated update DTO
     * @return CategoryResponse containing updated data
     * @throws CategoryNotFoundException if category is not found
     * @throws DuplicateCategorySlugException if the updated slug is already taken by another category
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // Check if slug changed and new slug already exists for another category
        if (!category.getSlug().equalsIgnoreCase(request.slug()) && categoryRepository.existsBySlug(request.slug())) {
            throw new DuplicateCategorySlugException(request.slug());
        }

        category.setName(request.name());
        category.setSlug(request.slug());
        category.setActive(request.active());

        Category updatedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(updatedCategory);
    }

    /**
     * Deletes a category by its unique identifier.
     *
     * Business Rules:
     * - The category must exist prior to deletion; otherwise throws CategoryNotFoundException.
     *
     * @param id category ID to delete
     * @throws CategoryNotFoundException if category does not exist
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        categoryRepository.delete(category);
    }
}
