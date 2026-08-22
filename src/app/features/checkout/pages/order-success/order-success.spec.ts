import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { OrderSuccess } from './order-success';
import { OrderService } from '../../../../core/services/order.service';

describe('OrderSuccess', () => {
  let component: OrderSuccess;
  let fixture: ComponentFixture<OrderSuccess>;
  let orderService: OrderService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrderSuccess],
      providers: [provideRouter([]), OrderService],
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
});
