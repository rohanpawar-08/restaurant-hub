import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { signal } from '@angular/core';
import { Profile } from './profile';
import { AuthService } from '../../../../core/services/auth.service';
import { User } from '../../../../shared/models/user.model';

describe('Profile', () => {
  let component: Profile;
  let fixture: ComponentFixture<Profile>;
  let authServiceMock: {
    currentUser: ReturnType<typeof signal<User | null>>;
    logout: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  const mockUser: User = {
    id: 'USR-SEED-001',
    fullName: 'Rohan Pawar',
    email: 'rohan@restauranthub.com',
    phone: '9876543210',
    createdAt: '2026-01-15T10:00:00.000Z',
  };

  beforeEach(async () => {
    authServiceMock = {
      currentUser: signal<User | null>(mockUser),
      logout: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [Profile],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Profile);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockImplementation(() => Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should create the profile component', () => {
    expect(component).toBeTruthy();
  });

  it('should render customer details correctly', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const nameEl = compiled.querySelector('.user-fullname');
    const emailEl = compiled.querySelector('.user-email');
    const initialsEl = compiled.querySelector('.avatar-initials');

    expect(nameEl?.textContent).toContain('Rohan Pawar');
    expect(emailEl?.textContent).toContain('rohan@restauranthub.com');
    expect(initialsEl?.textContent?.trim()).toBe('RP');
  });

  it('should compute avatar initials for single word names and multiple words', () => {
    expect(component.userInitials()).toBe('RP');

    authServiceMock.currentUser.set({
      id: 'USR-02',
      fullName: 'Alice',
      email: 'alice@example.com',
      phone: '9988776655',
      createdAt: '2026-01-01',
    });
    expect(component.userInitials()).toBe('AL');

    authServiceMock.currentUser.set({
      id: 'USR-03',
      fullName: 'John Middle Doe',
      email: 'john@example.com',
      phone: '9988776655',
      createdAt: '2026-01-01',
    });
    expect(component.userInitials()).toBe('JD');
  });

  it('should render links to /orders and /menu', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const ordersLink = compiled.querySelector('a[routerLink="/orders"]');
    const menuLink = compiled.querySelector('a[routerLink="/menu"]');

    expect(ordersLink).toBeTruthy();
    expect(menuLink).toBeTruthy();
  });

  it('should trigger logout and navigate to login on button click', () => {
    component.logout();
    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should render unauthenticated fallback message when currentUser is null', async () => {
    authServiceMock.currentUser.set(null);
    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    const fallbackEl = compiled.querySelector('.unauthenticated-card');
    expect(fallbackEl).toBeTruthy();
    expect(fallbackEl?.textContent).toContain('No Active Session');
  });
});
