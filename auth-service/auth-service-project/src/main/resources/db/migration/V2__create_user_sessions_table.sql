CREATE TABLE user_sessions (
    session_id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_credentials(user_id),
    issued_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL
)