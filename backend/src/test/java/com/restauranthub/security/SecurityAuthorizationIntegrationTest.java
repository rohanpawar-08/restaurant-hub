package com.restauranthub.security;

import com.restauranthub.category.Category;
import com.restauranthub.category.CategoryRepository;
import com.restauranthub.food.Food;
import com.restauranthub.food.FoodRepository;
import com.restauranthub.order.Order;
import com.restauranthub.order.OrderRepository;
import com.restauranthub.order.OrderStatus;
import com.restauranthub.order.PaymentMethod;
import com.restauranthub.user.User;
import com.restauranthub.user.UserRepository;
import com.restauranthub.user.UserRole;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class SecurityAuthorizationIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User customerUser;
    private User adminUser;
    private Category testCategory;
    private Food testFood;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        customerUser = userRepository.findByEmailIgnoreCase("cust.auth@example.com")
                .orElseGet(() -> userRepository.save(new User(
                        "Test Customer",
                        "cust.auth@example.com",
                        "9911111111",
                        "hashed_pwd",
                        UserRole.CUSTOMER
                )));

        adminUser = userRepository.findByEmailIgnoreCase("admin.auth@example.com")
                .orElseGet(() -> userRepository.save(new User(
                        "Test Admin",
                        "admin.auth@example.com",
                        "9922222222",
                        "hashed_pwd",
                        UserRole.ADMIN
                )));

        testCategory = categoryRepository.findBySlug("auth-sec-cat")
                .orElseGet(() -> categoryRepository.save(new Category("Security Test Category", "auth-sec-cat")));

        testFood = foodRepository.save(new Food(
                "Security Curry",
                "Spiced security curry",
                new BigDecimal("220.00"),
                new BigDecimal("4.6"),
                "curry.jpg",
                true,
                true,
                true,
                testCategory
        ));

        testOrder = new Order();
        testOrder.setUser(customerUser);
        testOrder.setStatus(OrderStatus.CONFIRMED);
        testOrder.setPaymentMethod(PaymentMethod.COD);
        testOrder.setSubtotal(new BigDecimal("220.00"));
        testOrder.setDeliveryFee(new BigDecimal("40.00"));
        testOrder.setTotal(new BigDecimal("260.00"));
        testOrder.setCustomerName("Test Customer");
        testOrder.setCustomerEmail("cust.auth@example.com");
        testOrder.setCustomerPhone("9911111111");
        testOrder.setAddressLine1("123 Security St");
        testOrder.setCity("Mumbai");
        testOrder.setState("Maharashtra");
        testOrder.setPostalCode("400001");
        testOrder.setEstimatedDeliveryMinutes(35);
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("1. Anonymous GET /api/v1/foods should return 200 OK")
    void shouldAllowAnonymousGetFoods() throws Exception {
        mockMvc.perform(get("/api/v1/foods"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. CUSTOMER GET /api/v1/foods should return 200 OK")
    void shouldAllowCustomerGetFoods() throws Exception {
        mockMvc.perform(get("/api/v1/foods")
                        .with(user("cust.auth@example.com").roles("CUSTOMER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("3. CUSTOMER POST /api/v1/foods should return 403 Forbidden")
    void shouldRejectCustomerPostFood() throws Exception {
        String json = String.format("""
                {
                    "name": "Hacker Dish",
                    "description": "Unauthorized creation",
                    "price": 100.00,
                    "rating": 5.0,
                    "image": "hack.jpg",
                    "veg": true,
                    "popular": false,
                    "categoryId": %d
                }
                """, testCategory.getId());

        mockMvc.perform(post("/api/v1/foods")
                        .with(user("cust.auth@example.com").roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. CUSTOMER PUT /api/v1/foods/{id} should return 403 Forbidden")
    void shouldRejectCustomerPutFood() throws Exception {
        String json = String.format("""
                {
                    "name": "Modified Curry",
                    "description": "Modified description",
                    "price": 300.00,
                    "rating": 4.9,
                    "image": "mod.jpg",
                    "veg": true,
                    "popular": true,
                    "categoryId": %d
                }
                """, testCategory.getId());

        mockMvc.perform(put("/api/v1/foods/" + testFood.getId())
                        .with(user("cust.auth@example.com").roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. CUSTOMER DELETE /api/v1/foods/{id} should return 403 Forbidden")
    void shouldRejectCustomerDeleteFood() throws Exception {
        mockMvc.perform(delete("/api/v1/foods/" + testFood.getId())
                        .with(user("cust.auth@example.com").roles("CUSTOMER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. ADMIN POST /api/v1/foods with CSRF should return 201 Created")
    void shouldAllowAdminPostFood() throws Exception {
        String json = String.format("""
                {
                    "name": "Admin Chef Special",
                    "description": "Exquisite culinary dish",
                    "price": 450.00,
                    "rating": 4.9,
                    "image": "special.jpg",
                    "veg": false,
                    "popular": true,
                    "categoryId": %d
                }
                """, testCategory.getId());

        mockMvc.perform(post("/api/v1/foods")
                        .with(user("admin.auth@example.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Admin Chef Special"));
    }

    @Test
    @DisplayName("7. CUSTOMER POST /api/v1/categories should return 403 Forbidden")
    void shouldRejectCustomerPostCategory() throws Exception {
        String json = """
                {
                    "name": "Unauthorized Category",
                    "slug": "unauthorized-category"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .with(user("cust.auth@example.com").roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("8. ADMIN POST /api/v1/categories with CSRF should return 201 Created")
    void shouldAllowAdminPostCategory() throws Exception {
        String json = """
                {
                    "name": "Gourmet Specials",
                    "slug": "gourmet-specials"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .with(user("admin.auth@example.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Gourmet Specials"));
    }

    @Test
    @DisplayName("9. Unauthenticated GET /api/v1/admin/dashboard/summary should return 401 Unauthorized")
    void shouldRejectUnauthenticatedAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("10. CUSTOMER GET /api/v1/admin/dashboard/summary should return 403 Forbidden")
    void shouldRejectCustomerAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/summary")
                        .with(user("cust.auth@example.com").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("11. ADMIN GET /api/v1/admin/dashboard/summary should return 200 OK")
    void shouldAllowAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/summary")
                        .with(user("admin.auth@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").isNumber())
                .andExpect(jsonPath("$.totalCustomers").isNumber());
    }

    @Test
    @DisplayName("12. CUSTOMER GET /api/v1/admin/orders should return 403 Forbidden")
    void shouldRejectCustomerAdminOrders() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .with(user("cust.auth@example.com").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("13. ADMIN GET /api/v1/admin/orders should return 200 OK with restaurant orders")
    void shouldAllowAdminOrders() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .with(user("admin.auth@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("14. ADMIN PATCH /api/v1/admin/orders/{id}/status WITHOUT CSRF should return 403 Forbidden")
    void shouldRejectAdminStatusPatchWithoutCsrf() throws Exception {
        String json = """
                {
                    "status": "PREPARING"
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/orders/" + testOrder.getId() + "/status")
                        .with(user("admin.auth@example.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("15. ADMIN PATCH /api/v1/admin/orders/{id}/status WITH CSRF should return 200 OK and advance status")
    void shouldAllowAdminStatusPatchWithCsrf() throws Exception {
        String json = """
                {
                    "status": "PREPARING"
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/orders/" + testOrder.getId() + "/status")
                        .with(user("admin.auth@example.com").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testOrder.getId()))
                .andExpect(jsonPath("$.status").value("PREPARING"));
    }
}
