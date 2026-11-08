package com.agrichain.repository;

import com.agrichain.entity.AgentRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRuleRepository extends JpaRepository<AgentRule, Long> {

    // 根据租户和状态查询规则（未来回注时用）
    List<AgentRule> findByTenantIdAndStatus(String tenantId, String status);
}