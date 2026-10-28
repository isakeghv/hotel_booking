CREATE TABLE variants_include(
    variant_id UUID NOT NULL REFERENCES room_type_variants(variant_id),
    extra_id UUID NOT NULL REFERENCES extras(extra_id),
    PRIMARY KEY (variant_id, extra_id),
    amount INT NOT NULL,
    show_amount BOOLEAN NOT NULL
);