package com.restauranthub.order;

import com.restauranthub.category.Category;
import com.restauranthub.food.Food;
import com.restauranthub.food.FoodRepository;
import com.restauranthub.food.exception.FoodNotFoundException;
import com.restauranthub.order.dto.CreateOrderRequest;
import com.restauranthub.order.dto.OrderItemRequest;
import com.restauranthub.order.dto.OrderResponse;
import com.restauranthub.order.exception.FoodUnavailableException;
import com.restauranthub.order.exception.OrderNotFoundException;
import com.restauranthub.user.User;
import com.restauranthub.user.UserRepository;
import com.restauranthub.user.UserRole;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.restauranthub.settings.RestaurantSettingsService settingsService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Food testFood1;
    private Food testFood2;
    private Food unavailableFood;
    private Category category;
    private com.restauranthub.settings.RestaurantSettings defaultSettings;

    @BeforeEach
    void setUp() {
        testUser = new User("Rohan Pawar", "rohan@example.com", "9876543210", "hashed_pwd", UserRole.CUSTOMER);
        testUser.setId(1L);

        category = new Category("Main Course", "main-course");
        category.setId(10L);

        testFood1 = new Food("Paneer Butter Masala", "Rich curry", new BigDecimal("250.00"), new BigDecimal("4.5"), "paneer.jpg", true, true, true, category);
        testFood1.setId(100L);

        testFood2 = new Food("Butter Naan", "Crisp bread", new BigDecimal("40.00"), new BigDecimal("4.8"), "naan.jpg", true, false, true, category);
        testFood2.setId(200L);

        unavailableFood = new Food("Seasonal Mango Shake", "Cold beverage", new BigDecimal("120.00"), new BigDecimal("4.9"), "mango.jpg", true, false, false, category);
        unavailableFood.setId(300L);

        defaultSettings = new com.restauranthub.settings.RestaurantSettings();
        defaultSettings.setId(1L);
        defaultSettings.setRestaurantName("RestaurantHub");
        defaultSettings.setDeliveryFee(new BigDecimal("40.00"));
        defaultSettings.setFreeDeliveryThreshold(new BigDecimal("500.00"));
        defaultSettings.setEstimatedDeliveryMinutes(35);
        defaultSettings.setAcceptingOrders(true);

        org.mockito.Mockito.lenient().when(settingsService.getActiveSettings()).thenReturn(defaultSettings);
    }

    @Test
    @DisplayName("Should successfully create order with server-calculated totals below free delivery threshold")
    void shouldCreateOrderWithStandardDeliveryFee() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        when(foodRepository.findById(100L)).thenReturn(Optional.of(testFood1));
        when(foodRepository.findById(200L)).thenReturn(Optional.of(testFood2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(500L);
            long itemId = 1000L;
            for (OrderItem item : o.getItems()) {
                item.setId(itemId++);
            }
            return o;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                "Apt 4B",
                "Mumbai",
                "Maharashtra",
                "400001",
                "Ring the doorbell",
                PaymentMethod.COD,
                List.of(
                        new OrderItemRequest(100L, 1), // 250.00
                        new OrderItemRequest(200L, 2)  // 80.00 -> subtotal = 330.00 (< 500 -> delivery = 40.00, total = 370.00)
                )
        );

        OrderResponse response = orderService.createOrder(request, "rohan@example.com");

        assertNotNull(response);
        assertEquals(500L, response.id());
        assertEquals(1L, response.userId());
        assertEquals(OrderStatus.CONFIRMED, response.status());
        assertEquals(PaymentMethod.COD, response.paymentMethod());
        assertEquals(new BigDecimal("330.00"), response.subtotal());
        assertEquals(new BigDecimal("40.00"), response.deliveryFee());
        assertEquals(new BigDecimal("370.00"), response.total());
        assertEquals(2, response.items().size());
        assertEquals("Paneer Butter Masala", response.items().get(0).foodName());
        assertEquals(new BigDecimal("250.00"), response.items().get(0).unitPrice());
        assertEquals(1, response.items().get(0).quantity());
        assertEquals(new BigDecimal("250.00"), response.items().get(0).lineTotal());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should successfully apply free delivery when subtotal reaches ₹500.00 threshold")
    void shouldApplyFreeDeliveryWhenSubtotalReachesThreshold() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        when(foodRepository.findById(100L)).thenReturn(Optional.of(testFood1)); // 250 * 2 = 500
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(501L);
            return o;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of(new OrderItemRequest(100L, 2)) // 250 * 2 = 500.00
        );

        OrderResponse response = orderService.createOrder(request, "rohan@example.com");

        assertNotNull(response);
        assertEquals(new BigDecimal("500.00"), response.subtotal());
        assertEquals(new BigDecimal("0.00"), response.deliveryFee());
        assertEquals(new BigDecimal("500.00"), response.total());
    }

    @Test
    @DisplayName("Should throw FoodNotFoundException when ordered food ID does not exist")
    void shouldThrowWhenFoodNotFound() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        when(foodRepository.findById(999L)).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of(new OrderItemRequest(999L, 1))
        );

        assertThrows(FoodNotFoundException.class, () ->
                orderService.createOrder(request, "rohan@example.com")
        );
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw FoodUnavailableException when ordered food belongs to an inactive category")
    void shouldThrowWhenCategoryIsInactive() {
        Category inactiveCategory = new Category("Seasonal Specials", "seasonal-specials");
        inactiveCategory.setId(20L);
        inactiveCategory.setActive(false);

        Food foodInInactiveCategory = new Food("Summer Thali", "Festive meal", new BigDecimal("350.00"), new BigDecimal("4.7"), "thali.jpg", true, true, true, inactiveCategory);
        foodInInactiveCategory.setId(400L);

        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        when(foodRepository.findById(400L)).thenReturn(Optional.of(foodInInactiveCategory));

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of(new OrderItemRequest(400L, 1))
        );

        FoodUnavailableException ex = assertThrows(FoodUnavailableException.class, () ->
                orderService.createOrder(request, "rohan@example.com")
        );
        assertEquals("Dish 'Summer Thali' (ID: 400) is currently unavailable.", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw FoodUnavailableException when food available flag is null")
    void shouldThrowWhenFoodAvailableIsNull() {
        Category activeCategory = new Category("Main Course", "main-course");
        activeCategory.setId(10L);

        Food foodWithNullAvailable = new Food("Mystery Dish", "Description", new BigDecimal("200.00"), new BigDecimal("4.0"), "mystery.jpg", true, false, true, activeCategory);
        foodWithNullAvailable.setId(401L);
        foodWithNullAvailable.setAvailable(null);

        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        when(foodRepository.findById(401L)).thenReturn(Optional.of(foodWithNullAvailable));

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of(new OrderItemRequest(401L, 1))
        );

        assertThrows(FoodUnavailableException.class, () ->
                orderService.createOrder(request, "rohan@example.com")
        );
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw FoodUnavailableException when food category is null")
    void shouldThrowWhenFoodCategoryIsNull() {
        Food foodWithNullCategory = new Food("Uncategorized Dish", "Description", new BigDecimal("200.00"), new BigDecimal("4.0"), "dish.jpg", true, false, true, null);
        foodWithNullCategory.setId(402L);

        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        when(foodRepository.findById(402L)).thenReturn(Optional.of(foodWithNullCategory));

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of(new OrderItemRequest(402L, 1))
        );

        assertThrows(FoodUnavailableException.class, () ->
                orderService.createOrder(request, "rohan@example.com")
        );
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw FoodUnavailableException when category active flag is null")
    void shouldThrowWhenCategoryActiveIsNull() {
        Category categoryWithNullActive = new Category("Special Category", "special");
        categoryWithNullActive.setId(30L);
        categoryWithNullActive.setActive(null);

        Food foodInNullActiveCategory = new Food("Special Dish", "Description", new BigDecimal("200.00"), new BigDecimal("4.0"), "special.jpg", true, false, true, categoryWithNullActive);
        foodInNullActiveCategory.setId(403L);

        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        when(foodRepository.findById(403L)).thenReturn(Optional.of(foodInNullActiveCategory));

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of(new OrderItemRequest(403L, 1))
        );

        assertThrows(FoodUnavailableException.class, () ->
                orderService.createOrder(request, "rohan@example.com")
        );
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should succeed when previously inactive category is reactivated")
    void shouldSucceedWhenCategoryIsReactivated() {
        Category seasonalCategory = new Category("Seasonal Specials", "seasonal-specials");
        seasonalCategory.setId(20L);
        seasonalCategory.setActive(true); // Reactivated

        Food foodInSeasonalCategory = new Food("Summer Thali", "Festive meal", new BigDecimal("350.00"), new BigDecimal("4.7"), "thali.jpg", true, true, true, seasonalCategory);
        foodInSeasonalCategory.setId(400L);

        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        when(foodRepository.findById(400L)).thenReturn(Optional.of(foodInSeasonalCategory));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(600L);
            return o;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of(new OrderItemRequest(400L, 1))
        );

        OrderResponse response = orderService.createOrder(request, "rohan@example.com");
        assertNotNull(response);
        assertEquals(600L, response.id());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw FoodUnavailableException when ordered food item is marked unavailable")
    void shouldThrowWhenFoodUnavailable() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        when(foodRepository.findById(300L)).thenReturn(Optional.of(unavailableFood));

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of(new OrderItemRequest(300L, 1))
        );

        assertThrows(FoodUnavailableException.class, () ->
                orderService.createOrder(request, "rohan@example.com")
        );
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when order items list is empty")
    void shouldThrowWhenItemsEmpty() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of()
        );

        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(request, "rohan@example.com")
        );
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when authenticated user cannot be resolved")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(
                "Unknown",
                "unknown@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of(new OrderItemRequest(100L, 1))
        );

        assertThrows(BadCredentialsException.class, () ->
                orderService.createOrder(request, "unknown@example.com")
        );
    }

    @Test
    @DisplayName("Should retrieve customer orders sorted newest first")
    void shouldGetCustomerOrders() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));

        Order order1 = new Order();
        order1.setId(10L);
        order1.setUser(testUser);
        order1.setStatus(OrderStatus.CONFIRMED);
        order1.setPaymentMethod(PaymentMethod.COD);
        order1.setSubtotal(new BigDecimal("300.00"));
        order1.setDeliveryFee(new BigDecimal("40.00"));
        order1.setTotal(new BigDecimal("340.00"));

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order1));

        List<OrderResponse> results = orderService.getCustomerOrders("rohan@example.com");

        assertEquals(1, results.size());
        assertEquals(10L, results.get(0).id());
        assertEquals(new BigDecimal("340.00"), results.get(0).total());
    }

    @Test
    @DisplayName("Should retrieve single owned order by ID")
    void shouldGetSingleOwnedOrder() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));

        Order order = new Order();
        order.setId(10L);
        order.setUser(testUser);
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setSubtotal(new BigDecimal("600.00"));
        order.setDeliveryFee(new BigDecimal("0.00"));
        order.setTotal(new BigDecimal("600.00"));

        when(orderRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderById(10L, "rohan@example.com");

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals(OrderStatus.DELIVERED, result.status());
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order does not belong to customer")
    void shouldThrowWhenOrderNotOwned() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () ->
                orderService.getOrderById(999L, "rohan@example.com")
        );
    }

    @Test
    @DisplayName("Should throw UnsupportedPaymentMethodException when paymentMethod is UPI")
    void shouldThrowWhenPaymentMethodIsUPI() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.UPI,
                List.of(new OrderItemRequest(100L, 1))
        );

        com.restauranthub.order.exception.UnsupportedPaymentMethodException ex = assertThrows(
                com.restauranthub.order.exception.UnsupportedPaymentMethodException.class,
                () -> orderService.createOrder(request, "rohan@example.com")
        );

        assertEquals("Online payment is not available yet. Please choose Cash on Delivery.", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw UnsupportedPaymentMethodException when paymentMethod is CARD")
    void shouldThrowWhenPaymentMethodIsCARD() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.CARD,
                List.of(new OrderItemRequest(100L, 1))
        );

        com.restauranthub.order.exception.UnsupportedPaymentMethodException ex = assertThrows(
                com.restauranthub.order.exception.UnsupportedPaymentMethodException.class,
                () -> orderService.createOrder(request, "rohan@example.com")
        );

        assertEquals("Online payment is not available yet. Please choose Cash on Delivery.", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when paymentMethod is null")
    void shouldThrowWhenPaymentMethodIsNull() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                null,
                List.of(new OrderItemRequest(100L, 1))
        );

        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(request, "rohan@example.com")
        );
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw OrdersClosedException when restaurant is not accepting orders")
    void shouldThrowWhenRestaurantNotAcceptingOrders() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        com.restauranthub.settings.RestaurantSettings closedSettings = new com.restauranthub.settings.RestaurantSettings();
        closedSettings.setAcceptingOrders(false);
        when(settingsService.getActiveSettings()).thenReturn(closedSettings);

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of(new OrderItemRequest(100L, 1))
        );

        com.restauranthub.order.exception.OrdersClosedException ex = assertThrows(
                com.restauranthub.order.exception.OrdersClosedException.class,
                () -> orderService.createOrder(request, "rohan@example.com")
        );

        assertEquals("We're currently not accepting online orders.", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should use dynamic delivery fee and threshold configured in RestaurantSettings")
    void shouldUseDynamicDeliveryFeeAndThreshold() {
        when(userRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(testUser));
        when(foodRepository.findById(100L)).thenReturn(Optional.of(testFood1)); // 250.00
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.restauranthub.settings.RestaurantSettings customSettings = new com.restauranthub.settings.RestaurantSettings();
        customSettings.setAcceptingOrders(true);
        customSettings.setDeliveryFee(new BigDecimal("75.00"));
        customSettings.setFreeDeliveryThreshold(new BigDecimal("800.00"));
        customSettings.setEstimatedDeliveryMinutes(45);
        when(settingsService.getActiveSettings()).thenReturn(customSettings);

        CreateOrderRequest request = new CreateOrderRequest(
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                PaymentMethod.COD,
                List.of(new OrderItemRequest(100L, 1)) // 250.00 < 800.00 -> delivery fee should be 75.00
        );

        OrderResponse response = orderService.createOrder(request, "rohan@example.com");

        assertNotNull(response);
        assertEquals(new BigDecimal("250.00"), response.subtotal());
        assertEquals(new BigDecimal("75.00"), response.deliveryFee());
        assertEquals(new BigDecimal("325.00"), response.total());
        assertEquals(45, response.estimatedDeliveryMinutes());
    }
}
