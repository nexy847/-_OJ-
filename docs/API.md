**Overview**
本文档覆盖当前系统全部 REST API，并补充每个接口的用途说明。

**Base URL**
`http://localhost:8080`

**Auth**
- 需要登录的接口：请求头带 `Authorization: Bearer <token>`
- 管理员接口：除登录状态外，还要求当前用户具有管理员身份

**Global Error Format**
- 错误码：HTTP 状态码
- 错误信息：纯文本字符串响应体
- 当前全局异常处理：
  - `400 Bad Request`：参数错误、对象不存在、日期格式错误、业务前置条件不满足
  - `403 Forbidden`：未登录、无权访问、非管理员
  - `429 Too Many Requests`：登录或提交触发限流
  - `500 Internal Server Error`：服务内部错误、导出失败等

**Enums**
- `Language`: `C`, `CPP`, `JAVA`, `PYTHON`
- `SubmissionStatus`: `PENDING`, `RUNNING`, `DONE`, `FAILED`
- `Verdict`: `PENDING`, `AC`, `WA`, `TLE`, `MLE`, `RE`, `CE`, `ERROR`
- `DifficultyLabel`: `Easy`, `Medium`, `Hard`

---

**Auth**

`POST /auth/login`
接口用途：用户登录，校验用户名和密码，返回 JWT。
请求方式：POST
入参格式：JSON Body
请求参数：
- `username` (string, required, max 64)：用户名
- `password` (string, required, max 128)：密码
响应结构（成功）：
```json
{
  "token": "<jwt>",
  "expiresAt": "2026-03-20T10:00:00Z"
}
```
错误响应：
- `400`：用户名或密码错误、参数校验失败
- `429`：登录过于频繁
- `500`：服务器内部错误

`GET /auth/me`
接口用途：获取当前登录用户的基础信息和角色，用于前端恢复会话、判断管理员菜单与路由权限。
请求方式：GET
入参格式：无
响应结构（成功）：
```json
{
  "id": 1,
  "username": "u1",
  "role": "ADMIN"
}
```
错误响应：
- `403`：未登录
- `400`：用户不存在
- `500`：服务器内部错误

---

**Users**

`POST /users`
接口用途：注册新用户。
请求方式：POST
入参格式：JSON Body
请求参数：
- `username` (string, required, max 64)
- `password` (string, required, max 128)
响应结构（成功）：
```json
{
  "id": 1,
  "username": "u1",
  "createdAt": "2026-03-20T09:00:00Z"
}
```
错误响应：
- `400`：用户名已存在、参数校验失败
- `500`：服务器内部错误

`GET /users/{id}`
接口用途：查询指定用户的基础信息。普通用户只能查自己，管理员可查任意用户。
请求方式：GET
入参格式：Path 参数
路径参数：
- `id` (long, required)：用户 ID
响应结构（成功）：
```json
{
  "id": 1,
  "username": "u1",
  "createdAt": "2026-03-20T09:00:00Z"
}
```
错误响应：
- `400`：用户不存在
- `403`：非本人且非管理员
- `500`：服务器内部错误

`PUT /users/me`
接口用途：修改当前登录用户的用户名和密码。
请求方式：PUT
入参格式：JSON Body
请求参数：
- `username` (string, required, max 64)：新用户名
- `currentPassword` (string, required)：当前密码，用于校验身份
- `newPassword` (string, optional, min 6, max 128)：新密码，不传则仅修改用户名
响应结构（成功）：
```json
{
  "id": 1,
  "username": "new-name",
  "createdAt": "2026-03-20T09:00:00Z"
}
```
错误响应：
- `400`：参数校验失败、当前密码错误、用户名重复
- `403`：未登录
- `500`：服务器内部错误

---

**Problems**

`GET /problems`
接口用途：查询题目列表。用于题库页面展示所有题目的基础元数据。
请求方式：GET
入参格式：无
响应结构（成功）：
```json
[
  {
    "id": 1,
    "title": "A + B",
    "description": "计算 A + B",
    "timeLimitMs": 1000,
    "memoryLimitMb": 256,
    "createdAt": "2026-03-20T09:10:00Z",
    "testcases": []
  }
]
```
错误响应：
- `500`：服务器内部错误

`POST /problems`
接口用途：管理员创建题目，可同时附带初始测试用例路径或测试用例内容。
请求方式：POST
入参格式：JSON Body
权限：管理员
请求参数：
- `title` (string, required, max 128)
- `description` (string, required, max 4000)
- `timeLimitMs` (int, required)
- `memoryLimitMb` (int, required)
- `testcases` (array, optional)：路径式测试用例数组
- `testcaseContents` (array, optional)：内容式测试用例数组，每项含 `inputContent`、`outputContent`、`weight`
响应结构（成功）：
```json
{
  "id": 1,
  "title": "A + B",
  "description": "计算 A + B",
  "timeLimitMs": 1000,
  "memoryLimitMb": 256,
  "createdAt": "2026-03-20T09:10:00Z",
  "testcases": [
    {
      "id": 1,
      "inputPath": "problem-1/case-1.in",
      "outputPath": "problem-1/case-1.out",
      "weight": 1
    }
  ]
}
```
错误响应：
- `400`：参数校验失败
- `403`：非管理员
- `500`：服务器内部错误

`GET /problems/{id}`
接口用途：查询题目详情。普通用户只看到题目元数据；管理员额外看到测试用例列表。
请求方式：GET
入参格式：Path 参数
路径参数：
- `id` (long, required)：题目 ID
响应结构（成功）：
```json
{
  "id": 1,
  "title": "A + B",
  "description": "计算 A + B",
  "timeLimitMs": 1000,
  "memoryLimitMb": 256,
  "createdAt": "2026-03-20T09:10:00Z",
  "testcases": []
}
```
错误响应：
- `400`：题目不存在
- `500`：服务器内部错误

`PUT /problems/{id}`
接口用途：管理员更新题目元数据，不直接修改测试用例。
请求方式：PUT
入参格式：Path 参数 + JSON Body
权限：管理员
路径参数：
- `id` (long, required)：题目 ID
请求参数：
- `title` (string, required, max 128)
- `description` (string, required, max 4000)
- `timeLimitMs` (int, required)
- `memoryLimitMb` (int, required)
响应结构（成功）：
```json
{
  "id": 1,
  "title": "A + B Updated",
  "description": "新的题目描述",
  "timeLimitMs": 1500,
  "memoryLimitMb": 512,
  "createdAt": "2026-03-20T09:10:00Z",
  "testcases": [
    {
      "id": 1,
      "inputPath": "problem-1/case-1.in",
      "outputPath": "problem-1/case-1.out",
      "weight": 1
    }
  ]
}
```
错误响应：
- `400`：题目不存在、参数校验失败
- `403`：非管理员
- `500`：服务器内部错误

---

**Testcases**

`POST /problems/{id}/testcases`
接口用途：管理员为指定题目新增一个测试用例，输入输出内容由系统写入 `.in/.out` 文件。
请求方式：POST
入参格式：Path 参数 + JSON Body
权限：管理员
路径参数：
- `id` (long, required)：题目 ID
请求参数：
- `inputContent` (string, required)：测试输入内容
- `outputContent` (string, required)：标准输出内容
- `weight` (int, required)：测试点权重
响应结构（成功）：
```json
{
  "id": 2,
  "inputPath": "problem-1/case-acde.in",
  "outputPath": "problem-1/case-acde.out",
  "weight": 1
}
```
错误响应：
- `400`：题目不存在、参数校验失败
- `403`：非管理员
- `500`：服务器内部错误

`GET /problems/{id}/testcases`
接口用途：管理员查询某道题的全部测试用例列表。
请求方式：GET
入参格式：Path 参数
权限：管理员
路径参数：
- `id` (long, required)：题目 ID
响应结构（成功）：
```json
[
  {
    "id": 2,
    "inputPath": "problem-1/case-acde.in",
    "outputPath": "problem-1/case-acde.out",
    "weight": 1
  }
]
```
错误响应：
- `400`：题目不存在
- `403`：非管理员
- `500`：服务器内部错误

`PUT /problems/{problemId}/testcases/{testcaseId}`
接口用途：管理员覆盖更新某个测试用例的输入、输出和权重。
请求方式：PUT
入参格式：Path 参数 + JSON Body
权限：管理员
路径参数：
- `problemId` (long, required)
- `testcaseId` (long, required)
请求参数：
- `inputContent` (string, required)
- `outputContent` (string, required)
- `weight` (int, required)
响应结构（成功）：
```json
{
  "id": 2,
  "inputPath": "problem-1/case-acde.in",
  "outputPath": "problem-1/case-acde.out",
  "weight": 1
}
```
错误响应：
- `400`：测试用例不存在、测试用例不属于题目、参数校验失败
- `403`：非管理员
- `500`：服务器内部错误

`DELETE /problems/{problemId}/testcases/{testcaseId}`
接口用途：管理员删除指定题目的一个测试用例。
请求方式：DELETE
入参格式：Path 参数
权限：管理员
路径参数：
- `problemId` (long, required)
- `testcaseId` (long, required)
响应结构（成功）：无内容，HTTP 204
错误响应：
- `400`：测试用例不存在、测试用例不属于题目
- `403`：非管理员
- `500`：服务器内部错误

---

**Submissions**

`POST /submissions`
接口用途：提交代码到判题系统。接口会创建 submission 记录和待执行判题任务。
请求方式：POST
入参格式：JSON Body
请求参数：
- `problemId` (long, required)
- `language` (enum, required)
- `code` (string, required)
响应结构（成功）：
```json
{
  "id": 1,
  "userId": 1,
  "problemId": 1,
  "language": "CPP",
  "status": "PENDING",
  "verdict": "PENDING",
  "createdAt": "2026-03-20T09:20:00Z",
  "updatedAt": "2026-03-20T09:20:00Z"
}
```
错误响应：
- `400`：参数校验失败、代码过大、题目不存在
- `403`：未登录
- `429`：提交过于频繁
- `500`：服务器内部错误

`GET /submissions/{id}`
接口用途：查询一次提交的当前判题状态和最终 verdict。
请求方式：GET
入参格式：Path 参数
路径参数：
- `id` (long, required)：提交 ID
响应结构（成功）：
```json
{
  "id": 1,
  "userId": 1,
  "problemId": 1,
  "language": "CPP",
  "status": "DONE",
  "verdict": "AC",
  "createdAt": "2026-03-20T09:20:00Z",
  "updatedAt": "2026-03-20T09:21:00Z"
}
```
错误响应：
- `400`：提交不存在
- `403`：非本人且非管理员
- `500`：服务器内部错误

`GET /submissions/{id}/result`
接口用途：查询一次提交的详细判题结果，包括编译错误、运行错误、耗时、内存等。
请求方式：GET
入参格式：Path 参数
路径参数：
- `id` (long, required)：提交 ID
响应结构（成功）：
```json
{
  "submissionId": 1,
  "verdict": "AC",
  "timeMs": 632,
  "memoryKb": 1234,
  "compileError": null,
  "runtimeError": null,
  "message": "Accepted",
  "createdAt": "2026-03-20T09:21:00Z"
}
```
错误响应：
- `400`：结果不存在、提交不存在
- `403`：非本人且非管理员
- `500`：服务器内部错误

---

**Admin**

`POST /admin/export-hdfs`
接口用途：将尚未导出的分析事件批量导出到 HDFS，供 Hive/Spark 做离线分析。
请求方式：POST
入参格式：无
权限：管理员
响应结构（成功）：纯文本
```text
exported=12
```
错误响应：
- `403`：非管理员
- `500`：导出失败

---

**Analytics - Base Daily Stats (管理员)**

`GET /analytics/summary?date={YYYY-MM-DD}`
接口用途：根据日期查询全站总提交数、总 AC 数、平均耗时、平均内存开销。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `date` (string, required)
响应结构（成功）：
```json
{
  "date": "2026-03-20",
  "total": 100,
  "accepted": 80,
  "avgTimeMs": 512.2,
  "avgMemoryKb": 2048.0
}
```
错误响应：
- `400`：日期格式错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/problems/daily?date={YYYY-MM-DD}`
接口用途：根据日期查询每道题的提交总数、AC 数、平均耗时、平均内存开销。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `date` (string, required)
响应结构（成功）：
```json
[
  {
    "problemId": 1,
    "total": 50,
    "accepted": 30,
    "avgTimeMs": 412.5,
    "avgMemoryKb": 1999.0
  }
]
```
错误响应：
- `400`：日期格式错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/users/daily?date={YYYY-MM-DD}`
接口用途：根据日期查询每个用户的提交总数、AC 数、平均耗时、平均内存开销。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `date` (string, required)
响应结构（成功）：
```json
[
  {
    "userId": 1,
    "total": 10,
    "accepted": 7,
    "avgTimeMs": 500.0,
    "avgMemoryKb": 1800.0
  }
]
```
错误响应：
- `400`：日期格式错误
- `403`：非管理员
- `500`：服务器内部错误

---

**Analytics - User Dashboard**

`GET /analytics/user/overview`
接口用途：查询指定用户或当前登录用户的提交总数、AC 数、AC 率、平均耗时、平均内存开销。
请求方式：GET
入参格式：Query 参数
查询参数：
- `userId` (long, optional)：不传时使用当前登录用户
响应结构（成功）：
```json
{
  "userId": 1,
  "total": 12,
  "accepted": 8,
  "acRate": 0.6667,
  "avgTimeMs": 520.5,
  "avgMemoryKb": 1900.0
}
```
错误响应：
- `400`：未登录、用户不存在
- `500`：服务器内部错误

`GET /analytics/user/daily?days={N}&userId={id}`
接口用途：查询指定用户在最近 N 天内每天的提交总数、AC 数、AC 率、平均耗时、平均内存开销。
请求方式：GET
入参格式：Query 参数
查询参数：
- `days` (int, optional, default 30)
- `userId` (long, optional)
响应结构（成功）：
```json
[
  {
    "date": "2026-03-17",
    "total": 3,
    "accepted": 2,
    "acRate": 0.6667,
    "avgTimeMs": 510.0,
    "avgMemoryKb": 1800.0
  }
]
```
错误响应：
- `400`：参数错误、未登录
- `500`：服务器内部错误

`GET /analytics/user/language?userId={id}`
接口用途：查询指定用户在各语言上的提交总数、AC 数、AC 率。
请求方式：GET
入参格式：Query 参数
查询参数：
- `userId` (long, optional)
响应结构（成功）：
```json
[
  { "language": "CPP", "total": 10, "accepted": 7, "acRate": 0.7 }
]
```
错误响应：
- `400`：参数错误、未登录
- `500`：服务器内部错误

`GET /analytics/user/verdict?userId={id}`
接口用途：查询指定用户的 verdict 分布，例如 AC、WA、RE、CE 各有多少次。
请求方式：GET
入参格式：Query 参数
查询参数：
- `userId` (long, optional)
响应结构（成功）：
```json
[
  { "verdict": "AC", "total": 7 },
  { "verdict": "WA", "total": 3 }
]
```
错误响应：
- `400`：参数错误、未登录
- `500`：服务器内部错误

`GET /analytics/user/language/daily?days={N}&userId={id}`
接口用途：查询指定用户最近 N 天内按“日期 + 语言”拆分的提交统计，用于语言趋势图。
请求方式：GET
入参格式：Query 参数
查询参数：
- `days` (int, optional, default 30)
- `userId` (long, optional)
响应结构（成功）：
```json
[
  {
    "date": "2026-03-17",
    "language": "CPP",
    "total": 3,
    "accepted": 2,
    "acRate": 0.6667
  }
]
```
错误响应：
- `400`：参数错误、未登录
- `500`：服务器内部错误

`GET /analytics/user/verdict/daily?days={N}&userId={id}`
接口用途：查询指定用户最近 N 天内按“日期 + verdict”拆分的统计，用于 verdict 变化趋势图。
请求方式：GET
入参格式：Query 参数
查询参数：
- `days` (int, optional, default 30)
- `userId` (long, optional)
响应结构（成功）：
```json
[
  {
    "date": "2026-03-17",
    "verdict": "AC",
    "total": 2
  }
]
```
错误响应：
- `400`：参数错误、未登录
- `500`：服务器内部错误

`GET /analytics/user/problems?limit={N}&userId={id}`
接口用途：查询指定用户在各题目上的提交统计，用于“做题情况 / 题目掌握度”展示。
请求方式：GET
入参格式：Query 参数
查询参数：
- `limit` (int, optional, default 50)
- `userId` (long, optional)
响应结构（成功）：
```json
[
  {
    "problemId": 1,
    "problemTitle": "A + B",
    "total": 5,
    "accepted": 4,
    "acRate": 0.8,
    "avgTimeMs": 420.0,
    "avgMemoryKb": 1500.0
  }
]
```
错误响应：
- `400`：参数错误、未登录
- `500`：服务器内部错误

`GET /analytics/user/recent?limit={N}&userId={id}`
接口用途：查询指定用户最近若干次提交的明细记录。
请求方式：GET
入参格式：Query 参数
查询参数：
- `limit` (int, optional, default 10)
- `userId` (long, optional)
响应结构（成功）：
```json
[
  {
    "submissionId": 12,
    "problemId": 1,
    "problemTitle": "A + B",
    "language": "CPP",
    "verdict": "AC",
    "timeMs": 380,
    "memoryKb": 1200,
    "createdAt": "2026-03-20T09:20:00Z"
  }
]
```
错误响应：
- `400`：参数错误、未登录
- `500`：服务器内部错误

---

**Analytics - Admin Dashboard**

`GET /analytics/admin/overview?date={YYYY-MM-DD}`
接口用途：查询某一天的全站总览，包括提交数、AC 数、AC 率、平均耗时、平均内存和活跃用户数。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `date` (string, optional，默认 UTC 今天)
响应结构（成功）：
```json
{
  "date": "2026-03-17",
  "total": 100,
  "accepted": 80,
  "acRate": 0.8,
  "avgTimeMs": 512.2,
  "avgMemoryKb": 2048.0,
  "activeUsers": 35
}
```
错误响应：
- `400`：日期格式错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/daily?days={N}`
接口用途：查询最近 N 天的全站日趋势，包括每日提交数、AC 数、AC 率、平均耗时、平均内存。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `days` (int, optional, default 30)
响应结构（成功）：
```json
[
  {
    "date": "2026-03-17",
    "total": 100,
    "accepted": 80,
    "acRate": 0.8,
    "avgTimeMs": 512.2,
    "avgMemoryKb": 2048.0
  }
]
```
错误响应：
- `400`：参数错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/language?date={YYYY-MM-DD}`
接口用途：查询某一天的语言分布，统计每种语言的提交数、AC 数、AC 率。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `date` (string, optional，默认 UTC 今天)
响应结构（成功）：
```json
[
  { "language": "CPP", "total": 60, "accepted": 45, "acRate": 0.75 }
]
```
错误响应：
- `400`：日期格式错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/verdict?date={YYYY-MM-DD}`
接口用途：查询某一天的 verdict 分布。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `date` (string, optional，默认 UTC 今天)
响应结构（成功）：
```json
[
  { "verdict": "AC", "total": 80 },
  { "verdict": "WA", "total": 20 }
]
```
错误响应：
- `400`：日期格式错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/language/daily?days={N}`
接口用途：查询最近 N 天的“日期 + 语言”统计，用于平台语言趋势图。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `days` (int, optional, default 30)
响应结构（成功）：
```json
[
  {
    "date": "2026-03-17",
    "language": "CPP",
    "total": 25,
    "accepted": 18,
    "acRate": 0.72,
    "avgTimeMs": 420.0,
    "avgMemoryKb": 1500.0
  }
]
```
错误响应：
- `400`：参数错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/verdict/daily?days={N}`
接口用途：查询最近 N 天的“日期 + verdict”统计，用于 verdict 趋势图。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `days` (int, optional, default 30)
响应结构（成功）：
```json
[
  {
    "date": "2026-03-17",
    "verdict": "AC",
    "total": 18
  }
]
```
错误响应：
- `400`：参数错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/hourly?date={YYYY-MM-DD}`
接口用途：查询某一天 24 小时内每小时的提交数、AC 数、AC 率、活跃用户数。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `date` (string, optional，默认 UTC 今天)
响应结构（成功）：
```json
[
  {
    "date": "2026-03-17",
    "hourOfDay": 14,
    "total": 12,
    "accepted": 8,
    "acRate": 0.6667,
    "activeUsers": 6
  }
]
```
错误响应：
- `400`：日期格式错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/problems/{problemId}/verdict?date={YYYY-MM-DD}`
接口用途：查询某一天某道题的 verdict 分布。
请求方式：GET
入参格式：Path 参数 + Query 参数
权限：管理员
路径参数：
- `problemId` (long, required)
查询参数：
- `date` (string, optional，默认 UTC 今天)
响应结构（成功）：
```json
[
  {
    "date": "2026-03-17",
    "problemId": 4,
    "verdict": "WA",
    "total": 6
  }
]
```
错误响应：
- `400`：日期格式错误或参数错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/problems/top?date={YYYY-MM-DD}&limit={N}`
接口用途：查询某一天提交量最高的题目排行，并返回通过率、平均耗时、平均内存等指标。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `date` (string, optional，默认 UTC 今天)
- `limit` (int, optional, default 20)
响应结构（成功）：
```json
[
  {
    "problemId": 1,
    "problemTitle": "A + B",
    "total": 50,
    "accepted": 30,
    "acRate": 0.6,
    "avgTimeMs": 410.0,
    "avgMemoryKb": 1900.0
  }
]
```
错误响应：
- `400`：参数错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/users/top?date={YYYY-MM-DD}&limit={N}`
接口用途：查询某一天提交量最高的用户排行，并返回通过率。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `date` (string, optional，默认 UTC 今天)
- `limit` (int, optional, default 20)
响应结构（成功）：
```json
[
  {
    "userId": 1,
    "username": "u1",
    "total": 10,
    "accepted": 7,
    "acRate": 0.7
  }
]
```
错误响应：
- `400`：参数错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/forecast/submissions?forecastDate={YYYY-MM-DD}`
接口用途：查询 Spark 生成的未来每日提交量预测结果；不传 `forecastDate` 时返回最新一批预测。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `forecastDate` (string, optional)
响应结构（成功）：
```json
[
  {
    "forecastDate": "2026-04-01",
    "targetDate": "2026-04-02",
    "predictedSubmissions": 18,
    "modelName": "linear_regression_v1"
  }
]
```
错误响应：
- `400`：日期格式错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/problems/difficulty?date={YYYY-MM-DD}&limit={N}&label={label}`
接口用途：查询某一天各题目的难度评估结果，包括 AC 率、平均耗时、平均尝试次数、难度分数和难度标签。
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
- `date` (string, optional，默认 UTC 今天)
- `limit` (int, optional, default 50)
- `label` (string, optional，可选值 `Easy` / `Medium` / `Hard`)
响应结构（成功）：
```json
[
  {
    "date": "2026-04-01",
    "problemId": 4,
    "problemTitle": "Bracket Sequence",
    "totalSubmissions": 26,
    "acceptedSubmissions": 10,
    "acRate": 0.3846,
    "avgTimeMs": 531.23,
    "avgMemoryKb": 1024.0,
    "avgAttemptsPerUser": 2.1667,
    "waRate": 0.3077,
    "reRate": 0.1154,
    "tleRate": 0.0385,
    "difficultyScore": 0.6412,
    "difficultyLabel": "Medium",
    "modelName": "rule_based_v1"
  }
]
```
错误响应：
- `400`：日期格式错误或参数错误
- `403`：非管理员
- `500`：服务器内部错误

`GET /analytics/admin/problems/{problemId}/difficulty/history?days={N}`
接口用途：查询某道题最近 N 天的难度分数变化历史，用于难度趋势图。
请求方式：GET
入参格式：Path 参数 + Query 参数
权限：管理员
路径参数：
- `problemId` (long, required)
查询参数：
- `days` (int, optional, default 30)
响应结构（成功）：
```json
[
  {
    "date": "2026-04-01",
    "difficultyScore": 0.6412,
    "difficultyLabel": "Medium",
    "acRate": 0.3846,
    "avgTimeMs": 531.23,
    "avgAttemptsPerUser": 2.1667
  }
]
```
错误响应：
- `400`：参数错误
- `403`：非管理员
- `500`：服务器内部错误
