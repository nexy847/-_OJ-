**Overview**
本文档描述当前数据库字段与含义，并重点说明 `analysis_*` 相关分析表的作用、字段意义、数据来源以及 Java 程序如何使用这些数据。

**Naming**
- Spring Boot 默认命名策略会将字段名转换为 snake_case
- 以下以 snake_case 形式展示列名
- `Instant` 通常映射为 `DATETIME(6)` 或 `TIMESTAMP`
- 分析表中的 `dt`、`forecast_date`、`target_date` 当前统一按 `YYYY-MM-DD` 字符串存储

---

**业务主表**

**users**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| id | BIGINT | NO | PK | 用户 ID，自增 |
| username | VARCHAR(64) | NO | UNIQUE | 用户名 |
| password_hash | VARCHAR(255) | NO |  | 密码哈希（BCrypt） |
| created_at | DATETIME(6) | NO |  | 创建时间（UTC） |

**problems**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| id | BIGINT | NO | PK | 题目 ID，自增 |
| title | VARCHAR(128) | NO |  | 标题 |
| description | VARCHAR(4000) | NO |  | 题目描述 |
| time_limit_ms | INT | NO |  | 时间限制（毫秒） |
| memory_limit_mb | INT | NO |  | 内存限制（MB） |
| created_at | DATETIME(6) | NO |  | 创建时间（UTC） |

**testcases**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| id | BIGINT | NO | PK | 测试用例 ID，自增 |
| problem_id | BIGINT | NO |  | 关联题目 ID |
| input_path | VARCHAR(512) | NO |  | 输入文件相对路径 |
| output_path | VARCHAR(512) | NO |  | 输出文件相对路径 |
| weight | INT | NO |  | 权重 |

**submissions**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| id | BIGINT | NO | PK | 提交 ID，自增 |
| user_id | BIGINT | NO |  | 提交用户 ID |
| problem_id | BIGINT | NO |  | 题目 ID |
| language | VARCHAR(16) | NO |  | 语言枚举（C/CPP/JAVA/PYTHON） |
| code | LONGTEXT | NO |  | 源代码 |
| status | VARCHAR(16) | NO |  | PENDING/RUNNING/DONE/FAILED |
| verdict | VARCHAR(16) | YES |  | PENDING/AC/WA/TLE/MLE/RE/CE/ERROR |
| created_at | DATETIME(6) | NO |  | 创建时间（UTC） |
| updated_at | DATETIME(6) | NO |  | 更新时间（UTC） |

**judge_tasks**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| id | BIGINT | NO | PK | 任务 ID，自增 |
| submission_id | BIGINT | NO |  | 关联提交 ID |
| status | VARCHAR(16) | NO |  | PENDING/RUNNING/DONE/FAILED |
| tries | INT | NO |  | 已重试次数 |
| created_at | DATETIME(6) | NO |  | 创建时间（UTC） |
| updated_at | DATETIME(6) | NO |  | 更新时间（UTC） |
| next_run_at | DATETIME(6) | YES |  | 下次执行时间（UTC） |

**judge_results**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| id | BIGINT | NO | PK | 结果 ID，自增 |
| submission_id | BIGINT | NO |  | 关联提交 ID |
| verdict | VARCHAR(16) | NO |  | 判题结果 |
| time_ms | BIGINT | NO |  | 运行时间（毫秒） |
| memory_kb | BIGINT | NO |  | 内存使用（KB） |
| compile_error | LONGTEXT | YES |  | 编译错误输出 |
| runtime_error | LONGTEXT | YES |  | 运行时错误输出 |
| message | LONGTEXT | YES |  | 结果说明 |
| created_at | DATETIME(6) | NO |  | 创建时间（UTC） |

---

**分析数据总览**

当前分析链路分四层：
- 原始事件层：`analysis_events`
- Hive/HDFS 基础日汇总层：`analysis_summary_daily`、`analysis_problem_daily`、`analysis_user_daily`
- Hive/HDFS 描述性分析层：语言、verdict、小时活跃、题目 verdict、用户语言、用户 verdict 等表
- Spark 预测 / 评估层：`analysis_submission_forecast_daily`、`analysis_problem_difficulty_daily`

这些表的创建意义不是替代业务表，而是把判题行为沉淀成适合报表、可视化和预测任务的数据结构。

---

**analysis_events**

**创建意义**
- 这是分析链路的原始事件表。
- 每次一次提交被判题完成后，系统都会写一条事件记录。
- 这张表是导出到 HDFS 的源头，也是用户实时分析接口直接查询的数据源。

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| id | BIGINT | NO | PK | 事件 ID，自增 |
| submission_id | BIGINT | NO |  | 对应哪次提交 |
| user_id | BIGINT | NO |  | 哪个用户提交的 |
| problem_id | BIGINT | NO |  | 对应哪道题 |
| language | VARCHAR(16) | NO |  | 提交语言 |
| verdict | VARCHAR(16) | NO |  | 判题结果 |
| time_ms | BIGINT | NO |  | 该次提交耗时 |
| memory_kb | BIGINT | NO |  | 该次提交内存开销 |
| created_at | DATETIME(6) | NO |  | 事件产生时间 |
| exported | TINYINT(1) | NO |  | 是否已导出到 HDFS |
| exported_at | DATETIME(6) | YES |  | 导出时间 |

**数据来源**
- Java 在判题完成后写入。
- 代码位置：`/D:/oj_system_with_codex/src/main/java/com/oj/entity/AnalysisEvent.java`
- 使用位置主要来自判题完成后的业务流程，以及导出服务 `AnalysisExportService`。

**Java 如何使用**
- 实时用户分析接口直接查它：
  - `/D:/oj_system_with_codex/src/main/java/com/oj/repository/AnalysisEventRepository.java`
  - 被 `/D:/oj_system_with_codex/src/main/java/com/oj/service/AnalyticsDashboardService.java` 调用
- HDFS 导出服务从这里读取未导出数据：
  - `/D:/oj_system_with_codex/src/main/java/com/oj/service/AnalysisExportService.java`

---

**基础日汇总表**

这些表由 Hive 基础聚合脚本计算，主要用于管理员基础看板。

计算来源：
- Hive 脚本：`/D:/oj_system_with_codex/scripts/hive_agg_daily.sql`
- 导回 MySQL 脚本：`/D:/oj_system_with_codex/scripts/agg_daily.sh`
- 建表脚本：`/D:/oj_system_with_codex/scripts/analysis_tables.sql`

**analysis_summary_daily**

**创建意义**
- 保存某一天全站层面的基础汇总。
- 适合管理员首页顶部 KPI 卡片和最近 N 天趋势图。

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期 |
| total | BIGINT | NO |  | 当天总提交数 |
| accepted | BIGINT | NO |  | 当天 AC 数 |
| avg_time_ms | DOUBLE | YES |  | 当天平均耗时 |
| avg_memory_kb | DOUBLE | YES |  | 当天平均内存 |

**数据来源**
- Hive 从 `oj.analysis_events` 按天聚合得到。

**Java 如何使用**
- 实体：`/D:/oj_system_with_codex/src/main/java/com/oj/entity/AnalysisSummaryDaily.java`
- 仓库：`/D:/oj_system_with_codex/src/main/java/com/oj/repository/AnalysisSummaryDailyRepository.java`
- 服务使用：`/D:/oj_system_with_codex/src/main/java/com/oj/service/AnalyticsReadService.java`、`/D:/oj_system_with_codex/src/main/java/com/oj/service/AnalyticsDashboardService.java`
- 主要接口：
  - `GET /analytics/summary`
  - `GET /analytics/admin/overview`
  - `GET /analytics/admin/daily`

**analysis_problem_daily**

**创建意义**
- 保存某一天、每道题的基础聚合结果。
- 用于管理员查看热门题目、题目统计和题目排行榜。

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期 |
| problem_id | BIGINT | NO | PK | 题目 ID |
| total | BIGINT | NO |  | 当天该题提交总数 |
| accepted | BIGINT | NO |  | 当天该题 AC 数 |
| avg_time_ms | DOUBLE | YES |  | 当天该题平均耗时 |
| avg_memory_kb | DOUBLE | YES |  | 当天该题平均内存 |

**数据来源**
- Hive 从 `oj.analysis_events` 按 `dt + problem_id` 聚合得到。

**Java 如何使用**
- 实体：`/D:/oj_system_with_codex/src/main/java/com/oj/entity/AnalysisProblemDaily.java`
- 仓库：`/D:/oj_system_with_codex/src/main/java/com/oj/repository/AnalysisProblemDailyRepository.java`
- 服务使用：`/D:/oj_system_with_codex/src/main/java/com/oj/service/AnalyticsReadService.java`、`/D:/oj_system_with_codex/src/main/java/com/oj/service/AnalyticsDashboardService.java`
- 主要接口：
  - `GET /analytics/problems/daily`
  - `GET /analytics/admin/problems/top`

**analysis_user_daily**

**创建意义**
- 保存某一天、每个用户的基础聚合结果。
- 用于管理员查看活跃用户数、用户排行。

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期 |
| user_id | BIGINT | NO | PK | 用户 ID |
| total | BIGINT | NO |  | 当天该用户提交总数 |
| accepted | BIGINT | NO |  | 当天该用户 AC 数 |
| avg_time_ms | DOUBLE | YES |  | 当天该用户平均耗时 |
| avg_memory_kb | DOUBLE | YES |  | 当天该用户平均内存 |

**数据来源**
- Hive 从 `oj.analysis_events` 按 `dt + user_id` 聚合得到。

**Java 如何使用**
- 实体：`/D:/oj_system_with_codex/src/main/java/com/oj/entity/AnalysisUserDaily.java`
- 仓库：`/D:/oj_system_with_codex/src/main/java/com/oj/repository/AnalysisUserDailyRepository.java`
- 服务使用：`/D:/oj_system_with_codex/src/main/java/com/oj/service/AnalyticsReadService.java`、`/D:/oj_system_with_codex/src/main/java/com/oj/service/AnalyticsDashboardService.java`
- 主要接口：
  - `GET /analytics/users/daily`
  - `GET /analytics/admin/overview`（活跃用户数）
  - `GET /analytics/admin/users/top`

---

**描述性分析专题表**

这些表由扩展描述性分析脚本生成：
- Hive 脚本：`/D:/oj_system_with_codex/scripts/hive_desc_analytics.sql`
- 导回 MySQL 脚本：`/D:/oj_system_with_codex/scripts/agg_desc.sh`
- 统一服务：`/D:/oj_system_with_codex/src/main/java/com/oj/service/AnalyticsPlatformService.java`

**analysis_language_daily**

**创建意义**
- 保存某一天、每种语言的提交统计。
- 用于管理员看语言分布和语言趋势。

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期 |
| language | VARCHAR(16) | NO | PK | 语言 |
| total | BIGINT | NO |  | 该语言提交总数 |
| accepted | BIGINT | NO |  | 该语言 AC 数 |
| avg_time_ms | DOUBLE | YES |  | 平均耗时 |
| avg_memory_kb | DOUBLE | YES |  | 平均内存 |

**数据来源**
- Hive 按 `dt + language` 聚合 `analysis_events`。

**Java 如何使用**
- 实体：`AnalysisLanguageDaily`
- 仓库：`AnalysisLanguageDailyRepository`
- 服务：`AnalyticsPlatformService#getAdminLanguageDaily`
- 接口：`GET /analytics/admin/language/daily`

**analysis_verdict_daily**

**创建意义**
- 保存某一天、每种 verdict 的数量。
- 用于管理员看 verdict 趋势。

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期 |
| verdict | VARCHAR(16) | NO | PK | 判题结果 |
| total | BIGINT | NO |  | 数量 |

**数据来源**
- Hive 按 `dt + verdict` 聚合 `analysis_events`。

**Java 如何使用**
- 实体：`AnalysisVerdictDaily`
- 仓库：`AnalysisVerdictDailyRepository`
- 服务：`AnalyticsPlatformService#getAdminVerdictDaily`
- 接口：`GET /analytics/admin/verdict/daily`

**analysis_hourly_activity**

**创建意义**
- 保存某一天、每个小时的提交活动情况。
- 用于管理员看高峰时段和小时活跃曲线。

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期 |
| hour_of_day | INT | NO | PK | 小时（0-23） |
| total | BIGINT | NO |  | 当小时提交数 |
| accepted | BIGINT | NO |  | 当小时 AC 数 |
| active_users | BIGINT | NO |  | 当小时活跃用户数 |

**数据来源**
- Hive 从 `created_at` 中提取小时，再按 `dt + hour_of_day` 聚合。

**Java 如何使用**
- 实体：`AnalysisHourlyActivity`
- 仓库：`AnalysisHourlyActivityRepository`
- 服务：`AnalyticsPlatformService#getAdminHourlyActivity`
- 接口：`GET /analytics/admin/hourly`

**analysis_problem_verdict_daily**

**创建意义**
- 保存某一天、某道题、某种 verdict 的数量。
- 用于单题错误类型分布图。

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期 |
| problem_id | BIGINT | NO | PK | 题目 ID |
| verdict | VARCHAR(16) | NO | PK | 判题结果 |
| total | BIGINT | NO |  | 数量 |

**数据来源**
- Hive 按 `dt + problem_id + verdict` 聚合。

**Java 如何使用**
- 实体：`AnalysisProblemVerdictDaily`
- 仓库：`AnalysisProblemVerdictDailyRepository`
- 服务：`AnalyticsPlatformService#getAdminProblemVerdict`
- 接口：`GET /analytics/admin/problems/{problemId}/verdict`

**analysis_user_language_daily**

**创建意义**
- 保存某一天、某个用户、某种语言的提交统计。
- 用于用户语言趋势图。

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期 |
| user_id | BIGINT | NO | PK | 用户 ID |
| language | VARCHAR(16) | NO | PK | 语言 |
| total | BIGINT | NO |  | 提交总数 |
| accepted | BIGINT | NO |  | AC 数 |

**数据来源**
- Hive 按 `dt + user_id + language` 聚合。

**Java 如何使用**
- 实体：`AnalysisUserLanguageDaily`
- 仓库：`AnalysisUserLanguageDailyRepository`
- 服务：`AnalyticsPlatformService#getUserLanguageDaily`
- 接口：`GET /analytics/user/language/daily`

**analysis_user_verdict_daily**

**创建意义**
- 保存某一天、某个用户、某种 verdict 的数量。
- 用于用户 verdict 趋势图。

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期 |
| user_id | BIGINT | NO | PK | 用户 ID |
| verdict | VARCHAR(16) | NO | PK | 判题结果 |
| total | BIGINT | NO |  | 数量 |

**数据来源**
- Hive 按 `dt + user_id + verdict` 聚合。

**Java 如何使用**
- 实体：`AnalysisUserVerdictDaily`
- 仓库：`AnalysisUserVerdictDailyRepository`
- 服务：`AnalyticsPlatformService#getUserVerdictDaily`
- 接口：`GET /analytics/user/verdict/daily`

---

**Spark 预测与评估表**

这部分不是 Hive 汇总，而是 Spark 作业直接计算并通过 JDBC 写回 MySQL。

**analysis_submission_forecast_daily**

**创建意义**
- 保存 Spark 对“未来每日提交量”的预测结果。
- 用于管理员预测页面 `/admin/perdict`。

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| id | BIGINT | NO | PK | 主键，自增 |
| forecast_date | VARCHAR(10) | NO | UNIQUE 组合键 | 这批预测是哪一天跑出来的 |
| target_date | VARCHAR(10) | NO | UNIQUE 组合键 | 预测的目标日期 |
| predicted_submissions | INT | NO |  | 预测提交量 |
| model_name | VARCHAR(64) | NO | UNIQUE 组合键 | 使用的模型名称 |
| created_at | DATETIME(6) | NO |  | 结果写入时间 |

**数据来源**
- Spark 脚本：`/D:/oj_system_with_codex/spark_jobs/submission_forecast.py`
- 脚本读取 Hive 表 `oj.analysis_events`，建模后写回 MySQL。

**Java 如何使用**
- 实体：`AnalysisSubmissionForecastDaily`
- 仓库：`AnalysisSubmissionForecastDailyRepository`
- 服务：`AnalyticsPlatformService#getAdminSubmissionForecast`
- 接口：`GET /analytics/admin/forecast/submissions`
- 前端页面：`/admin/perdict`

**analysis_problem_difficulty_daily**

**创建意义**
- 保存 Spark 基于历史判题行为算出的题目难度评估结果。
- 这不是“题目原始难度字段”，而是分析层的动态评估结果。
- 用途是为管理员提供：
  - 每题难度分数
  - `Easy / Medium / Hard` 难度标签
  - 难度历史变化

**字段说明**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 评估日期 |
| problem_id | BIGINT | NO | PK | 题目 ID |
| total_submissions | BIGINT | NO |  | 该题当日总提交数 |
| accepted_submissions | BIGINT | NO |  | 该题当日 AC 数 |
| ac_rate | DOUBLE | NO |  | 通过率 |
| avg_time_ms | DOUBLE | NO |  | 平均耗时 |
| avg_memory_kb | DOUBLE | NO |  | 平均内存 |
| avg_attempts_per_user | DOUBLE | NO |  | 平均每位用户尝试次数 |
| wa_rate | DOUBLE | NO |  | WA 占比 |
| re_rate | DOUBLE | NO |  | RE 占比 |
| tle_rate | DOUBLE | NO |  | TLE 占比 |
| difficulty_score | DOUBLE | NO |  | Spark 计算出的难度分数，越大越难 |
| difficulty_label | VARCHAR(16) | NO |  | 难度标签：Easy / Medium / Hard |
| model_name | VARCHAR(64) | NO |  | 当前规则/模型名称 |

**数据来源**
- Spark 脚本：`/D:/oj_system_with_codex/spark_jobs/problem_difficulty_v1.py`
- 该脚本读取 Hive 外表 `oj.analysis_events`，按题目聚合，再用规则型评分公式输出难度。
- 当前版本模型名为 `rule_based_v1`。

**Java 如何使用**
- 实体：`AnalysisProblemDifficultyDaily`
- 仓库：`AnalysisProblemDifficultyDailyRepository`
- 服务：`AnalyticsPlatformService#getAdminProblemDifficulty`、`getAdminProblemDifficultyHistory`
- 接口：
  - `GET /analytics/admin/problems/difficulty`
  - `GET /analytics/admin/problems/{problemId}/difficulty/history`
- 前端页面：`/admin/problems/difficulty`

---

**补充说明：这些表和 Java 的关系**

可以把 Java 对分析表的使用方式分成三类：

1. **直接查原始事件表做实时分析**
- 例如用户总览、用户最近提交、用户语言分布
- 主要代码：`AnalysisEventRepository` + `AnalyticsDashboardService`
- 优点：实时性高
- 缺点：数据量大后查询成本高

2. **查 Hive/Spark 回写后的聚合表做看板**
- 例如管理员日汇总、题目排行、小时活跃、语言趋势
- 主要代码：`AnalyticsReadService`、`AnalyticsDashboardService`、`AnalyticsPlatformService`
- 优点：查询快，适合图表页面

3. **查 Spark 预测/评估结果表做高级分析**
- 例如提交量预测、题目难度评估
- 主要代码：`AnalyticsPlatformService`
- 特点：离线跑批生成，前端只负责展示结果

---

**Notes**
- 业务表大多由 JPA 自动生成，分析表主要由 `scripts/analysis_tables.sql` 创建
- Hive 负责基础和描述性聚合，Spark 负责预测与规则型评估
- 如果后续再加新的分析任务，建议继续遵循：
  - 原始事件 -> HDFS/Hive -> Spark/Hive 计算 -> MySQL 分析结果表 -> Java API -> 前端展示
