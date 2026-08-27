import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { Cart } from './cart';
import { CartService } from '../../../../core/services/cart.service';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';
import { Food } from '../../../../shared/models/food.model';
import { CartItem } from '../../../../shared/models/cart-item.model';

describe('Cart Component', () => {
  let component: Cart;
  let fixture: ComponentFixture<Cart>;
  let cartServiceMock: {
    cartItems: ReturnType<typeof signal<CartItem[]>>;
    totalQuantity: ReturnType<typeof signal<number>>;
    subtotal: ReturnType<typeof signal<number>>;
    deliveryFee: ReturnType<typeof signal<number>>;
    grandTotal: ReturnType<typeof signal<number>>;
    isEmpty: ReturnType<typeof signal<boolean>>;
    increaseQuantity: ReturnType<typeof vi.fn>;
    decreaseQuantity: ReturnType<typeof vi.fn>;
    removeItem: ReturnType<typeof vi.fn>;
    clearCart: ReturnType<typeof vi.fn>;
  };

  const mockFood: Food = {
    id: '1',
    name: 'Butter Naan',
    description: 'Crispy buttered flatbread',
    category: 'Breads',
    categorySlug: 'breads',
    price: 60,
    rating: 4.8,
    image: 'naan.jpg',
    isVeg: true,
    isPopular: true,
  };

  beforeEach(async () => {
    cartServiceMock = {
      cartItems: signal<CartItem[]>([{ food: mockFood, quantity: 2 }]),
      totalQuantity: signal<number>(2),
      subtotal: signal<number>(120),
      deliveryFee: signal<number>(40),
      grandTotal: signal<number>(160),
      isEmpty: signal<number>(0).asReadonly() as any,
      increaseQuantity: vi.fn(),
      decreaseQuantity: vi.fn(),
      removeItem: vi.fn(),
      clearCart: vi.fn(),
    };
    (cartServiceMock as any).isEmpty = signal(false);

    const settingsServiceMock = {
      deliveryFee: signal(40),
      freeDeliveryThreshold: signal(500),
      currencySymbol: signal('₹'),
      isAcceptingOrders: signal(true),
    };

    await TestBed.configureTestingModule({
      imports: [Cart],
      providers: [
        provideRouter([]),
        { provide: CartService, useValue: cartServiceMock },
        { provide: RestaurantSettingsService, useValue: settingsServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Cart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create cart component and compute free delivery progress dynamically', () => {
    expect(component).toBeTruthy();
    expect(component.freeDeliveryThreshold()).toBe(500);
    expect(component.amountForFreeDelivery()).toBe(380); // 500 - 120
    expect(component.freeDeliveryProgress()).toBe(24); // (120 / 500) * 100
  });

  it('should delegate increase, decrease, remove, and clear actions to CartService', () => {
    component.onIncrease('1');
    expect(cartServiceMock.increaseQuantity).toHaveBeenCalledWith('1');

    component.onDecrease('1');
    expect(cartServiceMock.decreaseQuantity).toHaveBeenCalledWith('1');

    component.onRemove('1');
    expect(cartServiceMock.removeItem).toHaveBeenCalledWith('1');

    component.onClear();
    expect(cartServiceMock.clearCart).toHaveBeenCalled();
  });

  it('should display warning banner and disable checkout when accepting orders is false', () => {
    const settingsService = TestBed.inject(RestaurantSettingsService);
    (settingsService.isAcceptingOrders as any).set(false);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.cart-warning-banner')).toBeTruthy();
    expect(compiled.querySelector('.checkout-btn.btn-disabled')).toBeTruthy();
  });
});

