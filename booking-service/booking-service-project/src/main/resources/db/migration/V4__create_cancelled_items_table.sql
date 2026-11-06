-- One row per cancelled room. Only cancelled rooms get a row, so booking_items holds no cancellation columns.
-- cancelled_by only says who cancelled. Refunds are decided by the payment-service,
-- which uses cancellation_id to never refund the same room twice.
CREATE TABLE cancelled_items(
    cancellation_id UUID PRIMARY KEY,
    -- UNIQUE: the database refuses a second cancellation of the same room (e.g. double clicks)
    item_id UUID NOT NULL UNIQUE REFERENCES booking_items(item_id),
    booking_id UUID NOT NULL REFERENCES bookings(booking_id),
    cancelled_at TIMESTAMPTZ NOT NULL,
    cancelled_by VARCHAR(20) NOT NULL,
    CHECK (cancelled_by IN ('CUSTOMER', 'STAFF'))
);

CREATE INDEX idx_cancelled_items_booking ON cancelled_items(booking_id);
