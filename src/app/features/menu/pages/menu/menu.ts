import { Component, computed, inject, signal } from '@angular/core';
import { FoodService } from '../../../../core/services/food.service';
import { FoodCard } from '../../../../shared/components/food-card/food-card';
import { Food } from '../../../../shared/models/food.model';

export type FoodTypeFilter = 'all' | 'veg' | 'non-veg';
export type SortOption = 'recommended' | 'price-asc' | 'price-desc' | 'rating-desc';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [FoodCard],
  templateUrl: './menu.html',
  styleUrl: './menu.scss',
})
export class Menu {
  private readonly foodService = inject(FoodService);

  /** Source data signals */
  readonly categories = this.foodService.categories;
  readonly allFoods = this.foodService.foods;

  /** UI Filter state signals */
  readonly searchTerm = signal<string>('');
  readonly selectedCategory = signal<string>('all');
  readonly foodType = signal<FoodTypeFilter>('all');
  readonly sortOption = signal<SortOption>('recommended');

  /** Derived list of filtered and sorted foods */
  readonly filteredFoods = computed<Food[]>(() => {
    let result = this.allFoods();

    // 1. Filter by category
    const category = this.selectedCategory();
    if (category !== 'all') {
      result = result.filter((food) => food.categorySlug === category || food.category.toLowerCase() === category.toLowerCase());
    }

    // 2. Filter by food diet type (veg / non-veg)
    const type = this.foodType();
    if (type === 'veg') {
      result = result.filter((food) => food.isVeg);
    } else if (type === 'non-veg') {
      result = result.filter((food) => !food.isVeg);
    }

    // 3. Filter by search term (name, description, category)
    const query = this.searchTerm().trim().toLowerCase();
    if (query) {
      result = result.filter(
        (food) =>
          food.name.toLowerCase().includes(query) ||
          food.description.toLowerCase().includes(query) ||
          food.category.toLowerCase().includes(query)
      );
    }

    // 4. Sort the result (always sort a copy to avoid mutating the original signal state)
    const sort = this.sortOption();
    if (sort === 'recommended') {
      return result;
    }

    const sorted = [...result];
    switch (sort) {
      case 'price-asc':
        return sorted.sort((a, b) => a.price - b.price);
      case 'price-desc':
        return sorted.sort((a, b) => b.price - a.price);
      case 'rating-desc':
        return sorted.sort((a, b) => b.rating - a.rating);
      default:
        return sorted;
    }
  });

  /** Computed helper states */
  readonly totalCount = computed(() => this.filteredFoods().length);

  readonly hasActiveFilters = computed(() => {
    return (
      this.searchTerm().trim() !== '' ||
      this.selectedCategory() !== 'all' ||
      this.foodType() !== 'all' ||
      this.sortOption() !== 'recommended'
    );
  });

  /** Actions */
  onSearchInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchTerm.set(input.value);
  }

  clearSearch(): void {
    this.searchTerm.set('');
  }

  selectCategory(categorySlug: string): void {
    this.selectedCategory.set(categorySlug);
  }

  selectFoodType(type: FoodTypeFilter): void {
    this.foodType.set(type);
  }

  onSortChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.sortOption.set(select.value as SortOption);
  }

  resetAllFilters(): void {
    this.searchTerm.set('');
    this.selectedCategory.set('all');
    this.foodType.set('all');
    this.sortOption.set('recommended');
  }
}
