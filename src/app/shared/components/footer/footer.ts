import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RestaurantSettingsService } from '../../../core/services/restaurant-settings.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
})
export class Footer {
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
  readonly gstin = this.settingsService.gstin;
  readonly fssaiNumber = this.settingsService.fssaiNumber;
  readonly openingTime = this.settingsService.openingTime;
  readonly closingTime = this.settingsService.closingTime;

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
      return `Daily: ${open} - ${close}`;
    }
    return 'Mon - Sun: 10:00 AM - 11:00 PM';
  });
}
