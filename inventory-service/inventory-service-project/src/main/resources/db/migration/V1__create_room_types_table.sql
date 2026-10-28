CREATE TABLE room_type(
    type_id UUID PRIMARY KEY,
    room_price INT NOT NULL,
    name VARCHAR(40) NOT NULL,
    capacity INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true
);