package com.restauranthub.category;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for CategoryRepository against the configured MySQL database.
 * The @Transactional annotation ensures that any changes made during each test method
 * are automatically rolled back at the end of the test, keeping the database clean.
 */
@SpringBootTest
@Transactional
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Should persist and retrieve a category by slug")
    void shouldSaveAndFindBySlug() {
        // Arrange
        Category category = new Category("Beverages & Drinks", "beverages-drinks", true);

        // Act
        Category savedCategory = categoryRepository.save(category);

        // Assert
        assertNotNull(savedCategory.getId(), "Database should auto-generate an ID for the persisted entity");
        assertEquals("Beverages & Drinks", savedCategory.getName());
        assertEquals("beverages-drinks", savedCategory.getSlug());
        assertTrue(savedCategory.getActive());

        // Verify retrieval via findBySlug
        Optional<Category> foundCategory = categoryRepository.findBySlug("beverages-drinks");
        assertTrue(foundCategory.isPresent(), "Category should be found by slug");
        assertEquals(savedCategory.getId(), foundCategory.get().getId());
    }

    @Test
    @DisplayName("Should verify existence of category by slug")
    void shouldCheckExistsBySlug() {
        // Arrange
        Category category = new Category("Desserts", "desserts", true);
        categoryRepository.save(category);

        // Act & Assert
        assertTrue(categoryRepository.existsBySlug("desserts"), "existsBySlug should return true for existing slug");
        assertFalse(categoryRepository.existsBySlug("non-existent-slug"), "existsBySlug should return false for unknown slug");
    }

    @Test
    @DisplayName("Should default active to true when using convenience constructor")
    void shouldDefaultActiveToTrue() {
        // Arrange
        Category category = new Category("Appetizers", "appetizers");

        // Act
        Category saved = categoryRepository.save(category);

        // Assert
        assertTrue(saved.getActive(), "Default active flag should be true");
    }
}
