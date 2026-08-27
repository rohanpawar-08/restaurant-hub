package com.restauranthub.category;

import com.restauranthub.category.dto.CategoryCreateRequest;
import com.restauranthub.category.dto.CategoryResponse;
import com.restauranthub.category.dto.CategoryUpdateRequest;
import com.restauranthub.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing HTTP endpoints for the Category resource.
 *
 * Base Route: /api/v1/categories
 */
@Tag(name = "Categories", description = "Menu categories (public read, admin write)")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Handles POST /api/v1/categories to create a new category.
     *
     * @param request validated creation request body
     * @return 201 Created with newly created CategoryResponse
     */
    @Operation(summary = "Create category", description = "Creates a new menu category (Requires ROLE_ADMIN and CSRF token)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN and CSRF)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Category name or slug already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Handles GET /api/v1/categories to retrieve all categories.
     *
     * @param activeOnly optional filter to retrieve only active categories
     * @return 200 OK with list of CategoryResponse DTOs
     */
    @Operation(summary = "Get all categories", description = "Retrieves all menu categories, optionally filtered to active-only")
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class))))
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @Parameter(description = "Filter only active categories") @RequestParam(required = false) Boolean activeOnly
    ) {
        List<CategoryResponse> categories = categoryService.getAllCategories(activeOnly);
        return ResponseEntity.ok(categories);
    }

    /**
     * Handles GET /api/v1/categories/{id} to retrieve a specific category by ID.
     *
     * @param id category ID from URL path variable
     * @return 200 OK with CategoryResponse DTO
     */
    @Operation(summary = "Get category by ID", description = "Retrieves a single category by its primary key")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@Parameter(description = "Category ID") @PathVariable Long id) {
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
    @Operation(summary = "Update category", description = "Updates an existing category by ID (Requires ROLE_ADMIN and CSRF token)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN and CSRF)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Category name or slug already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
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
    @Operation(summary = "Delete category", description = "Deletes a category by ID (Requires ROLE_ADMIN and CSRF token)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN and CSRF)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Category cannot be deleted because foods are linked to it",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@Parameter(description = "Category ID") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
