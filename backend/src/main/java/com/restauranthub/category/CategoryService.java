package com.restauranthub.category;

import com.restauranthub.category.dto.CategoryCreateRequest;
import com.restauranthub.category.dto.CategoryResponse;
import com.restauranthub.category.dto.CategoryUpdateRequest;
import com.restauranthub.category.exception.CategoryInUseException;
import com.restauranthub.category.exception.CategoryNotFoundException;
import com.restauranthub.category.exception.DuplicateCategorySlugException;
import com.restauranthub.food.FoodRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer implementing business logic, validations, and transaction boundaries for Categories.
 */
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final FoodRepository foodRepository;

    public CategoryService(CategoryRepository categoryRepository, FoodRepository foodRepository) {
        this.categoryRepository = categoryRepository;
        this.foodRepository = foodRepository;
    }

    /**
     * Creates a new category.
     *
     * @param request validated creation DTO
     * @return CategoryResponse representing the newly persisted category
     */
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new DuplicateCategorySlugException(request.slug());
        }

        Category category = new Category(request.name(), request.slug(), true);
        Category savedCategory = categoryRepository.save(category);

        return CategoryResponse.fromEntity(savedCategory);
    }

    /**
     * Retrieves all categories, optionally filtered to active-only categories.
     *
     * @param activeOnly if true, returns only categories with active = true
     * @return List of CategoryResponse DTOs
     */
    public List<CategoryResponse> getAllCategories(Boolean activeOnly) {
        List<Category> categories = Boolean.TRUE.equals(activeOnly)
                ? categoryRepository.findByActiveTrue()
                : categoryRepository.findAll();

        return categories.stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves all categories from the database.
     */
    public List<CategoryResponse> getAllCategories() {
        return getAllCategories(false);
    }

    /**
     * Retrieves a single category by its unique identifier.
     *
     * @param id category ID
     * @return CategoryResponse DTO
     */
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryResponse::fromEntity)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    /**
     * Updates an existing category with new details.
     *
     * @param id category ID to update
     * @param request validated update DTO
     * @return CategoryResponse containing updated data
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

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
     * Deletes a category by its ID with referential integrity guard.
     * Throws CategoryInUseException (HTTP 409 Conflict) if any food items are currently assigned.
     *
     * @param id category ID to delete
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (foodRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException(id);
        }

        categoryRepository.delete(category);
    }
}
