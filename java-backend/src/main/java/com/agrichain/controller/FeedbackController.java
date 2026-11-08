package com.agrichain.controller;

import com.agrichain.entity.AgentRule;
import com.agrichain.repository.AgentRuleRepository;
import com.agrichain.service.AgentRouterService;
import com.agrichain.service.PythonReflectService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final AgentRuleRepository repository;
    private final PythonReflectService pythonReflectService;

    // 👇 新增：注入我们刚才写的主路由服务
    private final AgentRouterService agentRouterService;

    @Data
    public static class FeedbackRequest {
        private String userQuery;
        private String wrongSkill;
        private String correctSkill;
        private String tenantId;
    }

    // 1. 接收 BadCase 反馈，调用 Python 提炼规则
    @PostMapping("/submit")
    public String submitFeedback(@RequestBody FeedbackRequest request) {
        System.out.println("\n☕ [Java] 收到 BadCase 反馈，准备调用 Python...");

        String realRule = pythonReflectService.getReflectedRule(
                request.getUserQuery(),
                request.getWrongSkill(),
                request.getCorrectSkill(),
                request.getTenantId()
        );

        AgentRule rule = new AgentRule();
        rule.setTenantId(request.getTenantId());
        rule.setRuleContent(realRule);
        rule.setStatus("PENDING"); // 默认待审批

        repository.save(rule);

        return "反馈已入库，规则等待审批。真实规则内容: " + realRule;
    }

    // 2. 查询所有规则（用于查看状态）
    @GetMapping("/rules")
    public List<AgentRule> getAllRules() {
        return repository.findAll();
    }

    // 👇 3. 新增：模拟人工审批，将 PENDING 改为 ACTIVE
    @PutMapping("/approve/{id}")
    public String approveRule(@PathVariable Long id) {
        AgentRule rule = repository.findById(id).orElse(null);
        if (rule != null) {
            rule.setStatus("ACTIVE");
            repository.save(rule);
            return "✅ 规则已审批通过，开始生效！内容: " + rule.getRuleContent();
        }
        return "❌ 规则不存在";
    }

    // 👇 4. 新增：模拟用户提问，触发规则回注（去控制台看最终拼装的 Prompt）
    @GetMapping("/chat")
    public String chat(@RequestParam String query, @RequestParam String tenantId) {
        agentRouterService.processUserQuery(query, tenantId);
        return "Prompt 已组装完毕，请看 IDEA 控制台！";
    }
}