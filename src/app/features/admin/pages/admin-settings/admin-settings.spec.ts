import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { AdminSettings } from './admin-settings';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';
import { RestaurantSettings } from '../../../../shared/models/restaurant-settings.model';

describe('AdminSettings', () => {
  let component: AdminSettings;
  let fixture: ComponentFixture<AdminSettings>;
  let settingsServiceMock: {
    settings: ReturnType<typeof signal<RestaurantSettings | null>>;
    updateSettings: ReturnType<typeof vi.fn>;
    checkMediaStatus: ReturnType<typeof vi.fn>;
    uploadMedia: ReturnType<typeof vi.fn>;
  };

  const mockSettings: RestaurantSettings = {
    id: 1,
    restaurantName: 'Royal Spice Bistro',
    tagline: 'Authentic Indian Flavors',
    phone: '9876543210',
    email: 'contact@royalspice.com',
    addressLine1: '45 Palace Road',
    addressLine2: 'Near Central Park',
    city: 'Bengaluru',
    state: 'Karnataka',
    pinCode: '560001',
    currencyCode: 'INR',
    currencySymbol: '₹',
    deliveryFee: 40,
    freeDeliveryThreshold: 500,
    estimatedDeliveryMinutes: 35,
    gstin: '29ABCDE1234F1Z5',
    fssaiNumber: '11521000000001',
    openingTime: '09:00:00',
    closingTime: '23:00:00',
    acceptingOrders: true,
    logoUrl: 'https://example.com/logo.png',
    heroImageUrl: 'https://example.com/hero.png',
    primaryColor: '#FF6B00',
    secondaryColor: '#1E293B',
  };

  beforeEach(async () => {
    settingsServiceMock = {
      settings: signal<RestaurantSettings | null>(mockSettings),
      updateSettings: vi.fn().mockReturnValue(of(mockSettings)),
      checkMediaStatus: vi.fn().mockReturnValue(of({ available: true, provider: 'Cloudinary' })),
      uploadMedia: vi.fn().mockReturnValue(of({ url: 'https://cdn.example.com/new-logo.png', publicId: 'logo_1' })),
    };

    await TestBed.configureTestingModule({
      imports: [AdminSettings],
      providers: [
        { provide: RestaurantSettingsService, useValue: settingsServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminSettings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and populate all 8 form sections with current settings', () => {
    expect(component).toBeTruthy();
    expect(component.settingsForm.get('restaurantName')?.value).toBe('Royal Spice Bistro');
    expect(component.settingsForm.get('phone')?.value).toBe('9876543210');
    expect(component.settingsForm.get('pinCode')?.value).toBe('560001');
    expect(component.settingsForm.get('deliveryFee')?.value).toBe(40);
    expect(component.settingsForm.get('freeDeliveryThreshold')?.value).toBe(500);
    expect(component.settingsForm.get('acceptingOrders')?.value).toBe(true);
    expect(component.isMediaAvailable()).toBe(true);
  });

  it('should validate invalid Indian mobile phone format', () => {
    const phoneControl = component.settingsForm.get('phone');
    phoneControl?.setValue('12345');
    phoneControl?.markAsTouched();
    expect(phoneControl?.valid).toBe(false);

    phoneControl?.setValue('9876543210');
    expect(phoneControl?.valid).toBe(true);
  });

  it('should validate invalid 6-digit Indian PIN code format', () => {
    const pinControl = component.settingsForm.get('pinCode');
    pinControl?.setValue('012345'); // Starts with 0
    expect(pinControl?.valid).toBe(false);

    pinControl?.setValue('560001');
    expect(pinControl?.valid).toBe(true);
  });

  it('should submit valid form and display success message', () => {
    component.onSubmit();

    expect(settingsServiceMock.updateSettings).toHaveBeenCalled();
    expect(component.successMessage()).toContain('Restaurant settings updated successfully');
    expect(component.isSaving()).toBe(false);
  });

  it('should handle update failure gracefully without blocking UI', () => {
    settingsServiceMock.updateSettings.mockReturnValue(
      throwError(() => ({ error: { message: 'Invalid GSTIN provided' } }))
    );

    component.onSubmit();

    expect(component.errorMessage()).toBe('Invalid GSTIN provided');
    expect(component.isSaving()).toBe(false);
  });

  it('should upload logo image and patch logoUrl in form', () => {
    const file = new File(['dummy'], 'logo.png', { type: 'image/png' });
    const event = {
      target: { files: [file] },
    } as unknown as Event;

    component.onLogoFileSelected(event);

    expect(settingsServiceMock.uploadMedia).toHaveBeenCalledWith(file, 'restauranthub/branding');
    expect(component.settingsForm.get('logoUrl')?.value).toBe('https://cdn.example.com/new-logo.png');
  });
});
