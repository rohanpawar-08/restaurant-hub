package com.restauranthub.auth.exception;

/**
 * Thrown when customer registration fails due to duplicate email.
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
