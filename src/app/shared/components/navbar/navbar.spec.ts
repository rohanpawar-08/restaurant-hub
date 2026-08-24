import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { Navbar } from './navbar';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { User } from '../../models/user.model';

describe('Navbar', () => {
  let component: Navbar;
  let fixture: ComponentFixture<Navbar>;
  let authServiceMock: {
    currentUser: ReturnType<typeof signal<User | null>>;
    isAuthenticated: ReturnType<typeof signal<boolean>>;
    logout: ReturnType<typeof vi.fn>;
  };
  let cartServiceMock: {
    totalQuantity: ReturnType<typeof signal<number>>;
  };
  let router: Router;

  beforeEach(async () => {
    authServiceMock = {
      currentUser: signal<User | null>(null),
      isAuthenticated: signal<boolean>(false),
      logout: vi.fn().mockReturnValue(of(undefined)),
    };

    cartServiceMock = {
      totalQuantity: signal<number>(0),
    };

    await TestBed.configureTestingModule({
      imports: [Navbar],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
        { provide: CartService, useValue: cartServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Navbar);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render navigation link to /orders', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const ordersLink = compiled.querySelector('a[routerLink="/orders"]');
    expect(ordersLink).toBeTruthy();
    expect(ordersLink?.textContent?.trim()).toBe('Orders');
  });

  it('should show Login and Register buttons when unauthenticated', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const loginLink = compiled.querySelector('.login-btn');
    const registerLink = compiled.querySelector('.register-btn');
    const userSection = compiled.querySelector('.user-nav-section');

    expect(loginLink).toBeTruthy();
    expect(registerLink).toBeTruthy();
    expect(userSection).toBeNull();
  });

  it('should show user greeting, profile link, and logout button when authenticated', async () => {
    authServiceMock.currentUser.set({
      id: 'USR-01',
      fullName: 'Rohan Pawar',
      email: 'rohan@example.com',
      phone: '9876543210',
      createdAt: '2026-01-01',
    });
    authServiceMock.isAuthenticated.set(true);

    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    const loginLink = compiled.querySelector('.login-btn');
    const userSection = compiled.querySelector('.user-nav-section');
    const greeting = compiled.querySelector('.user-greeting');
    const profileLink = compiled.querySelector('a[routerLink="/profile"]');
    const logoutBtn = compiled.querySelector('.logout-btn');

    expect(loginLink).toBeNull();
    expect(userSection).toBeTruthy();
    expect(greeting?.textContent).toContain('Hi, Rohan');
    expect(profileLink).toBeTruthy();
    expect(logoutBtn).toBeTruthy();
  });

  it('should call authService.logout and navigate to root on logout click', () => {
    authServiceMock.isAuthenticated.set(true);
    authServiceMock.currentUser.set({
      id: 'USR-01',
      fullName: 'Rohan Pawar',
      email: 'rohan@example.com',
      phone: '9876543210',
      createdAt: '2026-01-01',
    });
    fixture.detectChanges();

    component.logout();

    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should display cart badge when cart has items', async () => {
    cartServiceMock.totalQuantity.set(3);
    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    const badge = compiled.querySelector('.cart-badge');
    expect(badge).toBeTruthy();
    expect(badge?.textContent?.trim()).toBe('3');
  });
});
