CREATE TABLE room_beds(
    type_id UUID NOT NULL REFERENCES room_type(type_id),
    bed_id UUID NOT NULL REFERENCES bed_types(bed_id),
    PRIMARY KEY (type_id, bed_id),
    amount INT NOT NULL
);