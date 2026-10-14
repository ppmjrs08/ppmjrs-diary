-- M2 integration contract. Review with the database member before applying.
-- No DROP, no shared user-table changes, no automatic application at startup.
CREATE TABLE IF NOT EXISTS m2_course (
 id VARCHAR(32) PRIMARY KEY,
 title VARCHAR(200) NOT NULL,
 description VARCHAR(2000) NOT NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS m2_prerequisite (
 course_id VARCHAR(32) NOT NULL,
 prerequisite_id VARCHAR(32) NOT NULL,
 PRIMARY KEY(course_id,prerequisite_id),
 FOREIGN KEY(course_id) REFERENCES m2_course(id),
 FOREIGN KEY(prerequisite_id) REFERENCES m2_course(id),
 CHECK(course_id<>prerequisite_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS m2_completed (
 student_id VARCHAR(64) NOT NULL,
 course_id VARCHAR(32) NOT NULL,
 PRIMARY KEY(student_id,course_id),
 FOREIGN KEY(course_id) REFERENCES m2_course(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS m2_profile (
 student_id VARCHAR(64) PRIMARY KEY,
 interests VARCHAR(500) NOT NULL,
 goals VARCHAR(300) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS m2_history (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 student_id VARCHAR(64) NOT NULL,
 query_text VARCHAR(801) NOT NULL,
 results_json TEXT NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 INDEX ix_m2_history_student(student_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
