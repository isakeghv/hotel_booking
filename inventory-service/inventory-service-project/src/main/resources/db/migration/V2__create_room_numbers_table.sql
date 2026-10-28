CREATE TABLE room_numbers(
    room_id UUID PRIMARY KEY,
    type_id UUID NOT NULL REFERENCES room_type(type_id),
    room_number INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true
);