CREATE TABLE exercise_categories (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name varchar(50) NOT NULL UNIQUE
);

CREATE TABLE exercises (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name varchar(50) NOT NULL UNIQUE,
    category_id integer NOT NULL REFERENCES exercise_categories(id)
);

CREATE TABLE routine_templates (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name varchar(50) NOT NULL UNIQUE
);

CREATE TABLE routine_template_days (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    routine_template_id integer NOT NULL REFERENCES routine_templates(id) ON DELETE CASCADE,
    day_order integer NOT NULL,
    UNIQUE (day_order, routine_template_id)
);

CREATE TABLE routine_template_day_exercises (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    routine_template_day_id integer NOT NULL REFERENCES routine_template_days(id) ON DELETE CASCADE,
    exercise_id integer NOT NULL REFERENCES exercises(id),
    exercise_order integer NOT NULL,
    UNIQUE (exercise_order, routine_template_day_id)
);

CREATE TABLE members (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name varchar(50) NOT NULL,
    birth_date date NOT NULL,
    user_id integer NOT NULL REFERENCES public.users(id),
    UNIQUE (user_id)
);

CREATE TABLE employees (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name varchar(50) NOT NULL,
    birth_date date NOT NULL,
    cpf varchar(11) NOT NULL UNIQUE,
    user_id integer NOT NULL REFERENCES public.users(id),
    UNIQUE (user_id)
);

CREATE TABLE membership_plans (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name varchar(50) NOT NULL UNIQUE,
    price numeric(10,2) NOT NULL,
    membership_duration_unit duration_unit NOT NULL,
    membership_duration_value integer NOT NULL
);

CREATE TABLE memberships (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    membership_plan_id integer NOT NULL REFERENCES membership_plans(id),
    payer_id integer NOT NULL REFERENCES members(id),
    member_id integer NOT NULL REFERENCES members(id),
    subscribed_at date NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    CHECK (start_date <= end_date),
    status membership_status NOT NULL
);

CREATE TABLE routines (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name varchar(50) NOT NULL,
    member_id integer NOT NULL REFERENCES members(id),
    employee_id integer NULL REFERENCES employees(id),
    UNIQUE (name, member_id)
);

CREATE TABLE routine_days (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    routine_id integer NOT NULL REFERENCES routines(id) ON DELETE CASCADE,
    day_order integer NOT NULL,
    UNIQUE (day_order, routine_id)
);

CREATE TABLE routine_day_exercises (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    routine_day_id integer NOT NULL REFERENCES routine_days(id) ON DELETE CASCADE,
    exercise_id integer NOT NULL REFERENCES exercises(id),
    exercise_order integer NOT NULL,
    reps integer NOT NULL,
    series integer NOT NULL,
    notes text NULL,
    UNIQUE (exercise_order, routine_day_id)
);

CREATE TABLE workouts (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    workout_date date NOT NULL,
    member_id integer NOT NULL REFERENCES members(id),
    routine_day_id integer NULL REFERENCES routine_days(id)
);

CREATE TABLE performed_exercises (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    workout_id integer NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    exercise_id integer NOT NULL REFERENCES exercises(id),
    exercise_order integer NOT NULL,
    UNIQUE (workout_id, exercise_order)
);

CREATE TABLE performed_sets (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    performed_exercise_id integer NOT NULL REFERENCES performed_exercises(id) ON DELETE CASCADE,
    set_order integer NOT NULL,
    weight float NOT NULL,
    repetitions integer NOT NULL,
    notes text NULL,
    UNIQUE (set_order, performed_exercise_id)
);

CREATE TABLE user_roles (
    id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    role_id integer NOT NULL REFERENCES public.roles(id),
    user_id integer NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    UNIQUE (role_id, user_id)
);