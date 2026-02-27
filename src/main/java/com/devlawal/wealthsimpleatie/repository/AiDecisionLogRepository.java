package com.devlawal.wealthsimpleatie.repository;

import com.devlawal.wealthsimpleatie.model.AiDecisionLog;
import com.devlawal.wealthsimpleatie.model.DecisionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link AiDecisionLog}.
 * Backed by the ai_decision_logs table.
 */
@Repository
public interface AiDecisionLogRepository extends JpaRepository<AiDecisionLog, Long> {

    /** Full audit trail for a given tax profile, newest first. */
    List<AiDecisionLog> findByProfileIdOrderByDecidedAtDesc(String profileId);

    /** Filter by decision type (e.g. all RECONCILIATION decisions). */
    List<AiDecisionLog> findByProfileIdAndDecisionTypeOrderByDecidedAtDesc(
            String profileId, DecisionType decisionType);
}
