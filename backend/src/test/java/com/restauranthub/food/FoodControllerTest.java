package com.restauranthub.food;

import com.restauranthub.category.exception.CategoryNotFoundException;
import com.restauranthub.common.exception.GlobalExceptionHandler;
import com.restauranthub.food.dto.FoodCreateRequest;
import com.restauranthub.food.dto.FoodResponse;
import com.restauranthub.food.dto.FoodUpdateRequest;
import com.restauranthub.food.exception.FoodNotFoundException;
import com.restauranthub.food.exception.InactiveCategoryException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for FoodController and GlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class FoodControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FoodService foodService;

    @InjectMocks
    private FoodController foodController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(foodController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/foods - Should return 201 Created on valid request")
    void createFood_Valid_Returns201() throws Exception {
        FoodResponse response = new FoodResponse(
                1L,
                "Margherita Pizza",
                "Classic pizza with tomato sauce and mozzarella",
                new BigDecimal("249.00"),
                new BigDecimal("4.6"),
                "/images/pizza.png",
                true,
                true,
                true,
                1L,
                "Pizza",
                "pizza"
        );

        when(foodService.createFood(any(FoodCreateRequest.class))).thenReturn(response);

        String json = """
                {
                    "name": "Margherita Pizza",
                    "description": "Classic pizza with tomato sauce and mozzarella",
                    "price": 249.00,
                    "rating": 4.6,
                    "image": "/images/pizza.png",
                    "veg": true,
                    "popular": true,
                    "categoryId": 1
                }
                """;

        mockMvc.perform(post("/api/v1/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Margherita Pizza"))
                .andExpect(jsonPath("$.price").value(249.00))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Pizza"))
                .andExpect(jsonPath("$.categorySlug").value("pizza"));
    }

    @Test
    @DisplayName("POST /api/v1/foods - Should return 400 Bad Request on validation errors (negative price, rating > 5, blank name)")
    void createFood_ValidationFailure_Returns400() throws Exception {
        String invalidJson = """
                {
                    "name": "",
                    "description": "Too short",
                    "price": -50.00,
                    "rating": 6.5,
                    "veg": true,
                    "categoryId": -1
                }
                """;

        mockMvc.perform(post("/api/v1/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists())
                .andExpect(jsonPath("$.validationErrors.rating").exists())
                .andExpect(jsonPath("$.validationErrors.categoryId").exists());
    }

    @Test
    @DisplayName("POST /api/v1/foods - Should return 404 Not Found when category does not exist")
    void createFood_CategoryNotFound_Returns404() throws Exception {
        when(foodService.createFood(any(FoodCreateRequest.class)))
                .thenThrow(new CategoryNotFoundException(99L));

        String json = """
                {
                    "name": "Garlic Bread",
                    "description": "Toasted bread with garlic butter",
                    "price": 120.00,
                    "rating": 4.3,
                    "veg": true,
                    "categoryId": 99
                }
                """;

        mockMvc.perform(post("/api/v1/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found with id: 99"));
    }

    @Test
    @DisplayName("POST /api/v1/foods - Should return 400 Bad Request when category is inactive")
    void createFood_InactiveCategory_Returns400() throws Exception {
        when(foodService.createFood(any(FoodCreateRequest.class)))
                .thenThrow(new InactiveCategoryException(5L));

        String json = """
                {
                    "name": "Winter Special Soup",
                    "description": "Warm vegetable soup for winter",
                    "price": 150.00,
                    "rating": 4.1,
                    "veg": true,
                    "categoryId": 5
                }
                """;

        mockMvc.perform(post("/api/v1/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot associate food item with inactive Category id: 5"));
    }

    @Test
    @DisplayName("GET /api/v1/foods - Should return 200 OK with list of foods")
    void getAllFoods_Returns200() throws Exception {
        FoodResponse food = new FoodResponse(
                1L,
                "Burger",
                "Veggie Burger",
                new BigDecimal("150.00"),
                new BigDecimal("4.4"),
                null,
                true,
                false,
                true,
                2L,
                "Burgers",
                "burgers"
        );

        when(foodService.getAllFoods(null, null, null)).thenReturn(List.of(food));

        mockMvc.perform(get("/api/v1/foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Burger"));
    }

    @Test
    @DisplayName("GET /api/v1/foods?categoryId=1 - Should filter by category ID")
    void getAllFoods_FilteredByCategory_Returns200() throws Exception {
        FoodResponse food = new FoodResponse(
                1L,
                "Margherita",
                "Classic pizza",
                new BigDecimal("249.00"),
                new BigDecimal("4.6"),
                null,
                true,
                true,
                true,
                1L,
                "Pizza",
                "pizza"
        );

        when(foodService.getAllFoods(1L, null, null)).thenReturn(List.of(food));

        mockMvc.perform(get("/api/v1/foods?categoryId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoryId").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/foods/{id} - Should return 200 OK when found")
    void getFoodById_Found_Returns200() throws Exception {
        FoodResponse food = new FoodResponse(
                5L,
                "Pasta",
                "Penne Arrabbiata",
                new BigDecimal("220.00"),
                new BigDecimal("4.5"),
                null,
                true,
                true,
                true,
                3L,
                "Pasta",
                "pasta"
        );

        when(foodService.getFoodById(5L)).thenReturn(food);

        mockMvc.perform(get("/api/v1/foods/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Pasta"));
    }

    @Test
    @DisplayName("GET /api/v1/foods/{id} - Should return 404 Not Found when ID does not exist")
    void getFoodById_NotFound_Returns404() throws Exception {
        when(foodService.getFoodById(99L)).thenThrow(new FoodNotFoundException(99L));

        mockMvc.perform(get("/api/v1/foods/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Food item not found with id: 99"));
    }

    @Test
    @DisplayName("PUT /api/v1/foods/{id} - Should return 200 OK on successful update")
    void updateFood_Success_Returns200() throws Exception {
        FoodResponse updated = new FoodResponse(
                1L,
                "Updated Pizza",
                "Updated description with more details",
                new BigDecimal("299.00"),
                new BigDecimal("4.8"),
                "/images/updated.png",
                true,
                true,
                true,
                1L,
                "Pizza",
                "pizza"
        );

        when(foodService.updateFood(eq(1L), any(FoodUpdateRequest.class))).thenReturn(updated);

        String updateJson = """
                {
                    "name": "Updated Pizza",
                    "description": "Updated description with more details",
                    "price": 299.00,
                    "rating": 4.8,
                    "image": "/images/updated.png",
                    "veg": true,
                    "popular": true,
                    "available": true,
                    "categoryId": 1
                }
                """;

        mockMvc.perform(put("/api/v1/foods/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Pizza"))
                .andExpect(jsonPath("$.price").value(299.00));
    }

    @Test
    @DisplayName("DELETE /api/v1/foods/{id} - Should return 204 No Content on deletion")
    void deleteFood_Success_Returns204() throws Exception {
        doNothing().when(foodService).deleteFood(1L);

        mockMvc.perform(delete("/api/v1/foods/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/foods/{id} - Should return 404 Not Found when food does not exist")
    void deleteFood_NotFound_Returns404() throws Exception {
        doThrow(new FoodNotFoundException(99L)).when(foodService).deleteFood(99L);

        mockMvc.perform(delete("/api/v1/foods/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
