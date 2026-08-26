import { Component, inject, signal } from '@angular/core';
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
}