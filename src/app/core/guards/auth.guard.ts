import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { Observable, map } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Functional Route Guard for Customer Authentication.
 * Protects customer-only routes (/profile, /orders, /checkout).
 * Ensures session restoration finishes before redirecting unauthenticated requests to /login.
 */
export const authGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
): Observable<boolean | UrlTree> | boolean | UrlTree => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isInitialized()) {
    if (authService.isAuthenticated()) {
      return true;
    }
    return router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url },
    });
  }

  return authService.checkSession().pipe(
    map((user) => {
      if (user) {
        return true;
      }
      return router.createUrlTree(['/login'], {
        queryParams: { returnUrl: state.url },
      });
    })
  );
};
