package com.restauranthub.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for Order entity operations.
 * Enforces customer ownership queries to prevent cross-account data leakage.
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
}
