CREATE TABLE room_information(
    information_id UUID PRIMARY KEY,
    type_id UUID NOT NULL REFERENCES room_type(type_id),
    information VARCHAR(255)[],
    category VARCHAR(50) NOT NULL,
    CONSTRAINT check_information_max_items CHECK ( array_length(information, 1) <= 3 )
);