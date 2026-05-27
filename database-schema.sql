--
-- PostgreSQL database dump
--

\restrict 6Ae4GuuCFg1CCkPpe7tn9PH0WZaMX1hkisDfalBW5SsUgwUzELFIb5mSLHH47Qt

-- Dumped from database version 17.10 (6a49db4)
-- Dumped by pg_dump version 17.10

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: attendance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.attendance (
    id bigint NOT NULL,
    class_id integer NOT NULL,
    student_id integer NOT NULL,
    date date NOT NULL,
    status character varying(1) NOT NULL,
    observation text,
    created_by integer,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT attendance_status_check CHECK (((status)::text = ANY ((ARRAY['A'::character varying, 'F'::character varying, 'J'::character varying, 'T'::character varying])::text[])))
);


--
-- Name: attendance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.attendance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: attendance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.attendance_id_seq OWNED BY public.attendance.id;


--
-- Name: curriculum_plan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.curriculum_plan (
    id bigint NOT NULL,
    class_id integer NOT NULL,
    unidad_number integer NOT NULL,
    unidad_name character varying(255) NOT NULL,
    tema_name character varying(255) NOT NULL,
    fecha_inicio date,
    fecha_fin date,
    objetivos text,
    competencias text,
    created_by integer,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: curriculum_plan_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.curriculum_plan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: curriculum_plan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.curriculum_plan_id_seq OWNED BY public.curriculum_plan.id;


--
-- Name: educational_resources; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.educational_resources (
    id bigint NOT NULL,
    title character varying(255) NOT NULL,
    description text,
    type character varying(50) NOT NULL,
    url character varying(500),
    file_path character varying(500),
    subject_id integer,
    grade_id integer,
    created_by integer,
    is_public boolean DEFAULT true,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: educational_resources_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.educational_resources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: educational_resources_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.educational_resources_id_seq OWNED BY public.educational_resources.id;


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    id bigint NOT NULL,
    user_id integer NOT NULL,
    task_id bigint,
    type character varying(50) NOT NULL,
    title character varying(255) NOT NULL,
    message text NOT NULL,
    is_read boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notifications_id_seq OWNED BY public.notifications.id;


--
-- Name: rubric_criteria; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rubric_criteria (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    max_score numeric(5,2) NOT NULL,
    weight numeric(5,2) NOT NULL,
    sort_order integer DEFAULT 0,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    active boolean DEFAULT true
);


--
-- Name: rubric_criteria_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.rubric_criteria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: rubric_criteria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.rubric_criteria_id_seq OWNED BY public.rubric_criteria.id;


--
-- Name: rubric_scores; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rubric_scores (
    id bigint NOT NULL,
    submission_id bigint NOT NULL,
    criterion_id bigint NOT NULL,
    score numeric(5,2) NOT NULL,
    feedback text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    active boolean DEFAULT true
);


--
-- Name: rubric_scores_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.rubric_scores_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: rubric_scores_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.rubric_scores_id_seq OWNED BY public.rubric_scores.id;


--
-- Name: submission_grade_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.submission_grade_logs (
    id bigint NOT NULL,
    submission_id bigint NOT NULL,
    old_grade numeric(5,2),
    new_grade numeric(5,2),
    changed_by integer NOT NULL,
    justification text,
    changed_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: submission_grade_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.submission_grade_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: submission_grade_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.submission_grade_logs_id_seq OWNED BY public.submission_grade_logs.id;


--
-- Name: submissions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.submissions (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    student_id integer NOT NULL,
    submission_date timestamp with time zone DEFAULT now() NOT NULL,
    status character varying(20) DEFAULT 'submitted'::character varying NOT NULL,
    grade numeric(5,2),
    feedback text,
    graded_by integer,
    graded_at timestamp with time zone,
    justification_reason text,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    presented boolean DEFAULT false,
    presented_at timestamp with time zone,
    observations text,
    is_late boolean DEFAULT false,
    justified_at timestamp with time zone,
    justified_by integer
);


--
-- Name: submissions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.submissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: submissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.submissions_id_seq OWNED BY public.submissions.id;


--
-- Name: task_files; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_files (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    file_name character varying(255) NOT NULL,
    file_url character varying(500) NOT NULL,
    file_type character varying(50) NOT NULL,
    file_size_kb integer,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: task_files_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_files_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_files_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_files_id_seq OWNED BY public.task_files.id;


--
-- Name: task_resources; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_resources (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    type character varying(50) NOT NULL,
    name character varying(255) NOT NULL,
    url text NOT NULL,
    size_kb integer,
    created_by integer,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: task_resources_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_resources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_resources_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_resources_id_seq OWNED BY public.task_resources.id;


--
-- Name: tasks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tasks (
    id bigint NOT NULL,
    title character varying(255) NOT NULL,
    description text,
    instructions text,
    class_id integer NOT NULL,
    criterion_id integer,
    points_value numeric(5,2) DEFAULT 0.00,
    due_date timestamp with time zone NOT NULL,
    scheduled_publish_date timestamp with time zone,
    scheduled_close_date timestamp with time zone,
    status character varying(20) DEFAULT 'draft'::character varying NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    created_by integer NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone
);


--
-- Name: tasks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tasks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tasks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tasks_id_seq OWNED BY public.tasks.id;


--
-- Name: attendance id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance ALTER COLUMN id SET DEFAULT nextval('public.attendance_id_seq'::regclass);


--
-- Name: curriculum_plan id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.curriculum_plan ALTER COLUMN id SET DEFAULT nextval('public.curriculum_plan_id_seq'::regclass);


--
-- Name: educational_resources id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.educational_resources ALTER COLUMN id SET DEFAULT nextval('public.educational_resources_id_seq'::regclass);


--
-- Name: notifications id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications ALTER COLUMN id SET DEFAULT nextval('public.notifications_id_seq'::regclass);


--
-- Name: rubric_criteria id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rubric_criteria ALTER COLUMN id SET DEFAULT nextval('public.rubric_criteria_id_seq'::regclass);


--
-- Name: rubric_scores id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rubric_scores ALTER COLUMN id SET DEFAULT nextval('public.rubric_scores_id_seq'::regclass);


--
-- Name: submission_grade_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission_grade_logs ALTER COLUMN id SET DEFAULT nextval('public.submission_grade_logs_id_seq'::regclass);


--
-- Name: submissions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submissions ALTER COLUMN id SET DEFAULT nextval('public.submissions_id_seq'::regclass);


--
-- Name: task_files id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_files ALTER COLUMN id SET DEFAULT nextval('public.task_files_id_seq'::regclass);


--
-- Name: task_resources id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_resources ALTER COLUMN id SET DEFAULT nextval('public.task_resources_id_seq'::regclass);


--
-- Name: tasks id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tasks ALTER COLUMN id SET DEFAULT nextval('public.tasks_id_seq'::regclass);


--
-- Name: attendance attendance_class_id_student_id_date_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance
    ADD CONSTRAINT attendance_class_id_student_id_date_key UNIQUE (class_id, student_id, date);


--
-- Name: attendance attendance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.attendance
    ADD CONSTRAINT attendance_pkey PRIMARY KEY (id);


--
-- Name: curriculum_plan curriculum_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.curriculum_plan
    ADD CONSTRAINT curriculum_plan_pkey PRIMARY KEY (id);


--
-- Name: educational_resources educational_resources_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.educational_resources
    ADD CONSTRAINT educational_resources_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: rubric_criteria rubric_criteria_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rubric_criteria
    ADD CONSTRAINT rubric_criteria_pkey PRIMARY KEY (id);


--
-- Name: rubric_scores rubric_scores_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rubric_scores
    ADD CONSTRAINT rubric_scores_pkey PRIMARY KEY (id);


--
-- Name: rubric_scores rubric_scores_submission_id_criterion_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rubric_scores
    ADD CONSTRAINT rubric_scores_submission_id_criterion_id_key UNIQUE (submission_id, criterion_id);


--
-- Name: submission_grade_logs submission_grade_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission_grade_logs
    ADD CONSTRAINT submission_grade_logs_pkey PRIMARY KEY (id);


--
-- Name: submissions submissions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submissions
    ADD CONSTRAINT submissions_pkey PRIMARY KEY (id);


--
-- Name: submissions submissions_task_id_student_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submissions
    ADD CONSTRAINT submissions_task_id_student_id_key UNIQUE (task_id, student_id);


--
-- Name: task_files task_files_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_files
    ADD CONSTRAINT task_files_pkey PRIMARY KEY (id);


--
-- Name: task_resources task_resources_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_resources
    ADD CONSTRAINT task_resources_pkey PRIMARY KEY (id);


--
-- Name: tasks tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tasks
    ADD CONSTRAINT tasks_pkey PRIMARY KEY (id);


--
-- Name: idx_attendance_class_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attendance_class_id ON public.attendance USING btree (class_id);


--
-- Name: idx_attendance_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attendance_date ON public.attendance USING btree (date);


--
-- Name: idx_attendance_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attendance_student_id ON public.attendance USING btree (student_id);


--
-- Name: idx_curriculum_plan_class_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_curriculum_plan_class_id ON public.curriculum_plan USING btree (class_id);


--
-- Name: idx_educational_resources_subject; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_educational_resources_subject ON public.educational_resources USING btree (subject_id);


--
-- Name: idx_educational_resources_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_educational_resources_type ON public.educational_resources USING btree (type);


--
-- Name: idx_notifications_is_read; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notifications_is_read ON public.notifications USING btree (is_read);


--
-- Name: idx_notifications_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notifications_user_id ON public.notifications USING btree (user_id);


--
-- Name: idx_rubric_criteria_task_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_rubric_criteria_task_id ON public.rubric_criteria USING btree (task_id);


--
-- Name: idx_rubric_scores_submission_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_rubric_scores_submission_id ON public.rubric_scores USING btree (submission_id);


--
-- Name: idx_submission_grade_logs_submission_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_submission_grade_logs_submission_id ON public.submission_grade_logs USING btree (submission_id);


--
-- Name: idx_submissions_is_late; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_submissions_is_late ON public.submissions USING btree (is_late);


--
-- Name: idx_submissions_presented; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_submissions_presented ON public.submissions USING btree (presented);


--
-- Name: idx_submissions_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_submissions_status ON public.submissions USING btree (status);


--
-- Name: idx_submissions_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_submissions_student_id ON public.submissions USING btree (student_id);


--
-- Name: idx_submissions_task_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_submissions_task_id ON public.submissions USING btree (task_id);


--
-- Name: idx_task_files_task_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_task_files_task_id ON public.task_files USING btree (task_id);


--
-- Name: idx_task_resources_task_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_task_resources_task_id ON public.task_resources USING btree (task_id);


--
-- Name: idx_tasks_class_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tasks_class_id ON public.tasks USING btree (class_id);


--
-- Name: idx_tasks_created_by; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tasks_created_by ON public.tasks USING btree (created_by);


--
-- Name: idx_tasks_due_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tasks_due_date ON public.tasks USING btree (due_date);


--
-- Name: idx_tasks_is_deleted; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tasks_is_deleted ON public.tasks USING btree (is_deleted);


--
-- Name: idx_tasks_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tasks_status ON public.tasks USING btree (status);


--
-- Name: notifications notifications_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id) ON DELETE CASCADE;


--
-- Name: rubric_criteria rubric_criteria_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rubric_criteria
    ADD CONSTRAINT rubric_criteria_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id) ON DELETE CASCADE;


--
-- Name: rubric_scores rubric_scores_criterion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rubric_scores
    ADD CONSTRAINT rubric_scores_criterion_id_fkey FOREIGN KEY (criterion_id) REFERENCES public.rubric_criteria(id) ON DELETE CASCADE;


--
-- Name: rubric_scores rubric_scores_submission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rubric_scores
    ADD CONSTRAINT rubric_scores_submission_id_fkey FOREIGN KEY (submission_id) REFERENCES public.submissions(id) ON DELETE CASCADE;


--
-- Name: submission_grade_logs submission_grade_logs_submission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submission_grade_logs
    ADD CONSTRAINT submission_grade_logs_submission_id_fkey FOREIGN KEY (submission_id) REFERENCES public.submissions(id) ON DELETE CASCADE;


--
-- Name: submissions submissions_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.submissions
    ADD CONSTRAINT submissions_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id) ON DELETE CASCADE;


--
-- Name: task_files task_files_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_files
    ADD CONSTRAINT task_files_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id) ON DELETE CASCADE;


--
-- Name: task_resources task_resources_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_resources
    ADD CONSTRAINT task_resources_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.tasks(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict 6Ae4GuuCFg1CCkPpe7tn9PH0WZaMX1hkisDfalBW5SsUgwUzELFIb5mSLHH47Qt

