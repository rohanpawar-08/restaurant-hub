import { Component, input } from '@angular/core';
import { RestaurantFeature } from '../../models/feature.model';

@Component({
  selector: 'app-feature-card',
  standalone: true,
  imports: [],
  templateUrl: './feature-card.html',
  styleUrl: './feature-card.scss',
})
export class FeatureCard {
  readonly feature = input.required<RestaurantFeature>();
}
