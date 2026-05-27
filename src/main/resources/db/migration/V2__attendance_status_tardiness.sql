ALTER TABLE public.attendance
    ALTER COLUMN status TYPE VARCHAR(1);

ALTER TABLE public.attendance
    DROP CONSTRAINT IF EXISTS attendance_status_check;

ALTER TABLE public.attendance
    ADD CONSTRAINT attendance_status_check
    CHECK (status IN ('A', 'F', 'J', 'T'));
