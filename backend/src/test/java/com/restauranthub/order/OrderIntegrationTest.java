package com.restauranthub.order;

import com.restauranthub.category.Category;
import com.restauranthub.category.CategoryRepository;
import com.restauranthub.food.Food;
import com.restauranthub.food.FoodRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class OrderIntegrationTest {

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

    private User customer1;
    private User customer2;
    private Food activeFood1;
    private Food activeFood2;
    private Food unavailableFood;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Create Customer 1
        customer1 = userRepository.findByEmailIgnoreCase("customer1@example.com")
                .orElseGet(() -> userRepository.save(new User(
                        "Customer One",
                        "customer1@example.com",
                        "9900000001",
                        "hashed_pwd",
                        UserRole.CUSTOMER
                )));

        // Create Customer 2
        customer2 = userRepository.findByEmailIgnoreCase("customer2@example.com")
                .orElseGet(() -> userRepository.save(new User(
                        "Customer Two",
                        "customer2@example.com",
                        "9900000002",
                        "hashed_pwd",
                        UserRole.CUSTOMER
                )));

        Category cat = categoryRepository.findBySlug("test-category")
                .orElseGet(() -> categoryRepository.save(new Category("Test Category", "test-category")));

        activeFood1 = foodRepository.save(new Food(
                "Butter Chicken",
                "Delicious creamy chicken curry",
                new BigDecimal("350.00"),
                new BigDecimal("4.8"),
                "butter_chicken.jpg",
                false,
                true,
                true,
                cat
        ));

        activeFood2 = foodRepository.save(new Food(
                "Garlic Naan",
                "Clay oven baked flatbread",
                new BigDecimal("60.00"),
                new BigDecimal("4.7"),
                "garlic_naan.jpg",
                true,
                false,
                true,
                cat
        ));

        unavailableFood = foodRepository.save(new Food(
                "Seasonal Lobster",
                "Catch of the day",
                new BigDecimal("990.00"),
                new BigDecimal("4.9"),
                "lobster.jpg",
                false,
                false,
                false,
                cat
        ));
    }

    @Test
    @DisplayName("Unauthenticated POST /api/v1/orders should return 401 Unauthorized")
    void shouldRejectUnauthenticatedOrderPost() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authenticated POST /api/v1/orders WITHOUT CSRF should return 403 Forbidden")
    void shouldRejectOrderPostWithoutCsrf() throws Exception {
        String json = String.format("""
                {
                    "customerName": "Customer One",
                    "customerEmail": "customer1@example.com",
                    "customerPhone": "9900000001",
                    "addressLine1": "123 Marine Drive",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "postalCode": "400020",
                    "paymentMethod": "COD",
                    "items": [
                        {"foodId": %d, "quantity": 1}
                    ]
                }
                """, activeFood1.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .with(user("customer1@example.com").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Authenticated customer creates order with server-calculated price and snapshots -> 201 Created")
    void shouldCreateOrderSuccessfullyAndPersistSnapshots() throws Exception {
        // activeFood1 = 350.00 * 1 = 350.00
        // activeFood2 = 60.00 * 2 = 120.00
        // subtotal = 470.00 (< 500.00 -> delivery fee = 40.00, grand total = 510.00)
        String json = String.format("""
                {
                    "customerName": "Customer One",
                    "customerEmail": "customer1@example.com",
                    "customerPhone": "9900000001",
                    "addressLine1": "123 Marine Drive",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "postalCode": "400020",
                    "paymentMethod": "COD",
                    "items": [
                        {"foodId": %d, "quantity": 1},
                        {"foodId": %d, "quantity": 2}
                    ]
                }
                """, activeFood1.getId(), activeFood2.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .with(user("customer1@example.com").roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentMethod").value("COD"))
                .andExpect(jsonPath("$.subtotal").value(470.00))
                .andExpect(jsonPath("$.deliveryFee").value(40.00))
                .andExpect(jsonPath("$.total").value(510.00))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].foodName").value("Butter Chicken"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(350.00))
                .andExpect(jsonPath("$.items[0].lineTotal").value(350.00))
                .andExpect(jsonPath("$.items[1].foodName").value("Garlic Naan"))
                .andExpect(jsonPath("$.items[1].unitPrice").value(60.00))
                .andExpect(jsonPath("$.items[1].lineTotal").value(120.00));
    }

    @Test
    @DisplayName("Ordering unavailable food should return 400 Bad Request")
    void shouldRejectUnavailableFoodOrder() throws Exception {
        String json = String.format("""
                {
                    "customerName": "Customer One",
                    "customerEmail": "customer1@example.com",
                    "customerPhone": "9900000001",
                    "addressLine1": "123 Marine Drive",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "postalCode": "400020",
                    "paymentMethod": "COD",
                    "items": [
                        {"foodId": %d, "quantity": 1}
                    ]
                }
                """, unavailableFood.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .with(user("customer1@example.com").roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Customer can only access their own orders; cannot fetch another customer's order (404)")
    void shouldEnforceCustomerIsolation() throws Exception {
        // 1. Create order for Customer 1
        String json1 = String.format("""
                {
                    "customerName": "Customer One",
                    "customerEmail": "customer1@example.com",
                    "customerPhone": "9900000001",
                    "addressLine1": "123 Marine Drive",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "postalCode": "400020",
                    "paymentMethod": "COD",
                    "items": [
                        {"foodId": %d, "quantity": 2}
                    ]
                }
                """, activeFood1.getId());

        String responseJson = mockMvc.perform(post("/api/v1/orders")
                        .with(user("customer1@example.com").roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json1))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract order ID
        Long orderId1 = com.jayway.jsonpath.JsonPath.parse(responseJson).read("$.id", Long.class);

        // 2. Customer 1 can retrieve their own order
        mockMvc.perform(get("/api/v1/orders/" + orderId1)
                        .with(user("customer1@example.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId1));

        // 3. Customer 2 CANNOT retrieve Customer 1's order -> 404 Not Found
        mockMvc.perform(get("/api/v1/orders/" + orderId1)
                        .with(user("customer2@example.com").roles("CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        // 4. Customer 2's order history does NOT contain Customer 1's order
        mockMvc.perform(get("/api/v1/orders")
                        .with(user("customer2@example.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Submitting order with paymentMethod = UPI should return 400 Bad Request and create no Order or OrderItem rows")
    void shouldRejectUpiOrderAndEnsureNoRowsPersisted() throws Exception {
        long initialOrderCount = orderRepository.count();

        String json = String.format("""
                {
                    "customerName": "Customer One",
                    "customerEmail": "customer1@example.com",
                    "customerPhone": "9900000001",
                    "addressLine1": "123 Marine Drive",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "postalCode": "400020",
                    "paymentMethod": "UPI",
                    "items": [
                        {"foodId": %d, "quantity": 1}
                    ]
                }
                """, activeFood1.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .with(user("customer1@example.com").roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Online payment is not available yet. Please choose Cash on Delivery."));

        assertEquals(initialOrderCount, orderRepository.count(), "Order count must remain unchanged when payment method is rejected");
    }

    @Test
    @DisplayName("Submitting order with paymentMethod = CARD should return 400 Bad Request and create no Order or OrderItem rows")
    void shouldRejectCardOrderAndEnsureNoRowsPersisted() throws Exception {
        long initialOrderCount = orderRepository.count();

        String json = String.format("""
                {
                    "customerName": "Customer One",
                    "customerEmail": "customer1@example.com",
                    "customerPhone": "9900000001",
                    "addressLine1": "123 Marine Drive",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "postalCode": "400020",
                    "paymentMethod": "CARD",
                    "items": [
                        {"foodId": %d, "quantity": 1}
                    ]
                }
                """, activeFood1.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .with(user("customer1@example.com").roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Online payment is not available yet. Please choose Cash on Delivery."));

        assertEquals(initialOrderCount, orderRepository.count(), "Order count must remain unchanged when payment method is rejected");
    }
}
