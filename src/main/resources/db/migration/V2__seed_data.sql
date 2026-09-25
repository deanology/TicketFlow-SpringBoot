INSERT INTO department (name) VALUES
    ('IT Support'),
    ('Engineering'),
    ('HR');

-- All demo users share the password: password123
INSERT INTO employee (full_name, email, password_hash, role, department_id) VALUES
    ('Ada Admin',    'ada.admin@ticketflow.dev',    '$2b$10$HVT/lqOUFF0GfGGQbWCgCeCBw5s0LcDO1MQm4q4hOF08vyuOuj8N2', 'ADMIN', 1),
    ('Sam Support',  'sam.support@ticketflow.dev',  '$2b$10$xAHzE/ky2bqM07vMqwKB2uS4jk2jtmbF9OOzPjxmJow.8.b.KamsG', 'AGENT', 1),
    ('Priya Patel',  'priya.patel@ticketflow.dev',  '$2b$10$y9PyLcvlx3d5m4W0TwMiU.f4A8xRL9cCOE0UOwcABRdvH4/U5y6vm', 'AGENT', 2),
    ('Uche User',    'uche.user@ticketflow.dev',    '$2b$10$ttqrJ9e0Zqu85JjKcLssoOeYa7EZoWu7/ktvwamWpZ2j54ZqAJbmu', 'USER',  2);

INSERT INTO ticket (title, description, status, priority, department_id, created_by_id, assigned_to_id) VALUES
    ('VPN keeps disconnecting every 10 minutes',
     'Since the last Windows update my VPN client drops the connection roughly every 10 minutes while working from home. Have to reconnect manually each time.',
     'OPEN', 'MEDIUM', 1, 4, 2),
    ('Production login endpoint returning 500',
     'Users are reporting intermittent 500 errors on POST /api/auth/login since this morning''s deploy. Seeing null pointer exceptions in the logs.',
     'IN_PROGRESS', 'HIGH', 2, 3, 3),
    ('Request access to shared design drive',
     'Need read/write access to the shared design assets drive for the new onboarding project.',
     'OPEN', 'LOW', 1, 4, NULL);
