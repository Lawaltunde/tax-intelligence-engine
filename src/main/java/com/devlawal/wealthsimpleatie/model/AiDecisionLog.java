package com.devlawal.wealthsimpleatie.model;

import jakarta.persistence.*;
import java.time.Instant;
/**
 * Immutable audit record for every AI decision made by ATIE.
 *
 * Captures:
 *  - which AI agent produced the output (agentName)
 *  - what decision type it was (decisionType: EXTRACTION, RECONCILIATION, OPTIMIZATION, PROVINCIAL)
 *  - the profileId it relates to
 *  - the prompt sent to the LLM (truncated to 2000 chars for storage)
 *  - the raw LLM response
 *  - the model used (e.g. gpt-4o)
 *  - timestamp of the decision
 *  - confidence score if applicable (null if not relevant)
 *
 * This supports the spec requirement:
 *  "All AI outputs are logged, rule references are traceable,
 *   decision paths are reproducible, policy versions are timestamped."
 */
@Entity
@Table(name = "ai_decision_logs", indexes = {
        @Index(name = "idx_audit_profile", columnList = "profile_id"),
        @Index(name = "idx_audit_agent",   columnList = "agent_name"),
        @Index(name = "idx_audit_ts",      columnList = "decided_at")
})
public final class AiDecisionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false, length = 20)
    private DecisionType decisionType;

    @Column(name = "agent_name", nullable = false, length = 100)
    private String agentName;

    @Column(name = "model_used", nullable = false, length = 50)
    private String modelUsed;

    @Column(name = "prompt_summary", columnDefinition = "TEXT")
    private String promptSummary;

    @Column(name = "ai_output", columnDefinition = "TEXT", nullable = false)
    private String aiOutput;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "decided_at", nullable = false, updatable = false)
    private Instant decidedAt;

    /** Required by JPA. */
    protected AiDecisionLog() {}

    private AiDecisionLog(Builder b) {
        this.profileId       = b.profileId;
        this.decisionType    = b.decisionType;
        this.agentName       = b.agentName;
        this.modelUsed       = b.modelUsed;
        this.promptSummary   = truncate(b.promptSummary, 2000);
        this.aiOutput        = b.aiOutput;
        this.confidenceScore = b.confidenceScore;
        this.decidedAt       = Instant.now();
    }

    public Long id()                    { return id; }
    public String profileId()           { return profileId; }
    public DecisionType decisionType()  { return decisionType; }
    public String agentName()           { return agentName; }
    public String modelUsed()           { return modelUsed; }
    public String promptSummary()       { return promptSummary; }
    public String aiOutput()            { return aiOutput; }
    public Double confidenceScore()     { return confidenceScore; }
    public Instant decidedAt()          { return decidedAt; }

    public static Builder builder()     { return new Builder(); }

    public static final class Builder {
        private String profileId;
        private DecisionType decisionType;
        private String agentName;
        private String modelUsed = "gpt-4o";
        private String promptSummary;
        private String aiOutput;
        private Double confidenceScore;

        public Builder profileId(String v)           { this.profileId = v; return this; }
        public Builder decisionType(DecisionType v)  { this.decisionType = v; return this; }
        public Builder agentName(String v)           { this.agentName = v; return this; }
        public Builder modelUsed(String v)           { this.modelUsed = v; return this; }
        public Builder promptSummary(String v)       { this.promptSummary = v; return this; }
        public Builder aiOutput(String v)            { this.aiOutput = v; return this; }
        public Builder confidenceScore(Double v)     { this.confidenceScore = v; return this; }

        public AiDecisionLog build()                 { return new AiDecisionLog(this); }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
