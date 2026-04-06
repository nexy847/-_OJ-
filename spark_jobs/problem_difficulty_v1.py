import argparse
from pyspark.sql import SparkSession
from pyspark.sql import functions as F
from pyspark.sql.window import Window

MODEL_NAME = "rule_based_v1"


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--date", required=True, help="Target dt in YYYY-MM-DD")
    parser.add_argument("--mysql-host", default="192.168.1.52")
    parser.add_argument("--mysql-port", default="3306")
    parser.add_argument("--mysql-db", default="oj")
    parser.add_argument("--mysql-user", default="root")
    parser.add_argument("--mysql-pass", default="12365478910vr")
    return parser.parse_args()

#使用了 PySpark 的 functions（通常简写为 F）来构建条件分支
#如果这一列的值小于 0，就强行把它变成 0.0
#如果这一列的值大于 1，就强行把它变成 1.0
#如果值在 0 和 1 之间（正常范围），则保持原样不变

def clamp01(col):
    return F.when(col < F.lit(0.0), F.lit(0.0)).when(col > F.lit(1.0), F.lit(1.0)).otherwise(col)


args = parse_args()
mysql_url = (
    f"jdbc:mysql://{args.mysql_host}:{args.mysql_port}/{args.mysql_db}"
    "?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8"
)
#给这个任务起名，开启对hive的支持，获取或创建（节省资源）
spark = SparkSession.builder.appName("problem-difficulty-v1").enableHiveSupport().getOrCreate()

base_df = spark.sql(f"""
    SELECT
      dt,
      problem_id,
      user_id,
      verdict,
      COALESCE(time_ms, 0) AS time_ms,
      COALESCE(memory_kb, 0) AS memory_kb
    FROM oj.analysis_events
    WHERE dt = '{args.date}'
""")
#看第一行数据是否存在，如果没有，那后面就都没有
if base_df.limit(1).count() == 0:
    raise RuntimeError(f"No analysis_events rows found for dt={args.date}")

#lit 是 literal，用 F.lit(1) 把数字包装成一个“值为1的伪列”（因为pyspark不识别不是列的对象）
agg_df = base_df.groupBy("dt", "problem_id").agg(
    F.count(F.lit(1)).alias("total_submissions"),#统计总数
    F.sum(F.when(F.col("verdict") == "AC", 1).otherwise(0)).alias("accepted_submissions"),#检查每一行数据的 verdict 这一列的值是否等于字符串 "AC"，是就为1。把上面转换出来的所有 1 和 0 全部加起来
    F.avg("time_ms").alias("avg_time_ms"),
    F.avg("memory_kb").alias("avg_memory_kb"),
    F.countDistinct("user_id").alias("active_users"),
    F.sum(F.when(F.col("verdict") == "WA", 1).otherwise(0)).alias("wa_total"),
    F.sum(F.when(F.col("verdict") == "RE", 1).otherwise(0)).alias("re_total"),
    F.sum(F.when(F.col("verdict") == "TLE", 1).otherwise(0)).alias("tle_total")
)

metrics_df = agg_df.select(
    F.col("dt"),
    F.col("problem_id"),
    F.col("total_submissions"),
    F.col("accepted_submissions"),
    (F.col("accepted_submissions") / F.col("total_submissions")).alias("ac_rate"),
    F.coalesce(F.col("avg_time_ms"), F.lit(0.0)).alias("avg_time_ms"),#有空值 自动填为零
    F.coalesce(F.col("avg_memory_kb"), F.lit(0.0)).alias("avg_memory_kb"),
    (F.col("total_submissions") / F.when(F.col("active_users") <= 0, F.lit(1)).otherwise(F.col("active_users"))).alias("avg_attempts_per_user"),#人均对这道题的尝试次数
    (F.col("wa_total") / F.col("total_submissions")).alias("wa_rate"),
    (F.col("re_total") / F.col("total_submissions")).alias("re_rate"),
    (F.col("tle_total") / F.col("total_submissions")).alias("tle_rate")
)

window_all = Window.partitionBy()#开窗(每行都多开一列) 不筛选带特定字段的行去开窗，针对所有数据
#创建新列，值为第二参数，此新列就是要开的窗
#每个题目都有平均耗时，选最大的那个，作为分母，归一化，其他题目的time_score就会落到0到1间(attempt_score同理)
#difficulty_score就是给每道题的每个指标搞权重，计算每道题的最终难度分
scored_df = metrics_df \
    .withColumn("max_avg_time_ms", F.max("avg_time_ms").over(window_all)) \
    .withColumn("max_avg_attempts_per_user", F.max("avg_attempts_per_user").over(window_all)) \
    .withColumn(
        "time_score",
        F.when(F.col("max_avg_time_ms") <= 0, F.lit(0.0)).otherwise(clamp01(F.col("avg_time_ms") / F.col("max_avg_time_ms")))
    ) \
    .withColumn(
        "attempt_score",
        F.when(F.col("max_avg_attempts_per_user") <= 0, F.lit(0.0)).otherwise(clamp01(F.col("avg_attempts_per_user") / F.col("max_avg_attempts_per_user")))
    ) \
    .withColumn(
        "difficulty_score",
        clamp01(
            (F.lit(0.45) * (F.lit(1.0) - F.col("ac_rate"))) +
            (F.lit(0.20) * F.col("attempt_score")) +
            (F.lit(0.15) * F.col("time_score")) +
            (F.lit(0.10) * F.col("re_rate")) +
            (F.lit(0.10) * F.col("tle_rate"))
        )
    ) \
    .withColumn(
        "difficulty_label",
        F.when(F.col("difficulty_score") < 0.35, F.lit("Easy"))
         .when(F.col("difficulty_score") < 0.65, F.lit("Medium"))
         .otherwise(F.lit("Hard"))
    ) \
    .withColumn("model_name", F.lit(MODEL_NAME)) \
    .select(
        "dt",
        "problem_id",
        "total_submissions",
        "accepted_submissions",
        F.round("ac_rate", 4).alias("ac_rate"),
        F.round("avg_time_ms", 2).alias("avg_time_ms"),
        F.round("avg_memory_kb", 2).alias("avg_memory_kb"),
        F.round("avg_attempts_per_user", 4).alias("avg_attempts_per_user"),
        F.round("wa_rate", 4).alias("wa_rate"),
        F.round("re_rate", 4).alias("re_rate"),
        F.round("tle_rate", 4).alias("tle_rate"),
        F.round("difficulty_score", 4).alias("difficulty_score"),
        "difficulty_label",
        "model_name"
    )

#调用spark背后的jvm
conn = spark._sc._gateway.jvm.java.sql.DriverManager.getConnection(mysql_url, args.mysql_user, args.mysql_pass)
stmt = conn.createStatement()
stmt.executeUpdate(
    f"DELETE FROM analysis_problem_difficulty_daily WHERE dt = '{args.date}'"
)
stmt.close()
conn.close()

(scored_df.write
    .format("jdbc")
    .option("url", mysql_url)
    .option("dbtable", "analysis_problem_difficulty_daily")
    .option("user", args.mysql_user)
    .option("password", args.mysql_pass)
    .option("driver", "com.mysql.cj.jdbc.Driver")
    .mode("append")
    .save())

spark.stop()
