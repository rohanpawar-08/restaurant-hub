import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { RestaurantSettingsService } from './restaurant-settings.service';
import { RestaurantSettings, UpdateRestaurantSettingsPayload } from '../../shared/models/restaurant-settings.model';

describe('RestaurantSettingsService', () => {
  let service: RestaurantSettingsService;
  let httpMock: HttpTestingController;

  const mockSettings: RestaurantSettings = {
    id: 1,
    restaurantName: 'Spice Symphony',
    tagline: 'Flavors of India',
    phone: '9876543210',
    email: 'info@spicesymphony.com',
    addressLine1: '100 Marine Drive',
    addressLine2: 'Nariman Point',
    city: 'Mumbai',
    state: 'Maharashtra',
    pinCode: '400021',
    currencyCode: 'INR',
    currencySymbol: '₹',
    deliveryFee: 50,
    freeDeliveryThreshold: 600,
    estimatedDeliveryMinutes: 40,
    gstin: '27AABCU9603R1ZM',
    fssaiNumber: '11521000000001',
    openingTime: '10:00:00',
    closingTime: '23:00:00',
    acceptingOrders: true,
    logoUrl: 'https://example.com/logo.png',
    heroImageUrl: 'https://example.com/hero.png',
    primaryColor: '#FF6B00',
    secondaryColor: '#1E293B',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        RestaurantSettingsService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    service = TestBed.inject(RestaurantSettingsService);

    // Handle constructor initial loadSettings() request
    const initReq = httpMock.expectOne('/api/v1/settings');
    expect(initReq.request.method).toBe('GET');
    initReq.flush(mockSettings);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created and populate settings reactively', () => {
    expect(service).toBeTruthy();
    expect(service.restaurantName()).toBe('Spice Symphony');
    expect(service.tagline()).toBe('Flavors of India');
    expect(service.phone()).toBe('9876543210');
    expect(service.deliveryFee()).toBe(50);
    expect(service.freeDeliveryThreshold()).toBe(600);
    expect(service.isAcceptingOrders()).toBe(true);
  });

  it('should update settings via PUT /api/v1/admin/settings and update signals', () => {
    const updatePayload: UpdateRestaurantSettingsPayload = {
      restaurantName: 'Spice Symphony Express',
      tagline: 'Quick Indian Flavors',
      phone: '9876543210',
      email: 'info@spicesymphony.com',
      addressLine1: '100 Marine Drive',
      city: 'Mumbai',
      state: 'Maharashtra',
      pinCode: '400021',
      currencyCode: 'INR',
      currencySymbol: '₹',
      deliveryFee: 30,
      freeDeliveryThreshold: 450,
      estimatedDeliveryMinutes: 25,
      acceptingOrders: false,
      primaryColor: '#FF5722',
      secondaryColor: '#0F172A',
    };

    const updatedResponse: RestaurantSettings = {
      ...mockSettings,
      ...updatePayload,
    };

    service.updateSettings(updatePayload).subscribe((res) => {
      expect(res.restaurantName).toBe('Spice Symphony Express');
      expect(res.deliveryFee).toBe(30);
      expect(res.acceptingOrders).toBe(false);
    });

    const updateReq = httpMock.expectOne('/api/v1/admin/settings');
    expect(updateReq.request.method).toBe('PUT');
    expect(updateReq.request.body).toEqual(updatePayload);
    updateReq.flush(updatedResponse);

    expect(service.restaurantName()).toBe('Spice Symphony Express');
    expect(service.deliveryFee()).toBe(30);
    expect(service.isAcceptingOrders()).toBe(false);
  });

  it('should check media status via GET /api/v1/admin/media/status', () => {
    service.checkMediaStatus().subscribe((status) => {
      expect(status.available).toBe(true);
      expect(status.provider).toBe('Cloudinary');
    });

    const req = httpMock.expectOne('/api/v1/admin/media/status');
    expect(req.request.method).toBe('GET');
    req.flush({ available: true, provider: 'Cloudinary' });
  });

  it('should upload media via POST /api/v1/admin/media/images', () => {
    const file = new File(['dummy content'], 'food.jpg', { type: 'image/jpeg' });

    service.uploadMedia(file, 'restauranthub/food').subscribe((result) => {
      expect(result.url).toBe('https://res.cloudinary.com/demo/image/upload/food.jpg');
      expect(result.publicId).toBe('food_123');
    });

    const req = httpMock.expectOne('/api/v1/admin/media/images');
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBe(true);
    req.flush({
      url: 'https://res.cloudinary.com/demo/image/upload/food.jpg',
      publicId: 'food_123',
    });
  });
});
