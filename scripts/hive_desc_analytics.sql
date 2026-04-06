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

MSCK REPAIR TABLE oj.analysis_events;-- 刷新分区列表

INSERT OVERWRITE DIRECTORY '/oj/analysis_desc/language_daily/dt=${hivevar:dt}'-- 将select结果写入到hdfs此目录中
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
SELECT
  '${hivevar:dt}' AS dt,
  language,
  COUNT(1) AS total,
  COALESCE(SUM(CASE WHEN verdict='AC' THEN 1 ELSE 0 END), 0) AS accepted,
  AVG(time_ms) AS avg_time_ms,
  AVG(memory_kb) AS avg_memory_kb
FROM oj.analysis_events
WHERE dt='${hivevar:dt}'
GROUP BY language;

INSERT OVERWRITE DIRECTORY '/oj/analysis_desc/verdict_daily/dt=${hivevar:dt}'
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
SELECT
  '${hivevar:dt}' AS dt,
  verdict,
  COUNT(1) AS total
FROM oj.analysis_events
WHERE dt='${hivevar:dt}'
GROUP BY verdict;

INSERT OVERWRITE DIRECTORY '/oj/analysis_desc/hourly_activity/dt=${hivevar:dt}'
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
SELECT
  '${hivevar:dt}' AS dt,
  CAST(SUBSTR(created_at, 12, 2) AS INT) AS hour_of_day,-- 截取小时,标准时间格式:2023-10-01 15:30:45
  COUNT(1) AS total,
  COALESCE(SUM(CASE WHEN verdict='AC' THEN 1 ELSE 0 END), 0) AS accepted,
  COUNT(DISTINCT user_id) AS active_users-- 在这个小时内，共有多少个不同的用户提交过代码
FROM oj.analysis_events
WHERE dt='${hivevar:dt}'
GROUP BY CAST(SUBSTR(created_at, 12, 2) AS INT);

INSERT OVERWRITE DIRECTORY '/oj/analysis_desc/problem_verdict_daily/dt=${hivevar:dt}'
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
SELECT
  '${hivevar:dt}' AS dt,
  problem_id,
  verdict,
  COUNT(1) AS total
FROM oj.analysis_events
WHERE dt='${hivevar:dt}'
GROUP BY problem_id, verdict;

INSERT OVERWRITE DIRECTORY '/oj/analysis_desc/user_language_daily/dt=${hivevar:dt}'
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
SELECT
  '${hivevar:dt}' AS dt,
  user_id,
  language,
  COUNT(1) AS total,
  COALESCE(SUM(CASE WHEN verdict='AC' THEN 1 ELSE 0 END), 0) AS accepted
FROM oj.analysis_events
WHERE dt='${hivevar:dt}'
GROUP BY user_id, language;

INSERT OVERWRITE DIRECTORY '/oj/analysis_desc/user_verdict_daily/dt=${hivevar:dt}'
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
SELECT
  '${hivevar:dt}' AS dt,
  user_id,
  verdict,
  COUNT(1) AS total
FROM oj.analysis_events
WHERE dt='${hivevar:dt}'
GROUP BY user_id, verdict;
