#!/bin/bash
# Usage: ./agg_desc.sh 2026-03-18

set -euo pipefail

DT=${1:-$(date -d "yesterday" +%F)}
HDFS_BASE=/oj/analysis_desc
TMP_BASE=/tmp/oj_analysis_desc_${DT}
MYSQL_HOST=${MYSQL_HOST:-"127.0.0.1"}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_DB=${MYSQL_DB:-"oj"}
MYSQL_USER=${MYSQL_USER:-"root"}
MYSQL_PASS=${MYSQL_PASS:-"12365478910vr"}

hive --hivevar dt=${DT} -f ./hive_desc_analytics.sql

mkdir -p "${TMP_BASE}"

merge_if_exists() {
  local hdfs_path=$1
  local local_file=$2
  if hdfs dfs -test -e "${hdfs_path}"; then
    hdfs dfs -getmerge "${hdfs_path}" "${local_file}"
  else
    echo "HDFS path not found: ${hdfs_path}"
  fi
}

merge_if_exists "${HDFS_BASE}/language_daily/dt=${DT}" "${TMP_BASE}/language_daily.csv"
merge_if_exists "${HDFS_BASE}/verdict_daily/dt=${DT}" "${TMP_BASE}/verdict_daily.csv"
merge_if_exists "${HDFS_BASE}/hourly_activity/dt=${DT}" "${TMP_BASE}/hourly_activity.csv"
merge_if_exists "${HDFS_BASE}/problem_verdict_daily/dt=${DT}" "${TMP_BASE}/problem_verdict_daily.csv"
merge_if_exists "${HDFS_BASE}/user_language_daily/dt=${DT}" "${TMP_BASE}/user_language_daily.csv"
merge_if_exists "${HDFS_BASE}/user_verdict_daily/dt=${DT}" "${TMP_BASE}/user_verdict_daily.csv"

mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
DELETE FROM analysis_language_daily WHERE dt='${DT}';
DELETE FROM analysis_verdict_daily WHERE dt='${DT}';
DELETE FROM analysis_hourly_activity WHERE dt='${DT}';
DELETE FROM analysis_problem_verdict_daily WHERE dt='${DT}';
DELETE FROM analysis_user_language_daily WHERE dt='${DT}';
DELETE FROM analysis_user_verdict_daily WHERE dt='${DT}';
SQL

if [ -f "${TMP_BASE}/language_daily.csv" ]; then
  mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
LOAD DATA LOCAL INFILE '${TMP_BASE}/language_daily.csv'
INTO TABLE analysis_language_daily
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(dt, language, total, accepted, avg_time_ms, avg_memory_kb);
SQL
fi

if [ -f "${TMP_BASE}/verdict_daily.csv" ]; then
  mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
LOAD DATA LOCAL INFILE '${TMP_BASE}/verdict_daily.csv'
INTO TABLE analysis_verdict_daily
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(dt, verdict, total);
SQL
fi

if [ -f "${TMP_BASE}/hourly_activity.csv" ]; then
  mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
LOAD DATA LOCAL INFILE '${TMP_BASE}/hourly_activity.csv'
INTO TABLE analysis_hourly_activity
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(dt, hour_of_day, total, accepted, active_users);
SQL
fi

if [ -f "${TMP_BASE}/problem_verdict_daily.csv" ]; then
  mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
LOAD DATA LOCAL INFILE '${TMP_BASE}/problem_verdict_daily.csv'
INTO TABLE analysis_problem_verdict_daily
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(dt, problem_id, verdict, total);
SQL
fi

if [ -f "${TMP_BASE}/user_language_daily.csv" ]; then
  mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
LOAD DATA LOCAL INFILE '${TMP_BASE}/user_language_daily.csv'
INTO TABLE analysis_user_language_daily
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(dt, user_id, language, total, accepted);
SQL
fi

if [ -f "${TMP_BASE}/user_verdict_daily.csv" ]; then
  mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
LOAD DATA LOCAL INFILE '${TMP_BASE}/user_verdict_daily.csv'
INTO TABLE analysis_user_verdict_daily
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(dt, user_id, verdict, total);
SQL
fi
