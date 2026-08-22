import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ComponentRef } from '@angular/core';
import { OrderCard } from './order-card';
import { Order } from '../../models/order.model';

describe('OrderCard', () => {
  let component: OrderCard;
  let fixture: ComponentFixture<OrderCard>;
  let componentRef: ComponentRef<OrderCard>;

  const mockOrder: Order = {
    id: 'RH-9001',
    items: [
      {
        food: {
          id: '101',
          name: 'Paneer Tikka Masala',
          description: 'Spiced cottage cheese',
          category: 'Curry',
          categorySlug: 'curry',
          price: 280,
          rating: 4.7,
          image: 'assets/paneer.jpg',
          isVeg: true,
          isPopular: true,
        },
        quantity: 2,
      },
      {
        food: {
          id: '102',
          name: 'Garlic Naan',
          description: 'Clay-oven baked flatbread',
          category: 'Breads',
          categorySlug: 'breads',
          price: 60,
          rating: 4.9,
          image: 'assets/naan.jpg',
          isVeg: true,
          isPopular: true,
        },
        quantity: 3,
      },
    ],
    customer: {
      fullName: 'Rohan Sharma',
      email: 'rohan@example.com',
      phone: '9876543210',
      addressLine1: 'Flat 304, Green Heights',
      addressLine2: 'Indiranagar',
      city: 'Bengaluru',
      state: 'Karnataka',
      postalCode: '560038',
      deliveryInstructions: 'Ring doorbell twice',
    },
    paymentMethod: 'cod',
    subtotal: 740,
    deliveryFee: 0,
    total: 740,
    status: 'confirmed',
    createdAt: '2026-08-22T10:00:00.000Z',
    estimatedDeliveryMinutes: 35,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrderCard],
    }).compileComponents();

    fixture = TestBed.createComponent(OrderCard);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('order', mockOrder);
    await fixture.whenStable();
  });

  it('should create the order card component', () => {
    expect(component).toBeTruthy();
  });

  it('should render key order summary details correctly', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('RH-9001');
    expect(compiled.textContent).toContain('Rohan Sharma');
    expect(compiled.textContent).toContain('5 items');
    expect(compiled.textContent).toContain('Paneer Tikka Masala');
  });

  it('should compute total item count correctly', () => {
    expect(component.totalItemCount()).toBe(5);
  });

  it('should format preview text correctly', () => {
    expect(component.itemsPreviewText()).toContain('2 × Paneer Tikka Masala');
    expect(component.itemsPreviewText()).toContain('3 × Garlic Naan');
  });

  it('should toggle expanded details state when action clicked', async () => {
    expect(component.isExpanded()).toBe(false);
    
    component.toggleDetails();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.isExpanded()).toBe(true);
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Flat 304, Green Heights');
    expect(compiled.textContent).toContain('Ring doorbell twice');
    expect(compiled.textContent).toContain('Receipt & Items Breakdown');
  });

  it('should format status and payment labels accurately', () => {
    expect(component.getStatusLabel('confirmed')).toBe('Confirmed');
    expect(component.getStatusLabel('preparing')).toBe('Preparing');
    expect(component.getStatusLabel('out_for_delivery')).toBe('Out for Delivery');
    expect(component.getStatusLabel('delivered')).toBe('Delivered');

    expect(component.getPaymentMethodLabel('cod')).toBe('Cash on Delivery');
    expect(component.getPaymentMethodLabel('upi')).toBe('UPI Payment');
    expect(component.getPaymentMethodLabel('card')).toBe('Credit / Debit Card');
  });
});
