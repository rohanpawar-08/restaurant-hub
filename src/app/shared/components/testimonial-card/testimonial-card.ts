import { Component, computed, input, signal } from '@angular/core';
import { Testimonial } from '../../models/testimonial.model';

@Component({
  selector: 'app-testimonial-card',
  standalone: true,
  imports: [],
  templateUrl: './testimonial-card.html',
  styleUrl: './testimonial-card.scss',
})
export class TestimonialCard {
  readonly testimonial = input.required<Testimonial>();
  readonly imageError = signal(false);

  readonly stars = computed(() => {
    const rating = Math.min(5, Math.max(1, Math.round(this.testimonial().rating || 5)));
    return Array.from({ length: rating }, (_, i) => i + 1);
  });

  readonly initials = computed(() => {
    const name = this.testimonial().name || '';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase() || 'CU';
  });

  onImageError(): void {
    this.imageError.set(true);
  }
}
