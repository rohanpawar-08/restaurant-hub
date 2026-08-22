export type PaymentMethod = 'cod' | 'upi' | 'card';

export interface CustomerDetails {
  fullName: string;
  email: string;
  phone: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  deliveryInstructions?: string;
}

export interface PaymentOption {
  id: PaymentMethod;
  title: string;
  subtitle: string;
  icon: string;
  badge?: string;
  isAvailableForMock: boolean;
}
