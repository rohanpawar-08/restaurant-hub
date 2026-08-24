package com.restauranthub.admin;

import com.restauranthub.common.exception.GlobalExceptionHandler;
import com.restauranthub.order.OrderStatus;
import com.restauranthub.order.PaymentMethod;
import com.restauranthub.order.dto.OrderResponse;
import com.restauranthub.order.exception.InvalidOrderStatusTransitionException;
import com.restauranthub.order.exception.OrderNotFoundException;
import java.math.BigDecimal;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminOrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminOrderService adminOrderService;

    @InjectMocks
    private AdminOrderController adminOrderController;

    private OrderResponse mockOrderResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminOrderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockOrderResponse = new OrderResponse(
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
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders should return 200 OK with list of all orders")
    void shouldReturnAllOrders() throws Exception {
        when(adminOrderService.getOrdersByStatus(null)).thenReturn(List.of(mockOrderResponse));

        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders?status=CONFIRMED should return 200 OK with filtered orders")
    void shouldReturnFilteredOrders() throws Exception {
        when(adminOrderService.getOrdersByStatus(OrderStatus.CONFIRMED)).thenReturn(List.of(mockOrderResponse));

        mockMvc.perform(get("/api/v1/admin/orders?status=CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders/{id} should return 200 OK for existing order")
    void shouldReturnOrderById() throws Exception {
        when(adminOrderService.getOrderById(100L)).thenReturn(mockOrderResponse);

        mockMvc.perform(get("/api/v1/admin/orders/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders/{id} should return 404 Not Found when order is missing")
    void shouldReturn404WhenOrderMissing() throws Exception {
        when(adminOrderService.getOrderById(999L)).thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/api/v1/admin/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{id}/status should return 200 OK on valid status transition")
    void shouldUpdateOrderStatusSuccessfully() throws Exception {
        OrderResponse preparingOrder = new OrderResponse(
                100L,
                1L,
                OrderStatus.PREPARING,
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

        when(adminOrderService.updateOrderStatus(eq(100L), eq(OrderStatus.PREPARING))).thenReturn(preparingOrder);

        String json = """
                {
                    "status": "PREPARING"
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/orders/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("PREPARING"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{id}/status should return 409 Conflict on invalid transition")
    void shouldReturn409OnInvalidTransition() throws Exception {
        when(adminOrderService.updateOrderStatus(eq(100L), eq(OrderStatus.CONFIRMED)))
                .thenThrow(new InvalidOrderStatusTransitionException(OrderStatus.DELIVERED, OrderStatus.CONFIRMED));

        String json = """
                {
                    "status": "CONFIRMED"
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/orders/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cannot transition order from status 'DELIVERED' to 'CONFIRMED'."));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{id}/status should return 400 Bad Request on invalid/missing status")
    void shouldReturn400OnInvalidStatusPayload() throws Exception {
        String json = """
                {
                    "status": null
                }
                """;

        mockMvc.perform(patch("/api/v1/admin/orders/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
