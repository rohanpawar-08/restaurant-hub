import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './contact.html',
  styleUrl: './contact.scss',
})
export class Contact {
  private readonly settingsService = inject(RestaurantSettingsService);

  readonly restaurantName = this.settingsService.restaurantName;
  readonly tagline = this.settingsService.tagline;
  readonly phone = this.settingsService.phone;
  readonly email = this.settingsService.email;
  readonly addressLine1 = this.settingsService.addressLine1;
  readonly addressLine2 = this.settingsService.addressLine2;
  readonly city = this.settingsService.city;
  readonly state = this.settingsService.state;
  readonly pinCode = this.settingsService.pinCode;
  readonly openingTime = this.settingsService.openingTime;
  readonly closingTime = this.settingsService.closingTime;
  readonly gstin = this.settingsService.gstin;
  readonly fssaiNumber = this.settingsService.fssaiNumber;

  readonly formSubmitted = signal<boolean>(false);

  readonly contactForm = new FormGroup({
    name: new FormControl('', [Validators.required, Validators.minLength(2)]),
    email: new FormControl('', [Validators.required, Validators.email]),
    phone: new FormControl('', [Validators.pattern(/^[6-9]\d{9}$/)]),
    subject: new FormControl('General Inquiry', [Validators.required]),
    message: new FormControl('', [Validators.required, Validators.minLength(10)]),
  });

  readonly formattedAddress = computed(() => {
    const parts = [
      this.addressLine1(),
      this.addressLine2(),
      this.city(),
      `${this.state()} ${this.pinCode()}`.trim(),
    ].filter((p) => p && p.trim().length > 0);
    return parts.join(', ');
  });

  readonly formattedHours = computed(() => {
    const open = this.openingTime();
    const close = this.closingTime();
    if (open && close) {
      return `${open} - ${close}`;
    }
    return '10:00 AM - 11:00 PM';
  });

  readonly mailtoUrl = computed(() => {
    const vals = this.contactForm.value;
    const recipient = this.email();
    const subject = encodeURIComponent(`[${this.restaurantName()}] ${vals.subject || 'Inquiry'}`);
    const body = encodeURIComponent(
      `Hello ${this.restaurantName()} Team,\n\nName: ${vals.name || ''}\nEmail: ${vals.email || ''}\nPhone: ${vals.phone || 'N/A'}\n\nMessage:\n${vals.message || ''}`
    );
    return `mailto:${recipient}?subject=${subject}&body=${body}`;
  });

  onSubmit(): void {
    if (this.contactForm.valid) {
      this.formSubmitted.set(true);
    } else {
      this.contactForm.markAllAsTouched();
    }
  }

  resetForm(): void {
    this.contactForm.reset({ subject: 'General Inquiry' });
    this.formSubmitted.set(false);
  }
}

