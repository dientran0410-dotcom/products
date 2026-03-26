-- ========================
-- EXTENSION (UUID)
-- ========================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================
-- SEED DATA - CATEGORIES
-- ========================
INSERT INTO categories (id, name, slug, is_topping, status, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Coffee', 'coffee', false, 'ACTIVE', NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', 'Milk Tea', 'milk-tea', false, 'ACTIVE', NOW(), NOW()),
    ('33333333-3333-3333-3333-333333333333', 'Topping', 'topping', true, 'ACTIVE', NOW(), NOW());

-- ========================
-- SEED DATA - PRODUCTS
-- ========================
INSERT INTO products (id, category_id, name, slug, has_variants, status, created_at, updated_at)
VALUES
-- Coffee
('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '11111111-1111-1111-1111-111111111111', 'Black Coffee', 'black-coffee', true, 'ACTIVE', NOW(), NOW()),
('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '11111111-1111-1111-1111-111111111111', 'Milk Coffee', 'milk-coffee', true, 'ACTIVE', NOW(), NOW()),

-- Milk Tea
('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', '22222222-2222-2222-2222-222222222222', 'Classic Milk Tea', 'classic-milk-tea', true, 'ACTIVE', NOW(), NOW()),
('aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4', '22222222-2222-2222-2222-222222222222', 'Matcha Milk Tea', 'matcha-milk-tea', true, 'ACTIVE', NOW(), NOW()),

-- Topping
('aaaaaaa5-aaaa-aaaa-aaaa-aaaaaaaaaaa5', '33333333-3333-3333-3333-333333333333', 'Pearl', 'pearl', true, 'ACTIVE', NOW(), NOW()),
('aaaaaaa6-aaaa-aaaa-aaaa-aaaaaaaaaaa6', '33333333-3333-3333-3333-333333333333', 'Cheese Foam', 'cheese-foam', true, 'ACTIVE', NOW(), NOW());

-- ========================
-- SEED DATA - PRODUCT VARIANTS (SIZE)
-- ========================
INSERT INTO product_variants (id, product_id, sku, name, price, is_default, status, created_at, updated_at)
VALUES
-- Black Coffee
('b1111111-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'CF-BLACK-S', 'Black Coffee S', 18000, false, 'ACTIVE', NOW(), NOW()),
('b1111112-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'CF-BLACK-M', 'Black Coffee M', 20000, true, 'ACTIVE', NOW(), NOW()),
('b1111113-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'CF-BLACK-L', 'Black Coffee L', 25000, false, 'ACTIVE', NOW(), NOW()),

-- Milk Coffee
('b2222221-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'CF-MILK-S', 'Milk Coffee S', 22000, false, 'ACTIVE', NOW(), NOW()),
('b2222222-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'CF-MILK-M', 'Milk Coffee M', 25000, true, 'ACTIVE', NOW(), NOW()),
('b2222223-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'CF-MILK-L', 'Milk Coffee L', 30000, false, 'ACTIVE', NOW(), NOW()),

-- Classic Milk Tea
('b3333331-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'MT-CLASSIC-S', 'Classic Milk Tea S', 25000, false, 'ACTIVE', NOW(), NOW()),
('b3333332-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'MT-CLASSIC-M', 'Classic Milk Tea M', 30000, true, 'ACTIVE', NOW(), NOW()),
('b3333333-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'MT-CLASSIC-L', 'Classic Milk Tea L', 35000, false, 'ACTIVE', NOW(), NOW()),

-- Matcha Milk Tea
('b4444441-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'MT-MATCHA-S', 'Matcha Milk Tea S', 30000, false, 'ACTIVE', NOW(), NOW()),
('b4444442-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'MT-MATCHA-M', 'Matcha Milk Tea M', 35000, true, 'ACTIVE', NOW(), NOW()),
('b4444443-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'MT-MATCHA-L', 'Matcha Milk Tea L', 40000, false, 'ACTIVE', NOW(), NOW()),

-- Topping (1 size)
('b5555551-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'aaaaaaa5-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'TOP-PEARL', 'Pearl', 5000, true, 'ACTIVE', NOW(), NOW()),
('b6666661-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'aaaaaaa6-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 'TOP-CHEESE', 'Cheese Foam', 7000, true, 'ACTIVE', NOW(), NOW());

-- ========================
-- SEED DATA - PRODUCT ADDONS
-- ========================
-- Bảng product_addons không kế thừa BaseAuditEntity nên giữ nguyên
INSERT INTO product_addons (product_id, addon_product_id, max_quantity)
VALUES
-- Coffee add topping
('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'aaaaaaa5-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 3),
('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'aaaaaaa6-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 2),

('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'aaaaaaa5-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 3),
('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'aaaaaaa6-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 2),

-- Milk tea add topping
('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'aaaaaaa5-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 3),
('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'aaaaaaa6-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 2),

('aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'aaaaaaa5-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 3),
('aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'aaaaaaa6-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 2);