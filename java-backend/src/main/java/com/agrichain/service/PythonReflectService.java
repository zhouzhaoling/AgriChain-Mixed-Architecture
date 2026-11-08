package com.agrichain.service;

import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class PythonReflectService {

    // Spring 自带的 HTTP 客户端
    private final RestTemplate restTemplate = new RestTemplate();

    // 定义发给 Python 的数据结构
    @Data
    public static class ReflectRequest {
        private String user_query;
        private String wrong_skill;
        private String correct_skill;
        private String tenant_id;
    }

    // 定义接收 Python 返回的数据结构
    @Data
    public static class ReflectResponse {
        private String status;
        private String new_rule;
    }

    /**
     * 调用 Python 微服务进行反思
     */
    public String getReflectedRule(String userQuery, String wrongSkill, String correctSkill, String tenantId) {
        String url = "http://127.0.0.1:8000/api/reflect";

        ReflectRequest request = new ReflectRequest();
        request.setUser_query(userQuery);
        request.setWrong_skill(wrongSkill);
        request.setCorrect_skill(correctSkill);
        request.setTenant_id(tenantId);

        System.out.println("☕ [Java] 正在跨语言调用 Python 微服务: " + url);

        try {
            // 发送 POST 请求给 Python，并映射返回的 JSON
            ReflectResponse response = restTemplate.postForObject(url, request, ReflectResponse.class);

            if (response != null && "success".equals(response.getStatus())) {
                System.out.println("☕ [Java] 成功收到 Python 生成的规则: " + response.getNew_rule());
                return response.getNew_rule();
            }
        } catch (Exception e) {
            System.err.println("❌ [Java] 调用 Python 微服务失败: " + e.getMessage());
        }

        // 兜底策略：如果 Python 挂了，返回一个默认规则，保证 Java 主流程不崩
        return "当用户提到未知错误时，请人工介入处理。";
    }
}