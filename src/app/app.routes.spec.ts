import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideLocationMocks } from '@angular/common/testing';
import { Location } from '@angular/common';
import { routes } from './app.routes';
import { AuthService } from './core/services/auth.service';
import { CartService } from './core/services/cart.service';
import { RestaurantSettingsService } from './core/services/restaurant-settings.service';
import { FoodService } from './core/services/food.service';
import { HomeContentService } from './core/services/home-content.service';
import { signal } from '@angular/core';
import { of } from 'rxjs';

describe('App Routing', () => {
  let router: Router;
  let location: Location;

  beforeEach(async () => {
    const authServiceMock = {
      currentUser: signal(null),
      isAuthenticated: signal(false),
      isInitialized: signal(true),
      isLoading: signal(false),
      checkSession: vi.fn().mockReturnValue(of(null)),
      logout: vi.fn().mockReturnValue(of(undefined)),
    };

    const cartServiceMock = {
      totalQuantity: signal(0),
      cart: signal({ items: [], subtotal: 0, deliveryFee: 0, total: 0 }),
    };

    const settingsServiceMock = {
      restaurantName: signal('RestaurantHub'),
      tagline: signal('Fresh food, delivered with care'),
      phone: signal('9876543210'),
      email: signal('contact@restauranthub.com'),
      addressLine1: signal('123 Gourmet Boulevard'),
      addressLine2: signal(''),
      city: signal('Mumbai'),
      state: signal('Maharashtra'),
      pinCode: signal('400001'),
      openingTime: signal('10:00 AM'),
      closingTime: signal('11:00 PM'),
      heroImageUrl: signal(null),
      logoUrl: signal(null),
      estimatedDeliveryMinutes: signal(35),
      gstin: signal(null),
      fssaiNumber: signal(null),
    };

    const foodServiceMock = {
      categories: signal([]),
      popularDishes: signal([]),
      isLoading: signal(false),
      error: signal(null),
    };

    const homeContentMock = {
      features: signal([]),
      testimonials: signal([]),
    };

    await TestBed.configureTestingModule({
      providers: [
        provideRouter(routes),
        provideLocationMocks(),
        { provide: AuthService, useValue: authServiceMock },
        { provide: CartService, useValue: cartServiceMock },
        { provide: RestaurantSettingsService, useValue: settingsServiceMock },
        { provide: FoodService, useValue: foodServiceMock },
        { provide: HomeContentService, useValue: homeContentMock },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
    router.initialNavigation();
  });

  it('should resolve /about route successfully', async () => {
    await router.navigate(['/about']);
    expect(location.path()).toBe('/about');
  });

  it('should resolve /contact route successfully', async () => {
    await router.navigate(['/contact']);
    expect(location.path()).toBe('/contact');
  });
});
