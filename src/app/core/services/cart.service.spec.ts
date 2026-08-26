import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { CartService } from './cart.service';
import { RestaurantSettingsService } from './restaurant-settings.service';
import { Food } from '../../shared/models/food.model';

describe('CartService', () => {
  let service: CartService;
  let settingsServiceMock: {
    deliveryFee: ReturnType<typeof signal<number>>;
    freeDeliveryThreshold: ReturnType<typeof signal<number>>;
  };

  const mockFood: Food = {
    id: '1',
    name: 'Paneer Biryani',
    description: 'Fragrant basmati rice with spiced paneer',
    category: 'Biryani',
    categorySlug: 'biryani',
    price: 300,
    rating: 4.7,
    image: 'biryani.jpg',
    isVeg: true,
    isPopular: true,
  };

  beforeEach(() => {
    if (typeof localStorage !== 'undefined') {
      localStorage.clear();
    }

    settingsServiceMock = {
      deliveryFee: signal(50),
      freeDeliveryThreshold: signal(600),
    };

    TestBed.configureTestingModule({
      providers: [
        CartService,
        { provide: RestaurantSettingsService, useValue: settingsServiceMock },
      ],
    });

    service = TestBed.inject(CartService);
    service.clearCart();
  });

  afterEach(() => {
    if (typeof localStorage !== 'undefined') {
      localStorage.clear();
    }
  });

  it('should be created with an empty cart and 0 delivery fee', () => {
    expect(service).toBeTruthy();
    expect(service.isEmpty()).toBe(true);
    expect(service.subtotal()).toBe(0);
    expect(service.deliveryFee()).toBe(0);
    expect(service.grandTotal()).toBe(0);
  });

  it('should calculate dynamic delivery fee when subtotal is below threshold', () => {
    service.addToCart(mockFood); // 1 x 300 = 300 (< 600)

    expect(service.subtotal()).toBe(300);
    expect(service.deliveryFee()).toBe(50);
    expect(service.grandTotal()).toBe(350);
  });

  it('should apply free delivery when subtotal meets or exceeds threshold', () => {
    service.addToCart(mockFood);
    service.addToCart(mockFood); // 2 x 300 = 600 (>= 600)

    expect(service.subtotal()).toBe(600);
    expect(service.deliveryFee()).toBe(0);
    expect(service.grandTotal()).toBe(600);
  });

  it('should adapt immediately when restaurant settings threshold/fee update dynamically', () => {
    service.addToCart(mockFood); // 300

    expect(service.deliveryFee()).toBe(50);

    // Update restaurant settings threshold to 250
    settingsServiceMock.freeDeliveryThreshold.set(250);
    expect(service.deliveryFee()).toBe(0); // Now 300 >= 250 -> free delivery!

    // Update settings threshold back to 400 with 60 delivery fee
    settingsServiceMock.freeDeliveryThreshold.set(400);
    settingsServiceMock.deliveryFee.set(60);
    expect(service.deliveryFee()).toBe(60);
    expect(service.grandTotal()).toBe(360);
  });
});
