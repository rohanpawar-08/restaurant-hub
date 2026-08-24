import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { Dashboard } from './dashboard';
import { AdminService } from '../../../../core/services/admin.service';
import { DashboardSummaryApiResponse } from '../../../../core/api/models/admin-api.model';

describe('Admin Dashboard', () => {
  let component: Dashboard;
  let fixture: ComponentFixture<Dashboard>;
  let adminService: AdminService;

  const mockSummary: DashboardSummaryApiResponse = {
    totalOrders: 45,
    confirmedOrders: 10,
    preparingOrders: 8,
    readyOrders: 4,
    outForDeliveryOrders: 3,
    totalCustomers: 60,
    totalFoods: 35,
    activeFoods: 32,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        AdminService,
      ],
    }).compileComponents();

    adminService = TestBed.inject(AdminService);
  });

  it('should create dashboard component and load metrics on initialization', () => {
    const summarySpy = vi.spyOn(adminService, 'getDashboardSummary').mockImplementation(() => {
      (adminService as any).dashboardSummaryState.set(mockSummary);
      return of(mockSummary);
    });

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(summarySpy).toHaveBeenCalled();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('45'); // Total Orders
    expect(compiled.textContent).toContain('10'); // Confirmed
    expect(compiled.textContent).toContain('60'); // Customers
    expect(compiled.textContent).toContain('32 / 35'); // Active Foods
  });

  it('should display error alert and trigger retry when metric load fails', () => {
    vi.spyOn(adminService, 'getDashboardSummary').mockImplementation(() => {
      (adminService as any).errorState.set('Server connection error');
      return throwError(() => new Error('Server connection error'));
    });

    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Server connection error');

    // Test retry
    const retrySpy = vi.spyOn(adminService, 'getDashboardSummary').mockImplementation(() => {
      (adminService as any).errorState.set(null);
      (adminService as any).dashboardSummaryState.set(mockSummary);
      return of(mockSummary);
    });
    const retryBtn = compiled.querySelector('.btn-retry') as HTMLButtonElement;
    retryBtn.click();
    fixture.detectChanges();

    expect(retrySpy).toHaveBeenCalled();
  });
});
