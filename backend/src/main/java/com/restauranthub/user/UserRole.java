package com.restauranthub.user;

/**
 * Defines application authority roles.
 * Stored as an explicit VARCHAR in MySQL for human-readability and migration safety.
 */
public enum UserRole {
    CUSTOMER,
    ADMIN
}
