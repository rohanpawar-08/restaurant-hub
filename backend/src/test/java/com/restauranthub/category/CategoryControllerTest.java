package com.restauranthub.category;

import com.restauranthub.category.dto.CategoryCreateRequest;
import com.restauranthub.category.dto.CategoryResponse;
import com.restauranthub.category.dto.CategoryUpdateRequest;
import com.restauranthub.category.exception.CategoryInUseException;
import com.restauranthub.category.exception.CategoryNotFoundException;
import com.restauranthub.category.exception.DuplicateCategorySlugException;
import com.restauranthub.common.exception.GlobalExceptionHandler;
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
 * Unit/MockMvc tests for CategoryController and GlobalExceptionHandler integration.
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should return 201 Created on valid request")
    void createCategory_Valid_Returns201() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Pizza", "pizza", true);

        when(categoryService.createCategory(any(CategoryCreateRequest.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "name": "Pizza",
                    "slug": "pizza"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pizza"))
                .andExpect(jsonPath("$.slug").value("pizza"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should return 400 Bad Request on validation failure (blank name, invalid slug)")
    void createCategory_ValidationFailure_Returns400() throws Exception {
        String invalidJsonPayload = """
                {
                    "name": "",
                    "slug": "INVALID SLUG!"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.slug").exists());
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should return 409 Conflict on duplicate slug")
    void createCategory_DuplicateSlug_Returns409() throws Exception {
        when(categoryService.createCategory(any(CategoryCreateRequest.class)))
                .thenThrow(new DuplicateCategorySlugException("pizza"));

        String jsonPayload = """
                {
                    "name": "Pizza",
                    "slug": "pizza"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Category already exists with slug: 'pizza'"));
    }

    @Test
    @DisplayName("GET /api/v1/categories - Should return 200 OK with list of categories")
    void getAllCategories_Returns200() throws Exception {
        List<CategoryResponse> categories = List.of(
                new CategoryResponse(1L, "Starters", "starters", true),
                new CategoryResponse(2L, "Desserts", "desserts", true)
        );

        when(categoryService.getAllCategories(any())).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Starters"))
                .andExpect(jsonPath("$[1].name").value("Desserts"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Should return 200 OK when found")
    void getCategoryById_Found_Returns200() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Beverages", "beverages", true);

        when(categoryService.getCategoryById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Beverages"))
                .andExpect(jsonPath("$.slug").value("beverages"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Should return 404 Not Found when ID does not exist")
    void getCategoryById_NotFound_Returns404() throws Exception {
        when(categoryService.getCategoryById(99L)).thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(get("/api/v1/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Category not found with id: 99"));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Should return 200 OK on successful update")
    void updateCategory_Success_Returns200() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Italian Pizza", "italian-pizza", true);

        when(categoryService.updateCategory(eq(1L), any(CategoryUpdateRequest.class))).thenReturn(response);

        String updateJson = """
                {
                    "name": "Italian Pizza",
                    "slug": "italian-pizza",
                    "active": true
                }
                """;

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Italian Pizza"))
                .andExpect(jsonPath("$.slug").value("italian-pizza"));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Should return 404 Not Found when updating missing category")
    void updateCategory_NotFound_Returns404() throws Exception {
        when(categoryService.updateCategory(eq(99L), any(CategoryUpdateRequest.class)))
                .thenThrow(new CategoryNotFoundException(99L));

        String updateJson = """
                {
                    "name": "Italian Pizza",
                    "slug": "italian-pizza",
                    "active": true
                }
                """;

        mockMvc.perform(put("/api/v1/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should return 204 No Content on successful deletion")
    void deleteCategory_Success_Returns204() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should return 409 Conflict when category contains menu items")
    void deleteCategory_InUse_Returns409() throws Exception {
        doThrow(new CategoryInUseException(1L)).when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("This category still contains menu items. Move or remove those items before deleting the category, or deactivate the category instead."));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should return 404 Not Found when category does not exist")
    void deleteCategory_NotFound_Returns404() throws Exception {
        doThrow(new CategoryNotFoundException(99L)).when(categoryService).deleteCategory(99L);

        mockMvc.perform(delete("/api/v1/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
