CREATE DATABASE IF NOT EXISTS oj DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oj;

CREATE TABLE IF NOT EXISTS analysis_summary_daily (
  dt VARCHAR(10) NOT NULL,
  total BIGINT NOT NULL,
  accepted BIGINT NOT NULL,
  avg_time_ms DOUBLE,
  avg_memory_kb DOUBLE,
  PRIMARY KEY (dt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analysis_problem_daily (
  dt VARCHAR(10) NOT NULL,
  problem_id BIGINT NOT NULL,
  total BIGINT NOT NULL,
  accepted BIGINT NOT NULL,
  avg_time_ms DOUBLE,
  avg_memory_kb DOUBLE,
  PRIMARY KEY (dt, problem_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analysis_user_daily (
  dt VARCHAR(10) NOT NULL,
  user_id BIGINT NOT NULL,
  total BIGINT NOT NULL,
  accepted BIGINT NOT NULL,
  avg_time_ms DOUBLE,
  avg_memory_kb DOUBLE,
  PRIMARY KEY (dt, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
