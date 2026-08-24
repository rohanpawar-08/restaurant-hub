import { Component, computed, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  /** Direct reactive signal from AuthService */
  readonly currentUser = this.authService.currentUser;

  /**
   * Derive customer initials for avatar placeholder (e.g., "Rohan Pawar" -> "RP").
   */
  readonly userInitials = computed(() => {
    const user = this.currentUser();
    if (!user || !user.fullName) {
      return 'RH';
    }
    const parts = user.fullName.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return user.fullName.slice(0, 2).toUpperCase();
  });

  /**
   * Handle user logout and navigate to login.
   */
  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
    });
  }
}
