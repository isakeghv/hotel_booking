CREATE TABLE room_images(
    image_id UUID PRIMARY KEY,
    type_id UUID NOT NULL REFERENCES room_type(type_id),
    file_name VARCHAR(255),
    image_text VARCHAR(150),
    alt_text VARCHAR(150),
    is_primary BOOLEAN NOT NULL DEFAULT false
);

-- At most one primary image per room type. The first image of a room type is always made primary by the service
CREATE UNIQUE INDEX one_primary_image_per_room_type
    ON room_images (type_id)
    WHERE is_primary;
