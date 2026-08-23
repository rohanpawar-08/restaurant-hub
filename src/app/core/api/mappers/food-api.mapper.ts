import { CategoryApiResponse } from '../models/category-api.model';
import { FoodApiResponse } from '../models/food-api.model';
import { FoodCategory } from '../../../shared/models/category.model';
import { Food } from '../../../shared/models/food.model';

/**
 * Category emoji icon mapping dictionary.
 * Keeps UI presentation concerns cleanly separated from backend data contracts.
 */
const CATEGORY_ICON_MAP: Record<string, string> = {
  pizza: '🍕',
  burgers: '🍔',
  burger: '🍔',
  pasta: '🍝',
  biryani: '🍛',
  salads: '🥗',
  salad: '🥗',
  desserts: '🍰',
  dessert: '🍰',
  beverages: '🥤',
  beverage: '🥤',
  drinks: '🥤',
  starters: '🥟',
  appetizers: '🥟',
  soup: '🍲',
  soups: '🍲',
};

/**
 * Get category icon for presentation.
 */
export function getCategoryIcon(slugOrName: string): string {
  const normalized = (slugOrName || '').toLowerCase().trim();
  return CATEGORY_ICON_MAP[normalized] || '🍽️';
}

/**
 * Maps a backend CategoryApiResponse DTO to the Angular frontend FoodCategory model.
 */
export function mapCategoryApiResponseToCategory(
  dto: CategoryApiResponse,
  itemCount?: number
): FoodCategory {
  return {
    id: String(dto.id),
    name: dto.name,
    slug: dto.slug,
    icon: getCategoryIcon(dto.slug),
    itemCount: itemCount,
  };
}

/**
 * Maps a backend FoodApiResponse DTO to the Angular frontend Food model.
 * Handles field name differences (e.g. veg -> isVeg, popular -> isPopular).
 */
export function mapFoodApiResponseToFood(dto: FoodApiResponse): Food {
  return {
    id: String(dto.id),
    name: dto.name,
    description: dto.description,
    category: dto.categoryName,
    categorySlug: dto.categorySlug,
    price: Number(dto.price),
    rating: Number(dto.rating),
    image: dto.image || '',
    icon: getCategoryIcon(dto.categorySlug),
    isVeg: Boolean(dto.veg),
    isPopular: Boolean(dto.popular),
  };
}
