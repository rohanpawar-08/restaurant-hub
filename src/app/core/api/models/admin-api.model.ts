/**
 * REST API response contracts for Admin Dashboard metrics.
 */
export interface DashboardSummaryApiResponse {
  totalOrders: number;
  confirmedOrders: number;
  preparingOrders: number;
  readyOrders: number;
  outForDeliveryOrders: number;
  totalCustomers: number;
  totalFoods: number;
  activeFoods: number;
}

/**
 * REST API payload for Admin order status updates.
 */
export interface UpdateOrderStatusApiRequest {
  status: 'CONFIRMED' | 'PREPARING' | 'READY' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED';
}
