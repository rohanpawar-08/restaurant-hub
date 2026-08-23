package com.restauranthub.food;

import com.restauranthub.food.dto.FoodCreateRequest;
import com.restauranthub.food.dto.FoodResponse;
import com.restauranthub.food.dto.FoodUpdateRequest;
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
@RestController
@RequestMapping("/api/v1/foods")
public class FoodController {

    private final FoodService foodService;

    /**
     * Constructor injection: Injects the FoodService component.
     */
    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    /**
     * POST /api/v1/foods - Creates a new food item.
     *
     * @param request validated creation request body
     * @return 201 Created with FoodResponse DTO
     */
    @PostMapping
    public ResponseEntity<FoodResponse> createFood(@Valid @RequestBody FoodCreateRequest request) {
        FoodResponse created = foodService.createFood(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/v1/foods - Retrieves all food items, optionally filtered by categoryId or popularity.
     *
     * Examples:
     * - GET /api/v1/foods
     * - GET /api/v1/foods?categoryId=1
     * - GET /api/v1/foods?popular=true
     *
     * @param categoryId optional category filter
     * @param popular optional popularity filter
     * @return 200 OK with list of FoodResponse DTOs
     */
    @GetMapping
    public ResponseEntity<List<FoodResponse>> getAllFoods(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean popular
    ) {
        List<FoodResponse> foods = foodService.getAllFoods(categoryId, popular);
        return ResponseEntity.ok(foods);
    }

    /**
     * GET /api/v1/foods/{id} - Retrieves a specific food item by ID.
     *
     * @param id food ID from URL path variable
     * @return 200 OK with FoodResponse DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(@PathVariable Long id) {
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
    @PutMapping("/{id}")
    public ResponseEntity<FoodResponse> updateFood(
            @PathVariable Long id,
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }
}
