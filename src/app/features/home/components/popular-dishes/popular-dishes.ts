import { Component, inject } from '@angular/core';
import { FoodService } from '../../../../core/services/food.service';
import { FoodCard } from '../../../../shared/components/food-card/food-card';

@Component({
  selector: 'app-popular-dishes',
  standalone: true,
  imports: [FoodCard],
  templateUrl: './popular-dishes.html',
  styleUrl: './popular-dishes.scss',
})
export class PopularDishes {
  private readonly foodService = inject(FoodService);
  readonly popularDishes = this.foodService.popularFoods;
}
