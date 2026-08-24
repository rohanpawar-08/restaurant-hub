import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, map, of, switchMap, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginCredentials, RegistrationData, User } from '../../shared/models/user.model';
import { UserApiResponse } from '../api/models/user-api.model';
import { mapUserApiResponseToUser } from '../api/mappers/user-api.mapper';

/**
 * Enterprise Angular Authentication Service.
 *
 * Architecture:
 * 1. Server-Side Session Management: Interacts with Spring Security using HttpOnly session cookies.
 * 2. Zero LocalStorage Credentials: Passwords and tokens are never persisted on the client.
 * 3. Reactive State Signals: Exposes reactive signals (`currentUser`, `isAuthenticated`, `isLoading`, `isInitialized`).
 * 4. Cross-Origin Credentials: All HTTP calls pass `withCredentials: true` to synchronize cookies.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly currentUserState = signal<User | null>(null);
  private readonly isInitializedState = signal<boolean>(false);
  private readonly isLoadingState = signal<boolean>(false);

  /** Public Signals */
  readonly currentUser = this.currentUserState.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserState() !== null);
  readonly isInitialized = this.isInitializedState.asReadonly();
  readonly isLoading = this.isLoadingState.asReadonly();

  constructor() {
    this.checkSession().subscribe();
  }

  /**
   * Initializes or refreshes anti-CSRF token cookie by calling GET /api/v1/auth/csrf.
   */
  initCsrf(): Observable<{ headerName: string; token: string } | null> {
    return this.http
      .get<{ headerName: string; token: string }>(`${this.baseUrl}/csrf`, { withCredentials: true })
      .pipe(catchError(() => of(null)));
  }

  /**
   * Verify active server-side HTTP session on application load or page reload.
   * Invokes `GET /api/v1/auth/me` with cookies.
   */
  checkSession(): Observable<User | null> {
    this.isLoadingState.set(true);

    return this.http
      .get<UserApiResponse>(`${this.baseUrl}/me`, { withCredentials: true })
      .pipe(
        map((response) => mapUserApiResponseToUser(response)),
        tap((user) => {
          this.currentUserState.set(user);
          this.isInitializedState.set(true);
          this.isLoadingState.set(false);
        }),
        catchError(() => {
          // 401 Unauthorized or network error means customer is not logged in
          this.currentUserState.set(null);
          this.isInitializedState.set(true);
          this.isLoadingState.set(false);
          return of(null);
        })
      );
  }

  /**
   * Customer login via Spring Security session cookie.
   * Invokes `POST /api/v1/auth/login`.
   */
  login(credentials: LoginCredentials): Observable<User> {
    this.isLoadingState.set(true);

    return this.http
      .post<UserApiResponse>(
        `${this.baseUrl}/login`,
        {
          email: credentials.email.trim().toLowerCase(),
          password: credentials.password,
        },
        { withCredentials: true }
      )
      .pipe(
        map((response) => mapUserApiResponseToUser(response)),
        tap((user) => {
          this.currentUserState.set(user);
          this.isLoadingState.set(false);
        }),
        catchError((error: HttpErrorResponse) => {
          this.isLoadingState.set(false);
          const message =
            error.error?.message ||
            (error.status === 401
              ? 'Invalid email or password. Please try again.'
              : 'Unable to sign in at this time. Please check your network connection.');
          return throwError(() => new Error(message));
        })
      );
  }

  /**
   * Customer registration with seamless automatic login.
   * Invokes `POST /api/v1/auth/register` followed by `POST /api/v1/auth/login`.
   */
  register(data: RegistrationData): Observable<User> {
    this.isLoadingState.set(true);

    const payload = {
      fullName: data.fullName.trim(),
      email: data.email.trim().toLowerCase(),
      phone: data.phone.trim(),
      password: data.password,
    };

    return this.http
      .post<UserApiResponse>(`${this.baseUrl}/register`, payload, {
        withCredentials: true,
      })
      .pipe(
        switchMap(() =>
          this.login({
            email: payload.email,
            password: payload.password,
          })
        ),
        catchError((error: HttpErrorResponse) => {
          this.isLoadingState.set(false);
          const message =
            error.error?.message ||
            (error.status === 409
              ? 'An account with this email or mobile number already exists.'
              : 'Registration failed. Please review your details and try again.');
          return throwError(() => new Error(message));
        })
      );
  }

  /**
   * Customer logout: terminates backend session and clears local user state.
   * Invokes `POST /api/v1/auth/logout`.
   */
  logout(): Observable<void> {
    this.isLoadingState.set(true);

    return this.http
      .post<void>(`${this.baseUrl}/logout`, {}, { withCredentials: true })
      .pipe(
        tap(() => {
          this.currentUserState.set(null);
          this.isLoadingState.set(false);
        }),
        catchError(() => {
          // Even if network call fails, guarantee local state is cleared
          this.currentUserState.set(null);
          this.isLoadingState.set(false);
          return of(undefined);
        })
      );
  }
}
