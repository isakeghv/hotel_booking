CREATE TABLE room_rules(
    rule_id UUID PRIMARY KEY,
    type_id UUID NOT NULL REFERENCES room_type(type_id),
    rule VARCHAR(100) NOT NULL,
    allowed BOOLEAN NOT NULL
);