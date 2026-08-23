import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { FoodService } from './food.service';
import { CategoryApiResponse } from '../api/models/category-api.model';
import { FoodApiResponse } from '../api/models/food-api.model';
import { environment } from '../../../environments/environment';

describe('FoodService', () => {
  let service: FoodService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiBaseUrl;

  const mockCategoriesResponse: CategoryApiResponse[] = [
    { id: 1, name: 'Pizza', slug: 'pizza', active: true },
    { id: 2, name: 'Burgers', slug: 'burgers', active: true },
  ];

  const mockFoodsResponse: FoodApiResponse[] = [
    {
      id: 101,
      name: 'Margherita Pizza',
      description: 'Cheesy and delicious',
      price: 299,
      rating: 4.9,
      image: 'assets/images/food/margherita-pizza.png',
      veg: true,
      popular: true,
      available: true,
      categoryId: 1,
      categoryName: 'Pizza',
      categorySlug: 'pizza',
    },
    {
      id: 102,
      name: 'Bacon Burger',
      description: 'Juicy patty',
      price: 349,
      rating: 4.7,
      image: 'assets/images/food/burger.png',
      veg: false,
      popular: false,
      available: true,
      categoryId: 2,
      categoryName: 'Burgers',
      categorySlug: 'burgers',
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        FoodService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function flushInitialRequests(
    categories = mockCategoriesResponse,
    foods = mockFoodsResponse
  ) {
    const catReq = httpMock.expectOne(`${apiUrl}/categories`);
    const foodReq = httpMock.expectOne(`${apiUrl}/foods`);
    catReq.flush(categories);
    foodReq.flush(foods);
  }

  it('should be created and initiate initial data load', () => {
    service = TestBed.inject(FoodService);

    expect(service.isLoading()).toBe(true);

    flushInitialRequests();

    expect(service.isLoading()).toBe(false);
    expect(service.errorMessage()).toBeNull();
    expect(service.categories().length).toBe(2);
    expect(service.foods().length).toBe(2);
  });

  it('should correctly map Category API response to FoodCategory model with icons', () => {
    service = TestBed.inject(FoodService);
    flushInitialRequests();

    const categories = service.categories();
    expect(categories[0].id).toBe('1');
    expect(categories[0].name).toBe('Pizza');
    expect(categories[0].slug).toBe('pizza');
    expect(categories[0].icon).toBe('🍕');
    expect(categories[0].itemCount).toBe(1);

    expect(categories[1].id).toBe('2');
    expect(categories[1].name).toBe('Burgers');
    expect(categories[1].slug).toBe('burgers');
    expect(categories[1].icon).toBe('🍔');
    expect(categories[1].itemCount).toBe(1);
  });

  it('should correctly map Food API response including veg -> isVeg and popular -> isPopular', () => {
    service = TestBed.inject(FoodService);
    flushInitialRequests();

    const foods = service.foods();
    const pizza = foods[0];
    const burger = foods[1];

    // Vegetarian and Popular pizza
    expect(pizza.id).toBe('101');
    expect(pizza.name).toBe('Margherita Pizza');
    expect(pizza.isVeg).toBe(true);
    expect(pizza.isPopular).toBe(true);
    expect(pizza.category).toBe('Pizza');
    expect(pizza.categorySlug).toBe('pizza');
    expect(pizza.price).toBe(299);
    expect(pizza.rating).toBe(4.9);

    // Non-Vegetarian and Non-popular burger
    expect(burger.id).toBe('102');
    expect(burger.isVeg).toBe(false);
    expect(burger.isPopular).toBe(false);
  });

  it('should compute popularFoods signal accurately', () => {
    service = TestBed.inject(FoodService);
    flushInitialRequests();

    const popular = service.popularFoods();
    expect(popular.length).toBe(1);
    expect(popular[0].name).toBe('Margherita Pizza');
  });

  it('should set error state signal when HTTP request fails', () => {
    service = TestBed.inject(FoodService);

    const catReq = httpMock.expectOne(`${apiUrl}/categories`);
    httpMock.expectOne(`${apiUrl}/foods`);

    catReq.error(new ProgressEvent('Network error'));

    expect(service.isLoading()).toBe(false);
    expect(service.errorMessage()).toBe("We couldn't load the menu right now.");
    expect(service.categories().length).toBe(0);
    expect(service.foods().length).toBe(0);
  });

  it('should allow retry after failure and restore state on subsequent success', () => {
    service = TestBed.inject(FoodService);

    const catReq = httpMock.expectOne(`${apiUrl}/categories`);
    httpMock.expectOne(`${apiUrl}/foods`);
    catReq.error(new ProgressEvent('Network error'));

    expect(service.errorMessage()).toBe("We couldn't load the menu right now.");

    // Retry
    service.retry();
    expect(service.isLoading()).toBe(true);

    const catReq2 = httpMock.expectOne(`${apiUrl}/categories`);
    const foodReq2 = httpMock.expectOne(`${apiUrl}/foods`);
    catReq2.flush(mockCategoriesResponse);
    foodReq2.flush(mockFoodsResponse);

    expect(service.isLoading()).toBe(false);
    expect(service.errorMessage()).toBeNull();
    expect(service.foods().length).toBe(2);
  });

  it('should filter foods by category slug and find items by ID', () => {
    service = TestBed.inject(FoodService);
    flushInitialRequests();

    const pizzaFoods = service.getFoodsByCategory('pizza');
    expect(pizzaFoods.length).toBe(1);
    expect(pizzaFoods[0].name).toBe('Margherita Pizza');

    const foundFood = service.getFoodById('101');
    expect(foundFood).toBeDefined();
    expect(foundFood?.name).toBe('Margherita Pizza');

    const foundCategory = service.getCategoryBySlug('burgers');
    expect(foundCategory).toBeDefined();
    expect(foundCategory?.name).toBe('Burgers');
  });
});
