package com.restauranthub.category;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository interface for the Category entity.
 * Provides standard CRUD operations, pagination, sorting, and derived query methods.
 * Spring dynamically creates the proxy implementation class at runtime.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a single Category by its unique slug.
     * Returns an Optional to cleanly represent presence or absence without null checks.
     *
     * @param slug unique URL slug of the category
     * @return Optional containing the Category if found, or Optional.empty()
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Checks whether a Category exists with the given slug.
     * Spring Data JPA derives an optimized 'SELECT count(...) > 0' or 'EXISTS' SQL query.
     *
     * @param slug unique URL slug to check
     * @return true if a matching category exists, false otherwise
     */
    boolean existsBySlug(String slug);

    /**
     * Finds all currently active categories.
     */
    java.util.List<Category> findByActiveTrue();
}
