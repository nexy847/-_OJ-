**Overview**
本文档描述当前数据库字段与含义。业务表由 JPA 自动建表，分析汇总表由脚本创建。

**Naming**
- Spring Boot 默认命名策略会将字段名转换为 snake_case
- 以下以 snake_case 形式展示列名
- `Instant` 通常映射为 `DATETIME(6)` 或 `TIMESTAMP`（取决于 MySQL 配置与方言）

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

**analysis_events**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| id | BIGINT | NO | PK | 事件 ID，自增 |
| submission_id | BIGINT | NO |  | 提交 ID |
| user_id | BIGINT | NO |  | 用户 ID |
| problem_id | BIGINT | NO |  | 题目 ID |
| language | VARCHAR(16) | NO |  | 语言枚举 |
| verdict | VARCHAR(16) | NO |  | 判题结果 |
| time_ms | BIGINT | NO |  | 运行时间（毫秒） |
| memory_kb | BIGINT | NO |  | 内存（KB） |
| created_at | DATETIME(6) | NO |  | 创建时间（UTC） |
| exported | TINYINT(1) | NO |  | 是否已导出到 HDFS |
| exported_at | DATETIME(6) | YES |  | 导出时间（UTC） |

**analysis_summary_daily**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期（YYYY-MM-DD） |
| total | BIGINT | NO |  | 提交总数 |
| accepted | BIGINT | NO |  | AC 数量 |
| avg_time_ms | DOUBLE | YES |  | 平均耗时 |
| avg_memory_kb | DOUBLE | YES |  | 平均内存 |

**analysis_problem_daily**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期（YYYY-MM-DD） |
| problem_id | BIGINT | NO | PK | 题目 ID |
| total | BIGINT | NO |  | 提交总数 |
| accepted | BIGINT | NO |  | AC 数量 |
| avg_time_ms | DOUBLE | YES |  | 平均耗时 |
| avg_memory_kb | DOUBLE | YES |  | 平均内存 |

**analysis_user_daily**
| Column | Type | Nullable | Key | Description |
| --- | --- | --- | --- | --- |
| dt | VARCHAR(10) | NO | PK | 日期（YYYY-MM-DD） |
| user_id | BIGINT | NO | PK | 用户 ID |
| total | BIGINT | NO |  | 提交总数 |
| accepted | BIGINT | NO |  | AC 数量 |
| avg_time_ms | DOUBLE | YES |  | 平均耗时 |
| avg_memory_kb | DOUBLE | YES |  | 平均内存 |

**Notes**
- 业务表由 JPA 自动生成，实际类型可能因 MySQL 方言略有差异
- 汇总表由 `scripts/analysis_tables.sql` 创建
