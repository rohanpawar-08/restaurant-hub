package com.restauranthub.auth.dto;

import com.restauranthub.user.User;
import com.restauranthub.user.UserRole;
import java.time.LocalDateTime;

/**
 * Safe public user representation returned by authentication endpoints.
 * Explicitly omits passwordHash to ensure sensitive credentials are never leaked.
 */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        UserRole role,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
