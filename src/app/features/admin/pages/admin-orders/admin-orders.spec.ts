import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { AdminOrders } from './admin-orders';
import { AdminService } from '../../../../core/services/admin.service';
import { Order, OrderStatus } from '../../../../shared/models/order.model';
import { Food } from '../../../../shared/models/food.model';

describe('AdminOrders', () => {
  let component: AdminOrders;
  let fixture: ComponentFixture<AdminOrders>;
  let adminService: AdminService;

  const mockFood: Food = {
    id: '1',
    name: 'Butter Chicken',
    description: 'Rich gravy',
    category: 'Main Course',
    categorySlug: 'main-course',
    price: 320,
    rating: 4.8,
    image: 'butter-chicken.jpg',
    isVeg: false,
    isPopular: true,
  };

  const mockOrders: Order[] = [
    {
      id: '101',
      status: 'confirmed',
      paymentMethod: 'cod',
      subtotal: 320,
      deliveryFee: 40,
      total: 360,
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
      items: [{ food: mockFood, quantity: 1 }],
    },
    {
      id: '102',
      status: 'preparing',
      paymentMethod: 'cod',
      subtotal: 640,
      deliveryFee: 0,
      total: 640,
      createdAt: '2026-08-24T10:30:00.000Z',
      customer: {
        fullName: 'Jane Doe',
        email: 'jane@example.com',
        phone: '9876543211',
        addressLine1: '456 Brigade Rd',
        city: 'Bengaluru',
        state: 'Karnataka',
        postalCode: '560001',
      },
      items: [{ food: mockFood, quantity: 2 }],
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminOrders],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        AdminService,
      ],
    }).compileComponents();

    adminService = TestBed.inject(AdminService);
  });

  it('should create admin orders component and load all orders on init', () => {
    const getOrdersSpy = vi.spyOn(adminService, 'getOrders').mockImplementation((status?: OrderStatus) => {
      const filtered = status ? mockOrders.filter((o) => o.status === status) : mockOrders;
      (adminService as any).adminOrdersState.set(filtered);
      return of(filtered);
    });

    fixture = TestBed.createComponent(AdminOrders);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(getOrdersSpy).toHaveBeenCalledWith(undefined);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Order #101');
    expect(compiled.textContent).toContain('Order #102');
    expect(compiled.textContent).toContain('Rohan Pawar');
  });

  it('should filter orders when clicking filter tabs', () => {
    vi.spyOn(adminService, 'getOrders').mockImplementation((status?: OrderStatus) => {
      const filtered = status ? mockOrders.filter((o) => o.status === status) : mockOrders;
      (adminService as any).adminOrdersState.set(filtered);
      return of(filtered);
    });

    fixture = TestBed.createComponent(AdminOrders);
    component = fixture.componentInstance;
    fixture.detectChanges();

    // Select Confirmed tab
    component.onSelectFilter('confirmed');
    fixture.detectChanges();

    expect(component.filteredOrders().length).toBe(1);
    expect(component.filteredOrders()[0].id).toBe('101');

    // Select Preparing tab
    component.onSelectFilter('preparing');
    fixture.detectChanges();

    expect(component.filteredOrders().length).toBe(1);
    expect(component.filteredOrders()[0].id).toBe('102');
  });

  it('should trigger status update to PREPARING when clicking Start Preparing', () => {
    vi.spyOn(adminService, 'getOrders').mockImplementation((status?: OrderStatus) => {
      const filtered = status ? mockOrders.filter((o) => o.status === status) : mockOrders;
      (adminService as any).adminOrdersState.set(filtered);
      return of(filtered);
    });
    const updateSpy = vi.spyOn(adminService, 'updateOrderStatus').mockReturnValue(
      of({ ...mockOrders[0], status: 'preparing' })
    );

    fixture = TestBed.createComponent(AdminOrders);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const startPrepBtn = fixture.nativeElement.querySelector('.btn-start-prep') as HTMLButtonElement;
    expect(startPrepBtn).toBeTruthy();
    startPrepBtn.click();

    expect(updateSpy).toHaveBeenCalledWith('101', 'preparing');
  });

  it('should trigger status update to CANCELLED when clicking Cancel Order', () => {
    vi.spyOn(adminService, 'getOrders').mockImplementation((status?: OrderStatus) => {
      const filtered = status ? mockOrders.filter((o) => o.status === status) : mockOrders;
      (adminService as any).adminOrdersState.set(filtered);
      return of(filtered);
    });
    const updateSpy = vi.spyOn(adminService, 'updateOrderStatus').mockReturnValue(
      of({ ...mockOrders[0], status: 'cancelled' })
    );

    fixture = TestBed.createComponent(AdminOrders);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const cancelBtn = fixture.nativeElement.querySelector('.btn-cancel') as HTMLButtonElement;
    expect(cancelBtn).toBeTruthy();
    cancelBtn.click();

    expect(updateSpy).toHaveBeenCalledWith('101', 'cancelled');
  });

  it('should display error message if status update is rejected by backend', () => {
    vi.spyOn(adminService, 'getOrders').mockImplementation((status?: OrderStatus) => {
      const filtered = status ? mockOrders.filter((o) => o.status === status) : mockOrders;
      (adminService as any).adminOrdersState.set(filtered);
      return of(filtered);
    });
    vi.spyOn(adminService, 'updateOrderStatus').mockReturnValue(
      throwError(() => new Error('Cannot transition order from status DELIVERED to PREPARING.'))
    );

    fixture = TestBed.createComponent(AdminOrders);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.updateStatus('101', 'preparing');
    fixture.detectChanges();

    expect(component.actionError()).toBe('Cannot transition order from status DELIVERED to PREPARING.');
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Cannot transition order from status DELIVERED to PREPARING.');
  });
});
