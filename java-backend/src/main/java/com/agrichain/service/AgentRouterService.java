package com.agrichain.service;

import com.agrichain.entity.AgentRule;
import com.agrichain.repository.AgentRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentRouterService {

    private final AgentRuleRepository repository;

    public void processUserQuery(String userQuery, String tenantId) {
        System.out.println("\n>>> [Java 主路由] 收到用户请求: " + userQuery);

        // 1. 从 H2 数据库读取当前租户所有已生效（ACTIVE）的规则
        List<AgentRule> activeRules = repository.findByTenantIdAndStatus(tenantId, "ACTIVE");

        String historyRules = activeRules.stream()
                .map(r -> "- " + r.getRuleContent())
                .collect(Collectors.joining("\n"));

        System.out.println(">>> [Java 主路由] 检索到 " + activeRules.size() + " 条生效规则。");

        // 2. 拼装最终的 Prompt（模拟企业级 Prompt 组装）
        String systemPrompt = """
                你是一个农牧供应链路由专家。请根据以下规则判断用户意图。
                
                【历史避坑规则（必须严格遵守）】
                %s
                
                请只输出意图（如 traceability, knowledge_graph）。
                """.formatted(historyRules.isEmpty() ? "暂无规则" : historyRules);

        System.out.println("========== 最终发往大模型的 Prompt ==========");
        System.out.println(systemPrompt);
        System.out.println("===========================================");

        // 3. 真实生产环境，这里会用 Spring AI 调大模型：
        // chatClient.prompt().system(systemPrompt).user(userQuery).call().content();
        // 这里为了演示，我们只打印出 Prompt，证明规则成功回注！
    }
}