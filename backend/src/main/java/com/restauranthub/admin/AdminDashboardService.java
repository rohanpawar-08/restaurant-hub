package com.restauranthub.admin;

import com.restauranthub.admin.dto.DashboardSummaryResponse;
import com.restauranthub.food.FoodRepository;
import com.restauranthub.order.OrderRepository;
import com.restauranthub.order.OrderStatus;
import com.restauranthub.user.UserRepository;
import com.restauranthub.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service providing operational dashboard metrics for restaurant administrators.
 * Uses lightweight SQL COUNT queries to prevent memory bottlenecks.
 */
@Service
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;

    public AdminDashboardService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            FoodRepository foodRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
    }

    /**
     * Gathers operational metrics for the admin dashboard summary cards.
     */
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        long totalOrders = orderRepository.count();
        long confirmedOrders = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long preparingOrders = orderRepository.countByStatus(OrderStatus.PREPARING);
        long readyOrders = orderRepository.countByStatus(OrderStatus.READY);
        long outForDeliveryOrders = orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY);

        long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);
        long totalFoods = foodRepository.count();
        long activeFoods = foodRepository.countByAvailableTrue();

        return new DashboardSummaryResponse(
                totalOrders,
                confirmedOrders,
                preparingOrders,
                readyOrders,
                outForDeliveryOrders,
                totalCustomers,
                totalFoods,
                activeFoods
        );
    }
}
