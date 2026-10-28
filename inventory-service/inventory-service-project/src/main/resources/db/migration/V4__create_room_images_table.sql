CREATE TABLE room_images(
    image_id UUID PRIMARY KEY,
    type_id UUID NOT NULL REFERENCES room_type(type_id),
    file_name VARCHAR(255),
    image_text VARCHAR(150),
    alt_text VARCHAR(150)
);