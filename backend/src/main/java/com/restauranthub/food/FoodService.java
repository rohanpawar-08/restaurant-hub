package com.restauranthub.food;

import com.restauranthub.category.Category;
import com.restauranthub.category.CategoryRepository;
import com.restauranthub.category.exception.CategoryNotFoundException;
import com.restauranthub.food.dto.FoodCreateRequest;
import com.restauranthub.food.dto.FoodResponse;
import com.restauranthub.food.dto.FoodUpdateRequest;
import com.restauranthub.food.exception.FoodNotFoundException;
import com.restauranthub.food.exception.InactiveCategoryException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing business rules, validations, and database transactions for Food items.
 */
@Service
@Transactional(readOnly = true)
public class FoodService {

    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Constructor injection: Injects repository dependencies cleanly.
     */
    public FoodService(FoodRepository foodRepository, CategoryRepository categoryRepository) {
        this.foodRepository = foodRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates a new Food item and associates it with a verified Category.
     *
     * Business Rules:
     * 1. The target Category must exist; otherwise throws CategoryNotFoundException.
     * 2. The target Category must be active; otherwise throws InactiveCategoryException.
     * 3. A new food item defaults available to true, and popular to false if omitted.
     *
     * @param request validated creation request
     * @return FoodResponse representing the newly persisted Food item
     */
    @Transactional
    public FoodResponse createFood(FoodCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new InactiveCategoryException(request.categoryId());
        }

        BigDecimal rating = (request.rating() != null) ? request.rating() : BigDecimal.ZERO;
        boolean popular = (request.popular() != null) ? request.popular() : false;
        boolean available = (request.available() != null) ? request.available() : true;

        Food food = new Food(
                request.name(),
                request.description(),
                request.price(),
                rating,
                request.image(),
                request.veg(),
                popular,
                available,
                category
        );

        Food savedFood = foodRepository.save(food);
        return FoodResponse.fromEntity(savedFood);
    }

    /**
     * Retrieves all foods, with optional filtering by categoryId, popularity, and active category status.
     *
     * @param categoryId optional filter by category
     * @param popular optional filter for popular items
     * @param activeOnly optional filter to only return foods from active categories
     * @return list of FoodResponse DTOs
     */
    public List<FoodResponse> getAllFoods(Long categoryId, Boolean popular, Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly)) {
            if (categoryId != null) {
                if (!categoryRepository.existsById(categoryId)) {
                    throw new CategoryNotFoundException(categoryId);
                }
                return foodRepository.findByCategoryIdAndCategoryActiveTrue(categoryId)
                        .stream()
                        .map(FoodResponse::fromEntity)
                        .toList();
            }

            if (Boolean.TRUE.equals(popular)) {
                return foodRepository.findByPopularTrueAndCategoryActiveTrue()
                        .stream()
                        .map(FoodResponse::fromEntity)
                        .toList();
            }

            return foodRepository.findByCategoryActiveTrue()
                    .stream()
                    .map(FoodResponse::fromEntity)
                    .toList();
        }

        if (categoryId != null) {
            if (!categoryRepository.existsById(categoryId)) {
                throw new CategoryNotFoundException(categoryId);
            }
            return foodRepository.findByCategoryId(categoryId)
                    .stream()
                    .map(FoodResponse::fromEntity)
                    .toList();
        }

        if (Boolean.TRUE.equals(popular)) {
            return foodRepository.findByPopularTrue()
                    .stream()
                    .map(FoodResponse::fromEntity)
                    .toList();
        }

        return foodRepository.findAll()
                .stream()
                .map(FoodResponse::fromEntity)
                .toList();
    }

    /**
     * Backward-compatible overload for getAllFoods without activeOnly filter.
     */
    public List<FoodResponse> getAllFoods(Long categoryId, Boolean popular) {
        return getAllFoods(categoryId, popular, null);
    }

    /**
     * Retrieves a single Food item by its unique ID.
     *
     * @param id food ID
     * @return FoodResponse DTO
     * @throws FoodNotFoundException if food does not exist
     */
    public FoodResponse getFoodById(Long id) {
        return foodRepository.findById(id)
                .map(FoodResponse::fromEntity)
                .orElseThrow(() -> new FoodNotFoundException(id));
    }

    /**
     * Updates an existing Food item.
     *
     * Business Rules:
     * 1. Target Food item must exist.
     * 2. If Category changes, the new Category must exist and be active.
     *
     * @param id food ID to update
     * @param request validated update payload
     * @return updated FoodResponse DTO
     */
    @Transactional
    public FoodResponse updateFood(Long id, FoodUpdateRequest request) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new FoodNotFoundException(id));

        // If category changed, validate and reassign
        if (!food.getCategory().getId().equals(request.categoryId())) {
            Category newCategory = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

            if (!Boolean.TRUE.equals(newCategory.getActive())) {
                throw new InactiveCategoryException(request.categoryId());
            }
            food.setCategory(newCategory);
        }

        food.setName(request.name());
        food.setDescription(request.description());
        food.setPrice(request.price());
        BigDecimal rating = (request.rating() != null) ? request.rating() : food.getRating();
        food.setRating(rating);
        food.setImage(request.image());
        food.setVeg(request.veg());
        food.setPopular(request.popular());
        food.setAvailable(request.available());

        Food updatedFood = foodRepository.save(food);
        return FoodResponse.fromEntity(updatedFood);
    }

    /**
     * Deletes a Food item by ID.
     *
     * @param id food ID
     * @throws FoodNotFoundException if food does not exist
     */
    @Transactional
    public void deleteFood(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new FoodNotFoundException(id));

        foodRepository.delete(food);
    }
}
