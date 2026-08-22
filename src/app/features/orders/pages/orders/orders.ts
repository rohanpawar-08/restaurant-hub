import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../../../core/services/order.service';
import { Order, OrderStatus } from '../../../../shared/models/order.model';
import { OrderCard } from '../../../../shared/components/order-card/order-card';

export type StatusFilterOption = 'all' | OrderStatus;

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [RouterLink, OrderCard],
  templateUrl: './orders.html',
  styleUrl: './orders.scss',
})
export class Orders {
  private readonly orderService = inject(OrderService);

  /** Direct reference to order history signal from OrderService */
  readonly rawOrders = this.orderService.orderHistory;

  /** Current active status filter */
  readonly selectedFilter = signal<StatusFilterOption>('all');

  /** Filter options for UI tabs */
  readonly filterOptions: { label: string; value: StatusFilterOption }[] = [
    { label: 'All Orders', value: 'all' },
    { label: 'Confirmed', value: 'confirmed' },
    { label: 'Preparing', value: 'preparing' },
    { label: 'Out for Delivery', value: 'out_for_delivery' },
    { label: 'Delivered', value: 'delivered' },
  ];

  /** Total count of all orders placed */
  readonly totalOrdersCount = computed(() => this.rawOrders().length);

  /**
   * Computed sorted & filtered list of orders.
   * Newest orders appear first without mutating the original state array.
   */
  readonly filteredOrders = computed<Order[]>(() => {
    const orders = this.rawOrders();
    const filter = this.selectedFilter();

    // Create a shallow copy before sorting to guarantee immutability
    const sorted = [...orders].sort((a, b) => {
      const timeA = new Date(a.createdAt).getTime();
      const timeB = new Date(b.createdAt).getTime();
      return timeB - timeA;
    });

    if (filter === 'all') {
      return sorted;
    }

    return sorted.filter((order) => order.status === filter);
  });

  /** Set status filter */
  setFilter(filter: StatusFilterOption): void {
    this.selectedFilter.set(filter);
  }

  /** Count orders for a specific status */
  getOrderCountByStatus(filter: StatusFilterOption): number {
    const orders = this.rawOrders();
    if (filter === 'all') {
      return orders.length;
    }
    return orders.filter((order) => order.status === filter).length;
  }
}
