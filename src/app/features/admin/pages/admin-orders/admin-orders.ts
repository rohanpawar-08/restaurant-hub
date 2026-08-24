import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { AdminService } from '../../../../core/services/admin.service';
import { Order, OrderStatus } from '../../../../shared/models/order.model';

type StatusFilter = 'all' | OrderStatus;

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [CommonModule, DatePipe, CurrencyPipe],
  templateUrl: './admin-orders.html',
  styleUrl: './admin-orders.scss',
})
export class AdminOrders implements OnInit {
  private readonly adminService = inject(AdminService);

  readonly selectedFilter = signal<StatusFilter>('all');
  readonly updatingOrderId = signal<string | null>(null);
  readonly actionError = signal<string | null>(null);

  readonly orders = this.adminService.adminOrders;
  readonly isLoading = this.adminService.isLoading;
  readonly error = this.adminService.error;

  readonly filterTabs: { label: string; value: StatusFilter }[] = [
    { label: 'All Orders', value: 'all' },
    { label: 'Confirmed', value: 'confirmed' },
    { label: 'Preparing', value: 'preparing' },
    { label: 'Ready', value: 'ready' },
    { label: 'Out for Delivery', value: 'out_for_delivery' },
    { label: 'Delivered', value: 'delivered' },
    { label: 'Cancelled', value: 'cancelled' },
  ];

  /** Filtered orders based on selected tab */
  readonly filteredOrders = computed(() => {
    const list = this.orders();
    const filter = this.selectedFilter();
    if (filter === 'all') {
      return list;
    }
    return list.filter((order) => order.status === filter);
  });

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.actionError.set(null);
    const filter = this.selectedFilter();
    const statusParam = filter === 'all' ? undefined : (filter as OrderStatus);
    this.adminService.getOrders(statusParam).subscribe({
      error: () => {}, // Handled by service error signal
    });
  }

  onSelectFilter(filter: StatusFilter): void {
    this.selectedFilter.set(filter);
    this.loadOrders();
  }

  updateStatus(orderId: string, newStatus: OrderStatus): void {
    this.actionError.set(null);
    this.updatingOrderId.set(orderId);

    this.adminService.updateOrderStatus(orderId, newStatus).subscribe({
      next: () => {
        this.updatingOrderId.set(null);
      },
      error: (err: Error) => {
        this.updatingOrderId.set(null);
        this.actionError.set(err.message || 'Failed to update status.');
      },
    });
  }
}
