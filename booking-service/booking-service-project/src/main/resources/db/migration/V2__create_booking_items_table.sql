-- One row per booked room. The item_id is the same id as inventory's occupied_room.occupied_id (the cart's itemId).
-- room_type_id and variant_id refer to the inventory-service's database (no foreign key across databases).
CREATE TABLE booking_items(
    item_id UUID PRIMARY KEY,
    booking_id UUID NOT NULL REFERENCES bookings(booking_id),
    -- Copies from inventory's reserve response, so lists and emails need no calls to inventory
    room_number INT NOT NULL,
    room_type_id UUID NOT NULL,
    room_type_name VARCHAR(40) NOT NULL,
    variant_id UUID NOT NULL,
    variant_name VARCHAR(50) NOT NULL,
    from_time TIMESTAMP NOT NULL,
    to_time TIMESTAMP NOT NULL,
    price INT NOT NULL,
    checked_in BOOLEAN NOT NULL DEFAULT false,
    checked_out BOOLEAN NOT NULL DEFAULT false,
    CHECK (to_time > from_time),
    CHECK (price >= 0),
    -- A room cannot be checked out before it is checked in
    CHECK (NOT checked_out OR checked_in)
);

CREATE INDEX idx_booking_items_booking ON booking_items(booking_id);
