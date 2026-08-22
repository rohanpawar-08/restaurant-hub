import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Checkout } from './checkout';
import { CartService } from '../../../../core/services/cart.service';
import { OrderService } from '../../../../core/services/order.service';

describe('Checkout', () => {
  let component: Checkout;
  let fixture: ComponentFixture<Checkout>;
  let cartService: CartService;
  let orderService: OrderService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Checkout],
      providers: [provideRouter([]), CartService, OrderService],
    }).compileComponents();

    fixture = TestBed.createComponent(Checkout);
    component = fixture.componentInstance;
    cartService = TestBed.inject(CartService);
    orderService = TestBed.inject(OrderService);
    await fixture.whenStable();
  });

  it('should create the checkout component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with an invalid form', () => {
    expect(component.checkoutForm.valid).toBe(false);
  });

  it('should validate 10-digit Indian phone numbers', () => {
    const phoneControl = component.checkoutForm.get('phone');
    phoneControl?.setValue('12345');
    expect(phoneControl?.valid).toBe(false);

    phoneControl?.setValue('9876543210');
    expect(phoneControl?.valid).toBe(true);

    phoneControl?.setValue('5876543210'); // starts with 5 (invalid for Indian mobile regex)
    expect(phoneControl?.valid).toBe(false);
  });

  it('should validate 6-digit PIN code', () => {
    const pinControl = component.checkoutForm.get('postalCode');
    pinControl?.setValue('5600');
    expect(pinControl?.valid).toBe(false);

    pinControl?.setValue('560038');
    expect(pinControl?.valid).toBe(true);
  });

  it('should validate email format', () => {
    const emailControl = component.checkoutForm.get('email');
    emailControl?.setValue('invalid-email');
    expect(emailControl?.valid).toBe(false);

    emailControl?.setValue('user@example.com');
    expect(emailControl?.valid).toBe(true);
  });

  it('should display payment gateway notice when UPI or Card is selected', () => {
    component.onSelectPayment('upi');
    expect(component.selectedPaymentMethod()).toBe('upi');
    expect(component.onlinePaymentNotice()).toContain('Online payment integration');

    component.onSelectPayment('cod');
    expect(component.selectedPaymentMethod()).toBe('cod');
    expect(component.onlinePaymentNotice()).toBeNull();
  });
});
