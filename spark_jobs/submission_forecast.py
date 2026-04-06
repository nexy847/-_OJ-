from pyspark.sql import SparkSession
from pyspark.sql.functions import col, count, to_date, lit, max as spark_max
from pyspark.ml.feature import VectorAssembler
from pyspark.ml.regression import LinearRegression
from pyspark.sql.types import StructType, StructField, IntegerType, StringType
from datetime import datetime, timedelta

MYSQL_HOST = "192.168.1.52"
MYSQL_PORT = "3306"
MYSQL_DB = "oj"
MYSQL_USER = "root"
MYSQL_PASS = "12365478910vr"
MYSQL_URL = f"jdbc:mysql://{MYSQL_HOST}:{MYSQL_PORT}/{MYSQL_DB}?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8"

MODEL_NAME = "linear_regression_v1"
FORECAST_DAYS = 7

spark = SparkSession.builder \
    .appName("submission-forecast-daily") \
    .enableHiveSupport() \
    .getOrCreate()

# 1. 读取 Hive 事件表，按天聚合提交量
daily_df = spark.sql("""
    SELECT
      dt,
      COUNT(*) AS total_submissions
    FROM oj.analysis_events
    GROUP BY dt
    ORDER BY dt
""")

rows = daily_df.collect()
if len(rows) < 2:
    raise RuntimeError("历史数据太少，至少需要 2 天数据才>能做第一版预测")

# 2. 构造训练数据：day_index -> total_submissions
train_data = []
for idx, row in enumerate(rows):#函数给每一行自动加一个索引 第一行索引idx为0(方便模型顺序地识别，因为模型不识别日期格式)
    train_data.append((idx, int(row["total_submissions"])))#把天数编号作x轴，总提交量作y轴
#类似于定义数据库的表和列
schema = StructType([
    StructField("day_index", IntegerType(), False),#名字，类型，是否允许为空
    StructField("label", IntegerType(), False),
])

train_df = spark.createDataFrame(train_data, schema=schema)#转换为spark内部的分布式数据集，schema定义结构

assembler = VectorAssembler(inputCols=["day_index"], outputCol="features")#将天数编号取出，作为向量类型的输入特征，才能被模型识别到(每行单独处理)
feature_df = assembler.transform(train_df)#将features合并进df，作为新列

# 3. 训练线性回归模型
lr = LinearRegression(featuresCol="features", labelCol="label", predictionCol="prediction")
# 线性回归 指定自变量的向量 指定y轴 确立预测出的结果列叫什么
# 线性回归 y=ax+b y为要预测的结果 x为天数编号 fit()通过最小二乘法找最优a和b 不断微调a和b 直到预测值与实际值类似
model = lr.fit(feature_df)

# 4. 预测未来 7 天
last_dt_str = rows[-1]["dt"]# 找出列表的最后一行 即最近的那一天
last_dt = datetime.strptime(str(last_dt_str), "%Y-%m-%d").date()# 并转为py的日期对象
start_index = len(rows)# 获取历史数据总天数

future_rows = []# 放入未来日子的编号和对应日期的字符串
for i in range(FORECAST_DAYS):# 从零始
    future_index = start_index + i# 明日的编号 作为model的x轴
    target_date = last_dt + timedelta(days=i + 1)# 换为日期对象
    future_rows.append((future_index, str(target_date)))

future_schema = StructType([
    StructField("day_index", IntegerType(), False),
    StructField("target_date", StringType(), False),
])

future_df = spark.createDataFrame(future_rows, schema=future_schema)
future_feature_df = assembler.transform(future_df)

#使用刚才算出的公式 带入 得出预测值
pred_df = model.transform(future_feature_df) \
    .select(
        lit(str(datetime.now().date())).alias("forecast_date"),# 记录这条预测是哪一天算出来的
        col("target_date"),
        col("prediction")
    )

result_df = pred_df.select(
    col("forecast_date"),
    col("target_date"),
    (col("prediction").cast("int")).alias("predicted_submissions"),# 转换为int 作为预测提交
    lit(MODEL_NAME).alias("model_name")
)

# 避免出现负数预测
#result_df = result_df.select(
#    col("forecast_date"),
#    col("target_date"),
#    (col("predicted_submissions")).alias("predicted_submissions"),
#    col("model_name")
#)

result_rows = result_df.collect()
fixed_rows = []
for row in result_rows:
    value = row["predicted_submissions"]
    if value is None or value < 0:
        value = 0
    fixed_rows.append((
        row["forecast_date"],
        row["target_date"],
        int(value),
        row["model_name"]
    ))

write_schema = StructType([
    StructField("forecast_date", StringType(), False),
    StructField("target_date", StringType(), False),
    StructField("predicted_submissions", IntegerType(), False),
    StructField("model_name", StringType(), False),
])

write_df = spark.createDataFrame(fixed_rows, schema=write_schema)

# 5. 先删除同一 forecast_date + model_name 的旧结果
forecast_date = str(datetime.now().date())

delete_sql = f"""
DELETE FROM analysis_submission_forecast_daily
WHERE forecast_date = '{forecast_date}'
  AND model_name = '{MODEL_NAME}'
"""

conn = spark._sc._gateway.jvm.java.sql.DriverManager.getConnection(
    MYSQL_URL, MYSQL_USER, MYSQL_PASS
)
stmt = conn.createStatement()
stmt.executeUpdate(delete_sql)
stmt.close()
conn.close()

# 6. 写入 MySQL
(write_df.write
    .format("jdbc")
    .option("url", MYSQL_URL)
    .option("dbtable", "analysis_submission_forecast_daily")
    .option("user", MYSQL_USER)
    .option("password", MYSQL_PASS)
    .option("driver", "com.mysql.cj.jdbc.Driver")
    .mode("append")
    .save())

spark.stop()