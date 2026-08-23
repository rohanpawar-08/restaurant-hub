package com.restauranthub.food.exception;

/**
 * Domain exception thrown when a requested Food item cannot be found in the database.
 */
public class FoodNotFoundException extends RuntimeException {

    public FoodNotFoundException(String message) {
        super(message);
    }

    public FoodNotFoundException(Long id) {
        super("Food item not found with id: " + id);
    }
}
