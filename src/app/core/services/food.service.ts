import { Injectable, computed, signal } from '@angular/core';
import { Food } from '../../shared/models/food.model';
import { FoodCategory } from '../../shared/models/category.model';

@Injectable({
  providedIn: 'root',
})
export class FoodService {
  private readonly categoriesState = signal<FoodCategory[]>([
    {
      id: 'cat-1',
      name: 'Pizza',
      icon: '🍕',
      slug: 'pizza',
      description: 'Artisan hand-tossed pizzas with gourmet toppings',
      itemCount: 12,
    },
    {
      id: 'cat-2',
      name: 'Burgers',
      icon: '🍔',
      slug: 'burgers',
      description: 'Juicy handcrafted burgers served with crispy fries',
      itemCount: 8,
    },
    {
      id: 'cat-3',
      name: 'Pasta',
      icon: '🍝',
      slug: 'pasta',
      description: 'Authentic Italian pasta freshly made daily',
      itemCount: 10,
    },
    {
      id: 'cat-4',
      name: 'Biryani',
      icon: '🍛',
      slug: 'biryani',
      description: 'Fragrant basmati rice slow-cooked with rich spices',
      itemCount: 6,
    },
    {
      id: 'cat-5',
      name: 'Salads',
      icon: '🥗',
      slug: 'salads',
      description: 'Organic garden-fresh greens and zesty dressings',
      itemCount: 9,
    },
    {
      id: 'cat-6',
      name: 'Desserts',
      icon: '🍰',
      slug: 'desserts',
      description: 'Decadent sweet treats and artisan pastries',
      itemCount: 7,
    },
  ]);

  private readonly foodsState = signal<Food[]>([
    {
      id: 'food-1',
      name: 'Artisan Margherita Pizza',
      description: 'San Marzano tomato sauce, fresh buffalo mozzarella, fragrant basil leaves, and extra virgin olive oil.',
      category: 'Pizza',
      categorySlug: 'pizza',
      price: 14.99,
      rating: 4.9,
      image: 'assets/images/food/margherita-pizza.png',
      icon: '🍕',
      isVeg: true,
      isPopular: true,
      preparationTime: '20-25 min',
    },
    {
      id: 'food-2',
      name: 'Double Truffle Cheeseburger',
      description: 'Prime Angus beef double patty, melted aged cheddar, black truffle aioli, and caramelized onions on brioche.',
      category: 'Burgers',
      categorySlug: 'burgers',
      price: 16.5,
      rating: 4.8,
      image: 'assets/images/food/truffle-cheeseburger.png',
      icon: '🍔',
      isVeg: false,
      isPopular: true,
      preparationTime: '15-20 min',
    },
    {
      id: 'food-3',
      name: 'Creamy Fettuccine Alfredo',
      description: 'Silky Parmigiano-Reggiano cream sauce tossed with handmade fettuccine and fresh cracked black pepper.',
      category: 'Pasta',
      categorySlug: 'pasta',
      price: 15.75,
      rating: 4.9,
      image: 'assets/images/food/fettuccine-alfredo.png',
      icon: '🍝',
      isVeg: true,
      isPopular: true,
      preparationTime: '18-22 min',
    },
    {
      id: 'food-4',
      name: 'Royal Dum Biryani',
      description: 'Tender spiced chicken cooked in fragrant long-grain basmati with saffron, cardamom, and fried shallots.',
      category: 'Biryani',
      categorySlug: 'biryani',
      price: 18.99,
      rating: 4.9,
      image: 'assets/images/food/royal-dum-biryani.png',
      icon: '🍛',
      isVeg: false,
      isPopular: true,
      preparationTime: '25-30 min',
    },
    {
      id: 'food-5',
      name: 'Mediterranean Quinoa Salad',
      description: 'Crisp English cucumber, Kalamata olives, cherry tomatoes, creamy feta, and lemon oregano vinaigrette.',
      category: 'Salads',
      categorySlug: 'salads',
      price: 12.5,
      rating: 4.7,
      image: 'assets/images/food/mediterranean-salad.png',
      icon: '🥗',
      isVeg: true,
      isPopular: true,
      preparationTime: '10-15 min',
    },
    {
      id: 'food-6',
      name: 'Molten Chocolate Lava Cake',
      description: 'Rich dark Belgian chocolate cake with a warm flowing ganache center, served with vanilla bean ice cream.',
      category: 'Desserts',
      categorySlug: 'desserts',
      price: 9.99,
      rating: 4.9,
      image: 'assets/images/food/chocolate-lava-cake.png',
      icon: '🍰',
      isVeg: true,
      isPopular: true,
      preparationTime: '12-15 min',
    },
    {
      id: 'food-7',
      name: 'Crispy Paneer Tikka Wrap',
      description: 'Marinated cottage cheese charred in tandoor with mint chutney and bell peppers rolled in a warm paratha.',
      category: 'Salads',
      categorySlug: 'salads',
      price: 11.99,
      rating: 4.8,
      image: 'assets/images/food/paneer-tikka-wrap.png',
      icon: '🌯',
      isVeg: true,
      isPopular: false,
      preparationTime: '15-18 min',
    },
    {
      id: 'food-8',
      name: 'Smoky BBQ Glazed Wings',
      description: 'Crispy jumbo wings tossed in house hickory smoked barbecue glaze, topped with toasted sesame seeds.',
      category: 'Burgers',
      categorySlug: 'burgers',
      price: 13.5,
      rating: 4.6,
      image: 'assets/images/food/bbq-wings.png',
      icon: '🍗',
      isVeg: false,
      isPopular: false,
      preparationTime: '15-20 min',
    },
  ]);

  /** Public readonly signals */
  readonly categories = this.categoriesState.asReadonly();
  readonly foods = this.foodsState.asReadonly();
  readonly popularFoods = computed(() =>
    this.foodsState().filter((food) => food.isPopular)
  );

  /** Filter foods by category slug */
  getFoodsByCategory(categorySlug: string): Food[] {
    return this.foodsState().filter((food) => food.categorySlug === categorySlug);
  }

  /** Find food by ID */
  getFoodById(id: string): Food | undefined {
    return this.foodsState().find((food) => food.id === id);
  }

  /** Find category by slug */
  getCategoryBySlug(slug: string): FoodCategory | undefined {
    return this.categoriesState().find((category) => category.slug === slug);
  }
}
