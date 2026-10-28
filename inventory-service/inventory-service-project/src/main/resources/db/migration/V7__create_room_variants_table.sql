CREATE TABLE room_type_variants(
    variant_id UUID PRIMARY KEY ,
    type_id UUID NOT NULL REFERENCES room_type(type_id),
    variant_name VARCHAR(50) NOT NULL ,
    extra_price INT NOT NULL,
    is_active BOOLEAN NOT NULL,
    is_price_fixed BOOLEAN NOT NULL
);