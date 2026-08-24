package com.restauranthub.food;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for the Food entity.
 *
 * N+1 Query Optimization:
 * Although fetch = FetchType.LAZY is configured on the Food.category association,
 * our API response DTO (FoodResponse) needs the category details (id, name, slug).
 * If we fetched foods lazily, Hibernate would execute 1 SQL query to fetch N foods,
 * followed by N individual SQL queries to fetch each food's Category (the classic N+1 problem).
 *
 * By adding @EntityGraph(attributePaths = {"category"}), Spring Data instructs Hibernate
 * to generate a single SQL query with a LEFT OUTER JOIN on the categories table.
 */
@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    /**
     * Retrieves all foods, eagerly fetching their Category in a single JOIN query.
     */
    @Override
    @EntityGraph(attributePaths = {"category"})
    List<Food> findAll();

    /**
     * Retrieves a single food by ID with its Category eagerly fetched.
     */
    @Override
    @EntityGraph(attributePaths = {"category"})
    Optional<Food> findById(Long id);

    /**
     * Finds all foods belonging to a specific category ID.
     */
    @EntityGraph(attributePaths = {"category"})
    List<Food> findByCategoryId(Long categoryId);

    /**
     * Finds all foods marked as popular.
     */
    @EntityGraph(attributePaths = {"category"})
    List<Food> findByPopularTrue();

    /**
     * Finds all currently available foods.
     */
    @EntityGraph(attributePaths = {"category"})
    List<Food> findByAvailableTrue();

    /**
     * Finds all foods by category URL slug.
     */
    @EntityGraph(attributePaths = {"category"})
    List<Food> findByCategorySlug(String slug);

    /**
     * Finds all foods belonging to active categories only.
     */
    @EntityGraph(attributePaths = {"category"})
    List<Food> findByCategoryActiveTrue();

    /**
     * Finds all foods belonging to a specific category ID if that category is active.
     */
    @EntityGraph(attributePaths = {"category"})
    List<Food> findByCategoryIdAndCategoryActiveTrue(Long categoryId);

    /**
     * Finds all popular foods belonging to active categories.
     */
    @EntityGraph(attributePaths = {"category"})
    List<Food> findByPopularTrueAndCategoryActiveTrue();

    /**
     * Counts all available food items.
     */
    long countByAvailableTrue();

    /**
     * Checks if any food item references a given category ID.
     */
    boolean existsByCategoryId(Long categoryId);

    /**
     * Counts how many food items belong to a given category ID.
     */
    long countByCategoryId(Long categoryId);
}
