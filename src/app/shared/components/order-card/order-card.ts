import { Component, computed, input, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, NgClass } from '@angular/common';
import { Order, OrderStatus } from '../../models/order.model';
import { PaymentMethod } from '../../models/checkout.model';

@Component({
  selector: 'app-order-card',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, NgClass],
  templateUrl: './order-card.html',
  styleUrl: './order-card.scss',
})
export class OrderCard {
  /** Required input signal with order data */
  readonly order = input.required<Order>();

  /** Local signal controlling expanded details panel */
  readonly isExpanded = signal(false);

  /** Computed total item count across all line items */
  readonly totalItemCount = computed(() =>
    this.order().items.reduce((acc, item) => acc + item.quantity, 0)
  );

  /** Computed preview text of the first few items */
  readonly itemsPreviewText = computed(() => {
    const items = this.order().items;
    if (!items || items.length === 0) {
      return 'No items';
    }
    return items
      .map((item) => `${item.quantity} × ${item.food.name}`)
      .join(', ');
  });

  /** Toggle expanded state */
  toggleDetails(): void {
    this.isExpanded.update((expanded) => !expanded);
  }

  /** Human-readable status label */
  getStatusLabel(status: OrderStatus): string {
    switch (status) {
      case 'confirmed':
        return 'Confirmed';
      case 'preparing':
        return 'Preparing';
      case 'ready':
        return 'Ready for Pickup';
      case 'out_for_delivery':
        return 'Out for Delivery';
      case 'delivered':
        return 'Delivered';
      case 'cancelled':
        return 'Cancelled';
      default:
        return status;
    }
  }

  /** Status CSS modifier class */
  getStatusClass(status: OrderStatus): string {
    switch (status) {
      case 'confirmed':
        return 'status-confirmed';
      case 'preparing':
        return 'status-preparing';
      case 'ready':
        return 'status-ready';
      case 'out_for_delivery':
        return 'status-out-for-delivery';
      case 'delivered':
        return 'status-delivered';
      case 'cancelled':
        return 'status-cancelled';
      default:
        return 'status-default';
    }
  }

  /** Status badge icon */
  getStatusIcon(status: OrderStatus): string {
    switch (status) {
      case 'confirmed':
        return '✓';
      case 'preparing':
        return '🍳';
      case 'ready':
        return '🔔';
      case 'out_for_delivery':
        return '🛵';
      case 'delivered':
        return '📦';
      case 'cancelled':
        return '✕';
      default:
        return '📋';
    }
  }

  /** Human-readable payment method label */
  getPaymentMethodLabel(method: PaymentMethod): string {
    switch (method) {
      case 'cod':
        return 'Cash on Delivery';
      case 'upi':
        return 'UPI Payment';
      case 'card':
        return 'Credit / Debit Card';
      default:
        return method;
    }
  }
}
