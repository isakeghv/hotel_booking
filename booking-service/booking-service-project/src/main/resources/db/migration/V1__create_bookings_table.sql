-- The booking_id is the cart's bookingId, created when the first room is added to the cart.
-- Saving the same cart twice is refused on the primary key.
CREATE TABLE bookings(
    booking_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    -- Earliest check-in and latest check-out of the booking's rooms (local hotel time)
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    total_price INT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMPTZ NOT NULL,
    -- True when every room of the booking is cancelled (see cancelled_items). Bookings are never deleted
    cancelled BOOLEAN NOT NULL DEFAULT false,
    CHECK (end_time > start_time),
    CHECK (total_price >= 0)
);

-- The customer's lists: upcoming, previous and cancelled
CREATE INDEX idx_bookings_user_cancelled_end ON bookings(user_id, cancelled, end_time);

-- The staff date search: bookings that start, end or cover a date
CREATE INDEX idx_bookings_start_end ON bookings(start_time, end_time);
