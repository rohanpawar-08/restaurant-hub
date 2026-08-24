import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { AdminCategories } from './admin-categories';
import { AdminMenuService } from '../../../../core/services/admin-menu.service';
import { FoodCategory } from '../../../../shared/models/category.model';

describe('AdminCategories Component', () => {
  let component: AdminCategories;
  let fixture: ComponentFixture<AdminCategories>;
  let adminMenuService: AdminMenuService;

  const mockCategories: FoodCategory[] = [
    { id: '1', name: 'Starters', slug: 'starters', icon: '🥟', isActive: true, itemCount: 5 },
    { id: '2', name: 'Main Course', slug: 'main-course', icon: '🍛', isActive: true, itemCount: 10 },
    { id: '3', name: 'Seasonal', slug: 'seasonal', icon: '🍽️', isActive: false, itemCount: 0 },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminCategories],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        AdminMenuService,
      ],
    }).compileComponents();

    adminMenuService = TestBed.inject(AdminMenuService);
  });

  it('should create component and load categories on init', () => {
    const loadSpy = vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });

    fixture = TestBed.createComponent(AdminCategories);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(loadSpy).toHaveBeenCalled();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Starters');
    expect(compiled.textContent).toContain('Main Course');
    expect(compiled.textContent).toContain('Seasonal');
  });

  it('should open Add Category modal and auto-generate slug on typing name', () => {
    vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });

    fixture = TestBed.createComponent(AdminCategories);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.openAddModal();
    fixture.detectChanges();

    expect(component.isModalOpen()).toBe(true);
    expect(component.editingCategory()).toBeNull();

    // Type into name field
    component.categoryForm.patchValue({ name: 'Ice Cream & Desserts' });
    component.onNameInput();

    expect(component.categoryForm.get('slug')?.value).toBe('ice-cream-desserts');
  });

  it('should create new category when submitting valid form', () => {
    vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });

    const createSpy = vi.spyOn(adminMenuService, 'createCategory').mockImplementation((req) => {
      const newCat: FoodCategory = { id: '4', name: req.name, slug: req.slug, icon: '🍽️', isActive: true, itemCount: 0 };
      (adminMenuService as any).categoriesState.update((c: FoodCategory[]) => [...c, newCat]);
      return of(newCat);
    });

    fixture = TestBed.createComponent(AdminCategories);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.openAddModal();
    component.categoryForm.setValue({
      name: 'Beverages',
      slug: 'beverages',
      active: true,
    });

    component.onSubmitCategory();
    fixture.detectChanges();

    expect(createSpy).toHaveBeenCalledWith({ name: 'Beverages', slug: 'beverages' });
    expect(component.isModalOpen()).toBe(false);
  });

  it('should display form error when duplicate slug error is returned by backend', () => {
    vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });

    vi.spyOn(adminMenuService, 'createCategory').mockReturnValue(
      throwError(() => new Error("Category already exists with slug: 'starters'"))
    );

    fixture = TestBed.createComponent(AdminCategories);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.openAddModal();
    component.categoryForm.setValue({
      name: 'Starters',
      slug: 'starters',
      active: true,
    });

    component.onSubmitCategory();
    fixture.detectChanges();

    expect(component.formError()).toContain("Category already exists with slug: 'starters'");
  });

  it('should open edit modal with prefilled data and save updates', () => {
    vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });

    const updateSpy = vi.spyOn(adminMenuService, 'updateCategory').mockImplementation((id, req) => {
      const updated: FoodCategory = { id, name: req.name, slug: req.slug, icon: '🥟', isActive: req.active, itemCount: 5 };
      return of(updated);
    });

    fixture = TestBed.createComponent(AdminCategories);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.openEditModal(mockCategories[0]);
    expect(component.isModalOpen()).toBe(true);
    expect(component.categoryForm.get('name')?.value).toBe('Starters');

    component.categoryForm.patchValue({ name: 'Hot Appetizers' });
    component.onSubmitCategory();

    expect(updateSpy).toHaveBeenCalledWith('1', {
      name: 'Hot Appetizers',
      slug: 'starters',
      active: true,
    });
    expect(component.isModalOpen()).toBe(false);
  });

  it('should toggle active status when clicking toggle button', () => {
    vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });

    const updateSpy = vi.spyOn(adminMenuService, 'updateCategory').mockReturnValue(
      of({ ...mockCategories[0], isActive: false })
    );

    fixture = TestBed.createComponent(AdminCategories);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.toggleActive(mockCategories[0]);

    expect(updateSpy).toHaveBeenCalledWith('1', {
      name: 'Starters',
      slug: 'starters',
      active: false,
    });
  });

  it('should open delete modal and handle 409 CategoryInUseException cleanly', () => {
    vi.spyOn(adminMenuService, 'loadCategories').mockImplementation(() => {
      (adminMenuService as any).categoriesState.set(mockCategories);
      return of(mockCategories);
    });

    vi.spyOn(adminMenuService, 'deleteCategory').mockReturnValue(
      throwError(() => new Error('This category still contains menu items. Move or remove those items before deleting the category, or deactivate the category instead.'))
    );

    fixture = TestBed.createComponent(AdminCategories);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.openDeleteModal(mockCategories[0]);
    expect(component.categoryToDelete()).toEqual(mockCategories[0]);

    component.confirmDelete();
    fixture.detectChanges();

    expect(component.deleteError()).toContain('This category still contains menu items');
    expect(component.categoryToDelete()).not.toBeNull();
  });
});
