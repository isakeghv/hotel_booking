CREATE TABLE room_includes(
    type_id UUID NOT NULL REFERENCES room_type(type_id),
    extra_id UUID NOT NULL REFERENCES extras(extra_id),
    PRIMARY KEY (type_id, extra_id),
    amount INT NOT NULL,
    show_amount BOOLEAN NOT NULL DEFAULT true
);