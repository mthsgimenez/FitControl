INSERT INTO public.exercises (name, category_id) VALUES
-- Peito
('Supino reto', 1),
('Supino inclinado', 1),
('Supino declinado', 1),
('Crucifixo', 1),
('Crossover', 1),

-- Tríceps
('Tríceps testa', 2),
('Tríceps pulley', 2),
('Tríceps francês', 2),

-- Dorsal
('Puxada frente', 3),
('Pull over', 3),
('Remada alta', 3),

-- Dorsal médio
('Remada curvada', 4),
('Remada sentado máquina', 4),
('Remada unilateral', 4),
('Remada baixa', 4),
('Pulley costas', 4),

-- Lombar
('Hiperextensão lombar', 5),
('Stiff', 5),
('Deadlift romeno', 5),

-- Trapézio
('Encolhimento de ombro com halteres', 6),
('Encolhimento de ombro barra', 6),
('Remada alta para trapézio', 6),
('Face pull', 6),

-- Bíceps
('Rosca direta', 7),
('Rosca alternada', 7),
('Rosca martelo', 7),
('Rosca 45', 7),
('Rosca Scott', 7),

-- Ombro
('Desenvolvimento ombro halteres', 8),
('Elevação lateral', 8),
('Elevação frontal', 8),
('Crucifixo inverso', 8),
('Deltoide posterior unilateral', 8),

-- Quadríceps
('Agachamento livre', 9),
('Agachamento smith', 9),
('Leg press', 9),
('Cadeira extensora', 9),
('Afundo', 9),

-- Posterior de coxa
('Mesa flexora', 10),
('Stiff', 10),
('Cadeira flexora', 9),
('Flexora unilateral em pé', 10),

-- Glúteo
('Elevação de quadril', 11),
('Avanço', 11),
('Cadeira abdutora', 11),

-- Panturrilha
('Panturrilha em pé', 12),
('Panturrilha banco', 12),
('Panturrilha burro', 12),

-- Adutor
('Adutor na máquina', 13),
('Adutor deitado', 13),
('Crossover de pernas', 13),

-- Abdutor
('Abdução na máquina', 14),
('Abdução em pé com faixa', 14),
('Abdução deitado lateral', 14),

-- Antebraço
('Rosca punho', 15),
('Rosca inversa', 15),
('Extensão de punho', 15),

-- Abdômen
('Prancha', 16),
('Abdominal supra', 16),
('Abdominal infra', 16),
('Abdominal oblíquo', 16),
('Crunch', 16)
ON CONFLICT (name) DO NOTHING;