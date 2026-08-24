package com.restauranthub.order.exception;

/**
 * Domain exception thrown when a requested Food item is out of stock or marked unavailable.
 */
public class FoodUnavailableException extends RuntimeException {

    public FoodUnavailableException(String message) {
        super(message);
    }

    public FoodUnavailableException(Long foodId, String foodName) {
        super("Dish '" + foodName + "' (ID: " + foodId + ") is currently unavailable.");
    }
}
