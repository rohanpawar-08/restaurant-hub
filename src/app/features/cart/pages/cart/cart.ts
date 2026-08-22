import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../../../../core/services/cart.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './cart.html',
  styleUrl: './cart.scss',
})
export class Cart {
  readonly cartService = inject(CartService);

  /** Direct reactive signals from CartService */
  readonly cartItems = this.cartService.cartItems;
  readonly totalQuantity = this.cartService.totalQuantity;
  readonly subtotal = this.cartService.subtotal;
  readonly deliveryFee = this.cartService.deliveryFee;
  readonly grandTotal = this.cartService.grandTotal;
  readonly isEmpty = this.cartService.isEmpty;

  /** Free delivery progress calculation */
  readonly freeDeliveryThreshold = 500;
  readonly amountForFreeDelivery = computed(() =>
    Math.max(0, this.freeDeliveryThreshold - this.subtotal())
  );
  readonly freeDeliveryProgress = computed(() => {
    const sub = this.subtotal();
    return Math.min(100, Math.round((sub / this.freeDeliveryThreshold) * 100));
  });

  /** Track image loading errors per food item ID */
  private readonly failedImages = signal<Record<string, boolean>>({});

  isImageFailed(foodId: string): boolean {
    return !!this.failedImages()[foodId];
  }

  onImageError(foodId: string): void {
    this.failedImages.update((current) => ({ ...current, [foodId]: true }));
  }

  /** Action delegation */
  onIncrease(foodId: string): void {
    this.cartService.increaseQuantity(foodId);
  }

  onDecrease(foodId: string): void {
    this.cartService.decreaseQuantity(foodId);
  }

  onRemove(foodId: string): void {
    this.cartService.removeItem(foodId);
  }

  onClear(): void {
    this.cartService.clearCart();
  }
}
