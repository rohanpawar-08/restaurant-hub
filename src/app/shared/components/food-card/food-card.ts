import { Component, input, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Food } from '../../models/food.model';

@Component({
  selector: 'app-food-card',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './food-card.html',
  styleUrl: './food-card.scss',
})
export class FoodCard {
  readonly food = input.required<Food>();
  readonly imageError = signal(false);

  onImageError(): void {
    this.imageError.set(true);
  }
}
