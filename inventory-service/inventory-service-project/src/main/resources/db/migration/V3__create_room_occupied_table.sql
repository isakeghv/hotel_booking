CREATE TABLE occupied_room(
    occupied_id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES room_numbers(room_id),
    from_time TIMESTAMP NOT NULL,
    to_time TIMESTAMP NOT NULL
);