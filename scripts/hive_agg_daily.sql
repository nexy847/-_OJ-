USE oj;

CREATE DATABASE IF NOT EXISTS oj;

CREATE EXTERNAL TABLE IF NOT EXISTS oj.analysis_events (
  submission_id BIGINT,
  user_id BIGINT,
  problem_id BIGINT,
  language STRING,
  verdict STRING,
  time_ms BIGINT,
  memory_kb BIGINT,
  created_at STRING
)
PARTITIONED BY (dt STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
STORED AS TEXTFILE
LOCATION '/oj/analysis'
TBLPROPERTIES ('skip.header.line.count'='1');

MSCK REPAIR TABLE oj.analysis_events;

INSERT OVERWRITE DIRECTORY '/oj/analysis_agg/summary/dt=${hivevar:dt}'
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
SELECT
  '${hivevar:dt}' AS dt,
  COUNT(1) AS total,
  COALESCE(SUM(CASE WHEN verdict='AC' THEN 1 ELSE 0 END),0) AS accepted,
  AVG(time_ms) AS avg_time_ms,
  AVG(memory_kb) AS avg_memory_kb
FROM oj.analysis_events
WHERE dt='${hivevar:dt}';

INSERT OVERWRITE DIRECTORY '/oj/analysis_agg/problem_daily/dt=${hivevar:dt}'
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
SELECT
  '${hivevar:dt}' AS dt,
  problem_id,
  COUNT(1) AS total,
  COALESCE(SUM(CASE WHEN verdict='AC' THEN 1 ELSE 0 END),0) AS accepted,
  AVG(time_ms) AS avg_time_ms,
  AVG(memory_kb) AS avg_memory_kb
FROM oj.analysis_events
WHERE dt='${hivevar:dt}'
GROUP BY problem_id;

INSERT OVERWRITE DIRECTORY '/oj/analysis_agg/user_daily/dt=${hivevar:dt}'
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
SELECT
  '${hivevar:dt}' AS dt,
  user_id,
  COUNT(1) AS total,
  COALESCE(SUM(CASE WHEN verdict='AC' THEN 1 ELSE 0 END),0) AS accepted,
  AVG(time_ms) AS avg_time_ms,
  AVG(memory_kb) AS avg_memory_kb
FROM oj.analysis_events
WHERE dt='${hivevar:dt}'
GROUP BY user_id;
