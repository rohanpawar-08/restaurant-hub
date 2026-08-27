import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';

export interface CoreValue {
  icon: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './about.html',
  styleUrl: './about.scss',
})
export class About {
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
  readonly estimatedDeliveryMinutes = this.settingsService.estimatedDeliveryMinutes;

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

  readonly coreValues: CoreValue[] = [
    {
      icon: '🌿',
      title: 'Fresh & Authentic Ingredients',
      description:
        'We source fresh produce, farm-selected herbs, and authentic regional spices every single day to ensure rich aroma and taste.',
    },
    {
      icon: '👨‍🍳',
      title: 'Artisan Culinary Mastery',
      description:
        'Our skilled chefs blend time-honored traditional cooking methods with contemporary presentation and utmost passion.',
    },
    {
      icon: '⚡',
      title: 'Prompt & Hot Delivery',
      description:
        'Every meal is prepared fresh upon order and dispatched in premium thermal packaging so it reaches you steaming hot.',
    },
    {
      icon: '🛡️',
      title: 'Strict Kitchen Hygiene',
      description:
        'We maintain rigorous food safety standards, regular sanitization cycles, and safe packaging across all kitchen operations.',
    },
    {
      icon: '❤️',
      title: 'Customer-Centric Care',
      description:
        'Your dining delight is our highest priority. We continuously refine our offerings based on your taste and suggestions.',
    },
    {
      icon: '🍲',
      title: 'Rich & Diverse Flavors',
      description:
        'From fragrant biryanis and slow-simmered curries to freshly baked breads and delightful desserts, there is something for everyone.',
    },
  ];
}
