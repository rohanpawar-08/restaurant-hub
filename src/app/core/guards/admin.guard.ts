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
 * Functional Route Guard protecting `/admin/**` endpoints.
 *
 * Rules:
 * 1. Unauthenticated users -> Redirect to `/login?returnUrl=...`
 * 2. Authenticated non-admin (CUSTOMER) -> Redirect to safe `/` home route
 * 3. Authenticated ADMIN -> Permit access
 */
export const adminGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
): Observable<boolean | UrlTree> | boolean | UrlTree => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isInitialized()) {
    const user = authService.currentUser();
    if (!user) {
      return router.createUrlTree(['/login'], {
        queryParams: { returnUrl: state.url },
      });
    }

    if (user.role === 'ADMIN') {
      return true;
    }

    // Authenticated as CUSTOMER -> unauthorized for admin area
    return router.createUrlTree(['/']);
  }

  return authService.checkSession().pipe(
    map((user) => {
      if (!user) {
        return router.createUrlTree(['/login'], {
          queryParams: { returnUrl: state.url },
        });
      }

      if (user.role === 'ADMIN') {
        return true;
      }

      return router.createUrlTree(['/']);
    })
  );
};
