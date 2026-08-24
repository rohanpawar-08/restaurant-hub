package com.restauranthub.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for Order entity operations.
 * Supports both customer ownership-isolated queries and restaurant-wide admin order queries.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Retrieves all orders placed by a specific user, sorted newest first.
     * Eagerly fetches items to eliminate N+1 query overhead during receipt rendering.
     */
    @EntityGraph(attributePaths = {"items", "items.food"})
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Retrieves a single order by ID belonging specifically to the requested user.
     * Guarantees that customer A cannot access customer B's order by ID.
     */
    @EntityGraph(attributePaths = {"items", "items.food"})
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    /**
     * Admin query: Retrieves all orders restaurant-wide, sorted newest first.
     * Eagerly fetches items to eliminate N+1 query overhead during admin dashboard rendering.
     */
    @EntityGraph(attributePaths = {"items", "items.food"})
    List<Order> findAllByOrderByCreatedAtDesc();

    /**
     * Admin query: Retrieves orders by lifecycle status, sorted newest first.
     */
    @EntityGraph(attributePaths = {"items", "items.food"})
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    /**
     * Admin query: Retrieves a single order by ID with eagerly loaded items.
     */
    @EntityGraph(attributePaths = {"items", "items.food"})
    Optional<Order> findWithItemsById(Long id);

    /**
     * Metrics query: Counts orders matching a specific status efficiently.
     */
    long countByStatus(OrderStatus status);
}
