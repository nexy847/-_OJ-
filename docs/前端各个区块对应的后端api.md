我对照前端源码给你逐项对应。  
结论先说：**很多页面区块不是一个区块一个 API，而是多个区块共用同一个 API 返回的数据。**

对应源码：
- 用户页：`/D:/oj_system_with_codex/web/src/pages/UserAnalyticsPage.tsx`
- 题目难度页：`/D:/oj_system_with_codex/web/src/pages/admin/AdminProblemDifficultyPage.tsx`
- 系统分析页：`/D:/oj_system_with_codex/web/src/pages/admin/AdminAnalyticsPage.tsx`

---

**1. `/analytics/user` 页面：My Analytics**

你问的这些区块对应关系如下：

- `30-Day Submission Trend`
  - API：`GET /analytics/user/daily?days=30`

- `30-Day AC Rate`
  - API：`GET /analytics/user/daily?days=30`
  - 和上面共用同一个接口，只是前端取了 `acRate`

- `Language Distribution`
  - API：`GET /analytics/user/language`

- `Verdict Distribution`
  - API：`GET /analytics/user/verdict`

- `Daily Language Activity`
  - API：`GET /analytics/user/language/daily?days=30`

- `Daily Verdict Activity`
  - API：`GET /analytics/user/verdict/daily?days=30`

- `Problem Stats`
  - API：`GET /analytics/user/problems?limit=50`

- `Recent Submissions`
  - API：`GET /analytics/user/recent?limit=10`

补充：这个页面顶部其实还有 5 个卡片，也来自：
- `Total Submissions`
- `Accepted`
- `AC Rate`
- `Average Time`
- `Average Memory`
  - API：`GET /analytics/user/overview`

---

**2. `/admin/problems/difficulty` 页面：Problem Difficulty**

这个页面里，很多区块都是共用同一个“难度列表”接口。

**共用的主接口：**
- `GET /analytics/admin/problems/difficulty?date=2026-04-07&limit=100&label=...`

这个接口返回整批题目的难度数据，前端再自己做统计和画图。

对应关系：

- `Tracked Problems`
  - API：`GET /analytics/admin/problems/difficulty?...`
  - 前端取返回数组长度 `rows.length`

- `Hard Problems`
  - API：`GET /analytics/admin/problems/difficulty?...`
  - 前端统计 `difficultyLabel === 'Hard'` 的数量

- `Medium Problems`
  - API：`GET /analytics/admin/problems/difficulty?...`
  - 前端统计 `difficultyLabel === 'Medium'` 的数量

- `Average Difficulty`
  - API：`GET /analytics/admin/problems/difficulty?...`
  - 前端对 `difficultyScore` 求平均

- `Difficulty Label Distribution (2026-04-07)`
  - API：`GET /analytics/admin/problems/difficulty?...`
  - 前端把返回数据按 `Easy / Medium / Hard` 分组画饼图

- `Top Hard Problems (2026-04-07)`
  - API：`GET /analytics/admin/problems/difficulty?...`
  - 前端直接取返回结果前 10 条画图
  - 因为后端本身已经按 `difficulty_score desc` 排序

- `Problem Difficulty Details (2026-04-07)`
  - API：`GET /analytics/admin/problems/difficulty?...`
  - 直接把返回数组渲染成表格

只有这个区块是另一个接口：

- `Difficulty History`
  - API：`GET /analytics/admin/problems/{problemId}/difficulty/history?days=30`
  - 这里的 `problemId` 来自页面右上角下拉框当前选中的题目
  - 如果没选，默认取难度列表中的第一题

---

**3. `/admin/analytics` 页面：System Analytics**

这个页面的区块对应关系如下。

**顶部 6 个卡片，共用一个接口：**
- `Daily Submissions`
- `Daily Accepted`
- `Daily AC Rate`
- `Average Time`
- `Average Memory`
- `Active Users`

对应 API：
- `GET /analytics/admin/overview?date=2026-04-07`

---

**这两个区块共用一个接口：**
- `30-Day Trend`
- `30-Day AC Rate`

对应 API：
- `GET /analytics/admin/daily?days=30`

说明：
- `30-Day Trend` 用的是 `total` 和 `accepted`
- `30-Day AC Rate` 用的是同一批数据里的 `acRate`

---

**单日分布类：**
- `Language Distribution (2026-04-07)`
  - API：`GET /analytics/admin/language?date=2026-04-07`

- `Verdict Distribution (2026-04-07)`
  - API：`GET /analytics/admin/verdict?date=2026-04-07`

---

**多日趋势类：**
- `30-Day Language Trend`
  - API：`GET /analytics/admin/language/daily?days=30`

- `30-Day Verdict Trend`
  - API：`GET /analytics/admin/verdict/daily?days=30`

---

**小时活跃：**
- `Hourly Activity (2026-04-07)`
  - API：`GET /analytics/admin/hourly?date=2026-04-07`

---

**题目相关：**
- `Top Problems (2026-04-07)`
  - API：`GET /analytics/admin/problems/top?date=2026-04-07&limit=20`

- `Problem Verdict Breakdown`
  - 先用：`GET /analytics/admin/problems/top?date=2026-04-07&limit=20`
  - 前端取 `topProblems[0].problemId`
  - 再调：`GET /analytics/admin/problems/{problemId}/verdict?date=2026-04-07`

也就是说这个区块实际上依赖 **两个步骤**：
1. 先拿 Top Problems
2. 再拿第一题的 verdict 分布

如果 `Top Problems` 为空，这个接口不会发出去。

---

**用户相关：**
- `Top Users (2026-04-07)`
  - API：`GET /analytics/admin/users/top?date=2026-04-07&limit=20`

---

**4. 你可以直接记成这张简表**

**`/analytics/user`**
- `30-Day Submission Trend` -> `/analytics/user/daily?days=30`
- `30-Day AC Rate` -> `/analytics/user/daily?days=30`
- `Language Distribution` -> `/analytics/user/language`
- `Verdict Distribution` -> `/analytics/user/verdict`
- `Daily Language Activity` -> `/analytics/user/language/daily?days=30`
- `Daily Verdict Activity` -> `/analytics/user/verdict/daily?days=30`
- `Problem Stats` -> `/analytics/user/problems?limit=50`
- `Recent Submissions` -> `/analytics/user/recent?limit=10`

**`/admin/problems/difficulty`**
- `Tracked Problems` -> `/analytics/admin/problems/difficulty?...`
- `Hard Problems` -> `/analytics/admin/problems/difficulty?...`
- `Medium Problems` -> `/analytics/admin/problems/difficulty?...`
- `Average Difficulty` -> `/analytics/admin/problems/difficulty?...`
- `Difficulty Label Distribution` -> `/analytics/admin/problems/difficulty?...`
- `Top Hard Problems` -> `/analytics/admin/problems/difficulty?...`
- `Difficulty History` -> `/analytics/admin/problems/{problemId}/difficulty/history?days=30`
- `Problem Difficulty Details` -> `/analytics/admin/problems/difficulty?...`

**`/admin/analytics`**
- `Daily Submissions` -> `/analytics/admin/overview?date=...`
- `Daily Accepted` -> `/analytics/admin/overview?date=...`
- `Daily AC Rate` -> `/analytics/admin/overview?date=...`
- `Average Time` -> `/analytics/admin/overview?date=...`
- `Average Memory` -> `/analytics/admin/overview?date=...`
- `Active Users` -> `/analytics/admin/overview?date=...`
- `30-Day Trend` -> `/analytics/admin/daily?days=30`
- `30-Day AC Rate` -> `/analytics/admin/daily?days=30`
- `Language Distribution` -> `/analytics/admin/language?date=...`
- `Verdict Distribution` -> `/analytics/admin/verdict?date=...`
- `30-Day Language Trend` -> `/analytics/admin/language/daily?days=30`
- `30-Day Verdict Trend` -> `/analytics/admin/verdict/daily?days=30`
- `Hourly Activity` -> `/analytics/admin/hourly?date=...`
- `Problem Verdict Breakdown` -> 先 `/analytics/admin/problems/top?...`，再 `/analytics/admin/problems/{problemId}/verdict?date=...`
- `Top Problems` -> `/analytics/admin/problems/top?date=...&limit=20`
- `Top Users` -> `/analytics/admin/users/top?date=...&limit=20`

如果你要，我下一步可以继续给你一份：
**“前端页面区块 -> 后端 API -> 数据表来源”** 三列表。