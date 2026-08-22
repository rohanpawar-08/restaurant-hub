import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Functional Route Guard for Customer Authentication.
 * Protects customer-only routes (/profile, /orders, /checkout).
 * Redirects unauthenticated requests to /login with the intended destination in returnUrl.
 */
export const authGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
): boolean | UrlTree => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Preserve the intended destination in returnUrl query parameter
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  });
};
