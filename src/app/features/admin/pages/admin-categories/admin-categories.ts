import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AdminMenuService } from '../../../../core/services/admin-menu.service';
import { FoodCategory } from '../../../../shared/models/category.model';
import { CreateCategoryApiRequest, UpdateCategoryApiRequest } from '../../../../core/api/models/admin-api.model';

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './admin-categories.html',
  styleUrl: './admin-categories.scss',
})
export class AdminCategories implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly adminMenuService = inject(AdminMenuService);

  readonly categories = this.adminMenuService.categories;
  readonly isLoading = this.adminMenuService.isLoading;
  readonly isSaving = this.adminMenuService.isSaving;
  readonly isDeleting = this.adminMenuService.isDeleting;
  readonly generalError = this.adminMenuService.errorMessage;

  // Modal State
  readonly isModalOpen = signal(false);
  readonly editingCategory = signal<FoodCategory | null>(null);
  readonly formError = signal<string | null>(null);

  // Delete Confirmation State
  readonly categoryToDelete = signal<FoodCategory | null>(null);
  readonly deleteError = signal<string | null>(null);

  // Reactive Category Form
  readonly categoryForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    slug: [
      '',
      [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100),
        Validators.pattern(/^[a-z0-9]+(-[a-z0-9]+)*$/),
      ],
    ],
    active: [true],
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.adminMenuService.loadCategories().subscribe({
      error: () => {},
    });
  }

  onNameInput(): void {
    // If creating a new category, auto-generate the slug from the name
    if (!this.editingCategory()) {
      const nameVal = this.categoryForm.get('name')?.value || '';
      const autoSlug = nameVal
        .toLowerCase()
        .trim()
        .replace(/[^a-z0-9\s-]/g, '')
        .replace(/\s+/g, '-');
      this.categoryForm.patchValue({ slug: autoSlug }, { emitEvent: false });
    }
  }

  openAddModal(): void {
    this.editingCategory.set(null);
    this.formError.set(null);
    this.categoryForm.reset({
      name: '',
      slug: '',
      active: true,
    });
    this.isModalOpen.set(true);
  }

  openEditModal(category: FoodCategory): void {
    this.editingCategory.set(category);
    this.formError.set(null);
    this.categoryForm.reset({
      name: category.name,
      slug: category.slug,
      active: category.isActive !== false,
    });
    this.isModalOpen.set(true);
  }

  closeModal(): void {
    this.isModalOpen.set(false);
    this.editingCategory.set(null);
    this.formError.set(null);
  }

  onSubmitCategory(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    const { name, slug, active } = this.categoryForm.getRawValue();
    const editing = this.editingCategory();

    if (editing) {
      const req: UpdateCategoryApiRequest = {
        name: name!.trim(),
        slug: slug!.trim(),
        active: Boolean(active),
      };

      this.adminMenuService.updateCategory(editing.id, req).subscribe({
        next: () => {
          this.closeModal();
        },
        error: (err) => {
          this.formError.set(err.message || 'Failed to update category.');
        },
      });
    } else {
      const req: CreateCategoryApiRequest = {
        name: name!.trim(),
        slug: slug!.trim(),
      };

      this.adminMenuService.createCategory(req).subscribe({
        next: () => {
          this.closeModal();
        },
        error: (err) => {
          this.formError.set(err.message || 'Failed to create category.');
        },
      });
    }
  }

  toggleActive(category: FoodCategory): void {
    const currentActive = category.isActive !== false;
    const req: UpdateCategoryApiRequest = {
      name: category.name,
      slug: category.slug,
      active: !currentActive,
    };

    this.adminMenuService.updateCategory(category.id, req).subscribe({
      error: (err) => {
        alert(err.message);
      },
    });
  }

  openDeleteModal(category: FoodCategory): void {
    this.categoryToDelete.set(category);
    this.deleteError.set(null);
  }

  closeDeleteModal(): void {
    this.categoryToDelete.set(null);
    this.deleteError.set(null);
  }

  confirmDelete(): void {
    const toDelete = this.categoryToDelete();
    if (!toDelete) return;

    this.adminMenuService.deleteCategory(toDelete.id).subscribe({
      next: () => {
        this.closeDeleteModal();
      },
      error: (err) => {
        this.deleteError.set(err.message || 'Failed to delete category.');
      },
    });
  }
}
