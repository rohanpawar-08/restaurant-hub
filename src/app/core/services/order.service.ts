import { Injectable, signal } from '@angular/core';
import { Order } from '../../shared/models/order.model';
import { CartItem } from '../../shared/models/cart-item.model';
import { CustomerDetails, PaymentMethod } from '../../shared/models/checkout.model';

const LATEST_ORDER_KEY = 'restaurant-hub-latest-order';
const ORDERS_HISTORY_KEY = 'restaurant-hub-order-history';

export interface CreateOrderParams {
  items: CartItem[];
  customer: CustomerDetails;
  paymentMethod: PaymentMethod;
  subtotal: number;
  deliveryFee: number;
  total: number;
}

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private readonly latestOrderState = signal<Order | null>(
    this.loadLatestOrderFromStorage()
  );
  private readonly orderHistoryState = signal<Order[]>(
    this.loadOrderHistoryFromStorage()
  );

  /** Public Signals */
  readonly latestOrder = this.latestOrderState.asReadonly();
  readonly orderHistory = this.orderHistoryState.asReadonly();

  /**
   * Create a new mock order and store it locally
   */
  createOrder(params: CreateOrderParams): Order {
    const orderId = this.generateOrderId();
    const newOrder: Order = {
      id: orderId,
      items: [...params.items],
      customer: { ...params.customer },
      paymentMethod: params.paymentMethod,
      subtotal: params.subtotal,
      deliveryFee: params.deliveryFee,
      total: params.total,
      status: 'confirmed',
      createdAt: new Date().toISOString(),
      estimatedDeliveryMinutes: 35,
    };

    // Update state signals
    this.latestOrderState.set(newOrder);
    this.orderHistoryState.update((history) => [newOrder, ...history]);

    // Persist to localStorage
    this.saveLatestOrderToStorage(newOrder);
    this.saveOrderHistoryToStorage(this.orderHistoryState());

    return newOrder;
  }

  /**
   * Retrieve the current latest order value
   */
  getLatestOrder(): Order | null {
    return this.latestOrderState();
  }

  /**
   * Clear the latest order state (e.g. after viewing confirmation if needed)
   */
  clearLatestOrder(): void {
    this.latestOrderState.set(null);
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        localStorage.removeItem(LATEST_ORDER_KEY);
      }
    } catch {
      // Gracefully ignore storage removal errors
    }
  }

  /**
   * Generate human-readable order reference ID (e.g. RH-839201)
   */
  private generateOrderId(): string {
    const timestampSegment = Date.now().toString().slice(-4);
    const randomSegment = Math.floor(1000 + Math.random() * 9000).toString();
    return `RH-${timestampSegment}${randomSegment}`;
  }

  /**
   * Load latest order from localStorage
   */
  private loadLatestOrderFromStorage(): Order | null {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        const raw = localStorage.getItem(LATEST_ORDER_KEY);
        if (raw) {
          const parsed = JSON.parse(raw);
          if (parsed && typeof parsed === 'object' && parsed.id && Array.isArray(parsed.items)) {
            return parsed as Order;
          }
        }
      }
    } catch {
      // Gracefully handle storage errors
    }
    return null;
  }

  /**
   * Save latest order to localStorage
   */
  private saveLatestOrderToStorage(order: Order): void {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        localStorage.setItem(LATEST_ORDER_KEY, JSON.stringify(order));
      }
    } catch {
      // Gracefully ignore storage errors
    }
  }

  /**
   * Load order history from localStorage
   */
  private loadOrderHistoryFromStorage(): Order[] {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        const raw = localStorage.getItem(ORDERS_HISTORY_KEY);
        if (raw) {
          const parsed = JSON.parse(raw);
          if (Array.isArray(parsed)) {
            return parsed;
          }
        }
      }
    } catch {
      // Gracefully handle storage errors
    }
    return [];
  }

  /**
   * Save order history to localStorage
   */
  private saveOrderHistoryToStorage(orders: Order[]): void {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        localStorage.setItem(ORDERS_HISTORY_KEY, JSON.stringify(orders));
      }
    } catch {
      // Gracefully ignore storage errors
    }
  }
}
