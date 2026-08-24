package com.restauranthub.order;

/**
 * Enumeration representing the lifecycle states of a customer order.
 */
public enum OrderStatus {
    CONFIRMED,
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
