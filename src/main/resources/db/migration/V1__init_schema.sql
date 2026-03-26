-- ========================
-- EXTENSION
-- ========================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================
-- 1. categories
-- ========================
CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            parent_id UUID,
                            name VARCHAR(100) NOT NULL,
                            slug VARCHAR(100) UNIQUE NOT NULL,
                            is_topping BOOLEAN NOT NULL DEFAULT false,
                            status VARCHAR(20) NOT NULL,
                            deleted_at TIMESTAMP,
    -- Audit columns
                            created_at TIMESTAMP,
                            updated_at TIMESTAMP,
                            created_by VARCHAR(50),
                            updated_by VARCHAR(50),

                            CONSTRAINT fk_category_parent
                                FOREIGN KEY (parent_id)
                                    REFERENCES categories(id)
);

-- ========================
-- 2. products
-- ========================
CREATE TABLE products (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          category_id UUID NOT NULL,
                          name VARCHAR(200) NOT NULL,
                          slug VARCHAR(200) UNIQUE NOT NULL,
                          description TEXT,
                          image_url VARCHAR(500),
                          has_variants BOOLEAN NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          deleted_at TIMESTAMP,
    -- Audit columns
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP,
                          created_by VARCHAR(50),
                          updated_by VARCHAR(50),

                          CONSTRAINT fk_product_category
                              FOREIGN KEY (category_id)
                                  REFERENCES categories(id)
);

-- ========================
-- 3. product_variants
-- ========================
CREATE TABLE product_variants (
                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  product_id UUID NOT NULL,
                                  sku VARCHAR(50) UNIQUE NOT NULL,
                                  name VARCHAR(100) NOT NULL,
                                  price DECIMAL(12,2) NOT NULL,
                                  is_default BOOLEAN NOT NULL,
                                  status VARCHAR(20) NOT NULL,
                                  deleted_at TIMESTAMP,
    -- Audit columns
                                  created_at TIMESTAMP,
                                  updated_at TIMESTAMP,
                                  created_by VARCHAR(50),
                                  updated_by VARCHAR(50),

                                  CONSTRAINT fk_variant_product
                                      FOREIGN KEY (product_id)
                                          REFERENCES products(id)
);

-- ========================
-- 4. ingredients
-- ========================
CREATE TABLE ingredients (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             sku VARCHAR(50) UNIQUE NOT NULL,
                             name VARCHAR(100) NOT NULL,
                             base_unit VARCHAR(20) NOT NULL,
                             status VARCHAR(20) NOT NULL,
                             preparation_time_minutes INT NOT NULL DEFAULT 0,
                             updated_at TIMESTAMP
);

-- ========================
-- 5. variant_ingredients
-- ========================
CREATE TABLE variant_ingredients (
                                     variant_id UUID NOT NULL,
                                     ingredient_id UUID NOT NULL,
                                     quantity DECIMAL(10,3) NOT NULL,

                                     PRIMARY KEY (variant_id, ingredient_id),

                                     CONSTRAINT fk_vi_variant
                                         FOREIGN KEY (variant_id)
                                             REFERENCES product_variants(id),

                                     CONSTRAINT fk_vi_ingredient
                                         FOREIGN KEY (ingredient_id)
                                             REFERENCES ingredients(id)
);

-- ========================
-- 6. product_addons
-- ========================
CREATE TABLE product_addons (
                                product_id UUID NOT NULL,
                                addon_product_id UUID NOT NULL,
                                max_quantity INT DEFAULT 5,

                                PRIMARY KEY (product_id, addon_product_id),

                                CONSTRAINT fk_pa_product
                                    FOREIGN KEY (product_id)
                                        REFERENCES products(id),

                                CONSTRAINT fk_pa_addon
                                    FOREIGN KEY (addon_product_id)
                                        REFERENCES products(id)
);

-- ========================
-- 7. franchise_products
-- ========================
CREATE TABLE franchise_products (
                                    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                    franchise_id UUID NOT NULL,
                                    product_id UUID NOT NULL,
                                    is_available BOOLEAN NOT NULL DEFAULT true,

                                    CONSTRAINT fk_fp_product
                                        FOREIGN KEY (product_id)
                                            REFERENCES products(id)
);

-- ========================
-- 8. cart
-- ========================
CREATE TABLE cart (
                      id BIGSERIAL PRIMARY KEY,
                      customer_id UUID,
                      created_at TIMESTAMP,
                      updated_at TIMESTAMP
);

-- ========================
-- 9. cart_item
-- ========================
CREATE TABLE cart_item (
                           id BIGSERIAL PRIMARY KEY,
                           cart_id BIGINT,
                           variant_id UUID NOT NULL,
                           product_id UUID,
                           quantity INT,
                           price DECIMAL,

                           CONSTRAINT fk_cart_item_cart
                               FOREIGN KEY (cart_id)
                                   REFERENCES cart(id)
);

-- ========================
-- 10. orders
-- ========================
CREATE TABLE orders (
                        id UUID PRIMARY KEY,
                        franchise_id UUID NOT NULL,
                        customer_id UUID,
                        order_source VARCHAR(20) NOT NULL,
                        order_number VARCHAR(50) UNIQUE NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        payment_status VARCHAR(20),
                        total_amount NUMERIC(12,2) NOT NULL,
                        notes TEXT,
                        version INT DEFAULT 0,

    -- Các cột nghiệp vụ mới từ Entity Order
                        estimated_preparation_time_minutes INT NOT NULL DEFAULT 0,
                        flagged BOOLEAN NOT NULL DEFAULT false,
                        flag_reason VARCHAR(255),
                        flag_notes TEXT,
                        flagged_at TIMESTAMP,
                        assigned_staff_id UUID,

    -- Audit columns
                        created_at TIMESTAMP,
                        updated_at TIMESTAMP,
                        created_by VARCHAR(50),
                        updated_by VARCHAR(50)
);

-- ========================
-- 11. order_items
-- ========================
CREATE TABLE order_items (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             order_id UUID NOT NULL,
                             variant_id UUID NOT NULL,
                             sku VARCHAR(50),
                             variant_name VARCHAR(150),
                             quantity INT,
                             unit_price DECIMAL(12,2),
                             subtotal DECIMAL(12,2),
                             notes TEXT,
                             preparation_time_minutes INT DEFAULT 0,

                             CONSTRAINT fk_oi_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
);

-- ========================
-- 12. order_item_addons
-- ========================
CREATE TABLE order_item_addons (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   order_item_id UUID NOT NULL,
                                   addon_variant_id UUID NOT NULL,
                                   sku VARCHAR(50),
                                   addon_name VARCHAR(100),
                                   quantity INT,
                                   unit_price DECIMAL(12,2),
                                   subtotal DECIMAL(12,2),

                                   CONSTRAINT fk_oia_item
                                       FOREIGN KEY (order_item_id)
                                           REFERENCES order_items(id)
);

-- ========================
-- 13. order_status_history
-- ========================
CREATE TABLE order_status_history (
                                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                      order_id UUID NOT NULL,
                                      new_status VARCHAR(30),
                                      reason TEXT,
                                      changed_by VARCHAR(50),
                                      created_at TIMESTAMP,

                                      CONSTRAINT fk_osh_order
                                          FOREIGN KEY (order_id)
                                              REFERENCES orders(id)
);

-- ========================
-- 14. order_audit_logs
-- ========================
CREATE TABLE order_audit_logs (
                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  order_id UUID NOT NULL,
                                  entity_type VARCHAR(30),
                                  action_type VARCHAR(20),
                                  changed_fields VARCHAR(255),
                                  reason TEXT,
                                  changed_by VARCHAR(50),
                                  created_at TIMESTAMP,

                                  CONSTRAINT fk_oal_order
                                      FOREIGN KEY (order_id)
                                          REFERENCES orders(id)
);

-- ========================
-- 15. invoices
-- ========================
CREATE TABLE invoices (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          code VARCHAR(100),
                          order_id UUID,
                          customer_id UUID,
                          sub_total DECIMAL,
                          discount_amount DECIMAL,
                          points_discount DECIMAL,
                          tax_amount DECIMAL,
                          shipping_fee DECIMAL,
                          total_amount DECIMAL,
                          currency VARCHAR(10),
                          status VARCHAR(20),
                          franchise_id BIGINT,
                          issued_at TIMESTAMP,
                          paid_at TIMESTAMP,
                          cancelled_at TIMESTAMP,
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP
);

-- ========================
-- 16. invoice_item
-- ========================
CREATE TABLE invoice_item (
                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              invoice_id UUID,
                              product_id UUID,
                              product_name VARCHAR(255),
                              price DECIMAL,
                              quantity INT,
                              total DECIMAL,

                              CONSTRAINT fk_invoice_item
                                  FOREIGN KEY (invoice_id)
                                      REFERENCES invoices(id)
);

-- ========================
-- 17. payment
-- ========================
CREATE TABLE payment (
                         id BIGSERIAL PRIMARY KEY,
                         invoice_id UUID,
                         method VARCHAR(20),
                         provider VARCHAR(20),
                         amount DECIMAL,
                         status VARCHAR(20),
                         paid_at TIMESTAMP,
                         created_at TIMESTAMP,
                         updated_at TIMESTAMP
);

-- ========================
-- 18. payment_transaction
-- ========================
CREATE TABLE payment_transaction (
                                     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                     order_id UUID,
                                     request_id VARCHAR(255),
                                     momo_trans_id VARCHAR(255),
                                     amount BIGINT,
                                     status VARCHAR(50),
                                     payment_method VARCHAR(50),
                                     result_code INT,
                                     message TEXT,
                                     pay_url TEXT,
                                     created_at TIMESTAMP,
                                     updated_at TIMESTAMP,
                                     momo_order_id VARCHAR(255)
);

-- ========================
-- 19. refund
-- ========================
CREATE TABLE refund (
                        id BIGSERIAL PRIMARY KEY,
                        payment_id BIGINT,
                        amount DECIMAL,
                        reason TEXT,
                        provider_refund_id VARCHAR(255),
                        status VARCHAR(20),
                        created_by BIGINT,
                        created_at TIMESTAMP
);