CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name_encrypted BYTEA,
    full_name_blind_index TEXT,
    phone_encrypted BYTEA,
    phone_blind_index TEXT,
    email_encrypted BYTEA,
    email_blind_index TEXT,
    password_hash TEXT NOT NULL,
    birth_date_encrypted BYTEA,
    role_encrypted BYTEA,
    role_blind_index TEXT,
    created_at_encrypted BYTEA
);

CREATE INDEX idx_users_phone_blind_index ON users(phone_blind_index);
CREATE INDEX idx_users_email_blind_index ON users(email_blind_index);
CREATE INDEX idx_users_full_name_blind_index ON users(full_name_blind_index);
CREATE INDEX idx_users_role_blind_index ON users(role_blind_index);

CREATE TABLE user_name_ngrams (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ngram_blind_index TEXT NOT NULL
);

CREATE INDEX idx_user_ngrams_index ON user_name_ngrams(ngram_blind_index);
CREATE INDEX idx_user_ngrams_user_id ON user_name_ngrams(user_id);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name_encrypted BYTEA,
    name_blind_index TEXT,
    parent_id BIGINT REFERENCES categories(id)
);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_name_blind_index ON categories(name_blind_index);

CREATE TABLE category_name_ngrams (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    ngram_blind_index TEXT NOT NULL
);

CREATE INDEX idx_cat_ngrams_index ON category_name_ngrams(ngram_blind_index);
CREATE INDEX idx_cat_ngrams_cat_id ON category_name_ngrams(category_id);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    title_encrypted BYTEA,
    title_blind_index TEXT,
    description_encrypted BYTEA,
    price_encrypted BYTEA,
    stock_quantity_encrypted BYTEA,
    rating_encrypted BYTEA,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    seller_id BIGINT NOT NULL REFERENCES users(id),
    created_at_encrypted BYTEA
);

CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_title_blind_index ON products(title_blind_index);

CREATE TABLE product_title_ngrams (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    ngram_blind_index TEXT NOT NULL
);

CREATE INDEX idx_prod_ngrams_index ON product_title_ngrams(ngram_blind_index);
CREATE INDEX idx_prod_ngrams_prod_id ON product_title_ngrams(product_id);

CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    region_encrypted BYTEA,
    region_blind_index TEXT,
    city_encrypted BYTEA,
    city_blind_index TEXT,
    street_encrypted BYTEA,
    house_apartment_encrypted BYTEA,
    postal_code_encrypted BYTEA,
    postal_code_blind_index TEXT
);

CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_addresses_region_blind_index ON addresses(region_blind_index);
CREATE INDEX idx_addresses_city_blind_index ON addresses(city_blind_index);
CREATE INDEX idx_addresses_postal_code_blind_index ON addresses(postal_code_blind_index);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    status_encrypted BYTEA,
    status_blind_index TEXT,
    total_price_encrypted BYTEA,
    delivery_address_encrypted BYTEA,
    recipient_phone_encrypted BYTEA,
    recipient_phone_blind_index TEXT,
    created_at_encrypted BYTEA
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status_blind_index ON orders(status_blind_index);
CREATE INDEX idx_orders_recipient_phone_blind_index ON orders(recipient_phone_blind_index);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity_encrypted BYTEA,
    price_at_order_encrypted BYTEA
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id),
    amount_encrypted BYTEA,
    status_encrypted BYTEA,
    status_blind_index TEXT,
    card_number_encrypted BYTEA,
    card_last4_blind_index TEXT,
    card_holder_name_encrypted BYTEA,
    created_at_encrypted BYTEA
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_card_last4_blind_index ON payments(card_last4_blind_index);
CREATE INDEX idx_payments_status_blind_index ON payments(status_blind_index);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    rating_encrypted BYTEA,
    comment_encrypted BYTEA,
    created_at_encrypted BYTEA
);

CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);

CREATE TABLE review_comment_ngrams (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    ngram_blind_index TEXT NOT NULL
);

CREATE INDEX idx_rev_ngrams_index ON review_comment_ngrams(ngram_blind_index);
CREATE INDEX idx_rev_ngrams_rev_id ON review_comment_ngrams(review_id);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action_encrypted BYTEA,
    action_blind_index TEXT,
    entity_name_encrypted BYTEA,
    entity_name_blind_index TEXT,
    entity_id BIGINT,
    ip_address_encrypted BYTEA,
    timestamp_encrypted BYTEA
);

CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_action_blind_index ON audit_log(action_blind_index);
CREATE INDEX idx_audit_log_entity_name_blind_index ON audit_log(entity_name_blind_index);

CREATE TABLE audit_action_ngrams (
    id BIGSERIAL PRIMARY KEY,
    audit_log_id BIGINT NOT NULL REFERENCES audit_log(id) ON DELETE CASCADE,
    ngram_blind_index TEXT NOT NULL
);

CREATE INDEX idx_aud_act_ngrams_index ON audit_action_ngrams(ngram_blind_index);
CREATE INDEX idx_aud_act_ngrams_audit_id ON audit_action_ngrams(audit_log_id);

CREATE TABLE audit_entity_name_ngrams (
    id BIGSERIAL PRIMARY KEY,
    audit_log_id BIGINT NOT NULL REFERENCES audit_log(id) ON DELETE CASCADE,
    ngram_blind_index TEXT NOT NULL
);

CREATE INDEX idx_aud_ent_ngrams_index ON audit_entity_name_ngrams(ngram_blind_index);
CREATE INDEX idx_aud_ent_ngrams_audit_id ON audit_entity_name_ngrams(audit_log_id);
