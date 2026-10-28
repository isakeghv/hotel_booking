CREATE TABLE extras(
    extra_id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    price INTEGER NOT NULL,
    addon_possible BOOLEAN NOT NULL DEFAULT true,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_price_fixed BOOLEAN NOT NULL DEFAULT false
);