import { Component, inject, input, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Food } from '../../models/food.model';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-food-card',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './food-card.html',
  styleUrl: './food-card.scss',
})
export class FoodCard {
  private readonly cartService = inject(CartService);

  readonly food = input.required<Food>();
  readonly imageError = signal(false);
  readonly isAdded = signal(false);

  onImageError(): void {
    this.imageError.set(true);
  }

  onAddToCart(): void {
    this.cartService.addToCart(this.food());
    this.isAdded.set(true);
    setTimeout(() => {
      this.isAdded.set(false);
    }, 1200);
  }
}

