import { Injectable, computed, inject, signal } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of, tap } from 'rxjs';
import {
  MediaStatusResponse,
  MediaUploadResponse,
  RestaurantSettings,
  UpdateRestaurantSettingsPayload,
} from '../../shared/models/restaurant-settings.model';

const HEX_COLOR_REGEX = /^#(?:[0-9a-fA-F]{3}){1,2}$/;

@Injectable({
  providedIn: 'root',
})
export class RestaurantSettingsService {
  private readonly http = inject(HttpClient);
  private readonly titleService = inject(Title);

  readonly settings = signal<RestaurantSettings | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  /** Readonly reactive derived signals with safe defaults */
  readonly restaurantName = computed(
    () => this.settings()?.restaurantName || 'RestaurantHub'
  );
  readonly tagline = computed(
    () => this.settings()?.tagline || 'Fresh food, delivered with care'
  );
  readonly phone = computed(() => this.settings()?.phone || '9876543210');
  readonly email = computed(
    () => this.settings()?.email || 'contact@restauranthub.com'
  );
  readonly addressLine1 = computed(
    () => this.settings()?.addressLine1 || '123 Gourmet Boulevard'
  );
  readonly addressLine2 = computed(() => this.settings()?.addressLine2 || '');
  readonly city = computed(() => this.settings()?.city || 'Mumbai');
  readonly state = computed(() => this.settings()?.state || 'Maharashtra');
  readonly pinCode = computed(() => this.settings()?.pinCode || '400001');
  readonly currencyCode = computed(
    () => this.settings()?.currencyCode || 'INR'
  );
  readonly currencySymbol = computed(
    () => this.settings()?.currencySymbol || '₹'
  );
  readonly deliveryFee = computed(() => this.settings()?.deliveryFee ?? 40);
  readonly freeDeliveryThreshold = computed(
    () => this.settings()?.freeDeliveryThreshold ?? 500
  );
  readonly estimatedDeliveryMinutes = computed(
    () => this.settings()?.estimatedDeliveryMinutes ?? 35
  );
  readonly isAcceptingOrders = computed(
    () => this.settings()?.acceptingOrders ?? true
  );
  readonly logoUrl = computed(() => this.settings()?.logoUrl || null);
  readonly heroImageUrl = computed(() => this.settings()?.heroImageUrl || null);
  readonly primaryColor = computed(
    () => this.settings()?.primaryColor || '#FF6B00'
  );
  readonly secondaryColor = computed(
    () => this.settings()?.secondaryColor || '#1E293B'
  );
  readonly gstin = computed(() => this.settings()?.gstin || null);
  readonly fssaiNumber = computed(() => this.settings()?.fssaiNumber || null);
  readonly openingTime = computed(() => this.settings()?.openingTime || null);
  readonly closingTime = computed(() => this.settings()?.closingTime || null);

  constructor() {
    this.loadSettings();
  }

  /**
   * Updates browser document title dynamically based on restaurant branding.
   */
  private updateDocumentTitle(name?: string | null): void {
    const brandName = name?.trim() || 'RestaurantHub';
    this.titleService.setTitle(`${brandName} | Online Ordering`);
  }

  /**
   * Fetches customer-safe restaurant settings from the backend.
   */
  loadSettings(): void {
    this.loading.set(true);
    this.error.set(null);

    this.http
      .get<RestaurantSettings>('/api/v1/settings')
      .pipe(
        tap((data) => {
          this.settings.set(data);
          this.loading.set(false);
          this.applyThemeColors(data.primaryColor, data.secondaryColor);
          this.updateDocumentTitle(data.restaurantName);
        }),
        catchError((err) => {
          this.loading.set(false);
          this.error.set('Failed to load restaurant settings.');
          this.updateDocumentTitle('RestaurantHub');
          return of(null);
        })
      )
      .subscribe();
  }

  /**
   * Admin mutation to update restaurant business settings and branding.
   */
  updateSettings(
    payload: UpdateRestaurantSettingsPayload
  ): Observable<RestaurantSettings> {
    return this.http
      .put<RestaurantSettings>('/api/v1/admin/settings', payload)
      .pipe(
        tap((updated) => {
          this.settings.set(updated);
          this.applyThemeColors(updated.primaryColor, updated.secondaryColor);
          this.updateDocumentTitle(updated.restaurantName);
        })
      );
  }

  /**
   * Checks if remote media upload provider is active.
   */
  checkMediaStatus(): Observable<MediaStatusResponse> {
    return this.http.get<MediaStatusResponse>('/api/v1/admin/media/status');
  }

  /**
   * Uploads an image asset to media storage with a controlled purpose (FOOD, LOGO, HERO).
   */
  uploadMedia(
    file: File,
    purpose: string = 'FOOD'
  ): Observable<MediaUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('purpose', purpose);
    formData.append('folder', purpose.toLowerCase());
    return this.http.post<MediaUploadResponse>(
      '/api/v1/admin/media/images',
      formData
    );
  }

  /**
   * Safely applies validated CSS custom properties to the document root without arbitrary injection.
   */
  private applyThemeColors(
    primary?: string | null,
    secondary?: string | null
  ): void {
    if (typeof document === 'undefined' || !document.documentElement) {
      return;
    }

    if (primary && HEX_COLOR_REGEX.test(primary)) {
      document.documentElement.style.setProperty(
        '--restaurant-primary',
        primary
      );
    }
    if (secondary && HEX_COLOR_REGEX.test(secondary)) {
      document.documentElement.style.setProperty(
        '--restaurant-secondary',
        secondary
      );
    }
  }
}
