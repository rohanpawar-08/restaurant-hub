import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';
import { LoginCredentials, RegistrationData } from '../../shared/models/user.model';
import { UserApiResponse } from '../api/models/user-api.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiBaseUrl}/auth`;

  const mockUserResponse: UserApiResponse = {
    id: 1,
    fullName: 'Rohan Pawar',
    email: 'rohan@restauranthub.com',
    phone: '9876543210',
    role: 'CUSTOMER',
    createdAt: '2026-01-15T10:00:00.000Z',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    service = TestBed.inject(AuthService);

    // Handle initial constructor checkSession request
    const initReq = httpMock.match(`${baseUrl}/me`);
    if (initReq.length > 0) {
      initReq[0].flush(null, { status: 401, statusText: 'Unauthorized' });
    }
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created and start in unauthenticated state after 401 on init', () => {
    expect(service).toBeTruthy();
    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('should restore session and update currentUser on GET /auth/me 200 OK', () => {
    service.checkSession().subscribe((user) => {
      expect(user).toBeTruthy();
      expect(user?.fullName).toBe('Rohan Pawar');
      expect(user?.email).toBe('rohan@restauranthub.com');
      expect(user?.role).toBe('CUSTOMER');
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(mockUserResponse);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()?.id).toBe('1');
  });

  it('should set currentUser to null on GET /auth/me 401 Unauthorized', () => {
    service.checkSession().subscribe((user) => {
      expect(user).toBeNull();
    });

    const req = httpMock.expectOne(`${baseUrl}/me`);
    expect(req.request.method).toBe('GET');
    req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('should successfully log in, set session state, and return user', () => {
    const credentials: LoginCredentials = {
      email: 'rohan@restauranthub.com',
      password: 'Password123',
    };

    service.login(credentials).subscribe((user) => {
      expect(user.fullName).toBe('Rohan Pawar');
      expect(user.email).toBe('rohan@restauranthub.com');
    });

    const req = httpMock.expectOne(`${baseUrl}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    expect(req.request.body.email).toBe('rohan@restauranthub.com');
    expect(req.request.body.password).toBe('Password123');

    req.flush(mockUserResponse);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()?.fullName).toBe('Rohan Pawar');
  });

  it('should handle login failure on bad credentials', () => {
    const credentials: LoginCredentials = {
      email: 'rohan@restauranthub.com',
      password: 'WrongPassword',
    };

    let errorMsg = '';
    service.login(credentials).subscribe({
      error: (err: Error) => {
        errorMsg = err.message;
      },
    });

    const req = httpMock.expectOne(`${baseUrl}/login`);
    req.flush(
      { message: 'Invalid email or password. Please try again.' },
      { status: 401, statusText: 'Unauthorized' }
    );

    expect(errorMsg).toBe('Invalid email or password. Please try again.');
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should register a customer, auto-login, and update authenticated state', () => {
    const regData: RegistrationData = {
      fullName: 'Anita Sharma',
      email: 'anita@example.com',
      phone: '9876543211',
      password: 'SecurePassword123',
    };

    const newMockUserResponse: UserApiResponse = {
      id: 2,
      fullName: 'Anita Sharma',
      email: 'anita@example.com',
      phone: '9876543211',
      role: 'CUSTOMER',
      createdAt: '2026-02-01T12:00:00.000Z',
    };

    service.register(regData).subscribe((user) => {
      expect(user.fullName).toBe('Anita Sharma');
      expect(user.email).toBe('anita@example.com');
    });

    const regReq = httpMock.expectOne(`${baseUrl}/register`);
    expect(regReq.request.method).toBe('POST');
    expect(regReq.request.body.fullName).toBe('Anita Sharma');
    regReq.flush(newMockUserResponse);

    const loginReq = httpMock.expectOne(`${baseUrl}/login`);
    expect(loginReq.request.method).toBe('POST');
    expect(loginReq.request.body.email).toBe('anita@example.com');
    loginReq.flush(newMockUserResponse);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()?.email).toBe('anita@example.com');
  });

  it('should handle duplicate email registration error', () => {
    const regData: RegistrationData = {
      fullName: 'Anita Duplicate',
      email: 'anita@example.com',
      phone: '9876543211',
      password: 'SecurePassword123',
    };

    let errorMsg = '';
    service.register(regData).subscribe({
      error: (err: Error) => {
        errorMsg = err.message;
      },
    });

    const regReq = httpMock.expectOne(`${baseUrl}/register`);
    regReq.flush(
      { message: 'An account with this email address already exists.' },
      { status: 409, statusText: 'Conflict' }
    );

    expect(errorMsg).toBe('An account with this email address already exists.');
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should log out by sending POST /auth/logout and clearing currentUser state', () => {
    // First simulate logged-in state
    service.login({ email: 'rohan@restauranthub.com', password: 'Password123' }).subscribe();
    const loginReq = httpMock.expectOne(`${baseUrl}/login`);
    loginReq.flush(mockUserResponse);
    expect(service.isAuthenticated()).toBe(true);

    // Logout
    service.logout().subscribe();

    const logoutReq = httpMock.expectOne(`${baseUrl}/logout`);
    expect(logoutReq.request.method).toBe('POST');
    expect(logoutReq.request.withCredentials).toBe(true);
    logoutReq.flush(null, { status: 204, statusText: 'No Content' });

    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('should call GET /auth/csrf on initCsrf', () => {
    service.initCsrf().subscribe((res) => {
      expect(res?.headerName).toBe('X-XSRF-TOKEN');
      expect(res?.token).toBe('csrf-test-token-123');
    });

    const req = httpMock.expectOne(`${baseUrl}/csrf`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush({ headerName: 'X-XSRF-TOKEN', token: 'csrf-test-token-123' });
  });
});
