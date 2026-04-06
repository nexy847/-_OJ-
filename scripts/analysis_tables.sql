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

CREATE TABLE IF NOT EXISTS analysis_language_daily (
  dt VARCHAR(10) NOT NULL,
  language VARCHAR(16) NOT NULL,
  total BIGINT NOT NULL,
  accepted BIGINT NOT NULL,
  avg_time_ms DOUBLE,
  avg_memory_kb DOUBLE,
  PRIMARY KEY (dt, language)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analysis_verdict_daily (
  dt VARCHAR(10) NOT NULL,
  verdict VARCHAR(16) NOT NULL,
  total BIGINT NOT NULL,
  PRIMARY KEY (dt, verdict)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analysis_hourly_activity (
  dt VARCHAR(10) NOT NULL,
  hour_of_day INT NOT NULL,
  total BIGINT NOT NULL,
  accepted BIGINT NOT NULL,
  active_users BIGINT NOT NULL,
  PRIMARY KEY (dt, hour_of_day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analysis_problem_verdict_daily (
  dt VARCHAR(10) NOT NULL,
  problem_id BIGINT NOT NULL,
  verdict VARCHAR(16) NOT NULL,
  total BIGINT NOT NULL,
  PRIMARY KEY (dt, problem_id, verdict)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analysis_user_language_daily (
  dt VARCHAR(10) NOT NULL,
  user_id BIGINT NOT NULL,
  language VARCHAR(16) NOT NULL,
  total BIGINT NOT NULL,
  accepted BIGINT NOT NULL,
  PRIMARY KEY (dt, user_id, language)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analysis_user_verdict_daily (
  dt VARCHAR(10) NOT NULL,
  user_id BIGINT NOT NULL,
  verdict VARCHAR(16) NOT NULL,
  total BIGINT NOT NULL,
  PRIMARY KEY (dt, user_id, verdict)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analysis_submission_forecast_daily (
  id BIGINT NOT NULL AUTO_INCREMENT,
  forecast_date VARCHAR(10) NOT NULL,
  target_date VARCHAR(10) NOT NULL,
  predicted_submissions INT NOT NULL,
  model_name VARCHAR(64) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_forecast_target_model (forecast_date, target_date, model_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS analysis_problem_difficulty_daily (
  dt VARCHAR(10) NOT NULL,
  problem_id BIGINT NOT NULL,
  total_submissions BIGINT NOT NULL,
  accepted_submissions BIGINT NOT NULL,
  ac_rate DOUBLE NOT NULL,
  avg_time_ms DOUBLE NOT NULL,
  avg_memory_kb DOUBLE NOT NULL,
  avg_attempts_per_user DOUBLE NOT NULL,
  wa_rate DOUBLE NOT NULL,
  re_rate DOUBLE NOT NULL,
  tle_rate DOUBLE NOT NULL,
  difficulty_score DOUBLE NOT NULL,
  difficulty_label VARCHAR(16) NOT NULL,
  model_name VARCHAR(64) NOT NULL,
  PRIMARY KEY (dt, problem_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
