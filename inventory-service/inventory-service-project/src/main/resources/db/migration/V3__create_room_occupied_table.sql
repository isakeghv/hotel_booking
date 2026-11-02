-- Needed to use "=" on a UUID inside a GiST exclusion constraint
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE occupied_room(
    occupied_id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES room_numbers(room_id),
    from_time TIMESTAMP NOT NULL,
    to_time TIMESTAMP NOT NULL,
    booked BOOLEAN NOT NULL,
    CHECK (to_time > from_time),
    -- Prevents two overlapping periods for the same room, even when requests arrive at the same time.
    -- '[)' includes the start but not the end, so a period ending at 12:00 and one starting at 12:00 do not overlap.
    EXCLUDE USING gist (
        room_id WITH =,
        tsrange(from_time, to_time, '[)') WITH &&
    )
);
