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
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FoodService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FoodService foodService;

    @Test
    @DisplayName("Should create food when valid category exists and is active")
    void createFood_Success() {
        Category category = new Category("Pizza", "pizza", true);
        category.setId(1L);

        FoodCreateRequest request = new FoodCreateRequest(
                "Margherita",
                "Classic pizza with tomato and mozzarella",
                new BigDecimal("249.00"),
                new BigDecimal("4.6"),
                "/images/margherita.png",
                true,
                true,
                true,
                1L
        );

        Food savedFood = new Food(
                "Margherita",
                "Classic pizza with tomato and mozzarella",
                new BigDecimal("249.00"),
                new BigDecimal("4.6"),
                "/images/margherita.png",
                true,
                true,
                true,
                category
        );
        savedFood.setId(100L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(foodRepository.save(any(Food.class))).thenReturn(savedFood);

        FoodResponse response = foodService.createFood(request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Margherita", response.name());
        assertEquals(new BigDecimal("249.00"), response.price());
        assertEquals(1L, response.categoryId());
        assertEquals("Pizza", response.categoryName());
        assertEquals("pizza", response.categorySlug());
        assertTrue(response.veg());
        assertTrue(response.popular());
        assertTrue(response.available());

        verify(categoryRepository).findById(1L);
        verify(foodRepository).save(any(Food.class));
    }

    @Test
    @DisplayName("Should create food with default neutral rating 0.0 and available true when omitted")
    void createFood_DefaultRatingAndAvailable_Success() {
        Category category = new Category("Pizza", "pizza", true);
        category.setId(1L);

        FoodCreateRequest request = new FoodCreateRequest(
                "Margherita",
                "Classic pizza with tomato and mozzarella",
                new BigDecimal("249.00"),
                null,
                null,
                true,
                null,
                null,
                1L
        );

        Food savedFood = new Food(
                "Margherita",
                "Classic pizza with tomato and mozzarella",
                new BigDecimal("249.00"),
                BigDecimal.ZERO,
                null,
                true,
                false,
                true,
                category
        );
        savedFood.setId(101L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(foodRepository.save(any(Food.class))).thenReturn(savedFood);

        FoodResponse response = foodService.createFood(request);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.rating());
        assertTrue(response.available());
        assertFalse(response.popular());
        verify(foodRepository).save(any(Food.class));
    }

    @Test
    @DisplayName("Should filter foods from active categories only when activeOnly is true")
    void getAllFoods_ActiveOnly_ReturnsFoodsFromActiveCategoriesOnly() {
        Category activeCategory = new Category("Pizza", "pizza", true);
        activeCategory.setId(1L);

        Food food1 = new Food("Margherita", "Pizza 1", new BigDecimal("200.00"), new BigDecimal("4.5"), null, true, false, true, activeCategory);
        food1.setId(1L);

        when(foodRepository.findByCategoryActiveTrue()).thenReturn(List.of(food1));

        List<FoodResponse> responses = foodService.getAllFoods(null, null, true);

        assertEquals(1, responses.size());
        assertEquals("Margherita", responses.get(0).name());
        verify(foodRepository).findByCategoryActiveTrue();
    }

    @Test
    @DisplayName("Should filter foods from active categories by categoryId when activeOnly is true")
    void getAllFoods_ActiveOnly_ByCategoryId_Success() {
        Category activeCategory = new Category("Pizza", "pizza", true);
        activeCategory.setId(1L);

        Food food1 = new Food("Margherita", "Pizza 1", new BigDecimal("200.00"), new BigDecimal("4.5"), null, true, false, true, activeCategory);
        food1.setId(1L);

        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(foodRepository.findByCategoryIdAndCategoryActiveTrue(1L)).thenReturn(List.of(food1));

        List<FoodResponse> responses = foodService.getAllFoods(1L, null, true);

        assertEquals(1, responses.size());
        assertEquals("Margherita", responses.get(0).name());
        verify(foodRepository).findByCategoryIdAndCategoryActiveTrue(1L);
    }

    @Test
    @DisplayName("Should filter popular foods from active categories when activeOnly and popular are true")
    void getAllFoods_ActiveOnly_Popular_Success() {
        Category activeCategory = new Category("Burgers", "burgers", true);
        activeCategory.setId(3L);

        Food food = new Food("Cheeseburger", "Juicy burger", new BigDecimal("199.00"), new BigDecimal("4.7"), null, false, true, true, activeCategory);
        food.setId(20L);

        when(foodRepository.findByPopularTrueAndCategoryActiveTrue()).thenReturn(List.of(food));

        List<FoodResponse> responses = foodService.getAllFoods(null, true, true);

        assertEquals(1, responses.size());
        assertEquals("Cheeseburger", responses.get(0).name());
        verify(foodRepository).findByPopularTrueAndCategoryActiveTrue();
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when category does not exist during food creation")
    void createFood_CategoryNotFound_ThrowsException() {
        FoodCreateRequest request = new FoodCreateRequest(
                "Margherita",
                "Classic pizza",
                new BigDecimal("249.00"),
                new BigDecimal("4.6"),
                null,
                true,
                false,
                true,
                99L
        );

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> foodService.createFood(request));
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    @DisplayName("Should throw InactiveCategoryException when category is inactive during food creation")
    void createFood_InactiveCategory_ThrowsException() {
        Category inactiveCategory = new Category("Seasonal", "seasonal", false);
        inactiveCategory.setId(2L);

        FoodCreateRequest request = new FoodCreateRequest(
                "Pumpkin Soup",
                "Seasonal special",
                new BigDecimal("180.00"),
                new BigDecimal("4.2"),
                null,
                true,
                false,
                true,
                2L
        );

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(inactiveCategory));

        assertThrows(InactiveCategoryException.class, () -> foodService.createFood(request));
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    @DisplayName("Should retrieve all foods without filter")
    void getAllFoods_NoFilter_ReturnsAll() {
        Category category = new Category("Pizza", "pizza", true);
        category.setId(1L);

        Food food1 = new Food("Margherita", "Pizza 1", new BigDecimal("200.00"), new BigDecimal("4.5"), null, true, false, true, category);
        food1.setId(1L);
        Food food2 = new Food("Pepperoni", "Pizza 2", new BigDecimal("300.00"), new BigDecimal("4.8"), null, false, true, true, category);
        food2.setId(2L);

        when(foodRepository.findAll()).thenReturn(List.of(food1, food2));

        List<FoodResponse> responses = foodService.getAllFoods(null, null);

        assertEquals(2, responses.size());
        assertEquals("Margherita", responses.get(0).name());
        assertEquals("Pepperoni", responses.get(1).name());
        verify(foodRepository).findAll();
    }

    @Test
    @DisplayName("Should filter foods by category ID when provided")
    void getAllFoods_ByCategoryId_Success() {
        Category category = new Category("Drinks", "drinks", true);
        category.setId(5L);

        Food food = new Food("Cola", "Chilled drink", new BigDecimal("50.00"), new BigDecimal("4.0"), null, true, false, true, category);
        food.setId(10L);

        when(categoryRepository.existsById(5L)).thenReturn(true);
        when(foodRepository.findByCategoryId(5L)).thenReturn(List.of(food));

        List<FoodResponse> responses = foodService.getAllFoods(5L, null);

        assertEquals(1, responses.size());
        assertEquals("Cola", responses.get(0).name());
        verify(foodRepository).findByCategoryId(5L);
    }

    @Test
    @DisplayName("Should filter popular foods when popular is true")
    void getAllFoods_Popular_ReturnsPopular() {
        Category category = new Category("Burgers", "burgers", true);
        category.setId(3L);

        Food food = new Food("Cheeseburger", "Juicy burger", new BigDecimal("199.00"), new BigDecimal("4.7"), null, false, true, true, category);
        food.setId(20L);

        when(foodRepository.findByPopularTrue()).thenReturn(List.of(food));

        List<FoodResponse> responses = foodService.getAllFoods(null, true);

        assertEquals(1, responses.size());
        assertEquals("Cheeseburger", responses.get(0).name());
        assertTrue(responses.get(0).popular());
        verify(foodRepository).findByPopularTrue();
    }

    @Test
    @DisplayName("Should return FoodResponse when food found by ID")
    void getFoodById_Success() {
        Category category = new Category("Pasta", "pasta", true);
        category.setId(4L);

        Food food = new Food("Alfredo", "Creamy pasta", new BigDecimal("280.00"), new BigDecimal("4.6"), null, true, false, true, category);
        food.setId(30L);

        when(foodRepository.findById(30L)).thenReturn(Optional.of(food));

        FoodResponse response = foodService.getFoodById(30L);

        assertNotNull(response);
        assertEquals(30L, response.id());
        assertEquals("Alfredo", response.name());
        assertEquals("Pasta", response.categoryName());
        verify(foodRepository).findById(30L);
    }

    @Test
    @DisplayName("Should throw FoodNotFoundException when food ID does not exist")
    void getFoodById_NotFound_ThrowsException() {
        when(foodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(FoodNotFoundException.class, () -> foodService.getFoodById(99L));
        verify(foodRepository).findById(99L);
    }

    @Test
    @DisplayName("Should update food details successfully")
    void updateFood_Success() {
        Category category = new Category("Pizza", "pizza", true);
        category.setId(1L);

        Food existingFood = new Food("Old Pizza", "Old description", new BigDecimal("200.00"), new BigDecimal("4.0"), null, true, false, true, category);
        existingFood.setId(10L);

        FoodUpdateRequest updateRequest = new FoodUpdateRequest(
                "New Pizza",
                "New description",
                new BigDecimal("250.00"),
                new BigDecimal("4.5"),
                "/new.png",
                false,
                true,
                false,
                1L
        );

        when(foodRepository.findById(10L)).thenReturn(Optional.of(existingFood));
        when(foodRepository.save(existingFood)).thenReturn(existingFood);

        FoodResponse response = foodService.updateFood(10L, updateRequest);

        assertNotNull(response);
        assertEquals("New Pizza", response.name());
        assertEquals(new BigDecimal("250.00"), response.price());
        assertFalse(response.veg());
        assertTrue(response.popular());
        assertFalse(response.available());

        verify(foodRepository).save(existingFood);
    }

    @Test
    @DisplayName("Should update food category when categoryId changes")
    void updateFood_CategoryChanged_Success() {
        Category oldCategory = new Category("Starters", "starters", true);
        oldCategory.setId(1L);
        Category newCategory = new Category("Main Course", "main-course", true);
        newCategory.setId(2L);

        Food existingFood = new Food("Paneer Tikka", "Grilled paneer", new BigDecimal("220.00"), new BigDecimal("4.5"), null, true, false, true, oldCategory);
        existingFood.setId(15L);

        FoodUpdateRequest updateRequest = new FoodUpdateRequest(
                "Paneer Tikka Platter",
                "Grilled paneer platter",
                new BigDecimal("320.00"),
                new BigDecimal("4.8"),
                null,
                true,
                true,
                true,
                2L
        );

        when(foodRepository.findById(15L)).thenReturn(Optional.of(existingFood));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(foodRepository.save(existingFood)).thenReturn(existingFood);

        FoodResponse response = foodService.updateFood(15L, updateRequest);

        assertNotNull(response);
        assertEquals(2L, response.categoryId());
        assertEquals("Main Course", response.categoryName());
        verify(categoryRepository).findById(2L);
        verify(foodRepository).save(existingFood);
    }

    @Test
    @DisplayName("Should delete food when found")
    void deleteFood_Success() {
        Category category = new Category("Pizza", "pizza", true);
        category.setId(1L);
        Food food = new Food("Pizza", "Desc", new BigDecimal("200.00"), new BigDecimal("4.0"), null, true, false, true, category);
        food.setId(10L);

        when(foodRepository.findById(10L)).thenReturn(Optional.of(food));

        foodService.deleteFood(10L);

        verify(foodRepository).findById(10L);
        verify(foodRepository).delete(food);
    }

    @Test
    @DisplayName("Should throw FoodNotFoundException when deleting non-existent food")
    void deleteFood_NotFound_ThrowsException() {
        when(foodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(FoodNotFoundException.class, () -> foodService.deleteFood(99L));
        verify(foodRepository, never()).delete(any(Food.class));
    }
}
