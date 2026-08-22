import { Component, inject } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../../../core/services/order.service';

@Component({
  selector: 'app-order-success',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './order-success.html',
  styleUrl: './order-success.scss',
})
export class OrderSuccess {
  private readonly orderService = inject(OrderService);

  /** Signal reading latest order */
  readonly order = this.orderService.latestOrder;

  /** Payment method label formatting helper */
  getPaymentMethodLabel(method: string | undefined): string {
    switch (method) {
      case 'cod':
        return 'Cash on Delivery (Pay on Arrival)';
      case 'upi':
        return 'UPI Payment';
      case 'card':
        return 'Credit / Debit Card';
      default:
        return method || 'Cash on Delivery';
    }
  }
}
