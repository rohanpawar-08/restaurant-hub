import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { OrderService } from './order.service';
import { CreateOrderApiRequest, OrderApiResponse } from '../api/models/order-api.model';

describe('OrderService', () => {
  let service: OrderService;
  let httpMock: HttpTestingController;

  const mockApiResponse: OrderApiResponse = {
    id: 101,
    userId: 1,
    status: 'CONFIRMED',
    paymentMethod: 'COD',
    subtotal: 500,
    deliveryFee: 0,
    total: 500,
    customerName: 'Aarav Sharma',
    customerEmail: 'aarav@example.com',
    customerPhone: '9876543210',
    addressLine1: '402 Sunshine Apts',
    addressLine2: null,
    city: 'Bengaluru',
    state: 'Karnataka',
    postalCode: '560038',
    deliveryInstructions: 'Leave at reception',
    estimatedDeliveryMinutes: 35,
    createdAt: '2026-08-24T10:00:00.000Z',
    items: [
      {
        id: 1,
        foodId: 10,
        foodName: 'Paneer Butter Masala',
        unitPrice: 250,
        quantity: 2,
        lineTotal: 500,
      },
    ],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        OrderService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(OrderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create order via POST /api/v1/orders without sending client totals or userId', () => {
    const requestPayload: CreateOrderApiRequest = {
      customerName: 'Aarav Sharma',
      customerEmail: 'aarav@example.com',
      customerPhone: '9876543210',
      addressLine1: '402 Sunshine Apts',
      addressLine2: null,
      city: 'Bengaluru',
      state: 'Karnataka',
      postalCode: '560038',
      deliveryInstructions: 'Leave at reception',
      paymentMethod: 'COD',
      items: [
        {
          foodId: 10,
          quantity: 2,
        },
      ],
    };

    service.createOrder(requestPayload).subscribe((order) => {
      expect(order).toBeTruthy();
      expect(order.id).toBe('101');
      expect(order.status).toBe('confirmed');
      expect(order.paymentMethod).toBe('cod');
      expect(order.subtotal).toBe(500);
      expect(order.deliveryFee).toBe(0);
      expect(order.total).toBe(500);
      expect(order.items.length).toBe(1);
      expect(order.items[0].food.name).toBe('Paneer Butter Masala');
      expect(order.items[0].food.price).toBe(250);
      expect(order.items[0].quantity).toBe(2);

      expect(service.latestOrder()).toEqual(order);
      expect(service.orderHistory()).toContain(order);
    });

    const req = httpMock.expectOne('/api/v1/orders');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    // Ensure no client-authoritative prices/totals or userId were sent
    expect((req.request.body as any).subtotal).toBeUndefined();
    expect((req.request.body as any).deliveryFee).toBeUndefined();
    expect((req.request.body as any).total).toBeUndefined();
    expect((req.request.body as any).userId).toBeUndefined();
    expect((req.request.body as any).status).toBeUndefined();
    expect(req.request.body.items[0].unitPrice).toBeUndefined();
    expect(req.request.body.items[0].lineTotal).toBeUndefined();

    req.flush(mockApiResponse);
  });

  it('should set error signal on failed order creation', () => {
    const requestPayload: CreateOrderApiRequest = {
      customerName: 'Aarav Sharma',
      customerEmail: 'aarav@example.com',
      customerPhone: '9876543210',
      addressLine1: '402 Sunshine Apts',
      city: 'Bengaluru',
      state: 'Karnataka',
      postalCode: '560038',
      paymentMethod: 'COD',
      items: [{ foodId: 10, quantity: 1 }],
    };

    service.createOrder(requestPayload).subscribe({
      next: () => {
        expect('Should not have succeeded').toBe('failed');
      },
      error: (err) => {
        expect(err.message).toBe('Dish is out of stock');
        expect(service.error()).toBe('Dish is out of stock');
        expect(service.isLoading()).toBe(false);
      },
    });

    const req = httpMock.expectOne('/api/v1/orders');
    req.flush(
      { message: 'Dish is out of stock' },
      { status: 400, statusText: 'Bad Request' }
    );
  });

  it('should load customer order history via GET /api/v1/orders', () => {
    service.loadOrders().subscribe((orders) => {
      expect(orders.length).toBe(1);
      expect(orders[0].id).toBe('101');
      expect(service.orderHistory()).toEqual(orders);
      expect(service.isLoading()).toBe(false);
    });

    const req = httpMock.expectOne('/api/v1/orders');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);

    req.flush([mockApiResponse]);
  });

  it('should fetch single order by ID via GET /api/v1/orders/{id}', () => {
    service.getOrderById(101).subscribe((order) => {
      expect(order.id).toBe('101');
      expect(service.latestOrder()).toEqual(order);
    });

    const req = httpMock.expectOne('/api/v1/orders/101');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);

    req.flush(mockApiResponse);
  });

  it('should clear latest order signal', () => {
    service.createOrder({} as any).subscribe();
    const req = httpMock.expectOne('/api/v1/orders');
    req.flush(mockApiResponse);

    expect(service.latestOrder()).not.toBeNull();
    service.clearLatestOrder();
    expect(service.latestOrder()).toBeNull();
  });
});
