import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RestaurantSettingsService } from '../../../../core/services/restaurant-settings.service';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './not-found.html',
  styleUrl: './not-found.scss',
})
export class NotFound {
  private readonly settingsService = inject(RestaurantSettingsService);
  readonly restaurantName = this.settingsService.restaurantName;
}
