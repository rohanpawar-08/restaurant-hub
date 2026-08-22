import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Register } from './register';
import { AuthService } from '../../../../core/services/auth.service';
import { User } from '../../../../shared/models/user.model';

describe('Register', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let authServiceSpy: { register: ReturnType<typeof vi.fn> };
  let router: Router;

  const mockUser: User = {
    id: 'USR-12345',
    fullName: 'Jane Doe',
    email: 'jane@example.com',
    phone: '9876543210',
    createdAt: '2026-02-01T12:00:00.000Z',
  };

  beforeEach(async () => {
    authServiceSpy = {
      register: vi.fn().mockReturnValue(of(mockUser)),
    };

    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockImplementation(() => Promise.resolve(true));
    fixture.detectChanges();
  });

  it('should create the register component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values and invalid status', () => {
    expect(component.registerForm.valid).toBe(false);
    expect(component.registerForm.get('fullName')?.value).toBe('');
    expect(component.registerForm.get('email')?.value).toBe('');
    expect(component.registerForm.get('phone')?.value).toBe('');
    expect(component.registerForm.get('password')?.value).toBe('');
    expect(component.registerForm.get('confirmPassword')?.value).toBe('');
  });

  it('should validate full name minimum length', () => {
    const control = component.registerForm.get('fullName');
    control?.setValue('ab');
    expect(control?.errors?.['minlength']).toBeTruthy();

    control?.setValue('Rohan');
    expect(control?.errors?.['minlength']).toBeFalsy();
  });

  it('should validate email format', () => {
    const control = component.registerForm.get('email');
    control?.setValue('not-an-email');
    expect(control?.errors?.['email']).toBeTruthy();

    control?.setValue('valid@example.com');
    expect(control?.errors?.['email']).toBeFalsy();
  });

  it('should validate Indian mobile number format', () => {
    const control = component.registerForm.get('phone');
    control?.setValue('12345');
    expect(control?.errors?.['pattern']).toBeTruthy();

    control?.setValue('5876543210'); // Starts with 5 (invalid)
    expect(control?.errors?.['pattern']).toBeTruthy();

    control?.setValue('9876543210'); // Valid 10 digits starting with 9
    expect(control?.errors?.['pattern']).toBeFalsy();
  });

  it('should validate password minimum length', () => {
    const control = component.registerForm.get('password');
    control?.setValue('pass12');
    expect(control?.errors?.['minlength']).toBeTruthy();

    control?.setValue('Password123');
    expect(control?.errors?.['minlength']).toBeFalsy();
  });

  it('should validate password confirmation matching', () => {
    component.registerForm.patchValue({
      fullName: 'Jane Doe',
      email: 'jane@example.com',
      phone: '9876543210',
      password: 'Password123',
      confirmPassword: 'DifferentPassword456',
    });

    expect(component.registerForm.hasError('passwordMismatch')).toBe(true);

    component.registerForm.patchValue({
      confirmPassword: 'Password123',
    });

    expect(component.registerForm.hasError('passwordMismatch')).toBe(false);
  });

  it('should not call authService.register when form is invalid', () => {
    component.onSubmit();
    expect(authServiceSpy.register).not.toHaveBeenCalled();
    expect(component.isSubmitted()).toBe(true);
  });

  it('should submit registration and navigate to root on success', () => {
    component.registerForm.setValue({
      fullName: 'Jane Doe',
      email: 'jane@example.com',
      phone: '9876543210',
      password: 'Password123',
      confirmPassword: 'Password123',
    });

    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalledWith({
      fullName: 'Jane Doe',
      email: 'jane@example.com',
      phone: '9876543210',
      password: 'Password123',
    });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('should display error message on registration failure', () => {
    authServiceSpy.register.mockReturnValue(
      throwError(() => new Error('An account with this email address already exists.'))
    );

    component.registerForm.setValue({
      fullName: 'Jane Doe',
      email: 'existing@example.com',
      phone: '9876543210',
      password: 'Password123',
      confirmPassword: 'Password123',
    });

    component.onSubmit();

    expect(component.errorMessage()).toBe(
      'An account with this email address already exists.'
    );
    expect(component.isSubmitting()).toBe(false);
  });

  it('should toggle password visibility flags', () => {
    expect(component.showPassword()).toBe(false);
    component.togglePasswordVisibility();
    expect(component.showPassword()).toBe(true);

    expect(component.showConfirmPassword()).toBe(false);
    component.toggleConfirmPasswordVisibility();
    expect(component.showConfirmPassword()).toBe(true);
  });
});
