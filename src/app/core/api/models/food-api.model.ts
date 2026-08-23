/**
 * Spring Boot FoodResponse REST API contract model.
 * Matches backend `FoodResponse` record.
 */
export interface FoodApiResponse {
  id: number;
  name: string;
  description: string;
  price: number;
  rating: number;
  image: string | null;
  veg: boolean;
  popular: boolean;
  available: boolean;
  categoryId: number;
  categoryName: string;
  categorySlug: string;
}
