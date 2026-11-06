-- One row per cancelled extra. Only cancelled extras get a row, so booking_addons holds no cancellation columns.
-- Cancelling a room also cancels its extras. cancellation_id lets the payment-service never refund the same extra twice.
CREATE TABLE cancelled_addons(
    cancellation_id UUID PRIMARY KEY,
    -- UNIQUE: the database refuses a second cancellation of the same extra
    addon_id UUID NOT NULL UNIQUE REFERENCES booking_addons(addon_id),
    booking_id UUID NOT NULL REFERENCES bookings(booking_id),
    cancelled_at TIMESTAMPTZ NOT NULL,
    cancelled_by VARCHAR(20) NOT NULL,
    CHECK (cancelled_by IN ('CUSTOMER', 'STAFF'))
);

CREATE INDEX idx_cancelled_addons_booking ON cancelled_addons(booking_id);
