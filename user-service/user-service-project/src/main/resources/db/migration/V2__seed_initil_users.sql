INSERT INTO users (user_id, email, first_name, last_name, phone_number, is_active)
VALUES
    ('d04b023b-ceb6-4db0-b107-b6be6e8663a7', 'owner@hotelbooking.com', 'Owner', 'Person', '+4790909090', true),
    ('d04b023b-ceb6-4db0-b107-b6be6e8663a8', 'manager@hotelbooking.com', 'Manager', 'Person', '+4790909090', true),
    ('d04b023b-ceb6-4db0-b107-b6be6e8663a9', 'employee@hotelbooking.com', 'Employee', 'Person', '+4790909090', true),
    ('d04b023b-ceb6-4db0-b107-b6be6e8663a5', 'customer@hotelbooking.com', 'Customer', 'Person', '+4790909090', true)
    ON CONFLICT (user_id) DO NOTHING;