import { CartItem } from './cart-item.model';
import { CustomerDetails, PaymentMethod } from './checkout.model';

export type OrderStatus = 'confirmed' | 'preparing' | 'out_for_delivery' | 'delivered';

export interface Order {
  id: string;
  items: CartItem[];
  customer: CustomerDetails;
  paymentMethod: PaymentMethod;
  subtotal: number;
  deliveryFee: number;
  total: number;
  status: OrderStatus;
  createdAt: string;
  estimatedDeliveryMinutes?: number;
}
