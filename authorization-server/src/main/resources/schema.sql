CREATE TABLE IF NOT EXISTS user_credentials (
    id UUID UNIQUE PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    roles VARCHAR[] NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS clients (
    id SERIAL UNIQUE PRIMARY KEY,
    client_id VARCHAR(255) UNIQUE NOT NULL,
    secret VARCHAR(255) NOT NULL,
    auth_methods VARCHAR(20)[] NOT NULL,
    grant_types VARCHAR(20)[] NOT NULL,
    scopes VARCHAR(20)[] NOT NULL
);
