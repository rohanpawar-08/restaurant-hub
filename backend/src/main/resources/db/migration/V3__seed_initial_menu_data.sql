-- ==============================================================================
-- Migration: V3__seed_initial_menu_data.sql
-- Description: Safely seed initial menu categories and food items
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. Seed Categories (Non-destructive: only insert if slug does not already exist)
-- ------------------------------------------------------------------------------

INSERT INTO categories (name, slug, active)
SELECT 'Pizza', 'pizza', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'pizza');

INSERT INTO categories (name, slug, active)
SELECT 'Burgers', 'burgers', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'burgers');

INSERT INTO categories (name, slug, active)
SELECT 'Pasta', 'pasta', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'pasta');

INSERT INTO categories (name, slug, active)
SELECT 'Biryani', 'biryani', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'biryani');

INSERT INTO categories (name, slug, active)
SELECT 'Salads', 'salads', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'salads');

INSERT INTO categories (name, slug, active)
SELECT 'Desserts', 'desserts', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'desserts');

INSERT INTO categories (name, slug, active)
SELECT 'Beverages', 'beverages', TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'beverages');

-- ------------------------------------------------------------------------------
-- 2. Seed Foods (Relational lookup via category slug, idempotently checking name & category)
-- ------------------------------------------------------------------------------

-- Pizza Category Items
INSERT INTO foods (name, description, price, rating, image, veg, popular, available, category_id)
SELECT 
    'Artisan Margherita Pizza',
    'San Marzano tomato sauce, fresh buffalo mozzarella, fragrant basil leaves, and extra virgin olive oil.',
    299.00,
    4.9,
    'assets/images/food/margherita-pizza.png',
    TRUE,
    TRUE,
    TRUE,
    c.id
FROM categories c
WHERE c.slug = 'pizza'
AND NOT EXISTS (
    SELECT 1 FROM foods f WHERE f.name = 'Artisan Margherita Pizza' AND f.category_id = c.id
)
LIMIT 1;

INSERT INTO foods (name, description, price, rating, image, veg, popular, available, category_id)
SELECT 
    'Farmhouse Supreme Pizza',
    'Loaded with crisp bell peppers, grilled mushrooms, sweet corn, black olives, and melted mozzarella.',
    349.00,
    4.8,
    'assets/images/food/farmhouse-pizza.png',
    TRUE,
    FALSE,
    TRUE,
    c.id
FROM categories c
WHERE c.slug = 'pizza'
AND NOT EXISTS (
    SELECT 1 FROM foods f WHERE f.name = 'Farmhouse Supreme Pizza' AND f.category_id = c.id
)
LIMIT 1;

-- Burger Category Items
INSERT INTO foods (name, description, price, rating, image, veg, popular, available, category_id)
SELECT 
    'Double Truffle Cheeseburger',
    'Prime Angus beef double patty, melted aged cheddar, black truffle aioli, and caramelized onions on brioche.',
    349.00,
    4.8,
    'assets/images/food/truffle-cheeseburger.png',
    FALSE,
    TRUE,
    TRUE,
    c.id
FROM categories c
WHERE c.slug = 'burgers'
AND NOT EXISTS (
    SELECT 1 FROM foods f WHERE f.name = 'Double Truffle Cheeseburger' AND f.category_id = c.id
)
LIMIT 1;

INSERT INTO foods (name, description, price, rating, image, veg, popular, available, category_id)
SELECT 
    'Smoky BBQ Glazed Wings',
    'Crispy jumbo wings tossed in house hickory smoked barbecue glaze, topped with toasted sesame seeds.',
    289.00,
    4.6,
    'assets/images/food/bbq-wings.png',
    FALSE,
    FALSE,
    TRUE,
    c.id
FROM categories c
WHERE c.slug = 'burgers'
AND NOT EXISTS (
    SELECT 1 FROM foods f WHERE f.name = 'Smoky BBQ Glazed Wings' AND f.category_id = c.id
)
LIMIT 1;

-- Pasta Category Items
INSERT INTO foods (name, description, price, rating, image, veg, popular, available, category_id)
SELECT 
    'Creamy Fettuccine Alfredo',
    'Silky Parmigiano-Reggiano cream sauce tossed with handmade fettuccine and fresh cracked black pepper.',
    279.00,
    4.9,
    'assets/images/food/fettuccine-alfredo.png',
    TRUE,
    TRUE,
    TRUE,
    c.id
FROM categories c
WHERE c.slug = 'pasta'
AND NOT EXISTS (
    SELECT 1 FROM foods f WHERE f.name = 'Creamy Fettuccine Alfredo' AND f.category_id = c.id
)
LIMIT 1;

-- Biryani Category Items
INSERT INTO foods (name, description, price, rating, image, veg, popular, available, category_id)
SELECT 
    'Royal Dum Biryani',
    'Tender spiced chicken cooked in fragrant long-grain basmati with saffron, cardamom, and fried shallots.',
    399.00,
    4.9,
    'assets/images/food/royal-dum-biryani.png',
    FALSE,
    TRUE,
    TRUE,
    c.id
FROM categories c
WHERE c.slug = 'biryani'
AND NOT EXISTS (
    SELECT 1 FROM foods f WHERE f.name = 'Royal Dum Biryani' AND f.category_id = c.id
)
LIMIT 1;

-- Salads Category Items
INSERT INTO foods (name, description, price, rating, image, veg, popular, available, category_id)
SELECT 
    'Mediterranean Quinoa Salad',
    'Crisp English cucumber, Kalamata olives, cherry tomatoes, creamy feta, and lemon oregano vinaigrette.',
    219.00,
    4.7,
    'assets/images/food/mediterranean-salad.png',
    TRUE,
    TRUE,
    TRUE,
    c.id
FROM categories c
WHERE c.slug = 'salads'
AND NOT EXISTS (
    SELECT 1 FROM foods f WHERE f.name = 'Mediterranean Quinoa Salad' AND f.category_id = c.id
)
LIMIT 1;

INSERT INTO foods (name, description, price, rating, image, veg, popular, available, category_id)
SELECT 
    'Crispy Paneer Tikka Wrap',
    'Marinated cottage cheese charred in tandoor with mint chutney and bell peppers rolled in a warm paratha.',
    229.00,
    4.8,
    'assets/images/food/paneer-tikka-wrap.png',
    TRUE,
    FALSE,
    TRUE,
    c.id
FROM categories c
WHERE c.slug = 'salads'
AND NOT EXISTS (
    SELECT 1 FROM foods f WHERE f.name = 'Crispy Paneer Tikka Wrap' AND f.category_id = c.id
)
LIMIT 1;

-- Desserts Category Items
INSERT INTO foods (name, description, price, rating, image, veg, popular, available, category_id)
SELECT 
    'Molten Chocolate Lava Cake',
    'Rich dark Belgian chocolate cake with a warm flowing ganache center, served with vanilla bean ice cream.',
    189.00,
    4.9,
    'assets/images/food/chocolate-lava-cake.png',
    TRUE,
    TRUE,
    TRUE,
    c.id
FROM categories c
WHERE c.slug = 'desserts'
AND NOT EXISTS (
    SELECT 1 FROM foods f WHERE f.name = 'Molten Chocolate Lava Cake' AND f.category_id = c.id
)
LIMIT 1;

-- Beverages Category Items
INSERT INTO foods (name, description, price, rating, image, veg, popular, available, category_id)
SELECT 
    'Iced Hazelnut Cold Coffee',
    'Slow-brewed dark roast espresso blended with velvety chilled milk, crushed ice, and roasted hazelnut syrup.',
    159.00,
    4.7,
    'assets/images/food/cold-coffee.png',
    TRUE,
    FALSE,
    TRUE,
    c.id
FROM categories c
WHERE c.slug = 'beverages'
AND NOT EXISTS (
    SELECT 1 FROM foods f WHERE f.name = 'Iced Hazelnut Cold Coffee' AND f.category_id = c.id
)
LIMIT 1;

INSERT INTO foods (name, description, price, rating, image, veg, popular, available, category_id)
SELECT 
    'Fresh Mint Lemonade',
    'Zesty freshly squeezed lemons infused with crushed spearmint leaves, cane sugar syrup, and sparkling soda.',
    129.00,
    4.6,
    'assets/images/food/mint-lemonade.png',
    TRUE,
    FALSE,
    TRUE,
    c.id
FROM categories c
WHERE c.slug = 'beverages'
AND NOT EXISTS (
    SELECT 1 FROM foods f WHERE f.name = 'Fresh Mint Lemonade' AND f.category_id = c.id
)
LIMIT 1;
