package com.devlawal.wealthsimpleatie.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for a pre-filing risk profile.
 * Score range 0–100; level derived from score thresholds.
 *
 * Drivers are stored as a single TEXT column (pipe-separated, '|' delimiter) to keep
 * the schema simple. A separate join table can be introduced when needed.
 */
@Entity
@Table(name = "risk_assessments")
public final class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 10)
    private RiskLevel riskLevel;

    /** Stored as delimited text; parsed back to list on read. */
    @Column(name = "drivers", columnDefinition = "TEXT")
    private String driversRaw;

    /** Required by JPA. */
    protected RiskAssessment() {}

    private RiskAssessment(Builder b) {
        this.profileId   = b.profileId;
        this.riskScore   = b.riskScore;
        this.riskLevel   = b.riskLevel;
        this.driversRaw  = String.join("|", b.drivers);
    }

    public Long id()                { return id; }
    public String profileId()       { return profileId; }
    public int riskScore()          { return riskScore; }
    public RiskLevel riskLevel()    { return riskLevel; }

    public List<String> drivers() {
        if (driversRaw == null || driversRaw.isBlank()) return List.of();
        return List.of(driversRaw.split("\\|"));
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String profileId;
        private int riskScore;
        private RiskLevel riskLevel;
        private List<String> drivers = new ArrayList<>();

        public Builder profileId(String v)          { this.profileId = v; return this; }
        public Builder riskScore(int score)         { this.riskScore = score; return this; }
        public Builder riskLevel(RiskLevel level)   { this.riskLevel = level; return this; }
        public Builder drivers(List<String> drivers) {
            this.drivers = drivers != null ? drivers : new ArrayList<>();
            return this;
        }

        public RiskAssessment build() { return new RiskAssessment(this); }
    }
}
