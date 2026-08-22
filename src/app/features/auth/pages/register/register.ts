import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

/**
 * Custom validator to ensure password and confirmPassword fields match.
 */
function passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;

  if (password && confirmPassword && password !== confirmPassword) {
    return { passwordMismatch: true };
  }
  return null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  /** Reactive state signals */
  readonly isSubmitting = signal(false);
  readonly isSubmitted = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly showPassword = signal(false);
  readonly showConfirmPassword = signal(false);

  /** Registration Form */
  readonly registerForm: FormGroup = this.fb.group(
    {
      fullName: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      phone: [
        '',
        [
          Validators.required,
          Validators.pattern(/^[6-9]\d{9}$/),
        ],
      ],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: [passwordMatchValidator] }
  );

  /** Toggle password visibility */
  togglePasswordVisibility(): void {
    this.showPassword.update((val) => !val);
  }

  /** Toggle confirm password visibility */
  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword.update((val) => !val);
  }

  /** Check if a field should display an invalid state */
  isFieldInvalid(fieldName: string): boolean {
    const control = this.registerForm.get(fieldName);
    if (!control) {
      return false;
    }
    const isTouchedOrSubmitted = control.touched || control.dirty || this.isSubmitted();

    if (fieldName === 'confirmPassword') {
      return (
        (control.invalid || this.registerForm.hasError('passwordMismatch')) &&
        isTouchedOrSubmitted
      );
    }

    return control.invalid && isTouchedOrSubmitted;
  }

  /** Get human-readable validation error messages */
  getFieldError(fieldName: string): string {
    const control = this.registerForm.get(fieldName);
    if (!control) {
      return '';
    }

    if (control.errors?.['required']) {
      switch (fieldName) {
        case 'fullName':
          return 'Full name is required';
        case 'email':
          return 'Email address is required';
        case 'phone':
          return 'Mobile number is required';
        case 'password':
          return 'Password is required';
        case 'confirmPassword':
          return 'Please confirm your password';
        default:
          return 'This field is required';
      }
    }

    if (control.errors?.['minlength']) {
      const min = control.errors['minlength'].requiredLength;
      return `Must be at least ${min} characters`;
    }

    if (control.errors?.['email']) {
      return 'Please enter a valid email address';
    }

    if (control.errors?.['pattern'] && fieldName === 'phone') {
      return 'Please enter a valid 10-digit Indian mobile number (e.g. 9876543210)';
    }

    if (fieldName === 'confirmPassword' && this.registerForm.hasError('passwordMismatch')) {
      return 'Passwords do not match';
    }

    return '';
  }

  /** Form submission */
  onSubmit(): void {
    this.isSubmitted.set(true);
    this.errorMessage.set(null);

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    if (this.isSubmitting()) {
      return;
    }

    this.isSubmitting.set(true);

    const values = this.registerForm.value;

    this.authService
      .register({
        fullName: values.fullName,
        email: values.email,
        phone: values.phone,
        password: values.password,
      })
      .subscribe({
        next: () => {
          this.isSubmitting.set(false);
          const rawReturnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
          const destination = this.sanitizeReturnUrl(rawReturnUrl);
          this.router.navigateByUrl(destination);
        },
        error: (err: Error) => {
          this.isSubmitting.set(false);
          this.errorMessage.set(
            err.message || 'Registration failed. Please try again.'
          );
        },
      });
  }

  /**
   * Sanitize returnUrl query parameter to prevent open redirect vulnerabilities.
   */
  private sanitizeReturnUrl(url: string | null): string {
    if (!url) {
      return '/';
    }
    const trimmed = url.trim();
    if (trimmed.startsWith('/') && !trimmed.startsWith('//') && !trimmed.includes('://')) {
      return trimmed;
    }
    return '/';
  }
}
