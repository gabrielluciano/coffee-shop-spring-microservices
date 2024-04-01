CREATE TABLE IF NOT EXISTS orders (
    id UUID UNIQUE PRIMARY KEY,
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    order_id UUID REFERENCES orders(id) NOT NULL,
    PRIMARY KEY (product_id, order_id)
);
