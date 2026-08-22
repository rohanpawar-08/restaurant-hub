import { Component, inject } from '@angular/core';
import { HomeContentService } from '../../../../core/services/home-content.service';
import { FeatureCard } from '../../../../shared/components/feature-card/feature-card';

@Component({
  selector: 'app-why-choose-us',
  standalone: true,
  imports: [FeatureCard],
  templateUrl: './why-choose-us.html',
  styleUrl: './why-choose-us.scss',
})
export class WhyChooseUs {
  private readonly homeContentService = inject(HomeContentService);
  readonly features = this.homeContentService.features;
}
