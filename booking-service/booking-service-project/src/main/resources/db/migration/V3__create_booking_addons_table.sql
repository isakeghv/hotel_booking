-- Extras added to one room (item_id) of a booking. extra_id refers to the inventory-service's database.
CREATE TABLE booking_addons(
    addon_id UUID PRIMARY KEY,
    booking_id UUID NOT NULL REFERENCES bookings(booking_id),
    item_id UUID NOT NULL REFERENCES booking_items(item_id),
    extra_id UUID NOT NULL,
    -- Copy of the extra's name, for display
    name VARCHAR(50) NOT NULL,
    amount INT NOT NULL,
    price INT NOT NULL,
    CHECK (amount > 0),
    CHECK (price >= 0),
    -- One line per extra per room: adding the same extra again increases the amount
    UNIQUE (item_id, extra_id)
);

CREATE INDEX idx_booking_addons_booking ON booking_addons(booking_id);
