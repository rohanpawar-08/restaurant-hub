import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Menu } from './menu';
import { FoodService } from '../../../../core/services/food.service';
import { environment } from '../../../../../environments/environment';

describe('Menu Component', () => {
  let component: Menu;
  let fixture: ComponentFixture<Menu>;
  let httpMock: HttpTestingController;

  const mockCategories = [
    { id: 1, name: 'Pizza', slug: 'pizza', active: true },
    { id: 2, name: 'Burgers', slug: 'burgers', active: true },
  ];

  const mockFoods = [
    {
      id: 1,
      name: 'Margherita Pizza',
      description: 'Cheesy tomato pizza',
      price: 250,
      rating: 4.8,
      image: null,
      veg: true,
      popular: true,
      available: true,
      categoryId: 1,
      categoryName: 'Pizza',
      categorySlug: 'pizza',
    },
    {
      id: 2,
      name: 'Cheeseburger Deluxe',
      description: 'Beef patty with cheddar',
      price: 350,
      rating: 4.5,
      image: null,
      veg: false,
      popular: true,
      available: true,
      categoryId: 2,
      categoryName: 'Burgers',
      categorySlug: 'burgers',
    },
    {
      id: 3,
      name: 'Farmhouse Pizza',
      description: 'Veggie loaded pizza',
      price: 300,
      rating: 4.9,
      image: null,
      veg: true,
      popular: false,
      available: true,
      categoryId: 1,
      categoryName: 'Pizza',
      categorySlug: 'pizza',
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Menu],
      providers: [
        FoodService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);

    fixture = TestBed.createComponent(Menu);
    component = fixture.componentInstance;

    // Flush initial requests initiated by FoodService
    httpMock.expectOne(`${environment.apiBaseUrl}/categories`).flush(mockCategories);
    httpMock.expectOne(`${environment.apiBaseUrl}/foods`).flush(mockFoods);

    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create menu component and display all items initially', () => {
    expect(component).toBeTruthy();
    expect(component.filteredFoods().length).toBe(3);
    expect(component.totalCount()).toBe(3);
  });

  it('should filter foods by category slug', () => {
    component.selectCategory('pizza');
    expect(component.filteredFoods().length).toBe(2);
    expect(component.filteredFoods().every((f) => f.categorySlug === 'pizza')).toBe(true);
  });

  it('should filter foods by dietary type (veg / non-veg)', () => {
    component.selectFoodType('veg');
    expect(component.filteredFoods().length).toBe(2);
    expect(component.filteredFoods().every((f) => f.isVeg)).toBe(true);

    component.selectFoodType('non-veg');
    expect(component.filteredFoods().length).toBe(1);
    expect(component.filteredFoods()[0].name).toBe('Cheeseburger Deluxe');
  });

  it('should filter foods by search term', () => {
    component.searchTerm.set('cheddar');
    expect(component.filteredFoods().length).toBe(1);
    expect(component.filteredFoods()[0].name).toBe('Cheeseburger Deluxe');
  });

  it('should sort foods by price and rating', () => {
    // Price low to high
    component.sortOption.set('price-asc');
    const ascPrices = component.filteredFoods().map((f) => f.price);
    expect(ascPrices).toEqual([250, 300, 350]);

    // Price high to load
    component.sortOption.set('price-desc');
    const descPrices = component.filteredFoods().map((f) => f.price);
    expect(descPrices).toEqual([350, 300, 250]);

    // Rating high to low
    component.sortOption.set('rating-desc');
    const descRatings = component.filteredFoods().map((f) => f.rating);
    expect(descRatings).toEqual([4.9, 4.8, 4.5]);
  });

  it('should reset all active filters', () => {
    component.selectCategory('pizza');
    component.selectFoodType('veg');
    component.searchTerm.set('Margherita');
    component.sortOption.set('price-desc');

    expect(component.hasActiveFilters()).toBe(true);

    component.resetAllFilters();

    expect(component.selectedCategory()).toBe('all');
    expect(component.foodType()).toBe('all');
    expect(component.searchTerm()).toBe('');
    expect(component.sortOption()).toBe('recommended');
    expect(component.hasActiveFilters()).toBe(false);
    expect(component.filteredFoods().length).toBe(3);
  });
});
