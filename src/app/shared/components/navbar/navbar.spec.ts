import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { Navbar } from './navbar';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { RestaurantSettingsService } from '../../../core/services/restaurant-settings.service';
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

    const settingsServiceMock = {
      restaurantName: signal('RestaurantHub'),
      logoUrl: signal(null),
    };

    await TestBed.configureTestingModule({
      imports: [Navbar],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
        { provide: CartService, useValue: cartServiceMock },
        { provide: RestaurantSettingsService, useValue: settingsServiceMock },
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

  it('should render navigation links to Home, Menu, Orders, About, Contact, and Cart', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const homeLink = compiled.querySelector('a[routerLink="/"]');
    const menuLink = compiled.querySelector('a[routerLink="/menu"]');
    const ordersLink = compiled.querySelector('a[routerLink="/orders"]');
    const aboutLink = compiled.querySelector('a[routerLink="/about"]');
    const contactLink = compiled.querySelector('a[routerLink="/contact"]');
    const cartLink = compiled.querySelector('a[routerLink="/cart"]');

    expect(homeLink).toBeTruthy();
    expect(menuLink?.textContent?.trim()).toBe('Menu');
    expect(ordersLink?.textContent?.trim()).toBe('Orders');
    expect(aboutLink?.textContent?.trim()).toBe('About');
    expect(contactLink?.textContent?.trim()).toBe('Contact');
    expect(cartLink).toBeTruthy();
  });

  it('should show Login and Register buttons and NOT show Admin Panel when unauthenticated', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const loginLink = compiled.querySelector('.login-btn');
    const registerLink = compiled.querySelector('.register-btn');
    const userSection = compiled.querySelector('.user-nav-section');
    const adminLink = compiled.querySelector('a[routerLink="/admin"]');

    expect(loginLink).toBeTruthy();
    expect(registerLink).toBeTruthy();
    expect(userSection).toBeNull();
    expect(adminLink).toBeNull();
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

  it('should show Admin Panel button pointing to /admin when authenticated user has ADMIN role', async () => {
    authServiceMock.currentUser.set({
      id: 'USR-ADMIN',
      fullName: 'Admin Chef',
      email: 'admin@example.com',
      phone: '9876543210',
      role: 'ADMIN',
      createdAt: '2026-01-01',
    });
    authServiceMock.isAuthenticated.set(true);

    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    const adminLink = compiled.querySelector('a[routerLink="/admin"]');
    expect(adminLink).toBeTruthy();
    expect(adminLink?.getAttribute('routerLink')).toBe('/admin');
    expect(adminLink?.textContent).toContain('Admin Panel');
  });

  it('should NOT show Admin Panel button when authenticated user has CUSTOMER role', async () => {
    authServiceMock.currentUser.set({
      id: 'USR-CUST',
      fullName: 'Customer Rohan',
      email: 'rohan@example.com',
      phone: '9876543210',
      role: 'CUSTOMER',
      createdAt: '2026-01-01',
    });
    authServiceMock.isAuthenticated.set(true);

    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    const adminLink = compiled.querySelector('a[routerLink="/admin"]');
    expect(adminLink).toBeNull();
  });

  it('should toggle and close the mobile navigation menu', () => {
    expect(component.isMenuOpen()).toBe(false);

    component.toggleMenu();
    expect(component.isMenuOpen()).toBe(true);

    component.toggleMenu();
    expect(component.isMenuOpen()).toBe(false);

    component.toggleMenu();
    expect(component.isMenuOpen()).toBe(true);

    component.closeMenu();
    expect(component.isMenuOpen()).toBe(false);
  });

  it('should close mobile menu on escape key', () => {
    component.isMenuOpen.set(true);
    expect(component.isMenuOpen()).toBe(true);

    component.onEscape();
    expect(component.isMenuOpen()).toBe(false);
  });
});



