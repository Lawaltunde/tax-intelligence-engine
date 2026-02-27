package com.devlawal.wealthsimpleatie.dto;

import com.devlawal.wealthsimpleatie.model.AiDecisionLog;
import com.devlawal.wealthsimpleatie.model.DecisionType;

import java.time.Instant;

/**
 * API response DTO for a single AI decision audit log entry.
 * Prevents exposing the raw JPA entity to the frontend.
 */
public record AuditLogDto(
        Long id,
        String profileId,
        DecisionType decisionType,
        String agentName,
        String promptSummary,
        String aiOutput,
        Double confidenceScore,
        Instant decidedAt
) {
    public static AuditLogDto from(AiDecisionLog log) {
        return new AuditLogDto(
                log.id(),
                log.profileId(),
                log.decisionType(),
                log.agentName(),
                log.promptSummary(),
                log.aiOutput(),
                log.confidenceScore(),
                log.decidedAt()
        );
    }
}
