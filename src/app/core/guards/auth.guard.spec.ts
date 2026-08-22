import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { signal } from '@angular/core';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let authServiceMock: {
    isAuthenticated: ReturnType<typeof signal<boolean>>;
  };
  let router: Router;

  beforeEach(() => {
    authServiceMock = {
      isAuthenticated: signal<boolean>(false),
    };

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
      ],
    });

    router = TestBed.inject(Router);
  });

  it('should allow navigation when user is authenticated', () => {
    authServiceMock.isAuthenticated.set(true);

    const dummyRoute = {} as ActivatedRouteSnapshot;
    const dummyState = { url: '/profile' } as RouterStateSnapshot;

    const result = TestBed.runInInjectionContext(() =>
      authGuard(dummyRoute, dummyState)
    );

    expect(result).toBe(true);
  });

  it('should redirect unauthenticated users to /login with returnUrl query parameter', () => {
    authServiceMock.isAuthenticated.set(false);

    const dummyRoute = {} as ActivatedRouteSnapshot;
    const dummyState = { url: '/checkout' } as RouterStateSnapshot;

    const result = TestBed.runInInjectionContext(() =>
      authGuard(dummyRoute, dummyState)
    );

    expect(result instanceof UrlTree).toBe(true);
    const urlTree = result as UrlTree;
    const serialized = router.serializeUrl(urlTree);
    expect(serialized).toBe('/login?returnUrl=%2Fcheckout');
  });

  it('should protect orders route by redirecting with returnUrl=/orders', () => {
    authServiceMock.isAuthenticated.set(false);

    const dummyRoute = {} as ActivatedRouteSnapshot;
    const dummyState = { url: '/orders' } as RouterStateSnapshot;

    const result = TestBed.runInInjectionContext(() =>
      authGuard(dummyRoute, dummyState)
    );

    expect(result instanceof UrlTree).toBe(true);
    const urlTree = result as UrlTree;
    const serialized = router.serializeUrl(urlTree);
    expect(serialized).toBe('/login?returnUrl=%2Forders');
  });
});
