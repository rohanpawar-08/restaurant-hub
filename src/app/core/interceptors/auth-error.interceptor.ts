import { inject } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpRequest,
} from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Global HTTP error interceptor handling 401 Unauthorized responses.
 * When an authenticated API call fails with 401, clears the active user session
 * and redirects gracefully to the login page with returnUrl and sessionExpired notice.
 */
export function authErrorInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        const isAuthEndpoint =
          req.url.includes('/api/v1/auth/login') ||
          req.url.includes('/api/v1/auth/register') ||
          req.url.includes('/api/v1/auth/csrf') ||
          req.url.includes('/api/v1/auth/me');

        if (!isAuthEndpoint && authService.isAuthenticated()) {
          const currentUrl = router.url;
          const returnUrl = currentUrl && currentUrl !== '/login' ? currentUrl : '/';
          authService.logout().subscribe({
            next: () => {
              router.navigate(['/login'], {
                queryParams: { sessionExpired: 'true', returnUrl },
              });
            },
          });
        }
      }

      return throwError(() => error);
    })
  );
}
