package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.model.AiDecisionLog;
import com.devlawal.wealthsimpleatie.model.DecisionType;
import com.devlawal.wealthsimpleatie.repository.AiDecisionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Centralised service for persisting and querying AI decision audit logs.
 *
 * All AI agents delegate here immediately after receiving an LLM response.
 * This ensures every AI output is traceable, timestamped, and reproducible —
 * satisfying the spec's Auditability & Compliance requirement.
 *
 * Design principle: fire-and-forget from the caller's perspective.
 * If logging fails, it must NOT propagate and break the main flow.
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AiDecisionLogRepository repository;

    public AuditLogService(AiDecisionLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Persists an AI decision log entry.
     * Swallows exceptions to prevent audit failures from disrupting the main flow.
     */
    public void record(AiDecisionLog entry) {
        // Capture profileId before entering the try block so the catch handler
        // can safely reference it even if entry is null or save() throws.
        String profileId = (entry != null) ? entry.profileId() : "<null-entry>";
        try {
            repository.save(entry);
            log.debug("AI decision logged: agent={}, profile={}, type={}",
                    entry.agentName(), profileId, entry.decisionType());
        } catch (Exception e) {
            // Audit failure must never break the calling service
            log.error("Failed to persist AI decision log for profile={}: {}",
                    profileId, e.getMessage(), e);
        }
    }

    /**
     * Returns the full audit trail for a profile, newest decision first.
     */
    public List<AiDecisionLog> getAuditTrail(String profileId) {
        return repository.findByProfileIdOrderByDecidedAtDesc(profileId);
    }

    /**
     * Returns audit entries filtered by decision type.
     */
    public List<AiDecisionLog> getAuditTrail(String profileId, DecisionType type) {
        return repository.findByProfileIdAndDecisionTypeOrderByDecidedAtDesc(profileId, type);
    }
}
