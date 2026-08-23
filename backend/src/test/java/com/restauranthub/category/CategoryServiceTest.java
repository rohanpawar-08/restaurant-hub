package com.restauranthub.category;

import com.restauranthub.category.dto.CategoryCreateRequest;
import com.restauranthub.category.dto.CategoryResponse;
import com.restauranthub.category.dto.CategoryUpdateRequest;
import com.restauranthub.category.exception.CategoryNotFoundException;
import com.restauranthub.category.exception.DuplicateCategorySlugException;
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
 * Unit tests for CategoryService using Mockito.
 * Tests business logic in isolation without starting a web server or database.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Should create and return category when slug is unique")
    void createCategory_Success() {
        CategoryCreateRequest request = new CategoryCreateRequest("Starters", "starters");
        Category savedCategory = new Category("Starters", "starters", true);
        savedCategory.setId(1L);

        when(categoryRepository.existsBySlug("starters")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Starters", response.name());
        assertEquals("starters", response.slug());
        assertTrue(response.active());

        verify(categoryRepository).existsBySlug("starters");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw DuplicateCategorySlugException when slug already exists during creation")
    void createCategory_DuplicateSlug_ThrowsException() {
        CategoryCreateRequest request = new CategoryCreateRequest("Starters", "starters");

        when(categoryRepository.existsBySlug("starters")).thenReturn(true);

        DuplicateCategorySlugException ex = assertThrows(
                DuplicateCategorySlugException.class,
                () -> categoryService.createCategory(request)
        );

        assertTrue(ex.getMessage().contains("starters"));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should retrieve all categories mapped to DTOs")
    void getAllCategories_Success() {
        Category cat1 = new Category("Pizza", "pizza", true);
        cat1.setId(1L);
        Category cat2 = new Category("Burgers", "burgers", true);
        cat2.setId(2L);

        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        List<CategoryResponse> responses = categoryService.getAllCategories();

        assertEquals(2, responses.size());
        assertEquals("Pizza", responses.get(0).name());
        assertEquals("Burgers", responses.get(1).name());
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Should return category by ID when found")
    void getCategoryById_Success() {
        Category category = new Category("Desserts", "desserts", true);
        category.setId(5L);

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getCategoryById(5L);

        assertNotNull(response);
        assertEquals(5L, response.id());
        assertEquals("Desserts", response.name());
        assertEquals("desserts", response.slug());
        assertTrue(response.active());
        verify(categoryRepository).findById(5L);
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when category ID does not exist")
    void getCategoryById_NotFound_ThrowsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        CategoryNotFoundException ex = assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getCategoryById(99L)
        );

        assertTrue(ex.getMessage().contains("99"));
        verify(categoryRepository).findById(99L);
    }

    @Test
    @DisplayName("Should update category successfully when valid")
    void updateCategory_Success() {
        Category existing = new Category("Drinks", "drinks", true);
        existing.setId(10L);

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest("Beverages", "beverages", false);
        Category updated = new Category("Beverages", "beverages", false);
        updated.setId(10L);

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsBySlug("beverages")).thenReturn(false);
        when(categoryRepository.save(existing)).thenReturn(updated);

        CategoryResponse response = categoryService.updateCategory(10L, updateRequest);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Beverages", response.name());
        assertEquals("beverages", response.slug());
        assertFalse(response.active());

        verify(categoryRepository).findById(10L);
        verify(categoryRepository).existsBySlug("beverages");
        verify(categoryRepository).save(existing);
    }

    @Test
    @DisplayName("Should throw DuplicateCategorySlugException when updated slug belongs to another category")
    void updateCategory_DuplicateSlug_ThrowsException() {
        Category existing = new Category("Old Name", "old-slug", true);
        existing.setId(10L);

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest("New Name", "taken-slug", true);

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsBySlug("taken-slug")).thenReturn(true);

        DuplicateCategorySlugException ex = assertThrows(
                DuplicateCategorySlugException.class,
                () -> categoryService.updateCategory(10L, updateRequest)
        );

        assertTrue(ex.getMessage().contains("taken-slug"));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when updating non-existent category")
    void updateCategory_NotFound_ThrowsException() {
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest("New Name", "new-slug", true);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.updateCategory(99L, updateRequest)
        );

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should delete category when found")
    void deleteCategory_Success() {
        Category existing = new Category("Pasta", "pasta", true);
        existing.setId(3L);

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(existing));

        categoryService.deleteCategory(3L);

        verify(categoryRepository).findById(3L);
        verify(categoryRepository).delete(existing);
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when deleting non-existent category")
    void deleteCategory_NotFound_ThrowsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.deleteCategory(99L)
        );

        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
