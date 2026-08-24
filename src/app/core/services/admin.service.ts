import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, catchError, map, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Order, OrderStatus } from '../../shared/models/order.model';
import { DashboardSummaryApiResponse, UpdateOrderStatusApiRequest } from '../api/models/admin-api.model';
import { OrderApiResponse } from '../api/models/order-api.model';
import { mapOrderApiResponseToOrder } from '../api/mappers/order-api.mapper';

/**
 * Enterprise Angular Admin Service managing operations for restaurant administrators.
 * Communicates with Spring Boot `/api/v1/admin/**` endpoints with CSRF token auto-inclusion.
 */
@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin`;

  private readonly dashboardSummaryState = signal<DashboardSummaryApiResponse | null>(null);
  private readonly adminOrdersState = signal<Order[]>([]);
  private readonly isLoadingState = signal<boolean>(false);
  private readonly errorState = signal<string | null>(null);

  /** Public readonly signals */
  readonly dashboardSummary = this.dashboardSummaryState.asReadonly();
  readonly adminOrders = this.adminOrdersState.asReadonly();
  readonly isLoading = this.isLoadingState.asReadonly();
  readonly error = this.errorState.asReadonly();

  /**
   * Loads administrative operational metrics summary.
   */
  getDashboardSummary(): Observable<DashboardSummaryApiResponse> {
    this.isLoadingState.set(true);
    this.errorState.set(null);

    return this.http
      .get<DashboardSummaryApiResponse>(`${this.baseUrl}/dashboard/summary`, {
        withCredentials: true,
      })
      .pipe(
        tap((summary) => {
          this.dashboardSummaryState.set(summary);
          this.isLoadingState.set(false);
        }),
        catchError((error: HttpErrorResponse) => {
          this.isLoadingState.set(false);
          const message = error.error?.message || 'Failed to load dashboard metrics.';
          this.errorState.set(message);
          return throwError(() => new Error(message));
        })
      );
  }

  /**
   * Loads restaurant orders, optionally filtered by status.
   */
  getOrders(status?: OrderStatus): Observable<Order[]> {
    this.isLoadingState.set(true);
    this.errorState.set(null);

    let params = new HttpParams();
    if (status) {
      params = params.set('status', status.toUpperCase());
    }

    return this.http
      .get<OrderApiResponse[]>(`${this.baseUrl}/orders`, {
        params,
        withCredentials: true,
      })
      .pipe(
        map((dtos) => dtos.map(mapOrderApiResponseToOrder)),
        tap((orders) => {
          this.adminOrdersState.set(orders);
          this.isLoadingState.set(false);
        }),
        catchError((error: HttpErrorResponse) => {
          this.isLoadingState.set(false);
          const message = error.error?.message || 'Failed to load restaurant orders.';
          this.errorState.set(message);
          return throwError(() => new Error(message));
        })
      );
  }

  /**
   * Retrieves single order by ID for admin inspection.
   */
  getOrderById(orderId: string | number): Observable<Order> {
    this.isLoadingState.set(true);
    this.errorState.set(null);

    return this.http
      .get<OrderApiResponse>(`${this.baseUrl}/orders/${orderId}`, {
        withCredentials: true,
      })
      .pipe(
        map((dto) => mapOrderApiResponseToOrder(dto)),
        tap(() => this.isLoadingState.set(false)),
        catchError((error: HttpErrorResponse) => {
          this.isLoadingState.set(false);
          const message = error.error?.message || 'Order not found.';
          this.errorState.set(message);
          return throwError(() => new Error(message));
        })
      );
  }

  /**
   * Advances or updates order status according to domain transition rules.
   */
  updateOrderStatus(orderId: string | number, newStatus: OrderStatus): Observable<Order> {
    this.isLoadingState.set(true);
    this.errorState.set(null);

    const payload: UpdateOrderStatusApiRequest = {
      status: newStatus.toUpperCase() as UpdateOrderStatusApiRequest['status'],
    };

    return this.http
      .patch<OrderApiResponse>(`${this.baseUrl}/orders/${orderId}/status`, payload, {
        withCredentials: true,
      })
      .pipe(
        map((dto) => mapOrderApiResponseToOrder(dto)),
        tap((updatedOrder) => {
          this.adminOrdersState.update((orders) =>
            orders.map((o) => (o.id === updatedOrder.id ? updatedOrder : o))
          );
          this.isLoadingState.set(false);
        }),
        catchError((error: HttpErrorResponse) => {
          this.isLoadingState.set(false);
          const message =
            error.error?.message ||
            (error.status === 409
              ? `Cannot change order status to '${newStatus}'.`
              : 'Failed to update order status.');
          this.errorState.set(message);
          return throwError(() => new Error(message));
        })
      );
  }
}
