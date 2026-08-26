import { Component, OnInit, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';
import { UpdateRestaurantSettingsPayload } from '../../../../shared/models/restaurant-settings.model';

@Component({
  selector: 'app-admin-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-settings.html',
  styleUrl: './admin-settings.scss',
})
export class AdminSettings implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly settingsService = inject(RestaurantSettingsService);

  readonly settingsForm: FormGroup;
  readonly isSaving = signal<boolean>(false);
  readonly successMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly isMediaAvailable = signal<boolean>(false);
  readonly mediaProvider = signal<string>('None');
  readonly isUploadingLogo = signal<boolean>(false);
  readonly isUploadingHero = signal<boolean>(false);
  readonly uploadLogoError = signal<string | null>(null);
  readonly uploadHeroError = signal<string | null>(null);

  constructor() {
    this.settingsForm = this.fb.group({
      restaurantName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(150)]],
      tagline: ['', [Validators.maxLength(255)]],
      phone: ['', [Validators.required, Validators.pattern('^(?:\\+91)?[6-9]\\d{9}$')]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      addressLine1: ['', [Validators.required, Validators.maxLength(255)]],
      addressLine2: ['', [Validators.maxLength(255)]],
      city: ['', [Validators.required, Validators.maxLength(100)]],
      state: ['', [Validators.required, Validators.maxLength(100)]],
      pinCode: ['', [Validators.required, Validators.pattern('^[1-9][0-9]{5}$')]],
      currencyCode: ['INR', [Validators.required, Validators.maxLength(10)]],
      currencySymbol: ['₹', [Validators.required, Validators.maxLength(10)]],
      deliveryFee: [40, [Validators.required, Validators.min(0)]],
      freeDeliveryThreshold: [500, [Validators.required, Validators.min(0)]],
      estimatedDeliveryMinutes: [35, [Validators.required, Validators.min(5), Validators.max(240)]],
      gstin: ['', [Validators.pattern('^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$')]],
      fssaiNumber: ['', [Validators.pattern('^$|^[0-9]{14}$')]],
      openingTime: ['09:00'],
      closingTime: ['23:00'],
      acceptingOrders: [true, [Validators.required]],
      logoUrl: ['', [Validators.maxLength(500)]],
      heroImageUrl: ['', [Validators.maxLength(500)]],
      primaryColor: ['#FF6B00', [Validators.pattern('^$|^#(?:[0-9a-fA-F]{3}){1,2}$')]],
      secondaryColor: ['#1E293B', [Validators.pattern('^$|^#(?:[0-9a-fA-F]{3}){1,2}$')]],
    });

    // Populate form reactively when settings signal updates
    effect(() => {
      const current = this.settingsService.settings();
      if (current) {
        this.populateForm(current);
      }
    });
  }

  ngOnInit(): void {
    this.checkMediaStatus();
  }

  private populateForm(data: any): void {
    this.settingsForm.patchValue({
      restaurantName: data.restaurantName || '',
      tagline: data.tagline || '',
      phone: data.phone || '',
      email: data.email || '',
      addressLine1: data.addressLine1 || '',
      addressLine2: data.addressLine2 || '',
      city: data.city || '',
      state: data.state || '',
      pinCode: data.pinCode || '',
      currencyCode: data.currencyCode || 'INR',
      currencySymbol: data.currencySymbol || '₹',
      deliveryFee: data.deliveryFee ?? 40,
      freeDeliveryThreshold: data.freeDeliveryThreshold ?? 500,
      estimatedDeliveryMinutes: data.estimatedDeliveryMinutes ?? 35,
      gstin: data.gstin || '',
      fssaiNumber: data.fssaiNumber || '',
      openingTime: data.openingTime ? data.openingTime.substring(0, 5) : '09:00',
      closingTime: data.closingTime ? data.closingTime.substring(0, 5) : '23:00',
      acceptingOrders: data.acceptingOrders ?? true,
      logoUrl: data.logoUrl || '',
      heroImageUrl: data.heroImageUrl || '',
      primaryColor: data.primaryColor || '#FF6B00',
      secondaryColor: data.secondaryColor || '#1E293B',
    });
  }

  checkMediaStatus(): void {
    this.settingsService.checkMediaStatus().subscribe({
      next: (status) => {
        this.isMediaAvailable.set(status.available);
        this.mediaProvider.set(status.provider);
      },
      error: () => {
        this.isMediaAvailable.set(false);
        this.mediaProvider.set('None');
      },
    });
  }

  onLogoFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    this.isUploadingLogo.set(true);
    this.uploadLogoError.set(null);

    this.settingsService.uploadMedia(file, 'LOGO').subscribe({
      next: (res) => {
        this.settingsForm.patchValue({ logoUrl: res.url });
        this.isUploadingLogo.set(false);
      },
      error: (err) => {
        this.isUploadingLogo.set(false);
        this.uploadLogoError.set(err?.error?.message || 'Image upload failed. You can enter an image URL directly.');
      },
    });
  }

  onHeroFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    this.isUploadingHero.set(true);
    this.uploadHeroError.set(null);

    this.settingsService.uploadMedia(file, 'HERO').subscribe({
      next: (res) => {
        this.settingsForm.patchValue({ heroImageUrl: res.url });
        this.isUploadingHero.set(false);
      },
      error: (err) => {
        this.isUploadingHero.set(false);
        this.uploadHeroError.set(err?.error?.message || 'Image upload failed. You can enter an image URL directly.');
      },
    });
  }

  onSubmit(): void {
    this.successMessage.set(null);
    this.errorMessage.set(null);

    if (this.settingsForm.invalid) {
      this.settingsForm.markAllAsTouched();
      this.errorMessage.set('Please correct the highlighted fields in the form.');
      return;
    }

    this.isSaving.set(true);
    const formVal = this.settingsForm.value;

    const payload: UpdateRestaurantSettingsPayload = {
      restaurantName: formVal.restaurantName.trim(),
      tagline: formVal.tagline ? formVal.tagline.trim() : null,
      phone: formVal.phone.trim(),
      email: formVal.email.trim(),
      addressLine1: formVal.addressLine1.trim(),
      addressLine2: formVal.addressLine2 ? formVal.addressLine2.trim() : null,
      city: formVal.city.trim(),
      state: formVal.state.trim(),
      pinCode: formVal.pinCode.trim(),
      currencyCode: formVal.currencyCode.trim(),
      currencySymbol: formVal.currencySymbol.trim(),
      deliveryFee: Number(formVal.deliveryFee),
      freeDeliveryThreshold: Number(formVal.freeDeliveryThreshold),
      estimatedDeliveryMinutes: Number(formVal.estimatedDeliveryMinutes),
      gstin: formVal.gstin ? formVal.gstin.trim() : null,
      fssaiNumber: formVal.fssaiNumber ? formVal.fssaiNumber.trim() : null,
      openingTime: formVal.openingTime ? (formVal.openingTime.length === 5 ? formVal.openingTime + ':00' : formVal.openingTime) : null,
      closingTime: formVal.closingTime ? (formVal.closingTime.length === 5 ? formVal.closingTime + ':00' : formVal.closingTime) : null,
      acceptingOrders: !!formVal.acceptingOrders,
      logoUrl: formVal.logoUrl ? formVal.logoUrl.trim() : null,
      heroImageUrl: formVal.heroImageUrl ? formVal.heroImageUrl.trim() : null,
      primaryColor: formVal.primaryColor ? formVal.primaryColor.trim() : '#FF6B00',
      secondaryColor: formVal.secondaryColor ? formVal.secondaryColor.trim() : '#1E293B',
    };

    this.settingsService.updateSettings(payload).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.successMessage.set('Restaurant settings updated successfully! Changes are live across the store.');
        if (typeof window !== 'undefined') {
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
      },
      error: (err) => {
        this.isSaving.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to update restaurant settings. Please try again.');
      },
    });
  }
}
