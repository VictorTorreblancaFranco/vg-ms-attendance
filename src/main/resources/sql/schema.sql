-- ==========================================
-- vg-ms-task DATABASE SCHEMA
-- ==========================================

-- 1. TASKS TABLE
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    class_id INTEGER NOT NULL,
    criterion_id INTEGER,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    instructions TEXT,
    assignment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    points_value DECIMAL(5,2) DEFAULT 0.00,
    allowed_attempts SMALLINT DEFAULT 1,
    is_group_task BOOLEAN NOT NULL DEFAULT FALSE,
    visible_to_parents BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(30) NOT NULL DEFAULT 'draft',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ÍNDICES
CREATE INDEX IF NOT EXISTS idx_tasks_class_id ON tasks(class_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);

-- 2. TASK_SUBMISSIONS TABLE
CREATE TABLE IF NOT EXISTS task_submissions (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    student_id INTEGER NOT NULL,
    attempt_number SMALLINT NOT NULL DEFAULT 1,
    submission_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status VARCHAR(30) NOT NULL DEFAULT 'submitted',
    grade DECIMAL(5,2),
    feedback TEXT,
    graded_by INTEGER,
    graded_at TIMESTAMPTZ,
    student_comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(task_id, student_id, attempt_number)
);

CREATE INDEX IF NOT EXISTS idx_task_submissions_task_id ON task_submissions(task_id);
CREATE INDEX IF NOT EXISTS idx_task_submissions_student_id ON task_submissions(student_id);

-- 3. TASK_RESOURCES TABLE
CREATE TABLE IF NOT EXISTS task_resources (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    size_kb INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_task_resources_task_id ON task_resources(task_id);

-- 4. SUBMISSION_FILES TABLE
CREATE TABLE IF NOT EXISTS submission_files (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES task_submissions(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    size_kb INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_submission_files_submission_id ON submission_files(submission_id);

-- 5. ATTENDANCE TABLE
CREATE TABLE IF NOT EXISTS attendance (
    id BIGSERIAL PRIMARY KEY,
    student_id INTEGER NOT NULL,
    class_id INTEGER NOT NULL,
    schedule_id INTEGER,
    date DATE NOT NULL,
    is_present BOOLEAN NOT NULL DEFAULT FALSE,
    is_late BOOLEAN NOT NULL DEFAULT FALSE,
    late_minutes SMALLINT,
    is_justified BOOLEAN NOT NULL DEFAULT FALSE,
    absence_reason VARCHAR(255),
    justification_document VARCHAR(500),
    registered_by INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(student_id, class_id, date)
);

CREATE INDEX IF NOT EXISTS idx_attendance_class_id ON attendance(class_id);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(date);

-- 6. CURRICULUM_PLAN TABLE
CREATE TABLE IF NOT EXISTS curriculum_plan (
    id BIGSERIAL PRIMARY KEY,
    class_id INTEGER NOT NULL,
    period_id INTEGER NOT NULL,
    unit SMALLINT NOT NULL,
    unit_title VARCHAR(255) NOT NULL,
    competencies TEXT,
    contents TEXT,
    strategies TEXT,
    resources TEXT,
    planned_hours SMALLINT,
    UNIQUE(class_id, period_id, unit)
);

CREATE INDEX IF NOT EXISTS idx_curriculum_plan_class_id ON curriculum_plan(class_id);

-- 7. EDUCATIONAL_RESOURCES TABLE
CREATE TABLE IF NOT EXISTS educational_resources (
    id BIGSERIAL PRIMARY KEY,
    institution_id INTEGER NOT NULL,
    uploaded_by INTEGER NOT NULL,
    subject_id INTEGER,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(30) NOT NULL,
    url VARCHAR(500) NOT NULL,
    size_kb INTEGER,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    downloads_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_educational_resources_institution_id ON educational_resources(institution_id);
CREATE INDEX IF NOT EXISTS idx_educational_resources_subject_id ON educational_resources(subject_id);
