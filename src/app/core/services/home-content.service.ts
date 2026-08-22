import { Injectable, signal } from '@angular/core';
import { RestaurantFeature } from '../../shared/models/feature.model';
import { Testimonial } from '../../shared/models/testimonial.model';

@Injectable({
  providedIn: 'root',
})
export class HomeContentService {
  private readonly featuresState = signal<RestaurantFeature[]>([
    {
      id: 'feat-1',
      title: 'Fast Delivery',
      description: 'Hot and freshly prepared gourmet meals delivered right to your doorstep in under 30 minutes.',
      icon: '🚀',
    },
    {
      id: 'feat-2',
      title: 'Fresh Ingredients',
      description: '100% organic, locally sourced produce and premium spices hand-selected daily by our culinary team.',
      icon: '🌿',
    },
    {
      id: 'feat-3',
      title: 'Expert Chefs',
      description: 'Masterfully crafted recipes developed by world-class chefs passionate about authentic flavor profiles.',
      icon: '👨‍🍳',
    },
    {
      id: 'feat-4',
      title: 'Secure Ordering',
      description: 'Effortless online ordering with end-to-end encrypted payments and real-time live delivery tracking.',
      icon: '🛡️',
    },
  ]);

  private readonly testimonialsState = signal<Testimonial[]>([
    {
      id: 'test-1',
      name: 'Sophia Montgomery',
      role: 'Culinary Journalist & Critic',
      rating: 5,
      comment: 'RestaurantHub has redefined gourmet dining at home. The Artisan Margherita arrived steaming hot with perfectly blistered crust and fragrant basil!',
      avatar: 'assets/images/testimonials/sophia.jpg',
    },
    {
      id: 'test-2',
      name: 'Marcus Vance',
      role: 'Verified Foodie & Regular Customer',
      rating: 5,
      comment: 'The Double Truffle Cheeseburger is unbeatable. Ordering is lightning-fast, and the real-time tracking makes dinner planning completely effortless.',
      avatar: 'assets/images/testimonials/marcus.jpg',
    },
    {
      id: 'test-3',
      name: 'Elena Rostova',
      role: 'Executive Event Planner',
      rating: 5,
      comment: 'From corporate banquets to cozy family dinners, the consistency in flavor, presentation, and delivery punctuality is unmatched anywhere else.',
      avatar: 'assets/images/testimonials/elena.jpg',
    },
  ]);

  /** Public readonly signals */
  readonly features = this.featuresState.asReadonly();
  readonly testimonials = this.testimonialsState.asReadonly();
}
