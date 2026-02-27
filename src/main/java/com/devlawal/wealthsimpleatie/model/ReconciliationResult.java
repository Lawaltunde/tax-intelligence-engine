package com.devlawal.wealthsimpleatie.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * JPA entity for a reconciled field discrepancy.
 * Deterministic logic populates all numeric fields; the AI layer supplies aiExplanation only.
 *
 * Linked to a TaxProfile via profile_id foreign key (stored as a plain column to
 * keep the model decoupled — a join can be added in a future migration).
 */
@Entity
@Table(name = "reconciliation_results")
public final class ReconciliationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @Column(name = "field_name", nullable = false)
    private String field;

    @Column(name = "uploaded_value", nullable = false)
    private double uploadedValue;

    @Column(name = "cra_value", nullable = false)
    private double craValue;

    @Column(name = "delta", nullable = false)
    private double delta;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private DiscrepancySeverity severity;

    @Column(name = "requires_review", nullable = false)
    private boolean requiresReview;

    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Required by JPA. */
    protected ReconciliationResult() {}

    private ReconciliationResult(Builder b) {
        this.profileId      = b.profileId;
        this.field          = b.field;
        this.uploadedValue  = b.uploadedValue;
        this.craValue       = b.craValue;
        this.delta          = b.delta;
        this.severity       = b.severity;
        this.requiresReview = b.requiresReview;
        this.aiExplanation  = b.aiExplanation;
        this.createdAt      = Instant.now();
    }

    public Long id()                       { return id; }
    public String profileId()              { return profileId; }
    public String field()                  { return field; }
    public double uploadedValue()          { return uploadedValue; }
    public double craValue()               { return craValue; }
    public double delta()                  { return delta; }
    public DiscrepancySeverity severity()  { return severity; }
    public boolean requiresReview()        { return requiresReview; }
    public String aiExplanation()          { return aiExplanation; }
    public Instant createdAt()             { return createdAt; }

    public static Builder builder()        { return new Builder(); }

    public static final class Builder {
        private String profileId;
        private String field;
        private double uploadedValue;
        private double craValue;
        private double delta;
        private DiscrepancySeverity severity;
        private boolean requiresReview;
        private String aiExplanation;

        public Builder profileId(String v)               { this.profileId = v; return this; }
        public Builder field(String field)               { this.field = field; return this; }
        public Builder uploadedValue(double v)           { this.uploadedValue = v; return this; }
        public Builder craValue(double v)                { this.craValue = v; return this; }
        public Builder delta(double v)                   { this.delta = v; return this; }
        public Builder severity(DiscrepancySeverity s)   { this.severity = s; return this; }
        public Builder requiresReview(boolean r)         { this.requiresReview = r; return this; }
        public Builder aiExplanation(String e)           { this.aiExplanation = e; return this; }

        public ReconciliationResult build() {
            if (profileId == null || profileId.isBlank())
                throw new IllegalStateException("ReconciliationResult requires 'profileId'");
            if (field == null || field.isBlank())
                throw new IllegalStateException("ReconciliationResult requires 'field'");
            if (severity == null)
                throw new IllegalStateException("ReconciliationResult requires 'severity'");
            return new ReconciliationResult(this);
        }
    }
}
