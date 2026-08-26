package com.restauranthub.settings;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for managing RestaurantSettings persistence.
 */
@Repository
public interface RestaurantSettingsRepository extends JpaRepository<RestaurantSettings, Long> {

    /**
     * Retrieves the primary active restaurant settings record (topmost or id=1).
     */
    @Query("SELECT s FROM RestaurantSettings s ORDER BY s.id ASC LIMIT 1")
    Optional<RestaurantSettings> findActiveSettings();
}
