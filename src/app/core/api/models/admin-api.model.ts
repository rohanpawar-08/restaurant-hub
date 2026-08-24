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

/**
 * REST API payload for creating a category.
 */
export interface CreateCategoryApiRequest {
  name: string;
  slug: string;
}

/**
 * REST API payload for updating a category.
 */
export interface UpdateCategoryApiRequest {
  name: string;
  slug: string;
  active: boolean;
}

/**
 * REST API payload for creating a food menu item.
 */
export interface CreateFoodApiRequest {
  name: string;
  description: string;
  price: number;
  rating?: number;
  image?: string;
  veg: boolean;
  popular?: boolean;
  available?: boolean;
  categoryId: number;
}

/**
 * REST API payload for updating a food menu item.
 */
export interface UpdateFoodApiRequest {
  name: string;
  description: string;
  price: number;
  rating?: number;
  image?: string;
  veg: boolean;
  popular: boolean;
  available: boolean;
  categoryId: number;
}
