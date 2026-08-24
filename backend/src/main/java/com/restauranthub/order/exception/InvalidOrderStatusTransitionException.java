package com.restauranthub.order.exception;

import com.restauranthub.order.OrderStatus;

/**
 * Domain exception thrown when an attempt is made to transition an order
 * to an invalid or disallowed OrderStatus in the restaurant lifecycle.
 */
public class InvalidOrderStatusTransitionException extends RuntimeException {

    private final OrderStatus currentStatus;
    private final OrderStatus attemptedStatus;

    public InvalidOrderStatusTransitionException(OrderStatus currentStatus, OrderStatus attemptedStatus) {
        super(String.format("Cannot transition order from status '%s' to '%s'.", currentStatus, attemptedStatus));
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public OrderStatus getAttemptedStatus() {
        return attemptedStatus;
    }
}
