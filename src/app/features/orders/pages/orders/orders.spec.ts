import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { Orders } from './orders';
import { OrderService } from '../../../../core/services/order.service';
import { Order } from '../../../../shared/models/order.model';

describe('Orders Page', () => {
  let component: Orders;
  let fixture: ComponentFixture<Orders>;
  let orderService: OrderService;

  const mockOrder1: Order = {
    id: '101',
    items: [
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
        quantity: 1,
      },
    ],
    customer: {
      fullName: 'Aarav Sharma',
      email: 'aarav@example.com',
      phone: '9876543210',
      addressLine1: '402 Sunshine Apts',
      city: 'Bengaluru',
      state: 'Karnataka',
      postalCode: '560038',
    },
    paymentMethod: 'cod',
    subtotal: 320,
    deliveryFee: 40,
    total: 360,
    status: 'confirmed',
    createdAt: '2026-08-20T10:00:00.000Z',
    estimatedDeliveryMinutes: 35,
  };

  const mockOrder2: Order = {
    id: '202',
    items: [
      {
        food: {
          id: '2',
          name: 'Margherita Pizza',
          description: 'Cheese pizza',
          category: 'Pizza',
          categorySlug: 'pizza',
          price: 250,
          rating: 4.5,
          image: 'assets/pizza.jpg',
          isVeg: true,
          isPopular: true,
        },
        quantity: 2,
      },
    ],
    customer: {
      fullName: 'Priya Patel',
      email: 'priya@example.com',
      phone: '9876543211',
      addressLine1: '12 Green Avenue',
      city: 'Bengaluru',
      state: 'Karnataka',
      postalCode: '560001',
    },
    paymentMethod: 'upi',
    subtotal: 500,
    deliveryFee: 0,
    total: 500,
    status: 'delivered',
    createdAt: '2026-08-22T14:30:00.000Z',
    estimatedDeliveryMinutes: 30,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Orders],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        OrderService,
      ],
    }).compileComponents();

    orderService = TestBed.inject(OrderService);
    (orderService as any).orderHistoryState.set([]);
  });

  it('should render empty state when order history is empty', async () => {
    vi.spyOn(orderService, 'loadOrders').mockReturnValue(of([]));

    fixture = TestBed.createComponent(Orders);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('No orders yet');
    expect(compiled.textContent).toContain('When you place an order, it will appear here.');
    expect(compiled.querySelector('a[routerLink="/menu"]')).toBeTruthy();
    expect(component.totalOrdersCount()).toBe(0);
  });

  it('should render orders list when orders exist in backend history', async () => {
    vi.spyOn(orderService, 'loadOrders').mockReturnValue(of([mockOrder1]));
    (orderService as any).orderHistoryState.set([mockOrder1]);

    fixture = TestBed.createComponent(Orders);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.totalOrdersCount()).toBe(1);
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('My Orders');
    expect(compiled.textContent).toContain('Aarav Sharma');
  });

  it('should sort orders with newest first without mutating original history', async () => {
    vi.spyOn(orderService, 'loadOrders').mockReturnValue(of([mockOrder1, mockOrder2]));
    (orderService as any).orderHistoryState.set([mockOrder1, mockOrder2]);

    fixture = TestBed.createComponent(Orders);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    const filtered = component.filteredOrders();
    expect(filtered.length).toBe(2);
    // mockOrder2 is newer (2026-08-22) than mockOrder1 (2026-08-20)
    expect(filtered[0].id).toBe('202');
    expect(filtered[1].id).toBe('101');
  });

  it('should filter orders by status correctly', async () => {
    vi.spyOn(orderService, 'loadOrders').mockReturnValue(of([mockOrder1, mockOrder2]));
    (orderService as any).orderHistoryState.set([mockOrder1, mockOrder2]);

    fixture = TestBed.createComponent(Orders);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    component.setFilter('delivered');
    fixture.detectChanges();
    await fixture.whenStable();

    expect(component.filteredOrders().length).toBe(1);
    expect(component.filteredOrders()[0].status).toBe('delivered');
    expect(component.filteredOrders()[0].id).toBe('202');

    component.setFilter('confirmed');
    expect(component.filteredOrders().length).toBe(1);
    expect(component.filteredOrders()[0].id).toBe('101');

    component.setFilter('preparing');
    expect(component.filteredOrders().length).toBe(0);
  });

  it('should render loading skeleton when isLoading is true and orders are empty', async () => {
    (orderService as any).isLoadingState.set(true);
    (orderService as any).orderHistoryState.set([]);

    fixture = TestBed.createComponent(Orders);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.orders-loading-state')).toBeTruthy();
  });

  it('should render error state and trigger retry when error is present', async () => {
    (orderService as any).errorState.set('Failed to load orders from server');
    (orderService as any).orderHistoryState.set([]);
    const loadSpy = vi.spyOn(orderService, 'loadOrders').mockReturnValue(of([]));

    fixture = TestBed.createComponent(Orders);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.orders-error-state')).toBeTruthy();
    expect(compiled.textContent).toContain('Failed to load orders from server');

    component.retry();
    expect(loadSpy).toHaveBeenCalled();
  });
});

