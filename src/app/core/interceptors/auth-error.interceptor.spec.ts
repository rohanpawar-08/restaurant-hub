import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
  HttpErrorResponse,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { authErrorInterceptor } from './auth-error.interceptor';
import { AuthService } from '../services/auth.service';

describe('authErrorInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let authServiceMock: {
    isAuthenticated: ReturnType<typeof signal<boolean>>;
    logout: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  beforeEach(() => {
    authServiceMock = {
      isAuthenticated: signal<boolean>(true),
      logout: vi.fn().mockReturnValue(of(undefined)),
    };

    const routerMock = {
      url: '/orders',
      navigate: vi.fn().mockReturnValue(Promise.resolve(true)),
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authErrorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should pass through successful requests', () => {
    let result: unknown;
    http.get('/api/v1/food').subscribe((res) => (result = res));

    const req = httpTesting.expectOne('/api/v1/food');
    req.flush({ data: 'ok' });

    expect(result).toEqual({ data: 'ok' });
    expect(authServiceMock.logout).not.toHaveBeenCalled();
  });

  it('should logout and redirect to login when an authenticated request returns 401', () => {
    let errorCaught = false;

    http.get('/api/v1/orders').subscribe({
      next: () => {},
      error: (err: HttpErrorResponse) => {
        expect(err.status).toBe(401);
        errorCaught = true;
      },
    });

    const req = httpTesting.expectOne('/api/v1/orders');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(errorCaught).toBe(true);
    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { sessionExpired: 'true', returnUrl: '/orders' },
    });
  });

  it('should not redirect for 401 on login endpoint', () => {
    let errorCaught = false;

    http.post('/api/v1/auth/login', {}).subscribe({
      next: () => {},
      error: () => {
        errorCaught = true;
      },
    });

    const req = httpTesting.expectOne('/api/v1/auth/login');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(errorCaught).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
