INSERT INTO public.roles (id, name) VALUES
    (1, 'ROLE_OWNER'),
    (2, 'ROLE_MANAGER'),
    (3, 'ROLE_FINANCE'),
    (4, 'ROLE_INSTRUCTOR'),
    (5, 'ROLE_MEMBER')
ON CONFLICT (id) DO NOTHING;