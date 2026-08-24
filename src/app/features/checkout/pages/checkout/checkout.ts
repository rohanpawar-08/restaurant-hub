import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../../../core/services/cart.service';
import { OrderService } from '../../../../core/services/order.service';
import { AuthService } from '../../../../core/services/auth.service';
import { CustomerDetails, PaymentMethod, PaymentOption } from '../../../../shared/models/checkout.model';
import { CreateOrderApiRequest } from '../../../../core/api/models/order-api.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe],
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss',
})
export class Checkout implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly cartService = inject(CartService);
  private readonly orderService = inject(OrderService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  /** Direct cart signals */
  readonly cartItems = this.cartService.cartItems;
  readonly totalQuantity = this.cartService.totalQuantity;
  readonly subtotal = this.cartService.subtotal;
  readonly deliveryFee = this.cartService.deliveryFee;
  readonly grandTotal = this.cartService.grandTotal;
  readonly isEmpty = this.cartService.isEmpty;

  /** Component state */
  readonly isSubmitting = signal(false);
  readonly isSubmitted = signal(false);
  readonly selectedPaymentMethod = signal<PaymentMethod>('cod');
  readonly onlinePaymentNotice = signal<string | null>(null);
  readonly serverErrorMessage = signal<string | null>(null);
  private readonly failedImages = signal<Record<string, boolean>>({});

  /** Payment Options: COD is active, online methods are marked Coming Soon and disabled */
  readonly paymentOptions: PaymentOption[] = [
    {
      id: 'cod',
      title: 'Cash on Delivery',
      subtitle: 'Pay with cash upon food delivery',
      icon: '💵',
      badge: 'Active',
      isAvailable: true,
    },
    {
      id: 'upi',
      title: 'UPI (GPay / PhonePe / Paytm)',
      subtitle: 'Instant payment via UPI ID or QR code',
      icon: '📱',
      badge: 'Coming Soon',
      isAvailable: false,
    },
    {
      id: 'card',
      title: 'Credit / Debit Card',
      subtitle: 'Visa, Mastercard, RuPay cards',
      icon: '💳',
      badge: 'Coming Soon',
      isAvailable: false,
    },
  ];

  /** Reactive Checkout Form */
  readonly checkoutForm: FormGroup = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    phone: [
      '',
      [
        Validators.required,
        Validators.pattern(/^[6-9]\d{9}$/),
      ],
    ],
    addressLine1: ['', [Validators.required, Validators.minLength(5)]],
    addressLine2: [''],
    city: ['', [Validators.required, Validators.minLength(2)]],
    state: ['', [Validators.required, Validators.minLength(2)]],
    postalCode: [
      '',
      [
        Validators.required,
        Validators.pattern(/^\d{6}$/),
      ],
    ],
    deliveryInstructions: [''],
    paymentMethod: ['cod', [Validators.required]],
  });

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.checkoutForm.patchValue({
        fullName: user.fullName || '',
        email: user.email || '',
        phone: user.phone || '',
      });
    }
  }

  /** Field validation helpers */
  isFieldInvalid(fieldName: string): boolean {
    const control = this.checkoutForm.get(fieldName);
    if (!control) {
      return false;
    }
    return control.invalid && (control.touched || control.dirty || this.isSubmitted());
  }

  getFieldError(fieldName: string): string {
    const control = this.checkoutForm.get(fieldName);
    if (!control || !control.errors) {
      return '';
    }

    if (control.errors['required']) {
      switch (fieldName) {
        case 'fullName':
          return 'Full name is required';
        case 'email':
          return 'Email address is required';
        case 'phone':
          return 'Mobile number is required';
        case 'addressLine1':
          return 'Address Line 1 is required';
        case 'city':
          return 'City is required';
        case 'state':
          return 'State is required';
        case 'postalCode':
          return 'PIN code is required';
        default:
          return 'This field is required';
      }
    }

    if (control.errors['email']) {
      return 'Please enter a valid email address';
    }

    if (control.errors['minlength']) {
      const requiredLength = control.errors['minlength'].requiredLength;
      return `Must be at least ${requiredLength} characters`;
    }

    if (control.errors['pattern']) {
      if (fieldName === 'phone') {
        return 'Please enter a valid 10-digit Indian mobile number (e.g. 9876543210)';
      }
      if (fieldName === 'postalCode') {
        return 'Please enter a valid 6-digit PIN code (e.g. 400001)';
      }
    }

    return 'Invalid value';
  }

  onSelectPayment(method: PaymentMethod): void {
    if (method !== 'cod') {
      this.onlinePaymentNotice.set(
        'Online payment is not available yet. Please choose Cash on Delivery.'
      );
      return;
    }

    this.selectedPaymentMethod.set('cod');
    this.checkoutForm.patchValue({ paymentMethod: 'cod' });
    this.onlinePaymentNotice.set(null);
  }

  isImageFailed(foodId: string): boolean {
    return !!this.failedImages()[foodId];
  }

  onImageError(foodId: string): void {
    this.failedImages.update((current) => ({ ...current, [foodId]: true }));
  }

  /**
   * Handle Place Order Form Submission with Server-Authoritative Processing
   */
  onSubmit(): void {
    this.isSubmitted.set(true);
    this.serverErrorMessage.set(null);

    if (this.isEmpty()) {
      return;
    }

    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      if (typeof document !== 'undefined') {
        const firstErrorEl = document.querySelector('.form-control.is-invalid, .form-group.has-error');
        firstErrorEl?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
      return;
    }

    const currentPaymentMethod = this.selectedPaymentMethod();
    if (currentPaymentMethod !== 'cod' || this.checkoutForm.get('paymentMethod')?.value !== 'cod') {
      this.onlinePaymentNotice.set(
        'Online payment is not available yet. Please choose Cash on Delivery.'
      );
      return;
    }

    if (this.isSubmitting()) {
      return;
    }

    this.isSubmitting.set(true);

    const formValues = this.checkoutForm.value;
    const payload: CreateOrderApiRequest = {
      customerName: formValues.fullName.trim(),
      customerEmail: formValues.email.trim().toLowerCase(),
      customerPhone: formValues.phone.trim(),
      addressLine1: formValues.addressLine1.trim(),
      addressLine2: formValues.addressLine2 ? formValues.addressLine2.trim() : null,
      city: formValues.city.trim(),
      state: formValues.state.trim(),
      postalCode: formValues.postalCode.trim(),
      deliveryInstructions: formValues.deliveryInstructions
        ? formValues.deliveryInstructions.trim()
        : null,
      paymentMethod: 'COD',
      items: this.cartItems().map((item) => ({
        foodId: Number(item.food.id),
        quantity: item.quantity,
      })),
    };

    this.orderService.createOrder(payload).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        // Clear cart only after backend confirms successful order creation
        this.cartService.clearCart();
        this.router.navigate(['/order-success']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.serverErrorMessage.set(
          err.message || 'Unable to complete your order. Please check your network and try again.'
        );
      },
    });
  }
}
