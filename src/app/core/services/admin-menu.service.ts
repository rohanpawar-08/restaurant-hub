import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, throwError } from 'rxjs';
import { catchError, finalize, map, tap } from 'rxjs/operators';
import { Food } from '../../shared/models/food.model';
import { FoodCategory } from '../../shared/models/category.model';
import { CategoryApiResponse } from '../api/models/category-api.model';
import { FoodApiResponse } from '../api/models/food-api.model';
import {
  CreateCategoryApiRequest,
  UpdateCategoryApiRequest,
  CreateFoodApiRequest,
  UpdateFoodApiRequest,
} from '../api/models/admin-api.model';
import {
  mapCategoryApiResponseToCategory,
  mapFoodApiResponseToFood,
} from '../api/mappers/food-api.mapper';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AdminMenuService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiBaseUrl;

  /** Internal signal state */
  private readonly categoriesState = signal<FoodCategory[]>([]);
  private readonly foodsState = signal<Food[]>([]);
  private readonly loadingState = signal<boolean>(false);
  private readonly savingState = signal<boolean>(false);
  private readonly deletingState = signal<boolean>(false);
  private readonly errorState = signal<string | null>(null);

  /** Public readonly signals */
  readonly categories = this.categoriesState.asReadonly();
  readonly foods = this.foodsState.asReadonly();
  readonly isLoading = this.loadingState.asReadonly();
  readonly isSaving = this.savingState.asReadonly();
  readonly isDeleting = this.deletingState.asReadonly();
  readonly errorMessage = this.errorState.asReadonly();

  // ==========================================
  // CATEGORIES MANAGEMENT
  // ==========================================

  /**
   * Fetches all categories (including inactive ones) and calculates food counts.
   */
  loadCategories(activeOnly?: boolean): Observable<FoodCategory[]> {
    this.loadingState.set(true);
    this.errorState.set(null);

    const params: Record<string, string> = {};
    if (activeOnly !== undefined) {
      params['activeOnly'] = String(activeOnly);
    }

    return forkJoin({
      categories: this.http.get<CategoryApiResponse[]>(`${this.apiUrl}/categories`, { params }),
      foods: this.http.get<FoodApiResponse[]>(`${this.apiUrl}/foods`),
    }).pipe(
      map(({ categories, foods }) => {
        const countMap = new Map<string, number>();
        for (const food of foods) {
          const count = countMap.get(food.categorySlug) || 0;
          countMap.set(food.categorySlug, count + 1);
        }

        return categories.map((cat) =>
          mapCategoryApiResponseToCategory(cat, countMap.get(cat.slug) || 0)
        );
      }),
      tap((categories) => {
        this.categoriesState.set(categories);
      }),
      catchError((err) => {
        const msg = err.error?.message || err.message || 'Failed to load categories.';
        this.errorState.set(msg);
        return throwError(() => new Error(msg));
      }),
      finalize(() => {
        this.loadingState.set(false);
      })
    );
  }

  /**
   * Creates a new Category via POST /api/v1/categories.
   */
  createCategory(req: CreateCategoryApiRequest): Observable<FoodCategory> {
    this.savingState.set(true);
    this.errorState.set(null);

    return this.http.post<CategoryApiResponse>(`${this.apiUrl}/categories`, req).pipe(
      map((res) => mapCategoryApiResponseToCategory(res, 0)),
      tap((newCategory) => {
        this.categoriesState.update((current) => [...current, newCategory]);
      }),
      catchError((err) => {
        const msg = err.error?.message || err.message || 'Failed to create category.';
        this.errorState.set(msg);
        return throwError(() => new Error(msg));
      }),
      finalize(() => {
        this.savingState.set(false);
      })
    );
  }

  /**
   * Updates an existing Category via PUT /api/v1/categories/{id}.
   */
  updateCategory(id: string, req: UpdateCategoryApiRequest): Observable<FoodCategory> {
    this.savingState.set(true);
    this.errorState.set(null);

    return this.http.put<CategoryApiResponse>(`${this.apiUrl}/categories/${id}`, req).pipe(
      map((res) => {
        const currentCat = this.categoriesState().find((c) => c.id === id);
        return mapCategoryApiResponseToCategory(res, currentCat?.itemCount || 0);
      }),
      tap((updatedCategory) => {
        this.categoriesState.update((current) =>
          current.map((c) => (c.id === id ? updatedCategory : c))
        );
      }),
      catchError((err) => {
        const msg = err.error?.message || err.message || 'Failed to update category.';
        this.errorState.set(msg);
        return throwError(() => new Error(msg));
      }),
      finalize(() => {
        this.savingState.set(false);
      })
    );
  }

  /**
   * Deletes a Category via DELETE /api/v1/categories/{id}.
   */
  deleteCategory(id: string): Observable<void> {
    this.deletingState.set(true);
    this.errorState.set(null);

    return this.http.delete<void>(`${this.apiUrl}/categories/${id}`).pipe(
      tap(() => {
        this.categoriesState.update((current) => current.filter((c) => c.id !== id));
      }),
      catchError((err) => {
        const msg = err.error?.message || err.message || 'Failed to delete category.';
        this.errorState.set(msg);
        return throwError(() => new Error(msg));
      }),
      finalize(() => {
        this.deletingState.set(false);
      })
    );
  }

  // ==========================================
  // FOODS MANAGEMENT
  // ==========================================

  /**
   * Fetches all foods.
   */
  loadFoods(categoryId?: number, popular?: boolean): Observable<Food[]> {
    this.loadingState.set(true);
    this.errorState.set(null);

    const params: Record<string, string> = {};
    if (categoryId !== undefined) {
      params['categoryId'] = String(categoryId);
    }
    if (popular !== undefined) {
      params['popular'] = String(popular);
    }

    return this.http.get<FoodApiResponse[]>(`${this.apiUrl}/foods`, { params }).pipe(
      map((foods) => foods.map(mapFoodApiResponseToFood)),
      tap((foods) => {
        this.foodsState.set(foods);
      }),
      catchError((err) => {
        const msg = err.error?.message || err.message || 'Failed to load foods.';
        this.errorState.set(msg);
        return throwError(() => new Error(msg));
      }),
      finalize(() => {
        this.loadingState.set(false);
      })
    );
  }

  /**
   * Creates a new Food item via POST /api/v1/foods.
   */
  createFood(req: CreateFoodApiRequest): Observable<Food> {
    this.savingState.set(true);
    this.errorState.set(null);

    return this.http.post<FoodApiResponse>(`${this.apiUrl}/foods`, req).pipe(
      map(mapFoodApiResponseToFood),
      tap((newFood) => {
        this.foodsState.update((current) => [...current, newFood]);
      }),
      catchError((err) => {
        const msg = err.error?.message || err.message || 'Failed to create food item.';
        this.errorState.set(msg);
        return throwError(() => new Error(msg));
      }),
      finalize(() => {
        this.savingState.set(false);
      })
    );
  }

  /**
   * Updates an existing Food item via PUT /api/v1/foods/{id}.
   */
  updateFood(id: string, req: UpdateFoodApiRequest): Observable<Food> {
    this.savingState.set(true);
    this.errorState.set(null);

    return this.http.put<FoodApiResponse>(`${this.apiUrl}/foods/${id}`, req).pipe(
      map(mapFoodApiResponseToFood),
      tap((updatedFood) => {
        this.foodsState.update((current) =>
          current.map((f) => (f.id === id ? updatedFood : f))
        );
      }),
      catchError((err) => {
        const msg = err.error?.message || err.message || 'Failed to update food item.';
        this.errorState.set(msg);
        return throwError(() => new Error(msg));
      }),
      finalize(() => {
        this.savingState.set(false);
      })
    );
  }

  /**
   * Deletes a Food item via DELETE /api/v1/foods/{id}.
   */
  deleteFood(id: string): Observable<void> {
    this.deletingState.set(true);
    this.errorState.set(null);

    return this.http.delete<void>(`${this.apiUrl}/foods/${id}`).pipe(
      tap(() => {
        this.foodsState.update((current) => current.filter((f) => f.id !== id));
      }),
      catchError((err) => {
        const msg = err.error?.message || err.message || 'Failed to delete food item.';
        this.errorState.set(msg);
        return throwError(() => new Error(msg));
      }),
      finalize(() => {
        this.deletingState.set(false);
      })
    );
  }

  /**
   * Convenience helper to toggle availability of a Food item.
   */
  toggleAvailability(food: Food, categoryId: number): Observable<Food> {
    const updateReq: UpdateFoodApiRequest = {
      name: food.name,
      description: food.description,
      price: food.price,
      rating: food.rating,
      image: food.image,
      veg: food.isVeg,
      popular: food.isPopular,
      available: !food.isAvailable,
      categoryId: categoryId,
    };

    return this.updateFood(food.id, updateReq);
  }
}
