CREATE TABLE IF NOT EXISTS products (
    id SERIAL UNIQUE PRIMARY KEY,
    name VARCHAR(70) UNIQUE NOT NULL,
    description VARCHAR(255),
    price NUMERIC(10, 2) NOT NULL,
    is_available BOOLEAN NOT NULL
);
