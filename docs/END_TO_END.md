**Overview**
本文档提供从注册到判题，再到分析查询的端到端调用示例。

**curl 流程**
1. 注册用户
```bash
curl -s -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"username":"u1","password":"123456"}'
```

2. 登录并获取 token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"u1","password":"123456"}' | jq -r .token)
```

3. 创建题目（管理员）
```bash
curl -s -X POST http://localhost:8080/problems \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{
    "title":"A + B",
    "description":"计算 A + B",
    "timeLimitMs":1000,
    "memoryLimitMb":256,
    "testcases":[]
  }'
```

4. 添加测试用例内容（管理员）
```bash
curl -s -X POST http://localhost:8080/problems/1/testcases \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{"inputContent":"1 2\n","outputContent":"3\n","weight":1}'
```

5. 提交代码
```bash
curl -s -X POST http://localhost:8080/submissions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{"problemId":1,"language":"CPP","code":"#include <bits/stdc++.h>\nint main(){int a,b; if(!(std::cin>>a>>b)) return 0; std::cout<<a+b; }"}'
```

6. 查询提交状态
```bash
curl -s -X GET http://localhost:8080/submissions/1 \
  -H "Authorization: Bearer ${TOKEN}"
```

7. 查询判题结果
```bash
curl -s -X GET http://localhost:8080/submissions/1/result \
  -H "Authorization: Bearer ${TOKEN}"
```

8. 导出分析事件到 HDFS（管理员）
```bash
curl -s -X POST http://localhost:8080/admin/export-hdfs \
  -H "Authorization: Bearer ${TOKEN}"
```

9. 分析查询（管理员）
```bash
curl -s -X GET "http://localhost:8080/analytics/summary?date=2026-03-17" \
  -H "Authorization: Bearer ${TOKEN}"
```

**PowerShell 流程**
1. 注册用户
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/users `
  -ContentType "application/json" `
  -Body '{"username":"u1","password":"123456"}'
```

2. 登录并获取 token
```powershell
$token = (Invoke-RestMethod -Method Post -Uri http://localhost:8080/auth/login `
  -ContentType "application/json" `
  -Body '{"username":"u1","password":"123456"}').token
$headers = @{ Authorization = "Bearer $token" }
```

3. 创建题目（管理员）
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/problems `
  -ContentType "application/json" `
  -Headers $headers `
  -Body '{"title":"A + B","description":"计算 A + B","timeLimitMs":1000,"memoryLimitMb":256,"testcases":[]}'
```

4. 添加测试用例内容（管理员）
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/problems/1/testcases `
  -ContentType "application/json" `
  -Headers $headers `
  -Body '{"inputContent":"1 2\n","outputContent":"3\n","weight":1}'
```

5. 提交代码
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/submissions `
  -ContentType "application/json" `
  -Headers $headers `
  -Body '{"problemId":1,"language":"CPP","code":"#include <bits/stdc++.h>\nint main(){int a,b; if(!(std::cin>>a>>b)) return 0; std::cout<<a+b; }"}'
```

6. 查询提交状态
```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/submissions/1 `
  -Headers $headers
```

7. 查询判题结果
```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/submissions/1/result `
  -Headers $headers
```

8. 导出分析事件到 HDFS（管理员）
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/admin/export-hdfs `
  -Headers $headers
```

9. 分析查询（管理员）
```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/analytics/summary?date=2026-03-17" `
  -Headers $headers
```

**Notes**
- 管理员用户名由 `ADMIN_USERS` 配置决定
- 导出日期按 UTC 计算，分析查询用导出分区日期
- `jq` 仅用于解析 token，可改用手动复制
