-- =====================================================================
-- RestaurantHub Flyway Migration V5
-- Description: Create 'orders' table for customer order persistence
-- =====================================================================

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CONFIRMED',
    payment_method VARCHAR(50) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    delivery_fee DECIMAL(10, 2) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    customer_name VARCHAR(150) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255) NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    delivery_instructions VARCHAR(500) NULL,
    estimated_delivery_minutes INT NOT NULL DEFAULT 35,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_created_at ON orders (created_at);
