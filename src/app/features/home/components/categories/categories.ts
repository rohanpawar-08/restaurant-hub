import { Component, inject } from '@angular/core';
import { FoodService } from '../../../../core/services/food.service';
import { CategoryCard } from '../../../../shared/components/category-card/category-card';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CategoryCard],
  templateUrl: './categories.html',
  styleUrl: './categories.scss',
})
export class Categories {
  private readonly foodService = inject(FoodService);

  readonly categories = this.foodService.categories;
  readonly isLoading = this.foodService.isLoading;
  readonly errorMessage = this.foodService.errorMessage;

  retry(): void {
    this.foodService.retry();
  }
}
