import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { AdminMenu } from './admin-menu';
import { AdminMenuService } from '../../../../core/services/admin-menu.service';
import { Food } from '../../../../shared/models/food.model';
import { FoodCategory } from '../../../../shared/models/category.model';

describe('AdminMenu Component', () => {
  let component: AdminMenu;
  let fixture: ComponentFixture<AdminMenu>;
  let adminMenuService: AdminMenuService;

  const mockCategories: FoodCategory[] = [
    { id: '1', name: 'Starters', slug: 'starters', icon: '🥟', isActive: true, itemCount: 1 },
    { id: '2', name: 'Main Course', slug: 'main-course', icon: '🍛', isActive: true, itemCount: 1 },
  ];

  const mockFoods: Food[] = [
    {
      id: '101',
      name: 'Paneer Tikka',
      description: 'Grilled spiced paneer cubes',
      category: 'Starters',
      categorySlug: 'starters',
      price: 240,
      rating: 4.6,
      image: '/images/paneer.jpg',
      isVeg: true,
      isPopular: true,
      isAvailable: true,
    },
    {
      id: '102',
      name: 'Butter Chicken',
      description: 'Rich tomato cream gravy chicken',
      category: 'Main Course',
      categorySlug: 'main-course',
      price: 350,
      rating: 4.8,
      image: '/images/chicken.jpg',
      isVeg: false,
      isPopular: false,
      isAvailable: false,
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminMenu],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        AdminMenuService,
      ],
    }).compileComponents();

    adminMenuService = TestBed.inject(AdminMenuService);
  });

  it('should create component and load menu items and categories on init', () => {
    const loadCatSpy = vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });
    const loadFoodsSpy = vi.spyOn(adminMenuService, 'loadFoods').mockImplementation(() => {
      (adminMenuService as any).foodsState.set(mockFoods);
      return of(mockFoods);
    });

    fixture = TestBed.createComponent(AdminMenu);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(loadCatSpy).toHaveBeenCalled();
    expect(loadFoodsSpy).toHaveBeenCalled();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Paneer Tikka');
    expect(compiled.textContent).toContain('Butter Chicken');
  });

  it('should filter menu items by search term and filters', () => {
    vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });
    vi.spyOn(adminMenuService, 'loadFoods').mockImplementation(() => {
      (adminMenuService as any).foodsState.set(mockFoods);
      return of(mockFoods);
    });

    fixture = TestBed.createComponent(AdminMenu);
    component = fixture.componentInstance;
    fixture.detectChanges();

    // Search for paneer
    component.searchTerm.set('paneer');
    fixture.detectChanges();
    expect(component.filteredFoods().length).toBe(1);
    expect(component.filteredFoods()[0].name).toBe('Paneer Tikka');

    // Reset search, filter available only
    component.searchTerm.set('');
    component.selectedAvailabilityFilter.set('available');
    fixture.detectChanges();
    expect(component.filteredFoods().length).toBe(1);
    expect(component.filteredFoods()[0].id).toBe('101');

    // Filter non-veg
    component.selectedAvailabilityFilter.set('all');
    component.selectedDietFilter.set('non-veg');
    fixture.detectChanges();
    expect(component.filteredFoods().length).toBe(1);
    expect(component.filteredFoods()[0].name).toBe('Butter Chicken');
  });

  it('should open Add Food modal and create dish on valid submission', () => {
    vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });
    vi.spyOn(adminMenuService, 'loadFoods').mockImplementation(() => {
      (adminMenuService as any).foodsState.set(mockFoods);
      return of(mockFoods);
    });

    const createSpy = vi.spyOn(adminMenuService, 'createFood').mockImplementation((req) => {
      const newFood: Food = {
        id: '103',
        name: req.name,
        description: req.description,
        price: req.price,
        rating: 0,
        image: req.image || '',
        category: 'Starters',
        categorySlug: 'starters',
        isVeg: req.veg,
        isPopular: req.popular || false,
        isAvailable: req.available !== false,
      };
      (adminMenuService as any).foodsState.update((f: Food[]) => [...f, newFood]);
      return of(newFood);
    });

    fixture = TestBed.createComponent(AdminMenu);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.openAddModal();
    fixture.detectChanges();

    expect(component.isModalOpen()).toBe(true);

    component.foodForm.setValue({
      name: 'Crispy Corn',
      description: 'Golden fried corn kernels with spices',
      categoryId: '1',
      price: 180,
      image: '',
      isVeg: true,
      isPopular: false,
      isAvailable: true,
    });

    component.onSubmitFood();
    fixture.detectChanges();

    expect(createSpy).toHaveBeenCalledWith({
      name: 'Crispy Corn',
      description: 'Golden fried corn kernels with spices',
      price: 180,
      rating: 0,
      image: undefined,
      veg: true,
      popular: false,
      available: true,
      categoryId: 1,
    });
    expect(component.isModalOpen()).toBe(false);
  });

  it('should open edit modal and save updated dish details', () => {
    vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });
    vi.spyOn(adminMenuService, 'loadFoods').mockImplementation(() => {
      (adminMenuService as any).foodsState.set(mockFoods);
      return of(mockFoods);
    });

    const updateSpy = vi.spyOn(adminMenuService, 'updateFood').mockImplementation((id, req) => {
      const updated: Food = {
        id,
        name: req.name,
        description: req.description,
        price: req.price,
        rating: 4.6,
        image: req.image || '',
        category: 'Starters',
        categorySlug: 'starters',
        isVeg: req.veg,
        isPopular: req.popular,
        isAvailable: req.available,
      };
      return of(updated);
    });

    fixture = TestBed.createComponent(AdminMenu);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.openEditModal(mockFoods[0]);
    expect(component.isModalOpen()).toBe(true);
    expect(component.foodForm.get('name')?.value).toBe('Paneer Tikka');

    component.foodForm.patchValue({ price: 260 });
    component.onSubmitFood();

    expect(updateSpy).toHaveBeenCalledWith('101', {
      name: 'Paneer Tikka',
      description: 'Grilled spiced paneer cubes',
      price: 260,
      rating: 4.6,
      image: '/images/paneer.jpg',
      veg: true,
      popular: true,
      available: true,
      categoryId: 1,
    });
    expect(component.isModalOpen()).toBe(false);
  });

  it('should toggle availability via toggleAvailability method', () => {
    vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });
    vi.spyOn(adminMenuService, 'loadFoods').mockImplementation(() => {
      (adminMenuService as any).foodsState.set(mockFoods);
      return of(mockFoods);
    });

    const toggleSpy = vi.spyOn(adminMenuService, 'toggleAvailability').mockReturnValue(
      of({ ...mockFoods[0], isAvailable: false })
    );

    fixture = TestBed.createComponent(AdminMenu);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.toggleAvailability(mockFoods[0]);

    expect(toggleSpy).toHaveBeenCalledWith(mockFoods[0], 1);
  });

  it('should delete food item upon confirmation in delete modal', () => {
    vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });
    vi.spyOn(adminMenuService, 'loadFoods').mockImplementation(() => {
      (adminMenuService as any).foodsState.set(mockFoods);
      return of(mockFoods);
    });

    const deleteSpy = vi.spyOn(adminMenuService, 'deleteFood').mockReturnValue(of(undefined));

    fixture = TestBed.createComponent(AdminMenu);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.openDeleteModal(mockFoods[0]);
    expect(component.foodToDelete()).toEqual(mockFoods[0]);

    component.confirmDelete();
    expect(deleteSpy).toHaveBeenCalledWith('101');
    expect(component.foodToDelete()).toBeNull();
  });
});
