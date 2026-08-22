import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Login } from './login';
import { AuthService } from '../../../../core/services/auth.service';
import { User } from '../../../../shared/models/user.model';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let authServiceSpy: { login: ReturnType<typeof vi.fn> };
  let router: Router;

  const mockUser: User = {
    id: 'USR-SEED-001',
    fullName: 'Rohan Pawar',
    email: 'rohan@restauranthub.com',
    phone: '9876543210',
    createdAt: '2026-01-15T10:00:00.000Z',
  };

  beforeEach(async () => {
    authServiceSpy = {
      login: vi.fn().mockReturnValue(of(mockUser)),
    };

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: vi.fn().mockReturnValue(null),
              },
            },
          },
        },
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockImplementation(() => Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should create the login component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form as invalid with empty fields', () => {
    expect(component.loginForm.valid).toBe(false);
    expect(component.loginForm.get('email')?.value).toBe('');
    expect(component.loginForm.get('password')?.value).toBe('');
  });

  it('should validate email format and presence', () => {
    const emailControl = component.loginForm.get('email');
    emailControl?.setValue('');
    expect(emailControl?.errors?.['required']).toBe(true);

    emailControl?.setValue('invalid-email');
    expect(emailControl?.errors?.['email']).toBe(true);

    emailControl?.setValue('rohan@restauranthub.com');
    expect(emailControl?.valid).toBe(true);
  });

  it('should auto-fill demo credentials on button trigger', () => {
    component.fillDemoCredentials();
    expect(component.loginForm.get('email')?.value).toBe('rohan@restauranthub.com');
    expect(component.loginForm.get('password')?.value).toBe('Password123');
    expect(component.loginForm.valid).toBe(true);
  });

  it('should not call authService.login if form is invalid', () => {
    component.onSubmit();
    expect(authServiceSpy.login).not.toHaveBeenCalled();
    expect(component.isSubmitted()).toBe(true);
  });

  it('should login successfully and navigate to default root path', () => {
    component.loginForm.setValue({
      email: 'rohan@restauranthub.com',
      password: 'Password123',
    });

    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({
      email: 'rohan@restauranthub.com',
      password: 'Password123',
    });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('should redirect to sanitized returnUrl after login', () => {
    const route = TestBed.inject(ActivatedRoute);
    vi.spyOn(route.snapshot.queryParamMap, 'get').mockReturnValue('/checkout');

    component.loginForm.setValue({
      email: 'rohan@restauranthub.com',
      password: 'Password123',
    });

    component.onSubmit();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/checkout');
  });

  it('should sanitize unsafe external returnUrls and fallback to root', () => {
    const route = TestBed.inject(ActivatedRoute);
    vi.spyOn(route.snapshot.queryParamMap, 'get').mockReturnValue('https://malicious.com');

    component.loginForm.setValue({
      email: 'rohan@restauranthub.com',
      password: 'Password123',
    });

    component.onSubmit();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('should display error message on invalid credentials', () => {
    authServiceSpy.login.mockReturnValue(
      throwError(() => new Error('Invalid email or password. Please try again.'))
    );

    component.loginForm.setValue({
      email: 'wrong@example.com',
      password: 'WrongPassword',
    });

    component.onSubmit();

    expect(component.errorMessage()).toBe(
      'Invalid email or password. Please try again.'
    );
    expect(component.isSubmitting()).toBe(false);
  });

  it('should toggle password visibility flag', () => {
    expect(component.showPassword()).toBe(false);
    component.togglePasswordVisibility();
    expect(component.showPassword()).toBe(true);
  });
});
