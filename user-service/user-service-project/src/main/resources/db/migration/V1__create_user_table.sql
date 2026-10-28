CREATE TABLE users(
    user_id UUID PRIMARY KEY,
    email VARCHAR(80) NOT NULL UNIQUE,
    first_name VARCHAR(35) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    country_code VARCHAR(5) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true
)