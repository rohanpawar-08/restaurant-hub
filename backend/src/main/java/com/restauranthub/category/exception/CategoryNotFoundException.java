package com.restauranthub.category.exception;

/**
 * Domain exception thrown when a requested Category cannot be found in the database.
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(Long id) {
        super("Category not found with id: " + id);
    }
}
