#!/bin/bash
# Usage: ./agg_daily.sh 2026-03-18

set -euo pipefail

DT=${1:-$(date -d "yesterday" +%F)} #获取昨天的时间,格式化输出,执行命令并把结果拿出来
HDFS_BASE=/oj/analysis_agg
TMP_BASE=/tmp/oj_analysis_agg_${DT}
MYSQL_HOST=${MYSQL_HOST:-"127.0.0.1"}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_DB=${MYSQL_DB:-"oj"}
MYSQL_USER=${MYSQL_USER:-"root"}
MYSQL_PASS=${MYSQL_PASS:-"12365478910vr"}

hive --hivevar dt=${DT} -f ./hive_agg_daily.sql

mkdir -p "${TMP_BASE}"

#getmerge：把指定 HDFS 目录下的所有小文件读出来，合并成一个大文件，然后下载到本地服务器

if hdfs dfs -test -e ${HDFS_BASE}/summary/dt=${DT}; then #test测试 e参数表示exist
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

# --local-infile:允许 MySQL 从本地文件加载数据
#<<SQL:作为输入流传给mysql执行
#在插入今天新算出来的结果之前，先删掉 MySQL 表中同一个日期的旧数据

mysql --local-infile=1 -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} ${MYSQL_DB} <<SQL
DELETE FROM analysis_summary_daily WHERE dt='${DT}';
DELETE FROM analysis_problem_daily WHERE dt='${DT}';
DELETE FROM analysis_user_daily WHERE dt='${DT}';
SQL

#load data local infile:从指定的文件中导入数据

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
