package com.restauranthub.order.exception;

/**
 * Domain exception thrown when an order does not exist or does not belong to the requesting user.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id);
    }
}
