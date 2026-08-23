package com.restauranthub.category;

import com.restauranthub.category.dto.CategoryCreateRequest;
import com.restauranthub.category.dto.CategoryResponse;
import com.restauranthub.category.dto.CategoryUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing HTTP endpoints for the Category resource.
 *
 * Base Route: /api/v1/categories
 *
 * Responsibilities:
 * - Maps HTTP request methods (POST, GET, PUT, DELETE) to Java methods.
 * - Triggers payload validation via @Valid on incoming request DTOs.
 * - Delegates all business decisions and persistence work to CategoryService.
 * - Translates service outcomes into standardized HTTP responses with appropriate status codes.
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Constructor injection: Injects the CategoryService component.
     *
     * @param categoryService the category business service
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Handles POST /api/v1/categories to create a new category.
     *
     * @param request validated creation request body
     * @return 201 Created with newly created CategoryResponse
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Handles GET /api/v1/categories to retrieve all categories.
     *
     * @return 200 OK with list of CategoryResponse DTOs
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Handles GET /api/v1/categories/{id} to retrieve a specific category by ID.
     *
     * @param id category ID from URL path variable
     * @return 200 OK with CategoryResponse DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Handles PUT /api/v1/categories/{id} to update an existing category.
     *
     * @param id category ID from URL path variable
     * @param request validated update request body
     * @return 200 OK with updated CategoryResponse DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        CategoryResponse updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Handles DELETE /api/v1/categories/{id} to remove a category.
     *
     * @param id category ID from URL path variable
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
