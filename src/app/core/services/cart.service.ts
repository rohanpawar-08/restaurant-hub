import { Injectable, computed, effect, signal } from '@angular/core';
import { Food } from '../../shared/models/food.model';
import { CartItem } from '../../shared/models/cart-item.model';

const STORAGE_KEY = 'restaurant-hub-cart';

@Injectable({
  providedIn: 'root',
})
export class CartService {
  private readonly cartState = signal<CartItem[]>(this.loadCartFromStorage());

  /** Readonly and computed signals */
  readonly cartItems = this.cartState.asReadonly();
  readonly totalQuantity = computed(() =>
    this.cartState().reduce((sum, item) => sum + item.quantity, 0)
  );
  readonly uniqueItemCount = computed(() => this.cartState().length);
  readonly subtotal = computed(() =>
    this.cartState().reduce((sum, item) => sum + item.food.price * item.quantity, 0)
  );
  readonly deliveryFee = computed(() => {
    const sub = this.subtotal();
    if (sub === 0) {
      return 0;
    }
    return sub >= 500 ? 0 : 40;
  });
  readonly grandTotal = computed(() => this.subtotal() + this.deliveryFee());
  readonly isEmpty = computed(() => this.cartState().length === 0);

  constructor() {
    // Automatically persist cart state to localStorage on every change
    effect(() => {
      this.saveCartToStorage(this.cartState());
    });
  }

  /**
   * Add a food item to the cart.
   * If item already exists, increments quantity.
   */
  addToCart(food: Food): void {
    this.cartState.update((items) => {
      const existingIndex = items.findIndex((i) => i.food.id === food.id);
      if (existingIndex > -1) {
        return items.map((item, index) =>
          index === existingIndex ? { ...item, quantity: item.quantity + 1 } : item
        );
      }
      return [...items, { food, quantity: 1 }];
    });
  }

  /**
   * Increase item quantity by 1.
   */
  increaseQuantity(foodId: string): void {
    this.cartState.update((items) =>
      items.map((item) =>
        item.food.id === foodId ? { ...item, quantity: item.quantity + 1 } : item
      )
    );
  }

  /**
   * Decrease item quantity by 1 (minimum 1).
   */
  decreaseQuantity(foodId: string): void {
    this.cartState.update((items) =>
      items.map((item) => {
        if (item.food.id === foodId) {
          const nextQuantity = Math.max(1, item.quantity - 1);
          return { ...item, quantity: nextQuantity };
        }
        return item;
      })
    );
  }

  /**
   * Remove a specific item from the cart.
   */
  removeItem(foodId: string): void {
    this.cartState.update((items) => items.filter((item) => item.food.id !== foodId));
  }

  /**
   * Clear all items from the cart.
   */
  clearCart(): void {
    this.cartState.set([]);
  }

  /**
   * Safe localStorage loader
   */
  private loadCartFromStorage(): CartItem[] {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (raw) {
          const parsed = JSON.parse(raw);
          if (Array.isArray(parsed)) {
            return parsed.filter(
              (item) =>
                item &&
                item.food &&
                item.food.id &&
                typeof item.quantity === 'number' &&
                item.quantity > 0
            );
          }
        }
      }
    } catch {
      // Gracefully ignore parsing / storage errors
    }
    return [];
  }

  /**
   * Safe localStorage saver
   */
  private saveCartToStorage(items: CartItem[]): void {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
      }
    } catch {
      // Gracefully ignore storage write errors
    }
  }
}
