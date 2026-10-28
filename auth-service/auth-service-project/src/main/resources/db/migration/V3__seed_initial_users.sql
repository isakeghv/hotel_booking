INSERT INTO user_credentials (user_id, email, password, permission_level, is_active)
VALUES
    ('d04b023b-ceb6-4db0-b107-b6be6e8663a7', 'owner@hotelbooking.com', '{bcrypt}$2a$10$02hQXWnOWV7y/VWcgMpnJ.5B651CLbkO7TfBpVgw2eQoFsUAkQvCq', 3, true),
    ('d04b023b-ceb6-4db0-b107-b6be6e8663a8', 'manager@hotelbooking.com', '{bcrypt}$2a$10$02hQXWnOWV7y/VWcgMpnJ.5B651CLbkO7TfBpVgw2eQoFsUAkQvCq', 2, true),
    ('d04b023b-ceb6-4db0-b107-b6be6e8663a9', 'employee@hotelbooking.com', '{bcrypt}$2a$10$02hQXWnOWV7y/VWcgMpnJ.5B651CLbkO7TfBpVgw2eQoFsUAkQvCq', 1, true),
    ('d04b023b-ceb6-4db0-b107-b6be6e8663a5', 'customer@hotelbooking.com', '{bcrypt}$2a$10$02hQXWnOWV7y/VWcgMpnJ.5B651CLbkO7TfBpVgw2eQoFsUAkQvCq', 0, true)
    ON CONFLICT (user_id) DO NOTHING;