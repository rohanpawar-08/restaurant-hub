import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { OrderSuccess } from './order-success';
import { OrderService } from '../../../../core/services/order.service';

describe('OrderSuccess', () => {
  let component: OrderSuccess;
  let fixture: ComponentFixture<OrderSuccess>;
  let orderService: OrderService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrderSuccess],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        OrderService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(OrderSuccess);
    component = fixture.componentInstance;
    orderService = TestBed.inject(OrderService);
    await fixture.whenStable();
  });

  it('should create the order success component', () => {
    expect(component).toBeTruthy();
  });

  it('should format payment method labels correctly', () => {
    expect(component.getPaymentMethodLabel('cod')).toContain('Cash on Delivery');
    expect(component.getPaymentMethodLabel('upi')).toBe('UPI Payment');
    expect(component.getPaymentMethodLabel('card')).toBe('Credit / Debit Card');
  });

  it('should render link to View My Orders', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const ordersLinks = compiled.querySelectorAll('a[routerLink="/orders"]');
    expect(ordersLinks.length).toBeGreaterThanOrEqual(1);
    expect(ordersLinks[0].textContent).toContain('View My Orders');
  });
});
