package com.restauranthub.auth;

import com.restauranthub.auth.dto.RegisterRequest;
import com.restauranthub.auth.dto.UserResponse;
import com.restauranthub.auth.exception.DuplicateEmailException;
import com.restauranthub.auth.exception.DuplicatePhoneException;
import com.restauranthub.user.User;
import com.restauranthub.user.UserRepository;
import com.restauranthub.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest(
                "Rohan Pawar",
                "rohan.test@example.com",
                "9876543210",
                "SecurePass123"
        );
    }

    @Test
    @DisplayName("Should successfully register customer with BCrypt hash and CUSTOMER role")
    void shouldRegisterCustomerSuccessfully() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("rohan.test@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("9876543210")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123")).thenReturn("$2a$10$hashedPasswordValue");

        User savedMockUser = new User(
                "Rohan Pawar",
                "rohan.test@example.com",
                "9876543210",
                "$2a$10$hashedPasswordValue",
                UserRole.CUSTOMER,
                true,
                LocalDateTime.now()
        );
        savedMockUser.setId(101L);

        when(userRepository.save(any(User.class))).thenReturn(savedMockUser);

        // Act
        UserResponse response = authService.register(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(101L, response.id());
        assertEquals("Rohan Pawar", response.fullName());
        assertEquals("rohan.test@example.com", response.email());
        assertEquals("9876543210", response.phone());
        assertEquals(UserRole.CUSTOMER, response.role());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals("rohan.test@example.com", capturedUser.getEmail());
        assertEquals("$2a$10$hashedPasswordValue", capturedUser.getPasswordHash());
        assertEquals(UserRole.CUSTOMER, capturedUser.getRole());
        assertTrue(capturedUser.getActive());
    }

    @Test
    @DisplayName("Should reject registration when email already exists")
    void shouldRejectDuplicateEmail() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("rohan.test@example.com")).thenReturn(true);

        // Act & Assert
        DuplicateEmailException ex = assertThrows(DuplicateEmailException.class, () -> {
            authService.register(validRequest);
        });
        assertTrue(ex.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when phone already exists")
    void shouldRejectDuplicatePhone() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("rohan.test@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("9876543210")).thenReturn(true);

        // Act & Assert
        DuplicatePhoneException ex = assertThrows(DuplicatePhoneException.class, () -> {
            authService.register(validRequest);
        });
        assertTrue(ex.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any(User.class));
    }
}
