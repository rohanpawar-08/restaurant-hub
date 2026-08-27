import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './hero.html',
  styleUrl: './hero.scss',
})
export class Hero {
  private readonly settingsService = inject(RestaurantSettingsService);

  readonly restaurantName = this.settingsService.restaurantName;
  readonly tagline = this.settingsService.tagline;
  readonly heroImageUrl = this.settingsService.heroImageUrl;
  readonly heroImageFailed = signal<boolean>(false);

  readonly defaultHeroImage = 'assets/images/hero/hero-food.png';

  /**
   * Computed hero image source:
   * Uses configured URL if present and has not failed; otherwise falls back to bundled asset.
   */
  readonly currentHeroImage = computed(() => {
    const configured = this.heroImageUrl()?.trim();
    if (configured && !this.heroImageFailed()) {
      return configured;
    }
    return this.defaultHeroImage;
  });

  /**
   * Graceful fallback handler when the configured hero image fails to load.
   */
  onImageError(): void {
    if (!this.heroImageFailed()) {
      this.heroImageFailed.set(true);
    }
  }
}