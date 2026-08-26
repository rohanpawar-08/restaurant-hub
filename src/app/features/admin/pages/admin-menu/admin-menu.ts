import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AdminMenuService } from '../../../../core/services/admin-menu.service';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';
import { Food } from '../../../../shared/models/food.model';
import { FoodCategory } from '../../../../shared/models/category.model';
import { CreateFoodApiRequest, UpdateFoodApiRequest } from '../../../../core/api/models/admin-api.model';

@Component({
  selector: 'app-admin-menu',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './admin-menu.html',
  styleUrl: './admin-menu.scss',
})
export class AdminMenu implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly adminMenuService = inject(AdminMenuService);
  private readonly settingsService = inject(RestaurantSettingsService);

  readonly foods = this.adminMenuService.foods;
  readonly categories = this.adminMenuService.categories;
  readonly isLoading = this.adminMenuService.isLoading;
  readonly isSaving = this.adminMenuService.isSaving;
  readonly isDeleting = this.adminMenuService.isDeleting;
  readonly generalError = this.adminMenuService.errorMessage;

  // Media state
  readonly isMediaAvailable = signal<boolean>(false);
  readonly isUploadingImage = signal<boolean>(false);
  readonly uploadImageError = signal<string | null>(null);

  // Filter States
  readonly searchTerm = signal('');
  readonly selectedCategoryFilter = signal('all');
  readonly selectedAvailabilityFilter = signal<'all' | 'available' | 'unavailable'>('all');
  readonly selectedDietFilter = signal<'all' | 'veg' | 'non-veg'>('all');

  // Filtered computed list
  readonly filteredFoods = computed(() => {
    const search = this.searchTerm().toLowerCase().trim();
    const catFilter = this.selectedCategoryFilter();
    const availFilter = this.selectedAvailabilityFilter();
    const dietFilter = this.selectedDietFilter();

    return this.foods().filter((item) => {
      // Search filter
      if (search) {
        const matchesName = item.name.toLowerCase().includes(search);
        const matchesDesc = item.description.toLowerCase().includes(search);
        const matchesCat = item.category.toLowerCase().includes(search);
        if (!matchesName && !matchesDesc && !matchesCat) return false;
      }

      // Category filter
      if (catFilter !== 'all' && item.categorySlug !== catFilter) {
        return false;
      }

      // Availability filter
      if (availFilter === 'available' && !item.isAvailable) return false;
      if (availFilter === 'unavailable' && item.isAvailable) return false;

      // Diet filter
      if (dietFilter === 'veg' && !item.isVeg) return false;
      if (dietFilter === 'non-veg' && item.isVeg) return false;

      return true;
    });
  });

  // Modal State
  readonly isModalOpen = signal(false);
  readonly editingFood = signal<Food | null>(null);
  readonly formError = signal<string | null>(null);

  // Delete Confirmation State
  readonly foodToDelete = signal<Food | null>(null);
  readonly deleteError = signal<string | null>(null);

  // Reactive Food Form
  readonly foodForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(150)]],
    description: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(1000)]],
    categoryId: ['', [Validators.required]],
    price: [
      null as number | null,
      [Validators.required, Validators.min(0.01), Validators.max(99999.99)],
    ],
    image: [''],
    isVeg: [true, [Validators.required]],
    isPopular: [false],
    isAvailable: [true],
  });

  ngOnInit(): void {
    this.loadData();
    this.checkMediaStatus();
  }

  checkMediaStatus(): void {
    this.settingsService.checkMediaStatus().subscribe({
      next: (status) => this.isMediaAvailable.set(status.available),
      error: () => this.isMediaAvailable.set(false),
    });
  }

  onFoodImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    this.isUploadingImage.set(true);
    this.uploadImageError.set(null);

    this.settingsService.uploadMedia(file, 'restauranthub/food').subscribe({
      next: (res) => {
        this.foodForm.patchValue({ image: res.url });
        this.isUploadingImage.set(false);
      },
      error: (err) => {
        this.isUploadingImage.set(false);
        this.uploadImageError.set(err?.error?.message || 'Image upload failed. You can enter an image URL directly.');
      },
    });
  }

  loadData(): void {
    this.adminMenuService.loadCategories().subscribe({ error: () => {} });
    this.adminMenuService.loadFoods().subscribe({ error: () => {} });
  }

  getCategoryIdForFood(food: Food): number {
    const cat = this.categories().find((c) => c.slug === food.categorySlug);
    return cat ? Number(cat.id) : 1;
  }

  openAddModal(): void {
    this.editingFood.set(null);
    this.formError.set(null);

    // Default to first active category if available
    const activeCats = this.categories().filter((c) => c.isActive !== false);
    const defaultCatId = activeCats.length > 0 ? activeCats[0].id : '';

    this.foodForm.reset({
      name: '',
      description: '',
      categoryId: defaultCatId,
      price: null,
      image: '',
      isVeg: true,
      isPopular: false,
      isAvailable: true,
    });
    this.isModalOpen.set(true);
  }

  openEditModal(food: Food): void {
    this.editingFood.set(food);
    this.formError.set(null);

    const catId = this.getCategoryIdForFood(food);

    this.foodForm.reset({
      name: food.name,
      description: food.description,
      categoryId: String(catId),
      price: food.price,
      image: food.image || '',
      isVeg: food.isVeg,
      isPopular: food.isPopular,
      isAvailable: food.isAvailable,
    });
    this.isModalOpen.set(true);
  }

  closeModal(): void {
    this.isModalOpen.set(false);
    this.editingFood.set(null);
    this.formError.set(null);
  }

  onSubmitFood(): void {
    if (this.foodForm.invalid) {
      this.foodForm.markAllAsTouched();
      return;
    }

    const { name, description, categoryId, price, image, isVeg, isPopular, isAvailable } =
      this.foodForm.getRawValue();

    const editing = this.editingFood();

    if (editing) {
      const req: UpdateFoodApiRequest = {
        name: name!.trim(),
        description: description!.trim(),
        price: Number(price),
        rating: editing.rating,
        image: image?.trim() || undefined,
        veg: Boolean(isVeg),
        popular: Boolean(isPopular),
        available: Boolean(isAvailable),
        categoryId: Number(categoryId),
      };

      this.adminMenuService.updateFood(editing.id, req).subscribe({
        next: () => {
          this.closeModal();
        },
        error: (err) => {
          this.formError.set(err.message || 'Failed to update menu item.');
        },
      });
    } else {
      const req: CreateFoodApiRequest = {
        name: name!.trim(),
        description: description!.trim(),
        price: Number(price),
        rating: 0.0,
        image: image?.trim() || undefined,
        veg: Boolean(isVeg),
        popular: Boolean(isPopular),
        available: Boolean(isAvailable),
        categoryId: Number(categoryId),
      };

      this.adminMenuService.createFood(req).subscribe({
        next: () => {
          this.closeModal();
        },
        error: (err) => {
          this.formError.set(err.message || 'Failed to create menu item.');
        },
      });
    }
  }

  toggleAvailability(food: Food): void {
    const catId = this.getCategoryIdForFood(food);
    this.adminMenuService.toggleAvailability(food, catId).subscribe({
      error: (err) => {
        alert(err.message);
      },
    });
  }

  openDeleteModal(food: Food): void {
    this.foodToDelete.set(food);
    this.deleteError.set(null);
  }

  closeDeleteModal(): void {
    this.foodToDelete.set(null);
    this.deleteError.set(null);
  }

  confirmDelete(): void {
    const toDelete = this.foodToDelete();
    if (!toDelete) return;

    this.adminMenuService.deleteFood(toDelete.id).subscribe({
      next: () => {
        this.closeDeleteModal();
      },
      error: (err) => {
        this.deleteError.set(err.message || 'Failed to delete menu item.');
      },
    });
  }
}
