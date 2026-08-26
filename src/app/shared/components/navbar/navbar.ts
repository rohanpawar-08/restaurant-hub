import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { RestaurantSettingsService } from '../../../core/services/restaurant-settings.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {
  private readonly cartService = inject(CartService);
  private readonly authService = inject(AuthService);
  private readonly settingsService = inject(RestaurantSettingsService);
  private readonly router = inject(Router);

  /** Direct reactive signals */
  readonly cartCount = this.cartService.totalQuantity;
  readonly isAuthenticated = this.authService.isAuthenticated;
  readonly currentUser = this.authService.currentUser;
  readonly isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');
  readonly restaurantName = this.settingsService.restaurantName;
  readonly logoUrl = this.settingsService.logoUrl;
  readonly logoFailed = signal<boolean>(false);

  /**
   * Derived short display name for the navigation greeting.
   * e.g. "Rohan Pawar" -> "Rohan"
   */
  readonly userDisplayName = computed(() => {
    const user = this.currentUser();
    if (!user || !user.fullName) {
      return '';
    }
    const firstName = user.fullName.trim().split(' ')[0];
    return firstName || user.fullName;
  });

  /**
   * Customer logout action.
   * Clears backend session & auth state and redirects to home. Cart contents remain preserved.
   */
  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/']),
    });
  }
}