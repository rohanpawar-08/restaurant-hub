import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { PublicLayout } from './public-layout';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { RestaurantSettingsService } from '../../services/restaurant-settings.service';

describe('PublicLayout', () => {
  let component: PublicLayout;
  let fixture: ComponentFixture<PublicLayout>;

  beforeEach(async () => {
    const authServiceMock = {
      currentUser: signal(null),
      isAuthenticated: signal(false),
      logout: vi.fn().mockReturnValue(of(undefined)),
    };

    const cartServiceMock = {
      totalQuantity: signal(0),
    };

    const settingsServiceMock = {
      restaurantName: signal('RestaurantHub'),
      tagline: signal('Fresh food, delivered with care'),
      phone: signal('9876543210'),
      email: signal('hello@restauranthub.com'),
      addressLine1: signal('123 Gourmet St'),
      addressLine2: signal(''),
      city: signal('Mumbai'),
      state: signal('Maharashtra'),
      pinCode: signal('400001'),
      gstin: signal(null),
      fssaiNumber: signal(null),
      openingTime: signal(null),
      closingTime: signal(null),
      logoUrl: signal(null),
    };

    await TestBed.configureTestingModule({
      imports: [PublicLayout],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
        { provide: CartService, useValue: cartServiceMock },
        { provide: RestaurantSettingsService, useValue: settingsServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PublicLayout);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
