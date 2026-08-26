import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RestaurantSettingsService } from '../../services/restaurant-settings.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss',
})
export class AdminLayout {
  private readonly authService = inject(AuthService);
  private readonly settingsService = inject(RestaurantSettingsService);
  private readonly router = inject(Router);

  readonly currentUser = this.authService.currentUser;
  readonly restaurantName = this.settingsService.restaurantName;

  readonly adminName = computed(() => {
    const user = this.currentUser();
    return user?.fullName || 'Administrator';
  });

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
    });
  }
}
