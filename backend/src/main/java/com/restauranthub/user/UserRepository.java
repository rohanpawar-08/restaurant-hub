package com.restauranthub.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email, ignoring case.
     *
     * @param email user email
     * @return Optional containing User if found
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Checks if a user already exists with the given email, ignoring case.
     *
     * @param email user email
     * @return true if email exists
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Checks if a user already exists with the given phone number.
     *
     * @param phone user phone
     * @return true if phone exists
     */
    boolean existsByPhone(String phone);
}
