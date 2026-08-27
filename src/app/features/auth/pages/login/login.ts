import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  /** Reactive state signals */
  readonly isSubmitting = signal(false);
  readonly isSubmitted = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly showPassword = signal(false);
  readonly isSessionExpired = signal<boolean>(
    this.route.snapshot.queryParamMap.get('sessionExpired') === 'true'
  );


  /** Reactive Login Form */
  readonly loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  /** Toggle password visibility */
  togglePasswordVisibility(): void {
    this.showPassword.update((val) => !val);
  }

  /** Check if a field is invalid for display */
  isFieldInvalid(fieldName: string): boolean {
    const control = this.loginForm.get(fieldName);
    if (!control) {
      return false;
    }
    return control.invalid && (control.touched || control.dirty || this.isSubmitted());
  }

  /** Retrieve field error message */
  getFieldError(fieldName: string): string {
    const control = this.loginForm.get(fieldName);
    if (!control) {
      return '';
    }

    if (control.errors?.['required']) {
      return fieldName === 'email'
        ? 'Email address is required'
        : 'Password is required';
    }

    if (control.errors?.['email']) {
      return 'Please enter a valid email address';
    }

    return '';
  }


  /** Handle Login Form Submission */
  onSubmit(): void {
    this.isSubmitted.set(true);
    this.errorMessage.set(null);

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    if (this.isSubmitting()) {
      return;
    }

    this.isSubmitting.set(true);

    const { email, password } = this.loginForm.value;

    this.authService
      .login({ email, password })
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
            err.message || 'Invalid email or password. Please try again.'
          );
        },
      });
  }

  /**
   * Validate and sanitize returnUrl to allow only internal application paths.
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
