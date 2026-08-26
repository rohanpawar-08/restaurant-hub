package com.restauranthub.order.exception;

/**
 * Thrown when a customer attempts to place an order while the restaurant is not accepting orders.
 */
public class OrdersClosedException extends RuntimeException {

    public OrdersClosedException() {
        super("We're currently not accepting online orders.");
    }

    public OrdersClosedException(String message) {
        super(message);
    }
}
