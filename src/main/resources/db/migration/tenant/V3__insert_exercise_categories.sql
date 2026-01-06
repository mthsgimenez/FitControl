INSERT INTO public.exercise_categories (id, name) VALUES
    (1, 'Peito'),
    (2, 'Tríceps'),
    (3, 'Dorsal'),
    (4, 'Dorsal médio'),
    (5, 'Lombar'),
    (6, 'Trapézio'),
    (7, 'Bíceps'),
    (8, 'Ombro'),
    (9, 'Quadríceps'),
    (10, 'Posterior de coxa'),
    (11, 'Glúteo'),
    (12, 'Panturrilha'),
    (13, 'Adutor'),
    (14, 'Abdutor'),
    (15, 'Antebraço'),
    (16, 'Abdômen')
ON CONFLICT (id) DO NOTHING;