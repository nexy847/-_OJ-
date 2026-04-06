先纠正一个表述，方便你答辩时更严谨：

**这 24 个 API 在调用时，最终都是 Spring Boot 在查 MySQL。**  
区别不在于“API 直接查 MySQL 还是直接查 Hadoop”，而在于：

- 有些 API 查的是 **MySQL 里的原始事件表 `analysis_events`**，属于**实时分析**
- 有些 API 查的是 **Hive 预聚合后回写到 MySQL 的结果表**，属于**离线分析结果服务**
- 有些 API 查的是 **Spark 计算后回写到 MySQL 的结果表**，属于**预测/评估结果服务**

所以更准确的说法是：

- **实时类接口：由 MySQL 原始事件直接分析**
- **离线类接口：由 Hadoop/Hive 先算，再把结果写回 MySQL**
- **预测类接口：由 Spark 先算，再把结果写回 MySQL**

这个说法是有理有据的。

---

**总的性能原则**

为什么会分这三种？

**1. 直接查 `analysis_events` 的条件**
- 查询范围小
- 维度简单
- 有明确用户过滤或单日时间过滤
- 前端要求“尽量实时”
- 没必要等离线批处理

**2. 先让 Hive 算再查结果表的条件**
- 涉及全站、全题目、全用户、多天趋势
- `GROUP BY` 维度多
- 数据扫描范围大
- 如果直接在 MySQL 原始事件表上反复跑，会和在线业务抢资源

**3. 先让 Spark 算再查结果表的条件**
- 不是普通统计，而是预测/评分/建模
- 逻辑复杂，适合离线批处理
- 前端只需要读取结果，不需要在线建模

---

下面我按 **24 个 API 一个个讲**。每个都给你：

- 它查什么
- 代表 SQL
- 为什么选这种方式

---

## 一、基础日报与管理员总览

### 1. `GET /analytics/summary?date={YYYY-MM-DD}`
读取来源：
- `analysis_summary_daily`
- 这是 Hive 聚合后回写的 MySQL 表

代表 SQL：
```sql
SELECT dt, total, accepted, avg_time_ms, avg_memory_kb
FROM analysis_summary_daily
WHERE dt = ?;
```

为什么不用原始 `analysis_events` 直接算：
- 这个接口是“全站某天总览”
- 如果直接在 `analysis_events` 上做：
```sql
SELECT COUNT(*), SUM(...), AVG(...), AVG(...)
FROM analysis_events
WHERE DATE(created_at)=?;
```
- 这会扫描当天全站所有事件
- 对平台规模变大后不经济
- 这种日汇总非常适合预聚合，Hive 一次算好，API 直接读结果，延迟最低

意义：
- 给管理员提供某一天的全站运行快照

---

### 2. `GET /analytics/problems/daily?date={YYYY-MM-DD}`
读取来源：
- `analysis_problem_daily`
- Hive 聚合结果

代表 SQL：
```sql
SELECT problem_id, total, accepted, avg_time_ms, avg_memory_kb
FROM analysis_problem_daily
WHERE dt = ?
ORDER BY problem_id;
```

为什么不用 `analysis_events` 直接算：
- 这是“某一天所有题目”的分组统计
- 直接查原始表需要：
```sql
SELECT problem_id, COUNT(*), SUM(...), AVG(...), AVG(...)
FROM analysis_events
WHERE DATE(created_at)=?
GROUP BY problem_id;
```
- 这类“全站按题目分组”会随着题目数和事件数增长变重
- 适合放到 Hive 日批里做

意义：
- 为题目排行、题目日统计提供底层数据

---

### 3. `GET /analytics/users/daily?date={YYYY-MM-DD}`
读取来源：
- `analysis_user_daily`
- Hive 聚合结果

代表 SQL：
```sql
SELECT user_id, total, accepted, avg_time_ms, avg_memory_kb
FROM analysis_user_daily
WHERE dt = ?
ORDER BY user_id;
```

为什么不用原始表直接算：
- 这是“某一天所有用户”的分组统计
- 直接跑：
```sql
SELECT user_id, COUNT(*), SUM(...), AVG(...), AVG(...)
FROM analysis_events
WHERE DATE(created_at)=?
GROUP BY user_id;
```
- 全站按用户分组，数据量增大后成本高
- 适合离线聚合后服务化

意义：
- 为用户排行、活跃用户分析提供底层数据

---

### 4. `GET /analytics/admin/overview?date={YYYY-MM-DD}`
读取来源：
- `analysis_summary_daily`
- `analysis_user_daily`

代表 SQL：
```sql
SELECT * FROM analysis_summary_daily WHERE dt = ?;
SELECT COUNT(*) FROM analysis_user_daily WHERE dt = ?;
```

为什么这样选：
- 这个接口是管理员首页顶部总览
- 需要快
- 它其实是“多个预聚合指标的组合”
- 活跃用户数直接数 `analysis_user_daily` 的行数，比扫原始事件表去 `COUNT(DISTINCT user_id)` 更轻

意义：
- 管理员总览卡片

---

### 5. `GET /analytics/admin/daily?days={N}`
读取来源：
- `analysis_summary_daily`

代表 SQL：
```sql
SELECT *
FROM analysis_summary_daily
WHERE dt BETWEEN ? AND ?
ORDER BY dt;
```

为什么选 Hive 结果表：
- 这是跨多天的全站趋势
- 如果直接跑原始事件表，需要跨多天反复 `GROUP BY DATE(created_at)`
- 随着历史数据累积，这种趋势查询非常适合用预聚合表

意义：
- 看平台整体趋势，而不是单天快照

---

### 6. `GET /analytics/admin/hourly?date={YYYY-MM-DD}`
读取来源：
- `analysis_hourly_activity`
- Hive 描述性分析结果

代表 SQL：
```sql
SELECT dt, hour_of_day, total, accepted, active_users
FROM analysis_hourly_activity
WHERE dt = ?
ORDER BY hour_of_day;
```

为什么不用原始表直接算：
- 需要按小时分桶，还要 `COUNT(DISTINCT user_id)`
- 原始 SQL 类似：
```sql
SELECT HOUR(created_at), COUNT(*), SUM(...), COUNT(DISTINCT user_id)
FROM analysis_events
WHERE DATE(created_at)=?
GROUP BY HOUR(created_at);
```
- 这种 SQL 虽然能写，但属于典型的离线报表计算
- 预先算好能避免每次打开页面都扫描当天所有日志

意义：
- 看高峰时段，服务于运维与容量规划

---

## 二、用户实时反馈类

### 7. `GET /analytics/user/overview`
读取来源：
- `analysis_events`

代表 SQL：
```sql
SELECT
  COUNT(*) AS total,
  SUM(CASE WHEN verdict='AC' THEN 1 ELSE 0 END) AS accepted,
  AVG(time_ms) AS avg_time_ms,
  AVG(memory_kb) AS avg_memory_kb
FROM analysis_events
WHERE user_id = ?;
```

为什么直接查 MySQL 原始事件：
- 只针对单个用户
- 范围很小
- 需要尽量实时，不想等离线批处理
- 属于典型“用户个人画像快照”，直接查事件表成本可控

意义：
- 让用户马上看到自己整体做题表现

---

### 8. `GET /analytics/user/daily?days={N}`
读取来源：
- `analysis_events`

代表 SQL：
```sql
SELECT
  DATE(created_at) AS dt,
  COUNT(*) AS total,
  SUM(CASE WHEN verdict='AC' THEN 1 ELSE 0 END) AS accepted,
  AVG(time_ms) AS avg_time_ms,
  AVG(memory_kb) AS avg_memory_kb
FROM analysis_events
WHERE user_id = ?
  AND created_at >= ?
  AND created_at < ?
GROUP BY DATE(created_at)
ORDER BY dt;
```

为什么直接查 MySQL：
- 仍然只针对单个用户
- 虽然是 N 天趋势，但过滤条件是 `user_id`
- 结果集最多也就 `days` 行
- 单用户范围下直接查比走离线结果更实时

意义：
- 让用户知道自己近期状态是在变好还是变差

---

### 9. `GET /analytics/user/language`
读取来源：
- `analysis_events`

代表 SQL：
```sql
SELECT
  language,
  COUNT(*) AS total,
  SUM(CASE WHEN verdict='AC' THEN 1 ELSE 0 END) AS accepted
FROM analysis_events
WHERE user_id = ?
GROUP BY language;
```

为什么直接查 MySQL：
- 单用户、小维度分组
- 数据量小
- 实时性高于离线性
- 不值得为了这么小的查询再走 Hadoop 结果表

意义：
- 看用户偏好哪些语言，哪些语言表现更好

---

### 10. `GET /analytics/user/verdict`
读取来源：
- `analysis_events`

代表 SQL：
```sql
SELECT verdict, COUNT(*) AS total
FROM analysis_events
WHERE user_id = ?
GROUP BY verdict;
```

为什么直接查 MySQL：
- 同样是单用户、小范围分组
- 实时查询成本低
- 能即时反映用户最近提交后的错误结构

意义：
- 帮用户知道自己主要错在 WA、RE 还是 TLE

---

### 11. `GET /analytics/user/problems`
读取来源：
- `analysis_events`

代表 SQL：
```sql
SELECT
  problem_id,
  COUNT(*) AS total,
  SUM(CASE WHEN verdict='AC' THEN 1 ELSE 0 END) AS accepted,
  AVG(time_ms) AS avg_time_ms,
  AVG(memory_kb) AS avg_memory_kb
FROM analysis_events
WHERE user_id = ?
GROUP BY problem_id;
```

为什么直接查 MySQL：
- 还是单用户维度
- 即便按题目分组，数据范围也只是这个用户自己的提交历史
- 相比全站按题目聚合，成本小很多

意义：
- 看用户在哪些题上做得好，哪些题反复尝试

---

### 12. `GET /analytics/user/recent`
读取来源：
- `analysis_events`

代表 SQL：
```sql
SELECT submission_id, problem_id, language, verdict, time_ms, memory_kb, created_at
FROM analysis_events
WHERE user_id = ?
ORDER BY created_at DESC
LIMIT ?;
```

为什么直接查 MySQL：
- 这是最典型的在线小查询
- 只取最近几条
- 完全不应该放 Hadoop

意义：
- 给用户“最近活动”列表

---

## 三、用户趋势型离线接口

### 13. `GET /analytics/user/language/daily?days={N}`
读取来源：
- `analysis_user_language_daily`
- Hive 描述性分析结果

代表 SQL：
```sql
SELECT dt, language, total, accepted
FROM analysis_user_language_daily
WHERE user_id = ?
  AND dt BETWEEN ? AND ?
ORDER BY dt, language;
```

为什么不用原始表直接算：
- 它是“用户 + 日期 + 语言”三维趋势
- 虽然也是单用户，但这是图表接口，前端会频繁刷新
- 预聚合后查询更稳定
- 同时和管理员的 `language/daily` 共享同一套离线口径

意义：
- 让用户看到自己不同语言的长期使用趋势和表现变化

---

### 14. `GET /analytics/user/verdict/daily?days={N}`
读取来源：
- `analysis_user_verdict_daily`
- Hive 描述性分析结果

代表 SQL：
```sql
SELECT dt, verdict, total
FROM analysis_user_verdict_daily
WHERE user_id = ?
  AND dt BETWEEN ? AND ?
ORDER BY dt, verdict;
```

为什么用 Hive 结果表：
- 和上一个同理
- 是“用户 + 日期 + verdict”的趋势分析
- 属于图表型接口，适合预聚合
- 避免每次都在原始事件上做多维分组

意义：
- 让用户看到自己错误类型有没有变得更健康

---

## 四、管理员单日实时分布类

### 15. `GET /analytics/admin/language?date={YYYY-MM-DD}`
读取来源：
- `analysis_events`

代表 SQL：
```sql
SELECT
  language,
  COUNT(*) AS total,
  SUM(CASE WHEN verdict='AC' THEN 1 ELSE 0 END) AS accepted
FROM analysis_events
WHERE created_at >= ? AND created_at < ?
GROUP BY language;
```

为什么这里反而直接查 MySQL，而不是查 `analysis_language_daily`：
- 这个接口是**单日快照**
- 管理员往往希望今天刚有新提交就能看到
- 如果完全依赖 Hive 批处理，要等 `agg_desc.sh` 跑完
- 当前系统规模下，单天全站按语言分组是可接受的

意义：
- 了解当天语言使用结构，并即时发现某种语言是否异常

---

### 16. `GET /analytics/admin/verdict?date={YYYY-MM-DD}`
读取来源：
- `analysis_events`

代表 SQL：
```sql
SELECT verdict, COUNT(*) AS total
FROM analysis_events
WHERE created_at >= ? AND created_at < ?
GROUP BY verdict;
```

为什么直接查 MySQL：
- 也是单天快照
- 强调“实时状态”
- 比如管理员今天排查系统状态时，希望马上看到 CE、RE、TLE 是否异常上升

意义：
- 看当天平台错误结构是否异常

---

## 五、管理员离线趋势与专题类

### 17. `GET /analytics/admin/language/daily?days={N}`
读取来源：
- `analysis_language_daily`
- Hive 描述性分析结果

代表 SQL：
```sql
SELECT dt, language, total, accepted, avg_time_ms, avg_memory_kb
FROM analysis_language_daily
WHERE dt BETWEEN ? AND ?
ORDER BY dt, language;
```

为什么不用原始表直接算：
- 这是“全站 + 多天 + 语言”的趋势分析
- 需要跨多天、跨全站事件反复分组
- 非常适合预聚合

意义：
- 看平台语言结构在长期是怎么变化的

---

### 18. `GET /analytics/admin/verdict/daily?days={N}`
读取来源：
- `analysis_verdict_daily`
- Hive 描述性分析结果

代表 SQL：
```sql
SELECT dt, verdict, total
FROM analysis_verdict_daily
WHERE dt BETWEEN ? AND ?
ORDER BY dt, verdict;
```

为什么用 Hive 结果：
- 这是全站 verdict 多日趋势
- 直接查原始事件表需要大范围扫描和分组
- 离线聚合更稳

意义：
- 看错误类型是不是持续异常增长

---

### 19. `GET /analytics/admin/problems/{problemId}/verdict?date={YYYY-MM-DD}`
读取来源：
- `analysis_problem_verdict_daily`
- Hive 描述性分析结果

代表 SQL：
```sql
SELECT dt, problem_id, verdict, total
FROM analysis_problem_verdict_daily
WHERE dt = ?
  AND problem_id = ?
ORDER BY verdict;
```

为什么用 Hive 结果：
- 这类接口用于单题错误结构图
- 如果每次打开某题都在原始表上做 `GROUP BY verdict`，长远来看没有必要
- 题目治理页面适合读稳定的离线口径

意义：
- 判断一道题的问题是 WA 多、RE 多还是 TLE 多

---

### 20. `GET /analytics/admin/problems/top?date={YYYY-MM-DD}&limit={N}`
读取来源：
- `analysis_problem_daily`
- Hive 基础聚合结果

代表 SQL：
```sql
SELECT *
FROM analysis_problem_daily
WHERE dt = ?
ORDER BY total DESC
LIMIT 50;
```

为什么用 Hive 结果：
- 这是“某天所有题目”的横向比较
- 属于典型排行榜查询
- 预聚合后排序很轻
- 如果每次都在原始事件上 `GROUP BY problem_id ORDER BY COUNT(*) DESC`，成本更高

意义：
- 看哪些题最受关注、最热门

---

### 21. `GET /analytics/admin/users/top?date={YYYY-MM-DD}&limit={N}`
读取来源：
- `analysis_user_daily`
- Hive 基础聚合结果

代表 SQL：
```sql
SELECT *
FROM analysis_user_daily
WHERE dt = ?
ORDER BY total DESC
LIMIT 50;
```

为什么用 Hive 结果：
- 和题目排行同理
- 是全站某天用户级排行
- 预聚合后直接排序即可

意义：
- 看哪些用户最活跃

---

## 六、Spark 预测与评估类

### 22. `GET /analytics/admin/forecast/submissions?forecastDate={YYYY-MM-DD}`
读取来源：
- `analysis_submission_forecast_daily`
- Spark 预测结果表

代表 SQL：
```sql
SELECT forecast_date, target_date, predicted_submissions, model_name
FROM analysis_submission_forecast_daily
WHERE forecast_date = ?
ORDER BY target_date;
```

如果不传 `forecastDate`，先查：
```sql
SELECT MAX(forecast_date) FROM analysis_submission_forecast_daily;
```

为什么必须由 Spark 先算：
- 这不是简单聚合，而是预测
- 需要离线建模
- API 只应该读结果，不应该在线跑模型

意义：
- 为资源预留、容量规划、未来自动扩容提供依据

---

### 23. `GET /analytics/admin/problems/difficulty?date={YYYY-MM-DD}&limit={N}&label={label}`
读取来源：
- `analysis_problem_difficulty_daily`
- Spark 规则评估结果表

代表 SQL：
```sql
SELECT *
FROM analysis_problem_difficulty_daily
WHERE dt = ?
ORDER BY difficulty_score DESC;
```

如果有 `label` 再做过滤。

为什么必须由 Spark 先算：
- 难度评分不是简单 `COUNT/AVG`
- 它综合了：
  - `ac_rate`
  - `avg_attempts_per_user`
  - `avg_time_ms`
  - `re_rate`
  - `tle_rate`
- 这是离线评估逻辑，不应该在 API 请求时在线计算

意义：
- 做题库治理，自动给题目打难度标签

---

### 24. `GET /analytics/admin/problems/{problemId}/difficulty/history?days={N}`
读取来源：
- `analysis_problem_difficulty_daily`
- Spark 规则评估结果表

代表 SQL：
```sql
SELECT dt, difficulty_score, difficulty_label, ac_rate, avg_time_ms, avg_attempts_per_user
FROM analysis_problem_difficulty_daily
WHERE problem_id = ?
  AND dt BETWEEN ? AND ?
ORDER BY dt;
```

为什么用 Spark 结果表：
- 这是“难度历史变化”
- 每个点本身就是离线评估结果
- API 不该重新算难度，只负责读历史

意义：
- 看某题难度是否稳定，是否因为题面、数据、环境变化而波动

---

# 最后给你一个可以直接答辩的总结

你可以这样回答老师：

**这 24 个分析接口虽然最终都由 Spring Boot 从 MySQL 返回，但它们背后的数据来源分成三类：一类是直接从原始事件表 `analysis_events` 做实时分析，适用于单用户、小范围、强实时的查询；一类是由 Hive 对历史日志做离线聚合后回写 MySQL，适用于全站、多维度、多日期的统计分析；还有一类是由 Spark 对历史数据进行预测或评估后回写 MySQL，适用于提交量预测和题目难度评估这类复杂计算。这样设计的原因是，在保证前端接口低延迟的同时，把重计算从在线业务库中解耦出去，从而兼顾系统性能与数据分析能力。**

如果你要，我下一步可以把这 24 个接口整理成一张**“API - 数据源 - 代表SQL - 选型理由”总表**，更适合你直接放进论文或答辩稿。