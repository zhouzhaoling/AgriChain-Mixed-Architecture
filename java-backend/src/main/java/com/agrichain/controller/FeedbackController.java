package com.agrichain.controller;

import com.agrichain.entity.AgentRule;
import com.agrichain.repository.AgentRuleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final AgentRuleRepository repository;

    // 接收 BadCase 的请求体
    @Data
    public static class FeedbackRequest {
        private String userQuery;
        private String wrongSkill;
        private String correctSkill;
        private String tenantId;
    }

    @PostMapping("/submit")
    public String submitFeedback(@RequestBody FeedbackRequest request) {
        System.out.println("☕ [Java] 收到 BadCase 反馈，准备调用 Python...");
        System.out.println("租户: " + request.getTenantId() + ", 错误路由: " + request.getWrongSkill());

        // 👇 第一阶段，我们先用一个假规则存进去，验证 Java 链路。
        // 👇 第二阶段，我们会把这里换成 HTTP 调用 Python 服务！
        String mockRule = "当用户提到 " + request.getUserQuery() + " 时，务必路由到 " + request.getCorrectSkill();

        AgentRule rule = new AgentRule();
        rule.setTenantId(request.getTenantId());
        rule.setRuleContent(mockRule);
        rule.setStatus("PENDING"); // 默认待审批

        repository.save(rule);

        return "反馈已入库，规则等待审批。规则内容: " + mockRule;
    }

    // 提供一个查询接口，看看我们存进去了没
    @GetMapping("/rules")
    public List<AgentRule> getAllRules() {
        return repository.findAll();
    }
}