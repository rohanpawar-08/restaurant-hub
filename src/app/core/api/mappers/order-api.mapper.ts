import { OrderApiResponse, OrderItemApiResponse } from '../models/order-api.model';
import { Order, OrderStatus } from '../../../shared/models/order.model';
import { CartItem } from '../../../shared/models/cart-item.model';
import { PaymentMethod } from '../../../shared/models/checkout.model';

/**
 * Maps a backend uppercase OrderStatus to the frontend lowercase status type.
 */
export function mapOrderStatus(status: string): OrderStatus {
  switch ((status || '').toUpperCase()) {
    case 'CONFIRMED':
      return 'confirmed';
    case 'PREPARING':
      return 'preparing';
    case 'READY':
      return 'ready';
    case 'OUT_FOR_DELIVERY':
      return 'out_for_delivery';
    case 'DELIVERED':
      return 'delivered';
    case 'CANCELLED':
      return 'cancelled';
    default:
      return 'confirmed';
  }
}

/**
 * Maps a backend uppercase PaymentMethod to the frontend lowercase payment method type.
 */
export function mapPaymentMethod(method: string): PaymentMethod {
  switch ((method || '').toUpperCase()) {
    case 'COD':
      return 'cod';
    case 'UPI':
      return 'upi';
    case 'CARD':
      return 'card';
    default:
      return 'cod';
  }
}

/**
 * Maps a backend OrderItemApiResponse DTO to a frontend CartItem model
 * while preserving immutable price snapshot data.
 */
export function mapOrderItemApiResponseToCartItem(dto: OrderItemApiResponse): CartItem {
  return {
    food: {
      id: String(dto.foodId || dto.id),
      name: dto.foodName,
      description: dto.foodName,
      category: '',
      categorySlug: '',
      price: Number(dto.unitPrice),
      rating: 5.0,
      image: '',
      isVeg: true,
      isPopular: false,
    },
    quantity: dto.quantity,
  };
}

/**
 * Maps a backend OrderApiResponse DTO to the Angular frontend Order model.
 */
export function mapOrderApiResponseToOrder(dto: OrderApiResponse): Order {
  return {
    id: String(dto.id),
    status: mapOrderStatus(dto.status),
    paymentMethod: mapPaymentMethod(dto.paymentMethod),
    subtotal: Number(dto.subtotal),
    deliveryFee: Number(dto.deliveryFee),
    total: Number(dto.total),
    createdAt: dto.createdAt,
    estimatedDeliveryMinutes: dto.estimatedDeliveryMinutes || 35,
    customer: {
      fullName: dto.customerName,
      email: dto.customerEmail,
      phone: dto.customerPhone,
      addressLine1: dto.addressLine1,
      addressLine2: dto.addressLine2 || undefined,
      city: dto.city,
      state: dto.state,
      postalCode: dto.postalCode,
      deliveryInstructions: dto.deliveryInstructions || undefined,
    },
    items: (dto.items || []).map(mapOrderItemApiResponseToCartItem),
  };
}
