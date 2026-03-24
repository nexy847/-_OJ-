**Overview**
本文档包含当前系统的全部 REST API。路径参数使用 `{}` 占位符。

**Base URL**
`http://localhost:8080`

**Auth**
- 登录获取 JWT
- 需要认证的接口需在请求头加入：`Authorization: Bearer <token>`

**Error Response (全局)**
- 错误响应为纯文本字符串
- HTTP 状态码即错误码
- 响应体内容即错误信息
- 示例：`403 Forbidden`，Body 为 `Forbidden`

**Enums**
- `Language`: `C`, `CPP`, `JAVA`, `PYTHON`
- `SubmissionStatus`: `PENDING`, `RUNNING`, `DONE`, `FAILED`
- `Verdict`: `PENDING`, `AC`, `WA`, `TLE`, `MLE`, `RE`, `CE`, `ERROR`

---

**Auth**

`POST /auth/login`
请求方式：POST
入参格式：JSON Body
请求参数：
`username` (string, required) 用户名
`password` (string, required) 密码
响应结构（成功）：
```json
{
  "token": "<jwt>",
  "expiresAt": "2026-03-20T10:00:00Z"
}
```
错误（失败）：
`400` 参数错误
`403` 权限不足
`500` 服务器错误
错误信息：纯文本字符串

---

**Users**

`POST /users`
请求方式：POST
入参格式：JSON Body
请求参数：
`username` (string, required)
`password` (string, required)
响应结构（成功）：
```json
{
  "id": 1,
  "username": "u1",
  "createdAt": "2026-03-20T09:00:00Z"
}
```
错误（失败）：
`400` 用户名已存在或参数错误
`500` 服务器错误
错误信息：纯文本字符串

`GET /users/{id}`
请求方式：GET
入参格式：Path 参数
路径参数：
`id` (long, required) 用户 ID
响应结构（成功）：
```json
{
  "id": 1,
  "username": "u1",
  "createdAt": "2026-03-20T09:00:00Z"
}
```
错误（失败）：
`403` 非本人且非管理员
`400` 用户不存在
`500` 服务器错误
错误信息：纯文本字符串

---

**Problems**

`POST /problems`
请求方式：POST
入参格式：JSON Body
权限：管理员
请求参数：
`title` (string, required)
`description` (string, required)
`timeLimitMs` (int, required)
`memoryLimitMb` (int, required)
`testcases` (array, optional) 仅路径式录入
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
错误（失败）：
`403` 非管理员
`400` 参数错误
`500` 服务器错误
错误信息：纯文本字符串

`GET /problems/{id}`
请求方式：GET
入参格式：Path 参数
路径参数：
`id` (long, required) 题目 ID
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
说明：普通用户返回空 `testcases`，管理员返回测试用例列表。
错误（失败）：
`400` 题目不存在
`500` 服务器错误
错误信息：纯文本字符串

---

**Testcases**

`POST /problems/{id}/testcases`
请求方式：POST
入参格式：Path 参数 + JSON Body
权限：管理员
路径参数：
`id` (long, required) 题目 ID
请求参数：
`inputContent` (string, required)
`outputContent` (string, required)
`weight` (int, required)
响应结构（成功）：
```json
{
  "id": 2,
  "inputPath": "problem-1/case-acde.in",
  "outputPath": "problem-1/case-acde.out",
  "weight": 1
}
```
错误（失败）：
`403` 非管理员
`400` 题目不存在或参数错误
`500` 服务器错误
错误信息：纯文本字符串

`GET /problems/{id}/testcases`
请求方式：GET
入参格式：Path 参数
权限：管理员
路径参数：
`id` (long, required) 题目 ID
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
错误（失败）：
`403` 非管理员
`400` 题目不存在
`500` 服务器错误
错误信息：纯文本字符串

`PUT /problems/{problemId}/testcases/{testcaseId}`
请求方式：PUT
入参格式：Path 参数 + JSON Body
权限：管理员
路径参数：
`problemId` (long, required)
`testcaseId` (long, required)
请求参数：
`inputContent` (string, required)
`outputContent` (string, required)
`weight` (int, required)
响应结构（成功）：
```json
{
  "id": 2,
  "inputPath": "problem-1/case-acde.in",
  "outputPath": "problem-1/case-acde.out",
  "weight": 1
}
```
错误（失败）：
`403` 非管理员
`400` 测试用例不存在或不属于该题目
`500` 服务器错误
错误信息：纯文本字符串

`DELETE /problems/{problemId}/testcases/{testcaseId}`
请求方式：DELETE
入参格式：Path 参数
权限：管理员
路径参数：
`problemId` (long, required)
`testcaseId` (long, required)
响应结构（成功）：无内容，HTTP 204
错误（失败）：
`403` 非管理员
`400` 测试用例不存在或不属于该题目
`500` 服务器错误
错误信息：纯文本字符串

---

**Submissions**

`POST /submissions`
请求方式：POST
入参格式：JSON Body
请求参数：
`problemId` (long, required)
`language` (enum, required)
`code` (string, required)
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
错误（失败）：
`400` 参数错误或代码超限
`403` 未登录
`429` 提交过于频繁
`500` 服务器错误
错误信息：纯文本字符串

`GET /submissions/{id}`
请求方式：GET
入参格式：Path 参数
路径参数：
`id` (long, required) 提交 ID
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
错误（失败）：
`403` 非本人且非管理员
`400` 提交不存在
`500` 服务器错误
错误信息：纯文本字符串

`GET /submissions/{id}/result`
请求方式：GET
入参格式：Path 参数
路径参数：
`id` (long, required) 提交 ID
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
错误（失败）：
`403` 非本人且非管理员
`400` 结果不存在
`500` 服务器错误
错误信息：纯文本字符串

---

**Admin**

`POST /admin/export-hdfs`
请求方式：POST
入参格式：无
权限：管理员
响应结构（成功）：
```
exported=<number>
```
错误（失败）：
`403` 非管理员
`500` 导出失败
错误信息：纯文本字符串

---

**Analytics (基础汇总接口，管理员)**

`GET /analytics/summary?date={YYYY-MM-DD}`
请求方式：GET
入参格式：Query 参数
查询参数：
`date` (string, required) 日期
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
错误（失败）：
`403` 非管理员
`400` 日期格式错误
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/problems/daily?date={YYYY-MM-DD}`
请求方式：GET
入参格式：Query 参数
查询参数：
`date` (string, required)
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
错误（失败）：
`403` 非管理员
`400` 日期格式错误
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/users/daily?date={YYYY-MM-DD}`
请求方式：GET
入参格式：Query 参数
查询参数：
`date` (string, required)
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
错误（失败）：
`403` 非管理员
`400` 日期格式错误
`500` 服务器错误
错误信息：纯文本字符串

---

**Analytics (用户页，登录即可)**

`GET /analytics/user/overview`
请求方式：GET
入参格式：Query 参数（可选）
查询参数：
`userId` (long, optional) 不传则使用当前登录用户
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
错误（失败）：
`400` 未登录或用户不存在
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/user/daily?days={N}`
请求方式：GET
入参格式：Query 参数
查询参数：
`days` (int, optional, default 30)
`userId` (long, optional)
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
错误（失败）：
`400` 参数错误或未登录
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/user/language`
请求方式：GET
入参格式：Query 参数（可选）
查询参数：
`userId` (long, optional)
响应结构（成功）：
```json
[
  { "language": "CPP", "total": 10, "accepted": 7, "acRate": 0.7 }
]
```
错误（失败）：
`400` 参数错误或未登录
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/user/verdict`
请求方式：GET
入参格式：Query 参数（可选）
查询参数：
`userId` (long, optional)
响应结构（成功）：
```json
[
  { "verdict": "AC", "total": 7 },
  { "verdict": "WA", "total": 3 }
]
```
错误（失败）：
`400` 参数错误或未登录
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/user/problems?limit={N}`
请求方式：GET
入参格式：Query 参数
查询参数：
`limit` (int, optional, default 50)
`userId` (long, optional)
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
错误（失败）：
`400` 参数错误或未登录
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/user/recent?limit={N}`
请求方式：GET
入参格式：Query 参数
查询参数：
`limit` (int, optional, default 10)
`userId` (long, optional)
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
错误（失败）：
`400` 参数错误或未登录
`500` 服务器错误
错误信息：纯文本字符串

---

**Analytics (管理员页)**

`GET /analytics/admin/overview?date={YYYY-MM-DD}`
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
`date` (string, optional，默认 UTC 今天)
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
错误（失败）：
`403` 非管理员
`400` 日期格式错误
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/admin/daily?days={N}`
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
`days` (int, optional, default 30)
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
错误（失败）：
`403` 非管理员
`400` 参数错误
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/admin/language?date={YYYY-MM-DD}`
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
`date` (string, optional，默认 UTC 今天)
响应结构（成功）：
```json
[
  { "language": "CPP", "total": 60, "accepted": 45, "acRate": 0.75 }
]
```
错误（失败）：
`403` 非管理员
`400` 日期格式错误
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/admin/verdict?date={YYYY-MM-DD}`
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
`date` (string, optional，默认 UTC 今天)
响应结构（成功）：
```json
[
  { "verdict": "AC", "total": 80 },
  { "verdict": "WA", "total": 20 }
]
```
错误（失败）：
`403` 非管理员
`400` 日期格式错误
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/admin/problems/top?date={YYYY-MM-DD}&limit={N}`
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
`date` (string, optional，默认 UTC 今天)
`limit` (int, optional, default 20)
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
错误（失败）：
`403` 非管理员
`400` 参数错误
`500` 服务器错误
错误信息：纯文本字符串

`GET /analytics/admin/users/top?date={YYYY-MM-DD}&limit={N}`
请求方式：GET
入参格式：Query 参数
权限：管理员
查询参数：
`date` (string, optional，默认 UTC 今天)
`limit` (int, optional, default 20)
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
错误（失败）：
`403` 非管理员
`400` 参数错误
`500` 服务器错误
错误信息：纯文本字符串
