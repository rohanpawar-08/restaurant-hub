export interface RestaurantSettings {
  id: number;
  restaurantName: string;
  tagline?: string | null;
  phone: string;
  email: string;
  addressLine1: string;
  addressLine2?: string | null;
  city: string;
  state: string;
  pinCode: string;
  currencyCode: string;
  currencySymbol: string;
  deliveryFee: number;
  freeDeliveryThreshold: number;
  estimatedDeliveryMinutes: number;
  gstin?: string | null;
  fssaiNumber?: string | null;
  openingTime?: string | null;
  closingTime?: string | null;
  acceptingOrders: boolean;
  logoUrl?: string | null;
  heroImageUrl?: string | null;
  primaryColor?: string | null;
  secondaryColor?: string | null;
}

export interface UpdateRestaurantSettingsPayload {
  restaurantName: string;
  tagline?: string | null;
  phone: string;
  email: string;
  addressLine1: string;
  addressLine2?: string | null;
  city: string;
  state: string;
  pinCode: string;
  currencyCode: string;
  currencySymbol: string;
  deliveryFee: number;
  freeDeliveryThreshold: number;
  estimatedDeliveryMinutes: number;
  gstin?: string | null;
  fssaiNumber?: string | null;
  openingTime?: string | null;
  closingTime?: string | null;
  acceptingOrders: boolean;
  logoUrl?: string | null;
  heroImageUrl?: string | null;
  primaryColor?: string | null;
  secondaryColor?: string | null;
}

export interface MediaUploadResponse {
  url: string;
  publicId: string;
}

export interface MediaStatusResponse {
  available: boolean;
  provider: string;
}
