export interface Food {
  id: string;
  name: string;
  description: string;
  category: string;
  categorySlug: string;
  price: number;
  rating: number;
  image: string;
  icon?: string;
  isVeg: boolean;
  isPopular: boolean;
  isAvailable?: boolean;
  preparationTime?: string;
}
