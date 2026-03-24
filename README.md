# OJ System (Spring Boot + Docker + Hadoop)

This is a minimal OJ system that follows the requested plan:
- Spring Boot monolith for users/problems/submissions/results.
- Judge worker executes submissions in Docker containers.
- Analysis events are exported to HDFS for Hive/Spark.

## Requirements
- Java 17+
- Maven
- Docker CLI accessible on the host
- MySQL 8+ (or compatible)
- Hadoop HDFS reachable from this machine

## Quick Start

```
mvn spring-boot:run
```

Initialize MySQL database (includes analysis tables):

```
mysql -u root -p < .\scripts\init_mysql.sql
```

## Configuration
Edit `src/main/resources/application.yml` or set environment variables:

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `OJ_WORK_DIR`, `OJ_TESTCASE_DIR`
- `HDFS_URI`, `HDFS_DIR`, `HADOOP_USER`, `HADOOP_CONF_DIR`
- `JWT_SECRET`, `JWT_EXP_MINUTES`, `ADMIN_USERS`
- `OJ_MAX_CONCURRENCY`, `OJ_MAX_RETRIES`, `OJ_RETRY_DELAY_SECONDS`, `OJ_MAX_OUTPUT_KB`, `OJ_MAX_CODE_SIZE_KB`
- `SUBMIT_PER_MINUTE`, `LOGIN_PER_MINUTE`

`ADMIN_USERS` is a comma-separated list of usernames that can access `/admin/*` and manage problems/testcases.

If your Hadoop `fs.defaultFS` is not `hdfs://192.168.174.10:9000`, update `HDFS_URI`
to match `core-site.xml`.
If available, set `HADOOP_CONF_DIR` to a directory containing `core-site.xml` and `hdfs-site.xml`.

## Testcase Layout
Place input/output files under `data/testcases` and reference them by relative path
when creating a problem. Example:

```
D:\oj_system_with_codex\data\testcases\sum\1.in
D:\oj_system_with_codex\data\testcases\sum\1.out
```

Create problem with testcases:

```
POST /problems
{
  "title": "Sum",
  "description": "Add two integers",
  "timeLimitMs": 1000,
  "memoryLimitMb": 256,
  "testcases": [
    {"inputPath": "sum/1.in", "outputPath": "sum/1.out", "weight": 1}
  ]
}
```

Upload testcase content:

```
POST /problems/1/testcases
{
  "inputContent": "1 2\n",
  "outputContent": "3\n",
  "weight": 1
}
```

Login to get JWT:

```
POST /auth/login
{
  "username": "u1",
  "password": "123456"
}
```

Then send `Authorization: Bearer <token>` for all protected APIs.
`/admin/*`, problem creation, and testcase management endpoints require an admin user (set via `ADMIN_USERS`).

Submission creation ignores any client-supplied user id and uses the authenticated user.

Create submission:

```
POST /submissions
{
  "problemId": 1,
  "language": "CPP",
  "code": "#include <bits/stdc++.h>\nusing namespace std;int main(){long long a,b; if(!(cin>>a>>b)) return 0; cout<<a+b; return 0;}"
}
```

## Hadoop Analysis (Option A: Hive/Spark -> MySQL)

This approach keeps Hive on the cluster and writes aggregated results back to MySQL.
The application only reads MySQL for analytics (no Hive JDBC in the app).

### 1) Run daily aggregation on node1
Copy these scripts to node1 (or run from this repo if shared):
- `scripts/hive_agg_daily.sql`
- `scripts/agg_daily.sh`

Run (example for 2026-03-18):

```
chmod +x ./agg_daily.sh
MYSQL_HOST=<mysql_ip> MYSQL_USER=root MYSQL_PASS=12365478910vr ./agg_daily.sh 2026-03-18
```

The script:
- Uses Hive to aggregate `analysis_events` into HDFS directories under `/oj/analysis_agg/...`
- Uses Sqoop to export results into MySQL tables

### 2) Query analytics from the app (admin)

```
GET /analytics/summary?date=2026-03-18
GET /analytics/problems/daily?date=2026-03-18
GET /analytics/users/daily?date=2026-03-18
```

## Judge Images
This project expects images named in `application.yml`:
- `oj-gcc` for C/C++
- `oj-java` for Java
- `oj-python` for Python

Build images:

```
docker build -t oj-gcc .\docker\oj-gcc
docker build -t oj-java .\docker\oj-java
docker build -t oj-python .\docker\oj-python
```

## API Summary
- `POST /users` (register)
- `POST /auth/login` (get JWT)
- `POST /problems`, `GET /problems/{id}`
- `POST /problems/{id}/testcases` (upload testcase content)
- `GET /problems/{id}/testcases` (admin list)
- `PUT /problems/{problemId}/testcases/{testcaseId}` (overwrite)
- `DELETE /problems/{problemId}/testcases/{testcaseId}` (delete)
- `POST /submissions`, `GET /submissions/{id}`, `GET /submissions/{id}/result`
- `POST /admin/export-hdfs`
- `GET /analytics/summary?date=YYYY-MM-DD`
- `GET /analytics/problems/daily?date=YYYY-MM-DD`
- `GET /analytics/users/daily?date=YYYY-MM-DD`

## Notes
- Output comparison trims trailing whitespace and ignores trailing blank lines.
- Runtime uses `/usr/bin/time` in the judge images to capture time/memory.
- Judge worker runs on a fixed delay from config.
- Submissions are tied to the authenticated user; users can only access their own submissions (admins can access all).
- Docker is run with `no-new-privileges` and dropped capabilities for sandboxing.
- Simple in-memory rate limits apply to login and submissions.
- Output size and code size limits are enforced via `OJ_MAX_OUTPUT_KB` and `OJ_MAX_CODE_SIZE_KB`.
- For HDFS access on Windows, you may need `HADOOP_CONF_DIR` and a valid client setup.
- For Docker Desktop on Windows, make sure the drive containing `OJ_WORK_DIR` and `OJ_TESTCASE_DIR` is shared.
