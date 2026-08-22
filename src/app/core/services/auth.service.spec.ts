import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { LoginCredentials, RegistrationData } from '../../shared/models/user.model';

const AUTH_USER_KEY = 'restaurant-hub-auth-user';
const MOCK_USERS_KEY = 'restaurant-hub-mock-users';

describe('AuthService', () => {
  let service: AuthService;
  let store: Record<string, string> = {};

  const mockLocalStorage = {
    getItem: vi.fn((key: string) => store[key] ?? null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value;
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key];
    }),
    clear: vi.fn(() => {
      store = {};
    }),
  };

  beforeEach(() => {
    store = {};
    Object.defineProperty(window, 'localStorage', {
      value: mockLocalStorage,
      writable: true,
      configurable: true,
    });

    TestBed.configureTestingModule({
      providers: [AuthService],
    });
    service = TestBed.inject(AuthService);
  });

  afterEach(() => {
    store = {};
    vi.clearAllMocks();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should start in unauthenticated state when no storage session exists', () => {
    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('should register a new user, update currentUser signal, and persist to storage', () => {
    const regData: RegistrationData = {
      fullName: 'Anita Sharma',
      email: 'anita@example.com',
      phone: '9876543211',
      password: 'SecurePassword123',
    };

    let createdUser: any;
    service.register(regData).subscribe({
      next: (user) => {
        createdUser = user;
      },
    });

    expect(createdUser).toBeTruthy();
    expect(createdUser.fullName).toBe('Anita Sharma');
    expect(createdUser.email).toBe('anita@example.com');
    expect(createdUser.id).toBeTruthy();
    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()?.email).toBe('anita@example.com');

    // Confirm session persisted in storage
    const rawStored = store[AUTH_USER_KEY];
    expect(rawStored).toBeTruthy();
    const parsed = JSON.parse(rawStored);
    expect(parsed.email).toBe('anita@example.com');
  });

  it('should throw an error when registering with an existing email', () => {
    const regData: RegistrationData = {
      fullName: 'Rohan Duplicate',
      email: 'rohan@restauranthub.com', // Already seeded
      phone: '9876543210',
      password: 'Password123',
    };

    let errorResult: any = null;
    service.register(regData).subscribe({
      error: (err: Error) => {
        errorResult = err;
      },
    });

    expect(errorResult).toBeTruthy();
    expect(errorResult?.message).toContain('already exists');
  });

  it('should authenticate user with valid credentials via login()', () => {
    const credentials: LoginCredentials = {
      email: 'rohan@restauranthub.com',
      password: 'Password123',
    };

    let loggedInUser: any;
    service.login(credentials).subscribe({
      next: (user) => {
        loggedInUser = user;
      },
    });

    expect(loggedInUser).toBeTruthy();
    expect(loggedInUser.email).toBe('rohan@restauranthub.com');
    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()?.fullName).toBe('Rohan Pawar');
  });

  it('should fail login when invalid credentials are provided', () => {
    const credentials: LoginCredentials = {
      email: 'rohan@restauranthub.com',
      password: 'WrongPassword999',
    };

    let errorResult: any = null;
    service.login(credentials).subscribe({
      error: (err: Error) => {
        errorResult = err;
      },
    });

    expect(errorResult).toBeTruthy();
    expect(errorResult?.message).toContain('Invalid email or password');
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should clear currentUser and remove session from localStorage on logout()', () => {
    // Perform login first
    service.login({
      email: 'rohan@restauranthub.com',
      password: 'Password123',
    }).subscribe();

    expect(service.isAuthenticated()).toBe(true);

    // Logout
    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
    expect(store[AUTH_USER_KEY]).toBeUndefined();
  });

  it('should gracefully handle malformed JSON in localStorage without throwing', () => {
    store[AUTH_USER_KEY] = '{ invalid JSON ::: corrupt';
    store[MOCK_USERS_KEY] = 'not valid json at all';

    // Instantiating a new service should not crash
    const newService = TestBed.runInInjectionContext(() => new AuthService());
    expect(newService.isAuthenticated()).toBe(false);
    expect(newService.currentUser()).toBeNull();
  });
});
