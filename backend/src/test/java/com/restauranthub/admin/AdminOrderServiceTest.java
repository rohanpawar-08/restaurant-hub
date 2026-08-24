package com.restauranthub.admin;

import com.restauranthub.order.Order;
import com.restauranthub.order.OrderRepository;
import com.restauranthub.order.OrderStatus;
import com.restauranthub.order.PaymentMethod;
import com.restauranthub.order.dto.OrderResponse;
import com.restauranthub.order.exception.InvalidOrderStatusTransitionException;
import com.restauranthub.order.exception.OrderNotFoundException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminOrderService adminOrderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(100L);
        testOrder.setStatus(OrderStatus.CONFIRMED);
        testOrder.setPaymentMethod(PaymentMethod.COD);
        testOrder.setSubtotal(new BigDecimal("300.00"));
        testOrder.setDeliveryFee(new BigDecimal("40.00"));
        testOrder.setTotal(new BigDecimal("340.00"));
        testOrder.setCustomerName("Rohan Pawar");
        testOrder.setCustomerEmail("rohan@example.com");
        testOrder.setCustomerPhone("9876543210");
        testOrder.setAddressLine1("123 MG Road");
        testOrder.setCity("Mumbai");
        testOrder.setState("Maharashtra");
        testOrder.setPostalCode("400001");
        testOrder.setEstimatedDeliveryMinutes(35);
    }

    @Test
    @DisplayName("Should retrieve all orders restaurant-wide sorted newest first")
    void shouldGetAllOrders() {
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(testOrder));

        List<OrderResponse> orders = adminOrderService.getAllOrders();

        assertEquals(1, orders.size());
        assertEquals(100L, orders.get(0).id());
        assertEquals(OrderStatus.CONFIRMED, orders.get(0).status());
    }

    @Test
    @DisplayName("Should retrieve orders filtered by status")
    void shouldGetOrdersByStatus() {
        when(orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.CONFIRMED)).thenReturn(List.of(testOrder));

        List<OrderResponse> orders = adminOrderService.getOrdersByStatus(OrderStatus.CONFIRMED);

        assertEquals(1, orders.size());
        assertEquals(OrderStatus.CONFIRMED, orders.get(0).status());
    }

    @Test
    @DisplayName("Should return all orders when status filter is null")
    void shouldReturnAllOrdersWhenStatusFilterIsNull() {
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(testOrder));

        List<OrderResponse> orders = adminOrderService.getOrdersByStatus(null);

        assertEquals(1, orders.size());
    }

    @Test
    @DisplayName("Should retrieve single order by ID for admin inspection")
    void shouldGetOrderById() {
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(testOrder));

        OrderResponse response = adminOrderService.getOrderById(100L);

        assertNotNull(response);
        assertEquals(100L, response.id());
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order does not exist")
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findWithItemsById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> adminOrderService.getOrderById(999L));
    }

    @Test
    @DisplayName("Valid transition: CONFIRMED -> PREPARING")
    void shouldTransitionConfirmedToPreparing() {
        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse updated = adminOrderService.updateOrderStatus(100L, OrderStatus.PREPARING);

        assertEquals(OrderStatus.PREPARING, updated.status());
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Valid transition: PREPARING -> READY")
    void shouldTransitionPreparingToReady() {
        testOrder.setStatus(OrderStatus.PREPARING);
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse updated = adminOrderService.updateOrderStatus(100L, OrderStatus.READY);

        assertEquals(OrderStatus.READY, updated.status());
    }

    @Test
    @DisplayName("Valid transition: READY -> OUT_FOR_DELIVERY")
    void shouldTransitionReadyToOutForDelivery() {
        testOrder.setStatus(OrderStatus.READY);
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse updated = adminOrderService.updateOrderStatus(100L, OrderStatus.OUT_FOR_DELIVERY);

        assertEquals(OrderStatus.OUT_FOR_DELIVERY, updated.status());
    }

    @Test
    @DisplayName("Valid transition: OUT_FOR_DELIVERY -> DELIVERED")
    void shouldTransitionOutForDeliveryToDelivered() {
        testOrder.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse updated = adminOrderService.updateOrderStatus(100L, OrderStatus.DELIVERED);

        assertEquals(OrderStatus.DELIVERED, updated.status());
    }

    @Test
    @DisplayName("Valid transition: CONFIRMED -> CANCELLED")
    void shouldTransitionConfirmedToCancelled() {
        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse updated = adminOrderService.updateOrderStatus(100L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, updated.status());
    }

    @Test
    @DisplayName("Valid transition: PREPARING -> CANCELLED")
    void shouldTransitionPreparingToCancelled() {
        testOrder.setStatus(OrderStatus.PREPARING);
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse updated = adminOrderService.updateOrderStatus(100L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, updated.status());
    }

    @Test
    @DisplayName("Invalid transition: DELIVERED -> PREPARING throws InvalidOrderStatusTransitionException")
    void shouldRejectDeliveredToPreparing() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(testOrder));

        InvalidOrderStatusTransitionException ex = assertThrows(
                InvalidOrderStatusTransitionException.class,
                () -> adminOrderService.updateOrderStatus(100L, OrderStatus.PREPARING)
        );

        assertEquals(OrderStatus.DELIVERED, ex.getCurrentStatus());
        assertEquals(OrderStatus.PREPARING, ex.getAttemptedStatus());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Invalid transition: DELIVERED -> CANCELLED throws InvalidOrderStatusTransitionException")
    void shouldRejectDeliveredToCancelled() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(testOrder));

        assertThrows(
                InvalidOrderStatusTransitionException.class,
                () -> adminOrderService.updateOrderStatus(100L, OrderStatus.CANCELLED)
        );
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Invalid transition: CANCELLED -> CONFIRMED throws InvalidOrderStatusTransitionException")
    void shouldRejectCancelledToConfirmed() {
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(testOrder));

        assertThrows(
                InvalidOrderStatusTransitionException.class,
                () -> adminOrderService.updateOrderStatus(100L, OrderStatus.CONFIRMED)
        );
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Invalid transition: READY -> CONFIRMED throws InvalidOrderStatusTransitionException")
    void shouldRejectReadyToConfirmed() {
        testOrder.setStatus(OrderStatus.READY);
        when(orderRepository.findWithItemsById(100L)).thenReturn(Optional.of(testOrder));

        assertThrows(
                InvalidOrderStatusTransitionException.class,
                () -> adminOrderService.updateOrderStatus(100L, OrderStatus.CONFIRMED)
        );
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when target status is null")
    void shouldThrowWhenTargetStatusIsNull() {
        assertThrows(IllegalArgumentException.class, () -> adminOrderService.updateOrderStatus(100L, null));
    }
}
