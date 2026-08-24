import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom, of } from 'rxjs';
import { adminGuard } from './admin.guard';
import { AuthService } from '../services/auth.service';
import { User } from '../../shared/models/user.model';

describe('adminGuard', () => {
  let authService: AuthService;
  let router: Router;

  const mockAdminUser: User = {
    id: '1',
    fullName: 'Admin User',
    email: 'admin@example.com',
    phone: '9999999999',
    role: 'ADMIN',
    createdAt: '2026-08-24T10:00:00.000Z',
  };

  const mockCustomerUser: User = {
    id: '2',
    fullName: 'Customer User',
    email: 'customer@example.com',
    phone: '8888888888',
    role: 'CUSTOMER',
    createdAt: '2026-08-24T10:00:00.000Z',
  };

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = { url: '/admin/orders' } as RouterStateSnapshot;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  it('should permit access when user is authenticated with ADMIN role', () => {
    (authService as any).isInitializedState.set(true);
    (authService as any).currentUserState.set(mockAdminUser);

    const result = TestBed.runInInjectionContext(() => adminGuard(mockRoute, mockState));
    expect(result).toBe(true);
  });

  it('should redirect to / when authenticated user has CUSTOMER role', () => {
    (authService as any).isInitializedState.set(true);
    (authService as any).currentUserState.set(mockCustomerUser);

    const result = TestBed.runInInjectionContext(() => adminGuard(mockRoute, mockState)) as UrlTree;
    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result)).toBe('/');
  });

  it('should redirect to /login with returnUrl when user is not authenticated', () => {
    (authService as any).isInitializedState.set(true);
    (authService as any).currentUserState.set(null);

    const result = TestBed.runInInjectionContext(() => adminGuard(mockRoute, mockState)) as UrlTree;
    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result)).toContain('/login');
    expect(result.queryParams['returnUrl']).toBe('/admin/orders');
  });

  it('should check session asynchronously and permit ADMIN', async () => {
    (authService as any).isInitializedState.set(false);
    vi.spyOn(authService, 'checkSession').mockReturnValue(of(mockAdminUser));

    const result$ = TestBed.runInInjectionContext(() => adminGuard(mockRoute, mockState));
    const res = await firstValueFrom(result$ as any);
    expect(res).toBe(true);
  });

  it('should check session asynchronously and redirect CUSTOMER to /', async () => {
    (authService as any).isInitializedState.set(false);
    vi.spyOn(authService, 'checkSession').mockReturnValue(of(mockCustomerUser));

    const result$ = TestBed.runInInjectionContext(() => adminGuard(mockRoute, mockState));
    const res = await firstValueFrom(result$ as any);
    expect(res instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(res as UrlTree)).toBe('/');
  });
});
