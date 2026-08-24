import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminMenuService } from './admin-menu.service';
import { CategoryApiResponse } from '../api/models/category-api.model';
import { FoodApiResponse } from '../api/models/food-api.model';
import {
  CreateCategoryApiRequest,
  UpdateCategoryApiRequest,
  CreateFoodApiRequest,
  UpdateFoodApiRequest,
} from '../api/models/admin-api.model';
import { environment } from '../../../environments/environment';

describe('AdminMenuService', () => {
  let service: AdminMenuService;
  let httpTesting: HttpTestingController;
  const apiUrl = environment.apiBaseUrl;

  const mockCategories: CategoryApiResponse[] = [
    { id: 1, name: 'Starters', slug: 'starters', active: true },
    { id: 2, name: 'Main Course', slug: 'main-course', active: true },
    { id: 3, name: 'Seasonal', slug: 'seasonal', active: false },
  ];

  const mockFoods: FoodApiResponse[] = [
    {
      id: 10,
      name: 'Paneer Tikka',
      description: 'Grilled paneer cubes',
      price: 240,
      rating: 4.6,
      image: '/images/paneer.jpg',
      veg: true,
      popular: true,
      available: true,
      categoryId: 1,
      categoryName: 'Starters',
      categorySlug: 'starters',
    },
    {
      id: 20,
      name: 'Butter Chicken',
      description: 'Rich gravy chicken',
      price: 350,
      rating: 4.8,
      image: '/images/chicken.jpg',
      veg: false,
      popular: true,
      available: false,
      categoryId: 2,
      categoryName: 'Main Course',
      categorySlug: 'main-course',
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AdminMenuService,
      ],
    });

    service = TestBed.inject(AdminMenuService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created and have initial empty signal states', () => {
    expect(service).toBeTruthy();
    expect(service.categories()).toEqual([]);
    expect(service.foods()).toEqual([]);
    expect(service.isLoading()).toBe(false);
  });

  it('should load categories and calculate item counts per category slug', () => {
    service.loadCategories().subscribe((categories) => {
      expect(categories.length).toBe(3);
      expect(categories[0].name).toBe('Starters');
      expect(categories[0].itemCount).toBe(1);
      expect(categories[1].name).toBe('Main Course');
      expect(categories[1].itemCount).toBe(1);
      expect(categories[2].name).toBe('Seasonal');
      expect(categories[2].itemCount).toBe(0);
      expect(categories[2].isActive).toBe(false);
    });

    const reqCat = httpTesting.expectOne(`${apiUrl}/categories`);
    expect(reqCat.request.method).toBe('GET');
    reqCat.flush(mockCategories);

    const reqFood = httpTesting.expectOne(`${apiUrl}/foods`);
    expect(reqFood.request.method).toBe('GET');
    reqFood.flush(mockFoods);

    expect(service.categories().length).toBe(3);
  });

  it('should create category via POST and update state signal', () => {
    const newCatReq: CreateCategoryApiRequest = { name: 'Desserts', slug: 'desserts' };
    const createdResponse: CategoryApiResponse = { id: 4, name: 'Desserts', slug: 'desserts', active: true };

    service.createCategory(newCatReq).subscribe((category) => {
      expect(category.id).toBe('4');
      expect(category.name).toBe('Desserts');
      expect(category.slug).toBe('desserts');
    });

    const req = httpTesting.expectOne(`${apiUrl}/categories`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newCatReq);
    req.flush(createdResponse);

    expect(service.categories().length).toBe(1);
  });

  it('should update category via PUT and reflect in state signal', () => {
    (service as any).categoriesState.set([
      { id: '1', name: 'Starters', slug: 'starters', icon: '🥟', isActive: true, itemCount: 2 },
    ]);

    const updateReq: UpdateCategoryApiRequest = { name: 'Appetizers', slug: 'appetizers', active: false };
    const updatedResponse: CategoryApiResponse = { id: 1, name: 'Appetizers', slug: 'appetizers', active: false };

    service.updateCategory('1', updateReq).subscribe((res) => {
      expect(res.name).toBe('Appetizers');
      expect(res.slug).toBe('appetizers');
      expect(res.isActive).toBe(false);
    });

    const req = httpTesting.expectOne(`${apiUrl}/categories/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(updatedResponse);

    const updatedInState = service.categories().find((c) => c.id === '1');
    expect(updatedInState?.name).toBe('Appetizers');
    expect(updatedInState?.isActive).toBe(false);
  });

  it('should delete category via DELETE and remove from state signal', () => {
    (service as any).categoriesState.set([
      { id: '1', name: 'Starters', slug: 'starters', icon: '🥟', isActive: true },
      { id: '2', name: 'Desserts', slug: 'desserts', icon: '🍰', isActive: true },
    ]);

    service.deleteCategory('1').subscribe();

    const req = httpTesting.expectOne(`${apiUrl}/categories/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    expect(service.categories().length).toBe(1);
    expect(service.categories()[0].id).toBe('2');
  });

  it('should load foods via GET and update state signal', () => {
    service.loadFoods().subscribe((foods) => {
      expect(foods.length).toBe(2);
      expect(foods[0].name).toBe('Paneer Tikka');
      expect(foods[0].isAvailable).toBe(true);
      expect(foods[1].name).toBe('Butter Chicken');
      expect(foods[1].isAvailable).toBe(false);
    });

    const req = httpTesting.expectOne(`${apiUrl}/foods`);
    expect(req.request.method).toBe('GET');
    req.flush(mockFoods);

    expect(service.foods().length).toBe(2);
  });

  it('should create food via POST and update state signal', () => {
    const newFoodReq: CreateFoodApiRequest = {
      name: 'Garlic Naan',
      description: 'Clay oven bread',
      price: 60,
      veg: true,
      categoryId: 2,
    };
    const createdResponse: FoodApiResponse = {
      id: 30,
      name: 'Garlic Naan',
      description: 'Clay oven bread',
      price: 60,
      rating: 4.5,
      image: null,
      veg: true,
      popular: false,
      available: true,
      categoryId: 2,
      categoryName: 'Main Course',
      categorySlug: 'main-course',
    };

    service.createFood(newFoodReq).subscribe((food) => {
      expect(food.id).toBe('30');
      expect(food.name).toBe('Garlic Naan');
    });

    const req = httpTesting.expectOne(`${apiUrl}/foods`);
    expect(req.request.method).toBe('POST');
    req.flush(createdResponse);

    expect(service.foods().length).toBe(1);
  });

  it('should update food via PUT and update state signal', () => {
    (service as any).foodsState.set([
      {
        id: '10',
        name: 'Paneer Tikka',
        description: 'Old desc',
        category: 'Starters',
        categorySlug: 'starters',
        price: 240,
        rating: 4.6,
        image: '',
        isVeg: true,
        isPopular: false,
        isAvailable: true,
      },
    ]);

    const updateReq: UpdateFoodApiRequest = {
      name: 'Paneer Tikka Deluxe',
      description: 'New desc',
      price: 280,
      rating: 4.6,
      veg: true,
      popular: true,
      available: true,
      categoryId: 1,
    };

    const updatedResponse: FoodApiResponse = {
      id: 10,
      name: 'Paneer Tikka Deluxe',
      description: 'New desc',
      price: 280,
      rating: 4.6,
      image: null,
      veg: true,
      popular: true,
      available: true,
      categoryId: 1,
      categoryName: 'Starters',
      categorySlug: 'starters',
    };

    service.updateFood('10', updateReq).subscribe((res) => {
      expect(res.name).toBe('Paneer Tikka Deluxe');
      expect(res.price).toBe(280);
    });

    const req = httpTesting.expectOne(`${apiUrl}/foods/10`);
    expect(req.request.method).toBe('PUT');
    req.flush(updatedResponse);

    expect(service.foods()[0].name).toBe('Paneer Tikka Deluxe');
    expect(service.foods()[0].price).toBe(280);
  });

  it('should delete food via DELETE and remove from state signal', () => {
    (service as any).foodsState.set([
      {
        id: '10',
        name: 'Paneer Tikka',
        description: 'desc',
        category: 'Starters',
        categorySlug: 'starters',
        price: 240,
        rating: 4.6,
        image: '',
        isVeg: true,
        isPopular: false,
        isAvailable: true,
      },
    ]);

    service.deleteFood('10').subscribe();

    const req = httpTesting.expectOne(`${apiUrl}/foods/10`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    expect(service.foods().length).toBe(0);
  });
});
