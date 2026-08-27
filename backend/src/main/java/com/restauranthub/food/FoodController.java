package com.restauranthub.food;

import com.restauranthub.common.exception.ErrorResponse;
import com.restauranthub.food.dto.FoodCreateRequest;
import com.restauranthub.food.dto.FoodResponse;
import com.restauranthub.food.dto.FoodUpdateRequest;
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
 * REST Controller exposing HTTP endpoints for the Food resource.
 *
 * Base Route: /api/v1/foods
 */
@Tag(name = "Foods", description = "Menu dishes and dietary information (public read, admin write)")
@RestController
@RequestMapping("/api/v1/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    /**
     * POST /api/v1/foods - Creates a new food item.
     *
     * @param request validated creation request body
     * @return 201 Created with FoodResponse DTO
     */
    @Operation(summary = "Create dish", description = "Creates a new food item under a category (Requires ROLE_ADMIN and CSRF token)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Food item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN and CSRF)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Target category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<FoodResponse> createFood(@Valid @RequestBody FoodCreateRequest request) {
        FoodResponse created = foodService.createFood(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/v1/foods - Retrieves all food items, optionally filtered by categoryId, popularity, or active status.
     *
     * @param categoryId optional category filter
     * @param popular optional popularity filter
     * @param activeOnly optional filter to only return foods from active categories
     * @return 200 OK with list of FoodResponse DTOs
     */
    @Operation(summary = "Get all dishes", description = "Retrieves dishes filtered by category ID, popularity flag, or active category status")
    @ApiResponse(responseCode = "200", description = "Dishes retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = FoodResponse.class))))
    @GetMapping
    public ResponseEntity<List<FoodResponse>> getAllFoods(
            @Parameter(description = "Category ID filter") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Popular items only filter") @RequestParam(required = false) Boolean popular,
            @Parameter(description = "Active category items only filter") @RequestParam(required = false) Boolean activeOnly
    ) {
        List<FoodResponse> foods = foodService.getAllFoods(categoryId, popular, activeOnly);
        return ResponseEntity.ok(foods);
    }

    /**
     * GET /api/v1/foods/{id} - Retrieves a specific food item by ID.
     *
     * @param id food ID from URL path variable
     * @return 200 OK with FoodResponse DTO
     */
    @Operation(summary = "Get dish by ID", description = "Retrieves detailed information for a single dish")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dish found"),
            @ApiResponse(responseCode = "404", description = "Dish not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(@Parameter(description = "Food ID") @PathVariable Long id) {
        FoodResponse food = foodService.getFoodById(id);
        return ResponseEntity.ok(food);
    }

    /**
     * PUT /api/v1/foods/{id} - Updates an existing food item.
     *
     * @param id food ID from URL path variable
     * @param request validated update request body
     * @return 200 OK with updated FoodResponse DTO
     */
    @Operation(summary = "Update dish", description = "Updates details, pricing, availability, or category of an existing dish (Requires ROLE_ADMIN and CSRF token)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dish updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN and CSRF)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Dish or Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<FoodResponse> updateFood(
            @Parameter(description = "Food ID") @PathVariable Long id,
            @Valid @RequestBody FoodUpdateRequest request
    ) {
        FoodResponse updated = foodService.updateFood(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/v1/foods/{id} - Deletes a food item.
     *
     * @param id food ID from URL path variable
     * @return 204 No Content
     */
    @Operation(summary = "Delete dish", description = "Deletes a food item by ID (Requires ROLE_ADMIN and CSRF token)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dish deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Full authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (Requires ROLE_ADMIN and CSRF)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Dish not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@Parameter(description = "Food ID") @PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }
}
