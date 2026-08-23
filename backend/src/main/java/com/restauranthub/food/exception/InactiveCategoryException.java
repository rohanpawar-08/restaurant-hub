package com.restauranthub.food.exception;

/**
 * Domain exception thrown when attempting to assign a food item to an inactive category.
 */
public class InactiveCategoryException extends RuntimeException {

    public InactiveCategoryException(Long categoryId) {
        super("Cannot associate food item with inactive Category id: " + categoryId);
    }
}
