-- ========================
-- INDEXES FOR PRODUCTS
-- ========================
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_deleted ON products(deleted_at);

-- ========================
-- INDEXES FOR PRODUCT VARIANTS
-- ========================
CREATE INDEX idx_variants_product ON product_variants(product_id);
CREATE INDEX idx_variants_status ON product_variants(status);

-- ========================
-- INDEXES FOR CATEGORY
-- ========================
CREATE INDEX idx_categories_parent ON categories(parent_id);

-- ========================
-- INDEXES FOR FRANCHISE MENU
-- ========================
CREATE INDEX idx_fp_franchise ON franchise_products(franchise_id);
CREATE INDEX idx_fp_product ON franchise_products(product_id);

-- ========================
-- INDEXES FOR ORDERS
-- ========================
CREATE INDEX idx_orders_franchise ON orders(franchise_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at);

-- ========================
-- INDEXES FOR ORDER ITEMS
-- ========================
CREATE INDEX idx_order_items_order ON order_items(order_id);

-- ========================
-- INDEXES FOR CART
-- ========================
CREATE INDEX idx_cart_customer ON cart(customer_id);

-- ========================
-- INDEXES FOR PAYMENT
-- ========================
CREATE INDEX idx_payment_invoice ON payment(invoice_id);