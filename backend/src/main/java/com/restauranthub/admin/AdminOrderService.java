package com.restauranthub.admin;

import com.restauranthub.order.Order;
import com.restauranthub.order.OrderRepository;
import com.restauranthub.order.OrderStatus;
import com.restauranthub.order.dto.OrderResponse;
import com.restauranthub.order.exception.InvalidOrderStatusTransitionException;
import com.restauranthub.order.exception.OrderNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service governing administrative restaurant-wide order operations and state machine transitions.
 *
 * Status Transition State Machine:
 * - CONFIRMED       -> PREPARING, CANCELLED
 * - PREPARING       -> READY, CANCELLED
 * - READY           -> OUT_FOR_DELIVERY
 * - OUT_FOR_DELIVERY -> DELIVERED
 * - DELIVERED, CANCELLED: Terminal states (no further transitions allowed)
 */
@Service
public class AdminOrderService {

    private final OrderRepository orderRepository;

    public AdminOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Retrieves all restaurant orders sorted newest first.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves orders filtered by status or all orders if status is null.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        if (status == null) {
            return getAllOrders();
        }
        return orderRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves a single order by ID for administrator inspection.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderResponse.fromEntity(order);
    }

    /**
     * Updates an order's lifecycle status according to the restaurant state machine rules.
     * Throws InvalidOrderStatusTransitionException (HTTP 409 Conflict) on disallowed transitions.
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Target order status cannot be null.");
        }

        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus currentStatus = order.getStatus();

        if (!isValidTransition(currentStatus, newStatus)) {
            throw new InvalidOrderStatusTransitionException(currentStatus, newStatus);
        }

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    /**
     * Evaluates whether a status transition is permitted by the domain state machine.
     */
    public boolean isValidTransition(OrderStatus current, OrderStatus next) {
        if (current == null || next == null || current == next) {
            return false;
        }

        return switch (current) {
            case CONFIRMED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.READY || next == OrderStatus.CANCELLED;
            case READY -> next == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
