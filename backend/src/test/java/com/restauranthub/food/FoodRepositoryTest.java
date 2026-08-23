package com.restauranthub.food;

import com.restauranthub.category.Category;
import com.restauranthub.category.CategoryRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for FoodRepository and relational mapping with Category.
 * Annotated with @Transactional to ensure automatic rollback after each test.
 */
@SpringBootTest
@Transactional
class FoodRepositoryTest {

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Should persist food with ManyToOne category relationship and retrieve with JOIN FETCH")
    void shouldSaveAndRetrieveFoodWithCategory() {
        // Arrange
        Category category = new Category("Italian Pastas", "italian-pastas", true);
        Category savedCategory = categoryRepository.save(category);

        Food food = new Food(
                "Penne All'Arrabbiata",
                "Spicy tomato sauce with garlic and dried red chili peppers",
                new BigDecimal("260.00"),
                new BigDecimal("4.7"),
                "/images/penne.png",
                true,
                true,
                true,
                savedCategory
        );

        // Act
        Food savedFood = foodRepository.save(food);

        // Assert
        assertNotNull(savedFood.getId(), "Database should generate primary key for food");
        assertEquals("Penne All'Arrabbiata", savedFood.getName());
        assertEquals(new BigDecimal("260.00"), savedFood.getPrice());
        assertEquals(savedCategory.getId(), savedFood.getCategory().getId());
        assertEquals("Italian Pastas", savedFood.getCategory().getName());

        // Verify retrieval via findById
        Optional<Food> found = foodRepository.findById(savedFood.getId());
        assertTrue(found.isPresent());
        assertEquals("italian-pastas", found.get().getCategory().getSlug());
    }

    @Test
    @DisplayName("Should query foods by Category ID")
    void shouldFindByCategoryId() {
        Category category = categoryRepository.save(new Category("Desserts Special", "desserts-special", true));

        Food food1 = new Food("Tiramisu", "Classic Italian dessert", new BigDecimal("180.00"), new BigDecimal("4.9"), null, true, true, true, category);
        Food food2 = new Food("Chocolate Mousse", "Rich dark chocolate mousse", new BigDecimal("160.00"), new BigDecimal("4.6"), null, true, false, true, category);

        foodRepository.save(food1);
        foodRepository.save(food2);

        List<Food> foods = foodRepository.findByCategoryId(category.getId());

        assertEquals(2, foods.size());
    }

    @Test
    @DisplayName("Should query popular foods")
    void shouldFindByPopularTrue() {
        Category category = categoryRepository.save(new Category("Burgers Special", "burgers-special", true));

        Food popularFood = new Food("Supreme Burger", "Double patty", new BigDecimal("220.00"), new BigDecimal("4.8"), null, false, true, true, category);
        Food regularFood = new Food("Plain Burger", "Single patty", new BigDecimal("120.00"), new BigDecimal("3.9"), null, false, false, true, category);

        foodRepository.save(popularFood);
        foodRepository.save(regularFood);

        List<Food> popularFoods = foodRepository.findByPopularTrue();

        assertFalse(popularFoods.isEmpty());
        assertTrue(popularFoods.stream().anyMatch(f -> f.getName().equals("Supreme Burger")));
    }

    @Test
    @DisplayName("Should query foods by category slug")
    void shouldFindByCategorySlug() {
        Category category = categoryRepository.save(new Category("Hot Beverages", "hot-beverages", true));
        Food tea = new Food("Masala Chai", "Indian spiced tea", new BigDecimal("40.00"), new BigDecimal("4.9"), null, true, true, true, category);
        foodRepository.save(tea);

        List<Food> foods = foodRepository.findByCategorySlug("hot-beverages");

        assertEquals(1, foods.size());
        assertEquals("Masala Chai", foods.get(0).getName());
    }
}
