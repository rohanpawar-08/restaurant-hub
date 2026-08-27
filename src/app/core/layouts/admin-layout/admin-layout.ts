import { Component, computed, DestroyRef, HostListener, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
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
  private readonly destroyRef = inject(DestroyRef);

  readonly currentUser = this.authService.currentUser;
  readonly restaurantName = this.settingsService.restaurantName;
  readonly isSidebarOpen = signal<boolean>(false);

  constructor() {
    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.closeSidebar();
      });
  }

  @HostListener('window:keydown.escape')
  onEscape(): void {
    if (this.isSidebarOpen()) {
      this.closeSidebar();
    }
  }

  toggleSidebar(): void {
    this.isSidebarOpen.update((open) => !open);
  }

  closeSidebar(): void {
    this.isSidebarOpen.set(false);
  }

  readonly adminName = computed(() => {
    const user = this.currentUser();
    return user?.fullName || 'Administrator';
  });

  logout(): void {
    this.closeSidebar();
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
    });
  }
}

