package com.restauranthub.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save and retrieve a user by email ignoring case")
    void shouldSaveAndFindByEmailIgnoreCase() {
        // Arrange
        User user = new User(
                "Jane Doe",
                "jane.doe@example.com",
                "9876543299",
                "$2a$10$encryptedHashValueForTestOnly",
                UserRole.CUSTOMER
        );
        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByEmailIgnoreCase("JANE.DOE@EXAMPLE.COM");

        // Assert
        assertTrue(found.isPresent(), "User should be found regardless of email case");
        assertEquals("Jane Doe", found.get().getFullName());
        assertEquals("9876543299", found.get().getPhone());
        assertEquals(UserRole.CUSTOMER, found.get().getRole());
        assertTrue(found.get().getActive());
    }

    @Test
    @DisplayName("Should check existence by email ignoring case")
    void shouldCheckExistsByEmailIgnoreCase() {
        // Arrange
        User user = new User(
                "Alex Smith",
                "alex.smith@example.com",
                "9876543298",
                "$2a$10$encryptedHashValueForTestOnly",
                UserRole.CUSTOMER
        );
        userRepository.save(user);

        // Act & Assert
        assertTrue(userRepository.existsByEmailIgnoreCase("alex.smith@example.com"));
        assertTrue(userRepository.existsByEmailIgnoreCase("ALEX.SMITH@EXAMPLE.COM"));
        assertFalse(userRepository.existsByEmailIgnoreCase("nonexistent@example.com"));
    }

    @Test
    @DisplayName("Should check existence by phone")
    void shouldCheckExistsByPhone() {
        // Arrange
        User user = new User(
                "Sam Wilson",
                "sam.wilson@example.com",
                "9876543297",
                "$2a$10$encryptedHashValueForTestOnly",
                UserRole.CUSTOMER
        );
        userRepository.save(user);

        // Act & Assert
        assertTrue(userRepository.existsByPhone("9876543297"));
        assertFalse(userRepository.existsByPhone("9999999999"));
    }

    @Test
    @DisplayName("Should reject duplicate email due to unique constraint")
    void shouldRejectDuplicateEmail() {
        // Arrange
        User user1 = new User(
                "User One",
                "duplicate@example.com",
                "9876543296",
                "$2a$10$hash1",
                UserRole.CUSTOMER
        );
        userRepository.saveAndFlush(user1);

        User user2 = new User(
                "User Two",
                "duplicate@example.com",
                "9876543295",
                "$2a$10$hash2",
                UserRole.CUSTOMER
        );

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }

    @Test
    @DisplayName("Should reject duplicate phone due to unique constraint")
    void shouldRejectDuplicatePhone() {
        // Arrange
        User user1 = new User(
                "User One",
                "user1@example.com",
                "9876543294",
                "$2a$10$hash1",
                UserRole.CUSTOMER
        );
        userRepository.saveAndFlush(user1);

        User user2 = new User(
                "User Two",
                "user2@example.com",
                "9876543294",
                "$2a$10$hash2",
                UserRole.CUSTOMER
        );

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }
}
