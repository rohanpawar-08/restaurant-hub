package com.restauranthub.admin;

import com.restauranthub.admin.dto.DashboardSummaryResponse;
import com.restauranthub.food.FoodRepository;
import com.restauranthub.order.OrderRepository;
import com.restauranthub.order.OrderStatus;
import com.restauranthub.user.UserRepository;
import com.restauranthub.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    @DisplayName("Should gather accurate dashboard summary counts from repository methods")
    void shouldCalculateDashboardSummaryMetrics() {
        when(orderRepository.count()).thenReturn(25L);
        when(orderRepository.countByStatus(OrderStatus.CONFIRMED)).thenReturn(5L);
        when(orderRepository.countByStatus(OrderStatus.PREPARING)).thenReturn(4L);
        when(orderRepository.countByStatus(OrderStatus.READY)).thenReturn(3L);
        when(orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY)).thenReturn(2L);

        when(userRepository.countByRole(UserRole.CUSTOMER)).thenReturn(42L);
        when(foodRepository.count()).thenReturn(30L);
        when(foodRepository.countByAvailableTrue()).thenReturn(28L);

        DashboardSummaryResponse summary = adminDashboardService.getDashboardSummary();

        assertNotNull(summary);
        assertEquals(25L, summary.totalOrders());
        assertEquals(5L, summary.confirmedOrders());
        assertEquals(4L, summary.preparingOrders());
        assertEquals(3L, summary.readyOrders());
        assertEquals(2L, summary.outForDeliveryOrders());
        assertEquals(42L, summary.totalCustomers());
        assertEquals(30L, summary.totalFoods());
        assertEquals(28L, summary.activeFoods());
    }
}
