import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { Checkout } from './checkout';
import { CartService } from '../../../../core/services/cart.service';
import { OrderService } from '../../../../core/services/order.service';
import { AuthService } from '../../../../core/services/auth.service';
import { Order } from '../../../../shared/models/order.model';
import { Food } from '../../../../shared/models/food.model';
import { User } from '../../../../shared/models/user.model';

import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';
import { signal } from '@angular/core';

describe('Checkout', () => {
  let component: Checkout;
  let fixture: ComponentFixture<Checkout>;
  let cartService: CartService;
  let orderService: OrderService;
  let authService: AuthService;
  let router: Router;
  let settingsServiceMock: {
    isAcceptingOrders: ReturnType<typeof signal<boolean>>;
    deliveryFee: ReturnType<typeof signal<number>>;
    freeDeliveryThreshold: ReturnType<typeof signal<number>>;
    currencySymbol: ReturnType<typeof signal<string>>;
  };

  const mockFood: Food = {
    id: '10',
    name: 'Paneer Butter Masala',
    description: 'Creamy curry',
    category: 'Main Course',
    categorySlug: 'main-course',
    price: 250,
    rating: 4.5,
    image: 'paneer.jpg',
    isVeg: true,
    isPopular: true,
  };

  const mockUser: User = {
    id: '1',
    fullName: 'Rohan Pawar',
    email: 'rohan@example.com',
    phone: '9876543210',
    role: 'CUSTOMER',
    createdAt: '2026-08-24T10:00:00.000Z',
  };

  const mockOrderResponse: Order = {
    id: '500',
    status: 'confirmed',
    paymentMethod: 'cod',
    subtotal: 500,
    deliveryFee: 0,
    total: 500,
    createdAt: '2026-08-24T10:00:00.000Z',
    customer: {
      fullName: 'Rohan Pawar',
      email: 'rohan@example.com',
      phone: '9876543210',
      addressLine1: '123 MG Road',
      city: 'Mumbai',
      state: 'Maharashtra',
      postalCode: '400001',
    },
    items: [{ food: mockFood, quantity: 2 }],
  };

  beforeEach(async () => {
    settingsServiceMock = {
      isAcceptingOrders: signal(true),
      deliveryFee: signal(40),
      freeDeliveryThreshold: signal(500),
      currencySymbol: signal('₹'),
    };

    await TestBed.configureTestingModule({
      imports: [Checkout],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        CartService,
        OrderService,
        AuthService,
        { provide: RestaurantSettingsService, useValue: settingsServiceMock },
      ],
    }).compileComponents();

    cartService = TestBed.inject(CartService);
    orderService = TestBed.inject(OrderService);
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);

    cartService.clearCart();
  });

  it('should create the checkout component and prefill user details if logged in', async () => {
    (authService as any).currentUserState.set(mockUser);

    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component).toBeTruthy();
    expect(component.checkoutForm.get('fullName')?.value).toBe('Rohan Pawar');
    expect(component.checkoutForm.get('email')?.value).toBe('rohan@example.com');
    expect(component.checkoutForm.get('phone')?.value).toBe('9876543210');
  });

  it('should initialize with an invalid form when fields are empty', async () => {
    (authService as any).currentUserState.set(null);

    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.checkoutForm.valid).toBe(false);
  });

  it('should validate 10-digit Indian phone numbers', async () => {
    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const phoneControl = component.checkoutForm.get('phone');
    phoneControl?.setValue('12345');
    expect(phoneControl?.valid).toBe(false);

    phoneControl?.setValue('9876543210');
    expect(phoneControl?.valid).toBe(true);

    phoneControl?.setValue('5876543210'); // Starts with 5 (invalid)
    expect(phoneControl?.valid).toBe(false);
  });

  it('should validate 6-digit PIN code', async () => {
    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const pinControl = component.checkoutForm.get('postalCode');
    pinControl?.setValue('5600');
    expect(pinControl?.valid).toBe(false);

    pinControl?.setValue('560038');
    expect(pinControl?.valid).toBe(true);
  });

  it('should display Coming Soon and disabled status for UPI and Card options in UI', async () => {
    cartService.addToCart(mockFood);

    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    const optionCards = compiled.querySelectorAll('.payment-option-card');
    expect(optionCards.length).toBe(3);

    // COD is active
    expect(compiled.textContent).toContain('Active');
    // UPI & Card are Coming Soon
    expect(compiled.textContent).toContain('Coming Soon');
    expect(compiled.textContent).toContain('Online payment will be available soon');

    const upiInput = compiled.querySelector('input[value="upi"]') as HTMLInputElement;
    const cardInput = compiled.querySelector('input[value="card"]') as HTMLInputElement;
    const codInput = compiled.querySelector('input[value="cod"]') as HTMLInputElement;

    expect(codInput.disabled).toBe(false);
    expect(upiInput.disabled).toBe(true);
    expect(cardInput.disabled).toBe(true);
  });

  it('should keep COD selected and display notice when selecting UPI or Card', async () => {
    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
    fixture.detectChanges();

    // Selecting UPI should not change selectedPaymentMethod from COD
    component.onSelectPayment('upi');
    expect(component.selectedPaymentMethod()).toBe('cod');
    expect(component.checkoutForm.get('paymentMethod')?.value).toBe('cod');
    expect(component.onlinePaymentNotice()).toBe(
      'Online payment is not available yet. Please choose Cash on Delivery.'
    );

    // Selecting CARD should also not change selectedPaymentMethod from COD
    component.onSelectPayment('card');
    expect(component.selectedPaymentMethod()).toBe('cod');
    expect(component.checkoutForm.get('paymentMethod')?.value).toBe('cod');
    expect(component.onlinePaymentNotice()).toBe(
      'Online payment is not available yet. Please choose Cash on Delivery.'
    );

    // Selecting COD clears notice
    component.onSelectPayment('cod');
    expect(component.selectedPaymentMethod()).toBe('cod');
    expect(component.onlinePaymentNotice()).toBeNull();
  });

  it('should submit COD order to backend and clear cart on success', async () => {
    cartService.addToCart(mockFood);
    cartService.addToCart(mockFood); // qty 2

    const createOrderSpy = vi
      .spyOn(orderService, 'createOrder')
      .mockReturnValue(of(mockOrderResponse));
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const clearCartSpy = vi.spyOn(cartService, 'clearCart');

    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.checkoutForm.setValue({
      fullName: 'Rohan Pawar',
      email: 'rohan@example.com',
      phone: '9876543210',
      addressLine1: '123 MG Road',
      addressLine2: '',
      city: 'Mumbai',
      state: 'Maharashtra',
      postalCode: '400001',
      deliveryInstructions: '',
      paymentMethod: 'cod',
    });

    component.onSubmit();

    expect(createOrderSpy).toHaveBeenCalledWith({
      customerName: 'Rohan Pawar',
      customerEmail: 'rohan@example.com',
      customerPhone: '9876543210',
      addressLine1: '123 MG Road',
      addressLine2: null,
      city: 'Mumbai',
      state: 'Maharashtra',
      postalCode: '400001',
      deliveryInstructions: null,
      paymentMethod: 'COD',
      items: [{ foodId: 10, quantity: 2 }],
    });

    expect(clearCartSpy).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/order-success']);
  });

  it('should NOT submit order if payment method is not COD', async () => {
    cartService.addToCart(mockFood);

    const createOrderSpy = vi.spyOn(orderService, 'createOrder');

    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.checkoutForm.setValue({
      fullName: 'Rohan Pawar',
      email: 'rohan@example.com',
      phone: '9876543210',
      addressLine1: '123 MG Road',
      addressLine2: '',
      city: 'Mumbai',
      state: 'Maharashtra',
      postalCode: '400001',
      deliveryInstructions: '',
      paymentMethod: 'upi',
    });
    (component.selectedPaymentMethod as any).set('upi');

    component.onSubmit();

    expect(createOrderSpy).not.toHaveBeenCalled();
    expect(component.onlinePaymentNotice()).toBe(
      'Online payment is not available yet. Please choose Cash on Delivery.'
    );
  });

  it('should NOT clear cart and should display error when backend creation fails', async () => {
    cartService.addToCart(mockFood);

    const createOrderSpy = vi
      .spyOn(orderService, 'createOrder')
      .mockReturnValue(throwError(() => new Error('Dish is currently out of stock')));
    const clearCartSpy = vi.spyOn(cartService, 'clearCart');
    const navigateSpy = vi.spyOn(router, 'navigate');

    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.checkoutForm.setValue({
      fullName: 'Rohan Pawar',
      email: 'rohan@example.com',
      phone: '9876543210',
      addressLine1: '123 MG Road',
      addressLine2: '',
      city: 'Mumbai',
      state: 'Maharashtra',
      postalCode: '400001',
      deliveryInstructions: '',
      paymentMethod: 'cod',
    });

    component.onSubmit();

    expect(createOrderSpy).toHaveBeenCalled();
    expect(clearCartSpy).not.toHaveBeenCalled();
    expect(navigateSpy).not.toHaveBeenCalled();
    expect(component.isSubmitting()).toBe(false);
    expect(component.serverErrorMessage()).toBe('Dish is currently out of stock');
    expect(cartService.isEmpty()).toBe(false);
  });

  it('should block order submission when store is not accepting orders', () => {
    settingsServiceMock.isAcceptingOrders.set(false);
    cartService.addToCart(mockFood);

    const createOrderSpy = vi.spyOn(orderService, 'createOrder');

    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.checkoutForm.setValue({
      fullName: 'Rohan Pawar',
      email: 'rohan@example.com',
      phone: '9876543210',
      addressLine1: '123 MG Road',
      addressLine2: '',
      city: 'Mumbai',
      state: 'Maharashtra',
      postalCode: '400001',
      deliveryInstructions: '',
      paymentMethod: 'cod',
    });

    component.onSubmit();

    expect(createOrderSpy).not.toHaveBeenCalled();
    expect(component.serverErrorMessage()).toContain('not accepting online orders');
  });
});
