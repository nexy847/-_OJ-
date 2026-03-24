#!/bin/bash
# Usage: ./agg_daily.sh 2026-03-18

set -euo pipefail

DT=${1:-$(date -d "yesterday" +%F)}
HDFS_BASE=/oj/analysis_agg
TMP_BASE=/tmp/oj_analysis_agg_${DT}
MYSQL_HOST=${MYSQL_HOST:-"127.0.0.1"}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_DB=${MYSQL_DB:-"oj"}
MYSQL_USER=${MYSQL_USER:-"root"}
MYSQL_PASS=${MYSQL_PASS:-"12365478910vr"}

hive --hivevar dt=${DT} -f ./hive_agg_daily.sql

mkdir -p "${TMP_BASE}"

if hdfs dfs -test -e ${HDFS_BASE}/summary/dt=${DT}; then
  hdfs dfs -getmerge ${HDFS_BASE}/summary/dt=${DT} ${TMP_BASE}/summary.csv
else
  echo "HDFS path not found: ${HDFS_BASE}/summary/dt=${DT}"
fi

if hdfs dfs -test -e ${HDFS_BASE}/problem_daily/dt=${DT}; then
  hdfs dfs -getmerge ${HDFS_BASE}/problem_daily/dt=${DT} ${TMP_BASE}/problem_daily.csv
else
  echo "HDFS path not found: ${HDFS_BASE}/problem_daily/dt=${DT}"
fi

if hdfs dfs -test -e ${HDFS_BASE}/user_daily/dt=${DT}; then
  hdfs dfs -getmerge ${HDFS_BASE}/user_daily/dt=${DT} ${TMP_BASE}/user_daily.csv
else
  echo "HDFS path not found: ${HDFS_BASE}/user_daily/dt=${DT}"
fi

mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
DELETE FROM analysis_summary_daily WHERE dt='${DT}';
DELETE FROM analysis_problem_daily WHERE dt='${DT}';
DELETE FROM analysis_user_daily WHERE dt='${DT}';
SQL

if [ -f "${TMP_BASE}/summary.csv" ]; then
  mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
LOAD DATA LOCAL INFILE '${TMP_BASE}/summary.csv'
INTO TABLE analysis_summary_daily
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(dt, total, accepted, avg_time_ms, avg_memory_kb);
SQL
fi

if [ -f "${TMP_BASE}/problem_daily.csv" ]; then
  mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
LOAD DATA LOCAL INFILE '${TMP_BASE}/problem_daily.csv'
INTO TABLE analysis_problem_daily
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(dt, problem_id, total, accepted, avg_time_ms, avg_memory_kb);
SQL
fi

if [ -f "${TMP_BASE}/user_daily.csv" ]; then
  mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
LOAD DATA LOCAL INFILE '${TMP_BASE}/user_daily.csv'
INTO TABLE analysis_user_daily
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(dt, user_id, total, accepted, avg_time_ms, avg_memory_kb);
SQL
fi
