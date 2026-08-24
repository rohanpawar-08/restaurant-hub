import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, map, tap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Order } from '../../shared/models/order.model';
import { CreateOrderApiRequest, OrderApiResponse } from '../api/models/order-api.model';
import { mapOrderApiResponseToOrder } from '../api/mappers/order-api.mapper';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/orders`;

  private readonly latestOrderState = signal<Order | null>(null);
  private readonly orderHistoryState = signal<Order[]>([]);
  private readonly isLoadingState = signal<boolean>(false);
  private readonly errorState = signal<string | null>(null);

  /** Public Readonly Signals */
  readonly latestOrder = this.latestOrderState.asReadonly();
  readonly orderHistory = this.orderHistoryState.asReadonly();
  readonly isLoading = this.isLoadingState.asReadonly();
  readonly error = this.errorState.asReadonly();

  /**
   * Create a new order via authenticated POST /api/v1/orders.
   * Server calculates all monetary values and enforces anti-CSRF token verification.
   */
  createOrder(payload: CreateOrderApiRequest): Observable<Order> {
    this.isLoadingState.set(true);
    this.errorState.set(null);

    return this.http
      .post<OrderApiResponse>(this.baseUrl, payload, { withCredentials: true })
      .pipe(
        map((response) => mapOrderApiResponseToOrder(response)),
        tap((newOrder) => {
          this.latestOrderState.set(newOrder);
          this.orderHistoryState.update((history) => [newOrder, ...history]);
          this.isLoadingState.set(false);
        }),
        catchError((err: HttpErrorResponse) => {
          this.isLoadingState.set(false);
          const message =
            err.error?.message ||
            (err.status === 401
              ? 'Please log in to place your order.'
              : 'Unable to place order. Please try again.');
          this.errorState.set(message);
          return throwError(() => new Error(message));
        })
      );
  }

  /**
   * Load the current authenticated customer's order history via GET /api/v1/orders.
   */
  loadOrders(): Observable<Order[]> {
    this.isLoadingState.set(true);
    this.errorState.set(null);

    return this.http
      .get<OrderApiResponse[]>(this.baseUrl, { withCredentials: true })
      .pipe(
        map((responseList) => (responseList || []).map(mapOrderApiResponseToOrder)),
        tap((orders) => {
          this.orderHistoryState.set(orders);
          this.isLoadingState.set(false);
        }),
        catchError((err: HttpErrorResponse) => {
          this.isLoadingState.set(false);
          const message =
            err.error?.message ||
            (err.status === 401
              ? 'Please log in to view your orders.'
              : 'Failed to load order history.');
          this.errorState.set(message);
          return throwError(() => new Error(message));
        })
      );
  }

  /**
   * Fetch a single order by ID for the authenticated customer via GET /api/v1/orders/{id}.
   */
  getOrderById(id: string | number): Observable<Order> {
    this.isLoadingState.set(true);
    this.errorState.set(null);

    return this.http
      .get<OrderApiResponse>(`${this.baseUrl}/${id}`, { withCredentials: true })
      .pipe(
        map((response) => mapOrderApiResponseToOrder(response)),
        tap((order) => {
          this.latestOrderState.set(order);
          this.isLoadingState.set(false);
        }),
        catchError((err: HttpErrorResponse) => {
          this.isLoadingState.set(false);
          const message = err.error?.message || 'Order not found.';
          this.errorState.set(message);
          return throwError(() => new Error(message));
        })
      );
  }

  /**
   * Retrieve current latest order value.
   */
  getLatestOrder(): Order | null {
    return this.latestOrderState();
  }

  /**
   * Clear the latest order state.
   */
  clearLatestOrder(): void {
    this.latestOrderState.set(null);
  }
}
