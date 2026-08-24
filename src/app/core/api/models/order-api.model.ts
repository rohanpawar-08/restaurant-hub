/**
 * Spring Boot Order REST API contract models.
 * Matches backend `CreateOrderRequest`, `OrderItemRequest`, `OrderResponse`, and `OrderItemResponse`.
 */

export interface OrderItemApiRequest {
  foodId: number;
  quantity: number;
}

export interface CreateOrderApiRequest {
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  addressLine1: string;
  addressLine2?: string | null;
  city: string;
  state: string;
  postalCode: string;
  deliveryInstructions?: string | null;
  paymentMethod: 'COD' | 'UPI' | 'CARD';
  items: OrderItemApiRequest[];
}

export interface OrderItemApiResponse {
  id: number;
  foodId: number | null;
  foodName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface OrderApiResponse {
  id: number;
  userId: number;
  status: 'CONFIRMED' | 'PREPARING' | 'READY' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED';
  paymentMethod: 'COD' | 'UPI' | 'CARD';
  subtotal: number;
  deliveryFee: number;
  total: number;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  addressLine1: string;
  addressLine2?: string | null;
  city: string;
  state: string;
  postalCode: string;
  deliveryInstructions?: string | null;
  estimatedDeliveryMinutes: number;
  createdAt: string;
  items: OrderItemApiResponse[];
}
