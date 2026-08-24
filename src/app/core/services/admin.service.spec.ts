import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import { DashboardSummaryApiResponse } from '../api/models/admin-api.model';
import { OrderApiResponse } from '../api/models/order-api.model';

describe('AdminService', () => {
  let service: AdminService;
  let httpTesting: HttpTestingController;

  const mockDashboardResponse: DashboardSummaryApiResponse = {
    totalOrders: 10,
    confirmedOrders: 2,
    preparingOrders: 3,
    readyOrders: 1,
    outForDeliveryOrders: 1,
    totalCustomers: 15,
    totalFoods: 20,
    activeFoods: 18,
  };

  const mockOrderApiResponse: OrderApiResponse = {
    id: 100,
    userId: 1,
    status: 'CONFIRMED',
    paymentMethod: 'COD',
    subtotal: 250,
    deliveryFee: 40,
    total: 290,
    customerName: 'Rohan Pawar',
    customerEmail: 'rohan@example.com',
    customerPhone: '9876543210',
    addressLine1: '123 MG Road',
    addressLine2: null,
    city: 'Mumbai',
    state: 'Maharashtra',
    postalCode: '400001',
    deliveryInstructions: null,
    estimatedDeliveryMinutes: 35,
    createdAt: '2026-08-24T10:00:00.000Z',
    items: [
      {
        id: 1,
        foodId: 10,
        foodName: 'Paneer Curry',
        unitPrice: 250,
        quantity: 1,
        lineTotal: 250,
      },
    ],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AdminService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(AdminService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should fetch dashboard summary metrics and update signal', () => {
    service.getDashboardSummary().subscribe((summary) => {
      expect(summary.totalOrders).toBe(10);
      expect(summary.confirmedOrders).toBe(2);
      expect(summary.totalCustomers).toBe(15);
    });

    const req = httpTesting.expectOne('/api/v1/admin/dashboard/summary');
    expect(req.request.method).toBe('GET');
    req.flush(mockDashboardResponse);

    expect(service.dashboardSummary()?.totalOrders).toBe(10);
    expect(service.isLoading()).toBe(false);
  });

  it('should fetch all restaurant orders and map to frontend models', () => {
    service.getOrders().subscribe((orders) => {
      expect(orders.length).toBe(1);
      expect(orders[0].id).toBe('100');
      expect(orders[0].status).toBe('confirmed');
      expect(orders[0].paymentMethod).toBe('cod');
    });

    const req = httpTesting.expectOne('/api/v1/admin/orders');
    expect(req.request.method).toBe('GET');
    req.flush([mockOrderApiResponse]);

    expect(service.adminOrders().length).toBe(1);
  });

  it('should fetch orders filtered by status param', () => {
    service.getOrders('confirmed').subscribe((orders) => {
      expect(orders.length).toBe(1);
    });

    const req = httpTesting.expectOne('/api/v1/admin/orders?status=CONFIRMED');
    expect(req.request.method).toBe('GET');
    req.flush([mockOrderApiResponse]);
  });

  it('should fetch single order by ID', () => {
    service.getOrderById('100').subscribe((order) => {
      expect(order.id).toBe('100');
      expect(order.customer.fullName).toBe('Rohan Pawar');
    });

    const req = httpTesting.expectOne('/api/v1/admin/orders/100');
    expect(req.request.method).toBe('GET');
    req.flush(mockOrderApiResponse);
  });

  it('should update order status via PATCH and update cached signal', () => {
    const updatedResponse: OrderApiResponse = {
      ...mockOrderApiResponse,
      status: 'PREPARING',
    };

    // Prepopulate signal
    (service as any).adminOrdersState.set([
      {
        id: '100',
        status: 'confirmed',
        paymentMethod: 'cod',
        subtotal: 250,
        deliveryFee: 40,
        total: 290,
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
        items: [],
      },
    ]);

    service.updateOrderStatus('100', 'preparing').subscribe((order) => {
      expect(order.status).toBe('preparing');
    });

    const req = httpTesting.expectOne('/api/v1/admin/orders/100/status');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'PREPARING' });
    req.flush(updatedResponse);

    expect(service.adminOrders()[0].status).toBe('preparing');
  });

  it('should set error signal on failed dashboard request', () => {
    let errorCaptured: Error | null = null;
    service.getDashboardSummary().subscribe({
      next: () => {},
      error: (err) => {
        errorCaptured = err;
      },
    });

    const req = httpTesting.expectOne('/api/v1/admin/dashboard/summary');
    req.flush({ message: 'Access denied' }, { status: 403, statusText: 'Forbidden' });

    expect(errorCaptured).toBeTruthy();
    expect((errorCaptured as any)?.message).toBe('Access denied');

    expect(service.error()).toBe('Access denied');
    expect(service.isLoading()).toBe(false);
  });
});
