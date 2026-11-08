package com.agrichain.service;

import com.agrichain.entity.AgentRule;
import com.agrichain.repository.AgentRuleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentRouterService {

    private final AgentRuleRepository repository;

    // 👇 1. 注入 Spring 自带的 HTTP 客户端
    private final RestTemplate restTemplate = new RestTemplate();

    // 👇 2. 定义发给 Python 的数据结构（必须和 Python 的 Pydantic 模型匹配）
    @Data
    public static class RouteRequest {
        private String user_query;
        private String skills_info; // 我们直接把拼装好的 Prompt 当作信息发给 Python
    }

    @Data
    public static class RouteResponse {
        private String intent;
    }

    public void processUserQuery(String userQuery, String tenantId) {
        System.out.println("\n>>> [Java 主路由] 收到用户请求: " + userQuery);

        // 1. 从 H2 数据库读取当前租户所有已生效（ACTIVE）的规则
        List<AgentRule> activeRules = repository.findByTenantIdAndStatus(tenantId, "ACTIVE");

        String historyRules = activeRules.stream()
                .map(r -> "- " + r.getRuleContent())
                .collect(Collectors.joining("\n"));

        System.out.println(">>> [Java 主路由] 检索到 " + activeRules.size() + " 条生效规则。");

        // 2. 拼装最终的 Prompt（包含避坑规则）
        String systemPrompt = """
                你是一个农牧供应链路由专家。请根据以下规则判断用户意图。
                
                【历史避坑规则（必须严格遵守）】
                %s
                
                请只输出意图（如 traceability, knowledge_graph）。
                """.formatted(historyRules.isEmpty() ? "暂无规则" : historyRules);

        System.out.println("========== 最终发往大模型的 Prompt ==========");
        System.out.println(systemPrompt);
        System.out.println("===========================================");

        // 3. 👇 核心：发起跨语言调用，让 Python 做决策
        String url = "http://127.0.0.1:8000/api/route";

        RouteRequest request = new RouteRequest();
        request.setUser_query(userQuery);
        request.setSkills_info(systemPrompt); // 把带规则的 Prompt 传给 Python 微服务

        System.out.println("☕ [Java 主路由] 正在跨语言调用 Python 路由服务...");

        try {
            // 发送 POST 请求给 Python
            RouteResponse response = restTemplate.postForObject(url, request, RouteResponse.class);

            if (response != null && response.getIntent() != null) {
                System.out.println("✅ [Java 主路由] Python 决策出的 Skill: " + response.getIntent());

                // 真实生产环境这里会继续拿着 intent 去执行具体逻辑
                // executeSkill(response.getIntent(), userQuery);
            } else {
                System.out.println("❌ [Java 主路由] Python 返回为空");
            }
        } catch (Exception e) {
            System.err.println("❌ [Java 主路由] 调用 Python 微服务失败: " + e.getMessage());
        }
    }
}