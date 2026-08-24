export interface FoodCategory {
  id: string;
  name: string;
  icon: string;
  slug: string;
  isActive?: boolean;
  description?: string;
  itemCount?: number;
}
