INSERT INTO exercise_categories (name) VALUES
    ('Peito'),
    ('Tríceps'),
    ('Dorsal'),
    ('Dorsal médio'),
    ('Lombar'),
    ('Trapézio'),
    ('Bíceps'),
    ('Ombro'),
    ('Quadríceps'),
    ('Posterior de coxa'),
    ('Glúteo'),
    ('Panturrilha'),
    ('Adutor'),
    ('Abdutor'),
    ('Antebraço'),
    ('Abdômen')
ON CONFLICT (name) DO NOTHING;