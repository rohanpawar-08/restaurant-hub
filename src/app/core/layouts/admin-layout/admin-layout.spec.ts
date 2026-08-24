import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { AdminLayout } from './admin-layout';
import { AuthService } from '../../services/auth.service';
import { User } from '../../../shared/models/user.model';

describe('AdminLayout', () => {
  let component: AdminLayout;
  let fixture: ComponentFixture<AdminLayout>;
  let authService: AuthService;
  let router: Router;

  const mockAdminUser: User = {
    id: '1',
    fullName: 'Admin Chef',
    email: 'admin@example.com',
    phone: '9999999999',
    role: 'ADMIN',
    createdAt: '2026-08-24T10:00:00.000Z',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminLayout],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService,
      ],
    }).compileComponents();

    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  it('should create admin layout and display admin full name in header', () => {
    (authService as any).currentUserState.set(mockAdminUser);

    fixture = TestBed.createComponent(AdminLayout);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component).toBeTruthy();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Admin Chef');
    expect(compiled.textContent).toContain('ADMIN PORTAL');
  });

  it('should trigger logout and redirect to /login', () => {
    const logoutSpy = vi.spyOn(authService, 'logout').mockReturnValue(of(undefined));
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture = TestBed.createComponent(AdminLayout);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.logout();

    expect(logoutSpy).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
