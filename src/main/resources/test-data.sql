-- =============================================
-- Test data for timetable database
-- 25 records per entity
-- Execution order matters due to foreign keys
-- =============================================

-- =============================================
-- 1. USERS (1 admin + 24 teachers)
-- Password for all: "password123" (BCrypt hash)
-- =============================================
INSERT INTO users (first_name, last_name, email, password_hash, role, phone)
VALUES
    ('Admin', 'Adminov', 'admin@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', '+7-900-000-00-01'),
    ('Ivan', 'Petrov', 'i.petrov@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-01'),
    ('Maria', 'Sidorova', 'm.sidorova@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-02'),
    ('Alexey', 'Kuznetsov', 'a.kuznetsov@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-03'),
    ('Elena', 'Popova', 'e.popova@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-04'),
    ('Dmitry', 'Volkov', 'd.volkov@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-05'),
    ('Olga', 'Novikova', 'o.novikova@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-06'),
    ('Sergey', 'Morozov', 's.morozov@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-07'),
    ('Anna', 'Pavlova', 'a.pavlova@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-08'),
    ('Nikolay', 'Sokolov', 'n.sokolov@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-09'),
    ('Tatiana', 'Mikhailova', 't.mikhailova@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-10'),
    ('Andrey', 'Fedorov', 'a.fedorov@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-11'),
    ('Natalia', 'Orlova', 'n.orlova@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-12'),
    ('Viktor', 'Zaytsev', 'v.zaytsev@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-13'),
    ('Irina', 'Belyaeva', 'i.belyaeva@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-14'),
    ('Pavel', 'Nikitin', 'p.nikitin@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-15'),
    ('Svetlana', 'Komarova', 's.komarova@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-16'),
    ('Roman', 'Titov', 'r.titov@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-17'),
    ('Yulia', 'Egorova', 'y.egorova@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-18'),
    ('Maksim', 'Krylov', 'm.krylov@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-19'),
    ('Ekaterina', 'Romanova', 'e.romanova@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-20'),
    ('Artem', 'Gusev', 'a.gusev@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-21'),
    ('Valentina', 'Lebedeva', 'v.lebedeva@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-22'),
    ('Kirill', 'Zhukov', 'k.zhukov@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-23'),
    ('Marina', 'Frolova', 'm.frolova@timetable.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'TEACHER', '+7-900-111-00-24');


-- =============================================
-- 2. STUDENT_GROUPS (25 groups)
-- =============================================
INSERT INTO student_groups (name, course_year, student_count)
VALUES
    ('IVT-11', 1, 25),
    ('IVT-12', 1, 28),
    ('IVT-21', 2, 22),
    ('IVT-22', 2, 27),
    ('IVT-31', 3, 20),
    ('PI-11', 1, 30),
    ('PI-12', 1, 26),
    ('PI-21', 2, 24),
    ('PI-22', 2, 23),
    ('PI-31', 3, 19),
    ('IS-11', 1, 32),
    ('IS-12', 1, 29),
    ('IS-21', 2, 21),
    ('IS-22', 2, 26),
    ('IS-31', 3, 18),
    ('MT-11', 1, 35),
    ('MT-12', 1, 33),
    ('MT-21', 2, 28),
    ('MT-22', 2, 30),
    ('MT-31', 3, 25),
    ('EK-11', 1, 31),
    ('EK-12', 1, 27),
    ('EK-21', 2, 24),
    ('EK-22', 2, 22),
    ('EK-31', 3, 20);


-- =============================================
-- 3. SUBJECTS (25 subjects)
-- =============================================
INSERT INTO subjects (name, code, faculty, description)
VALUES
    ('Higher Mathematics', 'MATH-101', 'Faculty of Mathematics', 'Calculus course for 1st year students'),
    ('Linear Algebra', 'MATH-102', 'Faculty of Mathematics', 'Fundamentals of linear algebra'),
    ('Mathematical Analysis', 'MATH-103', 'Faculty of Mathematics', 'Differential and integral calculus'),
    ('Physics', 'PHYS-101', 'Faculty of Physics', 'General physics course'),
    ('Mechanics', 'PHYS-102', 'Faculty of Physics', 'Theoretical mechanics'),
    ('Programming', 'CS-101', 'Faculty of Computer Science', 'Java programming fundamentals'),
    ('Algorithms and Data Structures', 'CS-102', 'Faculty of Computer Science', 'Algorithm design and analysis'),
    ('Databases', 'CS-201', 'Faculty of Computer Science', 'Database design and management'),
    ('Operating Systems', 'CS-202', 'Faculty of Computer Science', 'OS principles and architecture'),
    ('Computer Networks', 'CS-203', 'Faculty of Computer Science', 'Networking fundamentals'),
    ('Engineering Graphics', 'ENG-101', 'Engineering Faculty', 'Drafting and CAD basics'),
    ('Strength of Materials', 'ENG-102', 'Engineering Faculty', 'Structural mechanics'),
    ('Electrical Engineering', 'ENG-201', 'Engineering Faculty', 'Electrical engineering fundamentals'),
    ('Economics', 'ECON-101', 'Faculty of Economics', 'Micro and macroeconomics'),
    ('Accounting', 'ECON-102', 'Faculty of Economics', 'Accounting principles'),
    ('Philosophy', 'HUM-101', 'Faculty of Humanities', 'Fundamentals of philosophical thinking'),
    ('History', 'HUM-102', 'Faculty of Humanities', 'Russian and world history'),
    ('Foreign Language', 'HUM-103', 'Faculty of Humanities', 'Technical English'),
    ('Discrete Mathematics', 'CS-103', 'Faculty of Computer Science', 'Graph theory, logic, combinatorics'),
    ('Probability Theory', 'MATH-201', 'Faculty of Mathematics', 'Probability and statistics'),
    ('Web Development', 'CS-301', 'Faculty of Computer Science', 'HTML, CSS, JavaScript, React'),
    ('Machine Learning', 'CS-302', 'Faculty of Computer Science', 'ML and neural network basics'),
    ('Information Security', 'CS-303', 'Faculty of Computer Science', 'Cybersecurity fundamentals'),
    ('Physical Education', 'PE-101', 'Faculty of Physical Education', 'General physical training'),
    ('Law', 'HUM-201', 'Faculty of Humanities', 'Legal fundamentals');


-- =============================================
-- 4. ROOMS (25 rooms)
-- =============================================
INSERT INTO rooms (room_number, building, capacity)
VALUES
    ('101', 'Main Building', 50),
    ('102', 'Main Building', 40),
    ('103', 'Main Building', 35),
    ('201', 'Main Building', 30),
    ('202', 'Main Building', 45),
    ('301', 'Main Building', 55),
    ('302', 'Main Building', 60),
    ('101', 'Building B', 35),
    ('102', 'Building B', 40),
    ('201', 'Building B', 30),
    ('202', 'Building B', 25),
    ('301', 'Building B', 50),
    ('101', 'Building V', 45),
    ('102', 'Building V', 35),
    ('201', 'Building V', 30),
    ('202', 'Building V', 40),
    ('301', 'Building V', 55),
    ('101', 'Lab Building', 20),
    ('102', 'Lab Building', 25),
    ('201', 'Lab Building', 30),
    ('101', 'Sports Building', 100),
    ('102', 'Sports Building', 50),
    ('201', 'Sports Building', 40),
    ('101', 'Building G', 35),
    ('102', 'Building G', 45);


-- =============================================
-- 5. LESSONS (25 lessons)
-- start_at: Monday March 9 2026, different time slots
-- end_at: 1.5 hours after start
-- rule_type: WEEKLY / BIWEEKLY_EVEN / BIWEEKLY_ODD / NULL
-- =============================================
INSERT INTO lessons (start_at, end_at, room_id, subject_id, teacher_id, rule_type, is_override, is_cancelled)
VALUES
    -- Monday, 1st period 09:00-10:30
    ('2026-03-09 09:00:00', '2026-03-09 10:30:00', 1, 1, 2, 'WEEKLY', false, false),
    -- Monday, 2nd period 10:45-12:15
    ('2026-03-09 10:45:00', '2026-03-09 12:15:00', 2, 2, 3, 'WEEKLY', false, false),
    -- Monday, 3rd period 13:00-14:30
    ('2026-03-09 13:00:00', '2026-03-09 14:30:00', 3, 6, 4, 'BIWEEKLY_EVEN', false, false),
    -- Tuesday, 1st period
    ('2026-03-10 09:00:00', '2026-03-10 10:30:00', 4, 7, 5, 'WEEKLY', false, false),
    -- Tuesday, 2nd period
    ('2026-03-10 10:45:00', '2026-03-10 12:15:00', 5, 4, 6, 'WEEKLY', false, false),
    -- Tuesday, 3rd period
    ('2026-03-10 13:00:00', '2026-03-10 14:30:00', 6, 8, 7, 'BIWEEKLY_ODD', false, false),
    -- Wednesday, 1st period
    ('2026-03-11 09:00:00', '2026-03-11 10:30:00', 7, 3, 8, 'WEEKLY', false, false),
    -- Wednesday, 2nd period
    ('2026-03-11 10:45:00', '2026-03-11 12:15:00', 8, 9, 9, 'WEEKLY', false, false),
    -- Wednesday, 3rd period
    ('2026-03-11 13:00:00', '2026-03-11 14:30:00', 9, 10, 10, 'BIWEEKLY_EVEN', false, false),
    -- Thursday, 1st period
    ('2026-03-12 09:00:00', '2026-03-12 10:30:00', 10, 13, 11, 'WEEKLY', false, false),
    -- Thursday, 2nd period
    ('2026-03-12 10:45:00', '2026-03-12 12:15:00', 11, 14, 12, 'WEEKLY', false, false),
    -- Thursday, 3rd period
    ('2026-03-12 13:00:00', '2026-03-12 14:30:00', 12, 15, 13, 'BIWEEKLY_ODD', false, false),
    -- Friday, 1st period
    ('2026-03-13 09:00:00', '2026-03-13 10:30:00', 13, 16, 14, 'WEEKLY', false, false),
    -- Friday, 2nd period
    ('2026-03-13 10:45:00', '2026-03-13 12:15:00', 14, 17, 15, 'WEEKLY', false, false),
    -- Friday, 3rd period
    ('2026-03-13 13:00:00', '2026-03-13 14:30:00', 15, 18, 16, 'BIWEEKLY_EVEN', false, false),
    -- Next Monday, 4th period 14:45-16:15
    ('2026-03-16 14:45:00', '2026-03-16 16:15:00', 16, 19, 17, 'WEEKLY', false, false),
    -- Tuesday, 4th period
    ('2026-03-17 14:45:00', '2026-03-17 16:15:00', 17, 20, 18, 'WEEKLY', false, false),
    -- Wednesday, 4th period
    ('2026-03-18 14:45:00', '2026-03-18 16:15:00', 18, 21, 19, 'BIWEEKLY_ODD', false, false),
    -- Thursday, 4th period
    ('2026-03-19 14:45:00', '2026-03-19 16:15:00', 19, 22, 20, 'WEEKLY', false, false),
    -- Friday, 4th period
    ('2026-03-20 14:45:00', '2026-03-20 16:15:00', 20, 23, 21, 'WEEKLY', false, false),
    -- Cancelled lesson
    ('2026-03-16 09:00:00', '2026-03-16 10:30:00', 21, 24, 22, 'WEEKLY', false, true),
    -- Override lesson
    ('2026-03-16 10:45:00', '2026-03-16 12:15:00', 22, 25, 23, NULL, true, false),
    -- Lab, Building B
    ('2026-03-17 09:00:00', '2026-03-17 10:30:00', 23, 5, 24, 'WEEKLY', false, false),
    -- Practice, Main Building
    ('2026-03-18 09:00:00', '2026-03-18 10:30:00', 24, 11, 25, 'BIWEEKLY_EVEN', false, false),
    -- Seminar, Building G
    ('2026-03-19 09:00:00', '2026-03-19 10:30:00', 25, 12, 2, 'WEEKLY', false, false);


-- =============================================
-- 6. LESSON_STUDENT_GROUPS (linking lessons to groups)
-- Each lesson linked to 1-3 groups
-- =============================================
INSERT INTO lesson_student_groups (lesson_id, group_id)
VALUES
    (1, 1), (1, 2),
    (2, 3), (2, 4),
    (3, 5), (3, 6),
    (4, 7), (4, 8),
    (5, 9), (5, 10),
    (6, 11), (6, 12),
    (7, 13), (7, 14),
    (8, 15), (8, 16),
    (9, 17), (9, 18),
    (10, 19), (10, 20),
    (11, 21), (11, 22),
    (12, 23), (12, 24),
    (13, 25), (13, 1),
    (14, 2), (14, 3),
    (15, 4),
    (16, 5), (16, 6), (16, 7),
    (17, 8), (17, 9),
    (18, 10),
    (19, 11), (19, 12), (19, 13),
    (20, 14), (20, 15),
    (21, 16),
    (22, 17), (22, 18),
    (23, 19),
    (24, 20), (24, 21),
    (25, 22), (25, 23);


-- =============================================
-- 7. DAY_COMMENTS (25 comments)
-- =============================================
INSERT INTO day_comments (date, user_id, comment_text, is_deleted)
VALUES
    ('2026-03-09', 1, 'Start of the week - no schedule changes', false),
    ('2026-03-10', 2, 'Teacher substitution for IVT-11 3rd period', false),
    ('2026-03-11', 3, 'Technical break in Building B from 12:15 to 13:00', false),
    ('2026-03-12', 4, 'Room 201 closed for repairs until end of week', false),
    ('2026-03-13', 5, 'Open Day - classes end at 12:15', false),
    ('2026-03-16', 6, 'Holiday - no classes', false),
    ('2026-03-17', 7, 'Classes moved from room 101 to 302', false),
    ('2026-03-18', 8, 'Physics lab sessions cancelled', false),
    ('2026-03-19', 9, 'Additional consultations on Higher Mathematics', false),
    ('2026-03-20', 10, 'Ventilation check in Main Building', false),
    ('2026-03-23', 11, 'Schedule change for PI-21 group', false),
    ('2026-03-24', 12, 'Information Security seminar in room 301', false),
    ('2026-03-25', 13, 'Building V - hot water shutdown', false),
    ('2026-03-26', 14, 'Added 5th period for MT-11 group', false),
    ('2026-03-27', 15, 'Department meeting - no classes after 14:30', false),
    ('2026-03-30', 16, 'Spring exam session begins', false),
    ('2026-03-31', 17, 'Consultation schedule published on website', false),
    ('2026-04-01', 18, 'Room 102 - projector repair', false),
    ('2026-04-02', 19, 'Extra Machine Learning session added', false),
    ('2026-04-03', 20, 'Room change for IS-31 to room 201', false),
    ('2026-04-06', 21, 'Start of second week after vacation', false),
    ('2026-04-07', 22, 'Substitution: Physics replaced by CS for EK-11', false),
    ('2026-04-08', 23, 'Technical maintenance of schedule servers', false),
    ('2026-04-09', 24, 'Old comment removed', true),
    ('2026-04-10', 25, 'Schedule update for next semester', false);
