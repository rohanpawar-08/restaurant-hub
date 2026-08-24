import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize, map } from 'rxjs/operators';
import { Food } from '../../shared/models/food.model';
import { FoodCategory } from '../../shared/models/category.model';
import { CategoryApiResponse } from '../api/models/category-api.model';
import { FoodApiResponse } from '../api/models/food-api.model';
import {
  mapCategoryApiResponseToCategory,
  mapFoodApiResponseToFood,
} from '../api/mappers/food-api.mapper';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class FoodService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiBaseUrl;

  /** Internal writable signal states */
  private readonly categoriesState = signal<FoodCategory[]>([]);
  private readonly foodsState = signal<Food[]>([]);
  private readonly loadingState = signal<boolean>(false);
  private readonly errorState = signal<string | null>(null);

  /** Public readonly signals */
  readonly categories = this.categoriesState.asReadonly();
  readonly foods = this.foodsState.asReadonly();
  readonly isLoading = this.loadingState.asReadonly();
  readonly errorMessage = this.errorState.asReadonly();

  /** Computed signal for popular dishes */
  readonly popularFoods = computed(() =>
    this.foodsState().filter((food) => food.isPopular)
  );

  constructor() {
    // Automatically load initial menu data on service creation
    this.loadMenuData();
  }

  /**
   * Fetches categories and food items from the Spring Boot REST API.
   * Updates state signals reactively.
   */
  loadMenuData(): void {
    // Prevent duplicate simultaneous fetch requests
    if (this.loadingState()) {
      return;
    }

    this.loadingState.set(true);
    this.errorState.set(null);

    const categories$ = this.http.get<CategoryApiResponse[]>(`${this.apiUrl}/categories`, {
      params: { activeOnly: 'true' },
    });
    const foods$ = this.http.get<FoodApiResponse[]>(`${this.apiUrl}/foods`, {
      params: { activeOnly: 'true' },
    });

    forkJoin({
      categories: categories$,
      foods: foods$,
    })
      .pipe(
        finalize(() => {
          this.loadingState.set(false);
        })
      )
      .subscribe({
        next: ({ categories, foods }) => {
          const activeCategories = categories.filter((cat) => cat.active !== false);
          const activeCategorySlugs = new Set(activeCategories.map((cat) => cat.slug));

          // Exclude any foods whose category is inactive from the customer menu
          const mappedFoods = foods
            .map(mapFoodApiResponseToFood)
            .filter((food) => activeCategorySlugs.has(food.categorySlug));

          // Calculate item counts per active category slug
          const countMap = new Map<string, number>();
          for (const food of mappedFoods) {
            const count = countMap.get(food.categorySlug) || 0;
            countMap.set(food.categorySlug, count + 1);
          }

          const mappedCategories = activeCategories.map((cat) =>
            mapCategoryApiResponseToCategory(cat, countMap.get(cat.slug) || 0)
          );

          this.categoriesState.set(mappedCategories);
          this.foodsState.set(mappedFoods);
          this.errorState.set(null);
        },
        error: (err) => {
          console.error('Failed to load menu data from backend API:', err);
          this.errorState.set("We couldn't load the menu right now.");
        },
      });
  }

  /**
   * Re-attempts loading menu data after a failure.
   */
  retry(): void {
    this.loadMenuData();
  }

  /** Filter foods by category slug */
  getFoodsByCategory(categorySlug: string): Food[] {
    return this.foodsState().filter((food) => food.categorySlug === categorySlug);
  }

  /** Find food by ID */
  getFoodById(id: string): Food | undefined {
    return this.foodsState().find((food) => food.id === id);
  }

  /** Find category by slug */
  getCategoryBySlug(slug: string): FoodCategory | undefined {
    return this.categoriesState().find((category) => category.slug === slug);
  }
}
