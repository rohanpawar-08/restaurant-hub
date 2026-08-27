import { Component, computed, DestroyRef, HostListener, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { RestaurantSettingsService } from '../../../core/services/restaurant-settings.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {
  private readonly cartService = inject(CartService);
  private readonly authService = inject(AuthService);
  private readonly settingsService = inject(RestaurantSettingsService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  /** Direct reactive signals */
  readonly cartCount = this.cartService.totalQuantity;
  readonly isAuthenticated = this.authService.isAuthenticated;
  readonly currentUser = this.authService.currentUser;
  readonly isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');
  readonly restaurantName = this.settingsService.restaurantName;
  readonly logoUrl = this.settingsService.logoUrl;
  readonly logoFailed = signal<boolean>(false);
  readonly isMenuOpen = signal<boolean>(false);

  constructor() {
    // Automatically close mobile menu on route change
    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.closeMenu();
      });
  }

  /** Close menu on Escape key */
  @HostListener('window:keydown.escape')
  onEscape(): void {
    if (this.isMenuOpen()) {
      this.closeMenu();
    }
  }

  toggleMenu(): void {
    this.isMenuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.isMenuOpen.set(false);
  }

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
    this.closeMenu();
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/']),
    });
  }
}