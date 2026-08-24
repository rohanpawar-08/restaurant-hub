package com.restauranthub.order.exception;

/**
 * Domain exception thrown when an order is submitted with a payment method
 * that is not yet supported or activated (e.g. UPI or CARD before payment gateway integration).
 */
public class UnsupportedPaymentMethodException extends RuntimeException {

    public UnsupportedPaymentMethodException(String message) {
        super(message);
    }
}
