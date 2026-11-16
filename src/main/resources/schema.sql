-- ===================================================================
-- EduCore Student Management System - Relational Database Schema
-- Ready for SQLite, MySQL, PostgreSQL, or H2 database integration
-- ===================================================================

CREATE TABLE IF NOT EXISTS students (
    id VARCHAR(20) NOT NULL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(30),
    date_of_birth VARCHAR(20),
    gender VARCHAR(20),
    department VARCHAR(50) NOT NULL,
    academic_year INT NOT NULL,
    gpa DOUBLE NOT NULL,
    status VARCHAR(30) NOT NULL,
    enrollment_date VARCHAR(20),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_student_gpa CHECK (gpa >= 0.00 AND gpa <= 4.00),
    CONSTRAINT chk_student_year CHECK (academic_year >= 1 AND academic_year <= 6)
);

-- Performance indices for fast queries and joins
CREATE UNIQUE INDEX IF NOT EXISTS idx_student_email ON students (email);
CREATE INDEX IF NOT EXISTS idx_student_dept ON students (department);
CREATE INDEX IF NOT EXISTS idx_student_status ON students (status);

-- Seed Initial Dataset
INSERT OR IGNORE INTO students (id, first_name, last_name, email, phone, date_of_birth, gender, department, academic_year, gpa, status, enrollment_date, address)
VALUES
('STU-1001', 'Emma', 'Watson', 'emma.watson@university.edu', '+1 (555) 234-5678', '2003-04-15', 'Female', 'COMPUTER_SCIENCE', 3, 3.88, 'ACTIVE', '2023-09-01', '742 Evergreen Terrace, Seattle, WA'),
('STU-1002', 'Alexander', 'Hayes', 'alex.hayes@university.edu', '+1 (555) 345-6789', '2002-11-20', 'Male', 'DATA_SCIENCE', 4, 3.95, 'ACTIVE', '2022-09-01', '10880 Wilshire Blvd, Los Angeles, CA'),
('STU-1003', 'Sophia', 'Chen', 'sophia.chen@university.edu', '+1 (555) 456-7890', '2004-02-10', 'Female', 'ELECTRICAL_ENG', 2, 3.72, 'ACTIVE', '2024-09-01', '450 Serra Mall, Stanford, CA'),
('STU-1004', 'Marcus', 'Johnson', 'marcus.j@university.edu', '+1 (555) 567-8901', '2001-08-30', 'Male', 'BUSINESS_ADMIN', 4, 3.45, 'GRADUATED', '2021-09-01', '200 Park Ave, New York, NY'),
('STU-1005', 'Olivia', 'Martinez', 'olivia.m@university.edu', '+1 (555) 678-9012', '2003-07-25', 'Female', 'BIOMEDICAL', 3, 3.65, 'ACTIVE', '2023-09-01', '300 Pasteur Dr, Palo Alto, CA'),
('STU-1006', 'Lucas', 'Kim', 'lucas.kim@university.edu', '+1 (555) 789-0123', '2004-12-05', 'Male', 'COMPUTER_SCIENCE', 1, 3.20, 'ACTIVE', '2025-01-15', '123 Tech Way, Austin, TX'),
('STU-1007', 'Isabella', 'Taylor', 'isabella.t@university.edu', '+1 (555) 890-1234', '2002-03-18', 'Female', 'MECHANICAL_ENG', 4, 2.85, 'INACTIVE', '2022-09-01', '555 Campus Drive, Boston, MA'),
('STU-1008', 'David', 'Miller', 'david.m@university.edu', '+1 (555) 901-2345', '2003-09-14', 'Male', 'CIVIL_ENG', 2, 3.10, 'ACTIVE', '2024-09-01', '88 University Ave, Denver, CO'),
('STU-1009', 'Aria', 'Patel', 'aria.patel@university.edu', '+1 (555) 012-3456', '2004-05-22', 'Female', 'DATA_SCIENCE', 2, 4.00, 'ACTIVE', '2024-09-01', '700 Forbes Ave, Pittsburgh, PA'),
('STU-1010', 'Noah', 'Williams', 'noah.w@university.edu', '+1 (555) 123-4567', '2002-06-12', 'Male', 'BUSINESS_ADMIN', 3, 2.40, 'SUSPENDED', '2023-09-01', '401 Terry Ave N, Chicago, IL'),
('STU-1011', 'Chloe', 'Garcia', 'chloe.g@university.edu', '+1 (555) 234-8765', '2003-10-08', 'Female', 'COMPUTER_SCIENCE', 3, 3.78, 'ACTIVE', '2023-09-01', '100 Bay State Rd, San Jose, CA'),
('STU-1012', 'Ethan', 'Wright', 'ethan.w@university.edu', '+1 (555) 345-9876', '2001-01-19', 'Male', 'ELECTRICAL_ENG', 4, 3.52, 'GRADUATED', '2021-09-01', '77 Massachusetts Ave, Cambridge, MA');
