import { Injectable, computed, signal } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import {
  LoginCredentials,
  MockUserRecord,
  RegistrationData,
  User,
} from '../../shared/models/user.model';

const AUTH_USER_KEY = 'restaurant-hub-auth-user';
const MOCK_USERS_KEY = 'restaurant-hub-mock-users';

/**
 * Default mock seed user for testing and local development.
 * NOTE: This mock repository MUST be replaced by the Spring Boot backend authentication API.
 */
const DEFAULT_MOCK_USERS: MockUserRecord[] = [
  {
    id: 'USR-SEED-001',
    fullName: 'Rohan Pawar',
    email: 'rohan@restauranthub.com',
    phone: '9876543210',
    createdAt: '2026-01-15T10:00:00.000Z',
    passwordHashMock: 'Password123',
  },
];

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly currentUserState = signal<User | null>(
    this.loadUserFromStorage()
  );

  /** Public Signals */
  readonly currentUser = this.currentUserState.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserState() !== null);

  constructor() {
    this.initializeMockUsersStore();
  }

  /**
   * Register a new customer in the mock store and authenticate immediately.
   * Returns an Observable to mirror future Spring Boot HTTP endpoints.
   */
  register(data: RegistrationData): Observable<User> {
    const emailNormalized = data.email.trim().toLowerCase();
    const mockUsers = this.loadMockUsersFromStorage();

    const existingUser = mockUsers.find(
      (u) => u.email.toLowerCase() === emailNormalized
    );

    if (existingUser) {
      return throwError(
        () => new Error('An account with this email address already exists.')
      );
    }

    const newUserId = this.generateUserId();
    const newUser: User = {
      id: newUserId,
      fullName: data.fullName.trim(),
      email: data.email.trim(),
      phone: data.phone.trim(),
      createdAt: new Date().toISOString(),
    };

    const newMockRecord: MockUserRecord = {
      ...newUser,
      passwordHashMock: data.password,
    };

    // Save to mock users repository
    mockUsers.push(newMockRecord);
    this.saveMockUsersToStorage(mockUsers);

    // Set active session
    this.setCurrentUser(newUser);

    return of(newUser);
  }

  /**
   * Login with email and password against the mock store.
   * Returns an Observable to mirror future Spring Boot HTTP endpoints.
   */
  login(credentials: LoginCredentials): Observable<User> {
    const emailNormalized = credentials.email.trim().toLowerCase();
    const password = credentials.password;
    const mockUsers = this.loadMockUsersFromStorage();

    const matchedRecord = mockUsers.find(
      (u) =>
        u.email.toLowerCase() === emailNormalized &&
        u.passwordHashMock === password
    );

    if (!matchedRecord) {
      return throwError(
        () => new Error('Invalid email or password. Please try again.')
      );
    }

    const authenticatedUser: User = {
      id: matchedRecord.id,
      fullName: matchedRecord.fullName,
      email: matchedRecord.email,
      phone: matchedRecord.phone,
      createdAt: matchedRecord.createdAt,
    };

    this.setCurrentUser(authenticatedUser);
    return of(authenticatedUser);
  }

  /**
   * Logout the current user by clearing session state and storage.
   * Note: The shopping cart remains intact across logout.
   */
  logout(): void {
    this.currentUserState.set(null);
    this.removeUserFromStorage();
  }

  /**
   * Set and persist the current authenticated user.
   */
  private setCurrentUser(user: User): void {
    this.currentUserState.set(user);
    this.saveUserToStorage(user);
  }

  /**
   * Generate mock customer user ID.
   */
  private generateUserId(): string {
    const timestamp = Date.now().toString().slice(-4);
    const random = Math.floor(1000 + Math.random() * 9000).toString();
    return `USR-${timestamp}${random}`;
  }

  /**
   * Safe localStorage user loader.
   */
  private loadUserFromStorage(): User | null {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        const raw = localStorage.getItem(AUTH_USER_KEY);
        if (raw) {
          const parsed = JSON.parse(raw);
          if (
            parsed &&
            typeof parsed === 'object' &&
            typeof parsed.id === 'string' &&
            typeof parsed.email === 'string' &&
            typeof parsed.fullName === 'string'
          ) {
            return parsed as User;
          }
        }
      }
    } catch {
      // Gracefully handle corrupted storage
    }
    return null;
  }

  /**
   * Safe localStorage user saver.
   */
  private saveUserToStorage(user: User): void {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
      }
    } catch {
      // Gracefully ignore storage write failures
    }
  }

  /**
   * Safe localStorage user remover.
   */
  private removeUserFromStorage(): void {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        localStorage.removeItem(AUTH_USER_KEY);
      }
    } catch {
      // Gracefully ignore storage removal failures
    }
  }

  /**
   * Initialize mock users store with default seeds if empty.
   */
  private initializeMockUsersStore(): void {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        const existing = localStorage.getItem(MOCK_USERS_KEY);
        if (!existing) {
          localStorage.setItem(
            MOCK_USERS_KEY,
            JSON.stringify(DEFAULT_MOCK_USERS)
          );
        }
      }
    } catch {
      // Gracefully ignore storage initialization failures
    }
  }

  /**
   * Load mock users repository from localStorage.
   */
  private loadMockUsersFromStorage(): MockUserRecord[] {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        const raw = localStorage.getItem(MOCK_USERS_KEY);
        if (raw) {
          const parsed = JSON.parse(raw);
          if (Array.isArray(parsed)) {
            return parsed as MockUserRecord[];
          }
        }
      }
    } catch {
      // Fall back to default seed on corrupted data
    }
    return [...DEFAULT_MOCK_USERS];
  }

  /**
   * Save mock users repository to localStorage.
   */
  private saveMockUsersToStorage(users: MockUserRecord[]): void {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        localStorage.setItem(MOCK_USERS_KEY, JSON.stringify(users));
      }
    } catch {
      // Gracefully ignore storage write failures
    }
  }
}
