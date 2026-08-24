import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../../../core/services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private readonly adminService = inject(AdminService);

  readonly summary = this.adminService.dashboardSummary;
  readonly isLoading = this.adminService.isLoading;
  readonly error = this.adminService.error;

  ngOnInit(): void {
    this.loadMetrics();
  }

  loadMetrics(): void {
    this.adminService.getDashboardSummary().subscribe({
      error: () => {}, // Handled by signal error state
    });
  }
}
