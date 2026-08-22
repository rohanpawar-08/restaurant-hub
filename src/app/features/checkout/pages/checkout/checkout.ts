import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../../../core/services/cart.service';
import { OrderService } from '../../../../core/services/order.service';
import { CustomerDetails, PaymentMethod, PaymentOption } from '../../../../shared/models/checkout.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe],
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss',
})
export class Checkout {
  private readonly fb = inject(FormBuilder);
  readonly cartService = inject(CartService);
  private readonly orderService = inject(OrderService);
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
  private readonly failedImages = signal<Record<string, boolean>>({});

  /** Payment Options */
  readonly paymentOptions: PaymentOption[] = [
    {
      id: 'cod',
      title: 'Cash on Delivery',
      subtitle: 'Pay with cash upon food delivery',
      icon: '💵',
      badge: 'Active for mock checkout',
      isAvailableForMock: true,
    },
    {
      id: 'upi',
      title: 'UPI (GPay / PhonePe / Paytm)',
      subtitle: 'Instant payment via UPI ID or QR code',
      icon: '📱',
      badge: 'Gateway Demo',
      isAvailableForMock: false,
    },
    {
      id: 'card',
      title: 'Credit / Debit Card',
      subtitle: 'Visa, Mastercard, RuPay cards',
      icon: '💳',
      badge: 'Gateway Demo',
      isAvailableForMock: false,
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
    this.selectedPaymentMethod.set(method);
    this.checkoutForm.patchValue({ paymentMethod: method });

    if (method !== 'cod') {
      this.onlinePaymentNotice.set(
        'Online payment integration will be available when the backend/payment gateway is connected. Please select Cash on Delivery to complete your order.'
      );
    } else {
      this.onlinePaymentNotice.set(null);
    }
  }

  isImageFailed(foodId: string): boolean {
    return !!this.failedImages()[foodId];
  }

  onImageError(foodId: string): void {
    this.failedImages.update((current) => ({ ...current, [foodId]: true }));
  }

  /**
   * Handle Place Order Form Submission
   */
  onSubmit(): void {
    this.isSubmitted.set(true);

    if (this.isEmpty()) {
      return;
    }

    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      // Scroll to first error on screen
      if (typeof document !== 'undefined') {
        const firstErrorEl = document.querySelector('.form-control.is-invalid, .form-group.has-error');
        firstErrorEl?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
      return;
    }

    const currentPaymentMethod = this.selectedPaymentMethod();
    if (currentPaymentMethod !== 'cod') {
      this.onlinePaymentNotice.set(
        'Online payment integration will be available when the backend/payment gateway is connected. Please select Cash on Delivery to complete your order.'
      );
      return;
    }

    if (this.isSubmitting()) {
      return;
    }

    this.isSubmitting.set(true);

    const formValues = this.checkoutForm.value;
    const customer: CustomerDetails = {
      fullName: formValues.fullName.trim(),
      email: formValues.email.trim(),
      phone: formValues.phone.trim(),
      addressLine1: formValues.addressLine1.trim(),
      addressLine2: formValues.addressLine2 ? formValues.addressLine2.trim() : undefined,
      city: formValues.city.trim(),
      state: formValues.state.trim(),
      postalCode: formValues.postalCode.trim(),
      deliveryInstructions: formValues.deliveryInstructions
        ? formValues.deliveryInstructions.trim()
        : undefined,
    };

    try {
      // 1. Create order in OrderService
      const newOrder = this.orderService.createOrder({
        customer,
        paymentMethod: currentPaymentMethod,
        items: this.cartItems(),
        subtotal: this.subtotal(),
        deliveryFee: this.deliveryFee(),
        total: this.grandTotal(),
      });

      // 2. Clear cart after successful order creation
      this.cartService.clearCart();

      // 3. Navigate to order confirmation page
      this.router.navigate(['/order-success']);
    } catch {
      this.isSubmitting.set(false);
    }
  }
}
