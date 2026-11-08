package com.agrichain.controller;

import com.agrichain.entity.AgentRule;
import com.agrichain.repository.AgentRuleRepository;
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

    // 👇 注入我们刚写的 Python 调用服务
    private final PythonReflectService pythonReflectService;

    @Data
    public static class FeedbackRequest {
        private String userQuery;
        private String wrongSkill;
        private String correctSkill;
        private String tenantId;
    }

    @PostMapping("/submit")
    public String submitFeedback(@RequestBody FeedbackRequest request) {
        System.out.println("\n☕ [Java] 收到 BadCase 反馈，准备调用 Python...");

        // 👇 核心改造：删掉 mockRule，真实调用 Python 微服务！
        String realRule = pythonReflectService.getReflectedRule(
                request.getUserQuery(),
                request.getWrongSkill(),
                request.getCorrectSkill(),
                request.getTenantId()
        );

        // 将 Python 返回的真实规则存入 H2 数据库
        AgentRule rule = new AgentRule();
        rule.setTenantId(request.getTenantId());
        rule.setRuleContent(realRule);
        rule.setStatus("PENDING"); // 默认待审批

        repository.save(rule);

        return "反馈已入库，规则等待审批。真实规则内容: " + realRule;
    }

    @GetMapping("/rules")
    public List<AgentRule> getAllRules() {
        return repository.findAll();
    }
}