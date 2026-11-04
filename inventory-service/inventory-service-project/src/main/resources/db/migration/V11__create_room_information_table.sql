CREATE TABLE room_information(
    information_id UUID PRIMARY KEY,
    type_id UUID NOT NULL REFERENCES room_type(type_id),
    information VARCHAR(255)[],
    is_main_description BOOLEAN not null default false,
    title VARCHAR(50) NOT NULL,
    CONSTRAINT check_information_max_items CHECK ( array_length(information, 1) <= 3 )
);

-- At most one main description per room type
CREATE UNIQUE INDEX one_main_description_per_room_type
    ON room_information (type_id)
    WHERE is_main_description;
