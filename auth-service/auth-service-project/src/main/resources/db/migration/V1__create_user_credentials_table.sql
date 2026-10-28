CREATE TABLE user_credentials (
    user_id UUID PRIMARY KEY,
    email VARCHAR(80) NOT NULL UNIQUE,
    password VARCHAR(150) NOT NULL,
    permission_level INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT check_email_length CHECK (char_length(email) >= 6 ),
    CONSTRAINT check_pwd_length CHECK (char_length(password) >= 8 )
)