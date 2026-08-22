import { Component, inject } from '@angular/core';
import { HomeContentService } from '../../../../core/services/home-content.service';
import { TestimonialCard } from '../../../../shared/components/testimonial-card/testimonial-card';

@Component({
  selector: 'app-testimonials',
  standalone: true,
  imports: [TestimonialCard],
  templateUrl: './testimonials.html',
  styleUrl: './testimonials.scss',
})
export class Testimonials {
  private readonly homeContentService = inject(HomeContentService);
  readonly testimonials = this.homeContentService.testimonials;
}
