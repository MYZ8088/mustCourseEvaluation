-- 使用PostgreSQL语法创建表

-- 用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  full_name VARCHAR(100),
  student_id VARCHAR(50) UNIQUE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  role VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_STUDENT')),
  can_comment BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT NULL
);

-- 院系表
CREATE TABLE IF NOT EXISTS faculties (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  description TEXT
);

-- 教师表
CREATE TABLE IF NOT EXISTS teachers (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  title VARCHAR(50),
  email VARCHAR(100),
  research_field TEXT,
  achievements TEXT,
  faculty_id BIGINT,
  FOREIGN KEY (faculty_id) REFERENCES faculties (id) ON DELETE SET NULL
);

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  credits DOUBLE PRECISION NOT NULL DEFAULT 3.0,
  description TEXT,
  type VARCHAR(20) NOT NULL CHECK (type IN ('COMPULSORY', 'ELECTIVE')),
  assessment_criteria TEXT,
  faculty_id BIGINT NOT NULL,
  teacher_id BIGINT,
  FOREIGN KEY (faculty_id) REFERENCES faculties (id),
  FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE SET NULL
);

-- 评价表
CREATE TABLE IF NOT EXISTS reviews (
  id BIGSERIAL PRIMARY KEY,
  content TEXT NOT NULL,
  rating INT NOT NULL,
  anonymous BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
  pinned BOOLEAN NOT NULL DEFAULT FALSE,
  user_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
);

-- 评价投票表（点赞/踩）
CREATE TABLE IF NOT EXISTS review_votes (
  id BIGSERIAL PRIMARY KEY,
  review_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  vote_type VARCHAR(10) NOT NULL CHECK (vote_type IN ('LIKE', 'DISLIKE')),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  UNIQUE (review_id, user_id)
);

-- 课程时间表（每门课可有多个上课时间）
-- time_period: 1=09:00-11:50, 2=12:30-15:20, 3=15:30-18:20, 4=19:00-21:50
CREATE TABLE IF NOT EXISTS course_schedules (
  id BIGSERIAL PRIMARY KEY,
  course_id BIGINT NOT NULL,
  day_of_week INT NOT NULL CHECK (day_of_week >= 1 AND day_of_week <= 7),
  time_period INT NOT NULL CHECK (time_period >= 1 AND time_period <= 4),
  location VARCHAR(100),
  FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
  UNIQUE (course_id, day_of_week, time_period)
);

-- 用户课程时间表（用于AI推荐时的时间冲突检测）
CREATE TABLE IF NOT EXISTS user_schedules (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  day_of_week INT NOT NULL CHECK (day_of_week >= 1 AND day_of_week <= 7),
  time_period INT NOT NULL CHECK (time_period >= 1 AND time_period <= 4),
  course_name VARCHAR(100),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  UNIQUE (user_id, day_of_week, time_period)
);
