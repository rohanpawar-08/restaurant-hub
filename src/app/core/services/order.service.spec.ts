import { TestBed } from '@angular/core/testing';
import { OrderService } from './order.service';
import { CustomerDetails } from '../../shared/models/checkout.model';
import { CartItem } from '../../shared/models/cart-item.model';

describe('OrderService', () => {
  let service: OrderService;

  const mockCustomer: CustomerDetails = {
    fullName: 'Aarav Sharma',
    email: 'aarav@example.com',
    phone: '9876543210',
    addressLine1: '402 Sunshine Apts',
    city: 'Bengaluru',
    state: 'Karnataka',
    postalCode: '560038',
  };

  const mockItems: CartItem[] = [
    {
      food: {
        id: '1',
        name: 'Butter Chicken',
        description: 'Rich curry',
        category: 'Main Course',
        categorySlug: 'main-course',
        price: 320,
        rating: 4.8,
        image: 'assets/butter-chicken.jpg',
        isVeg: false,
        isPopular: true,
      },
      quantity: 2,
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OrderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create an order with unique ID and confirmed status', () => {
    const order = service.createOrder({
      items: mockItems,
      customer: mockCustomer,
      paymentMethod: 'cod',
      subtotal: 640,
      deliveryFee: 0,
      total: 640,
    });

    expect(order).toBeTruthy();
    expect(order.id).toMatch(/^RH-\d+$/);
    expect(order.status).toBe('confirmed');
    expect(order.total).toBe(640);
    expect(order.customer.fullName).toBe('Aarav Sharma');
    expect(service.latestOrder()).toEqual(order);
    expect(service.orderHistory().length).toBeGreaterThanOrEqual(1);
  });

  it('should clear the latest order', () => {
    service.createOrder({
      items: mockItems,
      customer: mockCustomer,
      paymentMethod: 'cod',
      subtotal: 640,
      deliveryFee: 0,
      total: 640,
    });

    expect(service.latestOrder()).not.toBeNull();
    service.clearLatestOrder();
    expect(service.latestOrder()).toBeNull();
  });
});
