package com.restauranthub.auth.exception;

/**
 * Thrown when customer registration fails due to duplicate phone number.
 */
public class DuplicatePhoneException extends RuntimeException {
    public DuplicatePhoneException(String message) {
        super(message);
    }
}
