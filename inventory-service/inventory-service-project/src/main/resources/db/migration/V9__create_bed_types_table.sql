CREATE TABLE bed_types(
    bed_id UUID PRIMARY KEY ,
    width INT NOT NULL,
    height INT NOT NULL,
    capacity INT NOT NULL,
    name varchar(50) NOT NULL
);