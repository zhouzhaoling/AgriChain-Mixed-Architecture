# 🚀 AgriChain-Mixed-Architecture (企业级 AI Loop 混合微服务架构)

本项目是一个面向农牧产业链（如生猪养殖、饲料供应）的**企业级多 Agent 智能决策平台**原型。核心目标是演示如何通过 **“Java 主干管控 + Python 算法微服务”** 的分布式架构，落地具备**自我进化（AI Loop）**能力的智能体系统。

项目不仅是代码实现，更是对传统“单体式 Prompt 调优”的降维打击，彻底解决大模型应用中“改一个坏一片”和“无法沉淀经验”的规模化落地难题。

## 🏗️ 核心架构设计

本仓库采用 **Monorepo（单仓多模块）** 结构管理，实现了业务逻辑与 AI 推理的彻底解耦：

```text
AgriChain-Mixed-Architecture/
├── java-backend/          # ☕ Java 主干 (Spring Boot 3 + H2)
│   ├── controller/        # 接口层：接收 BadCase、人工审批、用户提问
│   ├── service/           # 业务层：调用 Python 微服务、动态拼装 Prompt
│   ├── entity/            # 实体层：AI 规则状态机 (PENDING/ACTIVE)
│   └── repository/        # 持久层：Spring Data JPA
│
└── python-reflect-service/ # 🐍 Python 微服务 (FastAPI + LangChain)
    ├── main.py            # 算法层：接收 Java 请求，调用本地大模型进行反思 (Reflection)
    └── requirements.txt
```

### 📡 跨语言交互流 (AI Loop 闭环)

1. **BadCase 输入**：Java 接收到错误路由的用户提问。
2. **跨服务反思**：Java 通过 `RestTemplate` 发起 HTTP 请求，将 BadCase 传给 Python 微服务。Python 调用 Ollama (Qwen2.5) 提炼出“避坑规则”。
3. **状态机管控**：Java 将规则存入 H2 数据库，状态置为 `PENDING`（待人工审批）。
4. **动态回注**：规则被审批通过变为 `ACTIVE` 后，Java 在下一次主路由时，会从数据库读取所有生效规则，动态拼装至 System Prompt 中，实现**系统自我进化**。

## 🛠️ 技术栈

| 模块 | 技术选型 | 核心职责 |
| :--- | :--- | :--- |
| **Java 主干** | Spring Boot 3.4, Spring Data JPA, H2 Database | 业务状态机管理、多租户隔离、规则持久化与 Prompt 回注 |
| **Python 微服务** | Python 3.10, FastAPI, Pydantic | 独立的高性能 AI 算法推理与 HTTP 接口暴露 |
| **LLM 引擎** | Ollama (Qwen2.5-7B), LangChain | 本地私有化部署的模型反思与规则提炼引擎 |
| **工具链** | Maven, Pip, Apifox | 依赖管理与 API 联调 |

## 🚀 快速启动

### 1. 启动 Python AI 微服务 (端口 8000)
```bash
cd python-reflect-service
python -m venv .venv
.venv\Scripts\activate      # Windows
pip install -r requirements.txt
# 确保本地 Ollama 已启动并拉取模型: ollama run qwen2.5:7b
uvicorn main:app --reload --port 8000
```

### 2. 启动 Java 主干服务 (端口 8080)
```bash
cd java-backend
# IDEA 导入项目，等待 Maven 依赖拉取完成
# 运行 JavaBackendApplication.java
```
*(注：项目使用 H2 内存数据库，重启即重置，方便快速验证)*

## 🎬 核心场景演示 (通过 Apifox / Postman 测试)

### 场景一：AI 产生 BadCase 并反思沉淀
- **请求**: `POST http://localhost:8080/api/feedback/submit`
- **Body**: 
  ```json
  {
    "userQuery": "生猪不吃食",
    "wrongSkill": "sales_analysis",
    "correctSkill": "knowledge_graph",
    "tenantId": "dabeinong"
  }
  ```
- **现象**: Java 调用 Python，Python 生成规则并返回，Java 将规则以 `PENDING` 状态落库。

### 场景二：人工审批让规则生效
- **请求**: `PUT http://localhost:8080/api/feedback/approve/1`
- **现象**: 数据库中 ID 为 1 的规则状态从 `PENDING` 变为 `ACTIVE`。

### 场景三：Prompt 动态回注 (自我进化)
- **请求**: `GET http://localhost:8080/api/feedback/chat?query=我这猪怎么不吃东西&tenantId=dabeinong`
- **现象**: 查看 IDEA 控制台，你会看到系统查出了刚才审批的规则，并将 `- 当用户提到不吃食时 务必路由到knowledge_graph` 强行注入到了最终的 Prompt 中。

## 💡 为什么采用这种架构？

1. **解决单机瓶颈**：Python 有 GIL 锁限制，不适合做高并发的主控网关。Java 作为主干，能完美抗住企业级的高并发请求。
2. **算法敏捷迭代**：AI 算法团队可以独立迭代 Python 微服务，不受 Java 业务系统发版周期的影响。
3. **金融级合规安全**：AI 生成的规则不能直接生效，必须经过 Java 侧的 `PENDING` 状态审批，杜绝大模型“乱说话”导致的业务风险。

## 📝 演进规划 (TODO)
- [ ] 将 H2 数据库替换为 MySQL，增加 MyBatis 拦截器实现底层多租户隔离。
- [ ] 引入 Redis，实现 Python 微服务的分布式限流与规则缓存。
- [ ] 结合 LangGraph，将 Java 主控升级为基于状态机的复杂多 Agent 协同。