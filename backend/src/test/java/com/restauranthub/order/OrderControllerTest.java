package com.restauranthub.order;

import com.restauranthub.common.exception.GlobalExceptionHandler;
import com.restauranthub.order.dto.CreateOrderRequest;
import com.restauranthub.order.dto.OrderItemRequest;
import com.restauranthub.order.dto.OrderItemResponse;
import com.restauranthub.order.dto.OrderResponse;
import com.restauranthub.order.exception.FoodUnavailableException;
import com.restauranthub.order.exception.OrderNotFoundException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockPrincipal = () -> "rohan@example.com";
    }

    @Test
    @DisplayName("POST /api/v1/orders should return 201 Created on valid order payload")
    void shouldCreateOrderSuccessfully() throws Exception {
        OrderResponse response = new OrderResponse(
                100L,
                1L,
                OrderStatus.CONFIRMED,
                PaymentMethod.COD,
                new BigDecimal("250.00"),
                new BigDecimal("40.00"),
                new BigDecimal("290.00"),
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                "Apt 4B",
                "Mumbai",
                "Maharashtra",
                "400001",
                "Call before delivery",
                35,
                LocalDateTime.now(),
                List.of(
                        new OrderItemResponse(1L, 10L, "Paneer Butter Masala", new BigDecimal("250.00"), 1, new BigDecimal("250.00"))
                )
        );

        when(orderService.createOrder(any(CreateOrderRequest.class), eq("rohan@example.com"))).thenReturn(response);

        String json = """
                {
                    "customerName": "Rohan Pawar",
                    "customerEmail": "rohan@example.com",
                    "customerPhone": "9876543210",
                    "addressLine1": "123 MG Road",
                    "addressLine2": "Apt 4B",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "postalCode": "400001",
                    "deliveryInstructions": "Call before delivery",
                    "paymentMethod": "COD",
                    "items": [
                        {
                            "foodId": 10,
                            "quantity": 1
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentMethod").value("COD"))
                .andExpect(jsonPath("$.subtotal").value(250.00))
                .andExpect(jsonPath("$.deliveryFee").value(40.00))
                .andExpect(jsonPath("$.total").value(290.00))
                .andExpect(jsonPath("$.items[0].foodName").value("Paneer Butter Masala"));
    }

    @Test
    @DisplayName("POST /api/v1/orders should return 400 Bad Request on invalid customer info")
    void shouldReturn400OnInvalidCustomerInfo() throws Exception {
        String json = """
                {
                    "customerName": "R",
                    "customerEmail": "invalid-email",
                    "customerPhone": "123",
                    "addressLine1": "Too",
                    "city": "",
                    "state": "",
                    "postalCode": "12",
                    "paymentMethod": "COD",
                    "items": []
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("POST /api/v1/orders should return 400 Bad Request when food is unavailable")
    void shouldReturn400WhenFoodUnavailable() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class), eq("rohan@example.com")))
                .thenThrow(new FoodUnavailableException(10L, "Paneer Butter Masala"));

        String json = """
                {
                    "customerName": "Rohan Pawar",
                    "customerEmail": "rohan@example.com",
                    "customerPhone": "9876543210",
                    "addressLine1": "123 MG Road",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "postalCode": "400001",
                    "paymentMethod": "COD",
                    "items": [
                        {
                            "foodId": 10,
                            "quantity": 1
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Dish 'Paneer Butter Masala' (ID: 10) is currently unavailable."));
    }

    @Test
    @DisplayName("GET /api/v1/orders should return 200 OK with list of customer orders")
    void shouldReturnCustomerOrders() throws Exception {
        OrderResponse order = new OrderResponse(
                100L,
                1L,
                OrderStatus.CONFIRMED,
                PaymentMethod.COD,
                new BigDecimal("250.00"),
                new BigDecimal("40.00"),
                new BigDecimal("290.00"),
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                35,
                LocalDateTime.now(),
                List.of()
        );

        when(orderService.getCustomerOrders("rohan@example.com")).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/orders")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} should return 200 OK when owned")
    void shouldReturnOrderByIdWhenOwned() throws Exception {
        OrderResponse order = new OrderResponse(
                100L,
                1L,
                OrderStatus.CONFIRMED,
                PaymentMethod.COD,
                new BigDecimal("250.00"),
                new BigDecimal("40.00"),
                new BigDecimal("290.00"),
                "Rohan Pawar",
                "rohan@example.com",
                "9876543210",
                "123 MG Road",
                null,
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                35,
                LocalDateTime.now(),
                List.of()
        );

        when(orderService.getOrderById(100L, "rohan@example.com")).thenReturn(order);

        mockMvc.perform(get("/api/v1/orders/100")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} should return 404 Not Found when order is missing or not owned")
    void shouldReturn404WhenOrderNotOwned() throws Exception {
        when(orderService.getOrderById(999L, "rohan@example.com"))
                .thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/api/v1/orders/999")
                        .principal(mockPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/orders should return 400 Bad Request when payment method is unsupported")
    void shouldReturn400WhenPaymentMethodIsUnsupported() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class), eq("rohan@example.com")))
                .thenThrow(new com.restauranthub.order.exception.UnsupportedPaymentMethodException(
                        "Online payment is not available yet. Please choose Cash on Delivery."
                ));

        String json = """
                {
                    "customerName": "Rohan Pawar",
                    "customerEmail": "rohan@example.com",
                    "customerPhone": "9876543210",
                    "addressLine1": "123 MG Road",
                    "city": "Mumbai",
                    "state": "Maharashtra",
                    "postalCode": "400001",
                    "paymentMethod": "UPI",
                    "items": [
                        {
                            "foodId": 10,
                            "quantity": 1
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Online payment is not available yet. Please choose Cash on Delivery."));
    }
}
