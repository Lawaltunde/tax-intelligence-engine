package com.devlawal.wealthsimpleatie.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.persistence.*;

/**
 * JPA entity representing a normalised tax profile built from all ingested sources.
 * All monetary values are in Canadian dollars.
 *
 * Persistence notes:
 * - profileId is the business-generated UUID primary key (not a DB sequence)
 * - ConfidenceScores is embedded (no separate table needed)
 */
@Entity
@Table(name = "tax_profiles")
@JsonDeserialize(builder = TaxProfile.Builder.class)
public final class TaxProfile {

    @Id
    @Column(name = "profile_id", nullable = false, updatable = false)
    private String profileId;

    @Column(name = "tax_year", nullable = false, length = 4)
    private String taxYear;

    @Column(name = "employment_income", nullable = false)
    private double employmentIncome;

    @Column(name = "rrsp_contribution", nullable = false)
    private double rrspContribution;

    @Column(name = "capital_gains", nullable = false)
    private double capitalGains;

    @Column(name = "rrsp_contribution_room")
    private double rrspContributionRoom;

    /**
     * Net capital losses carried forward from prior tax years (CRA Schedule 3).
     * Applied to reduce capital gains in the current year. Zero if none.
     */
    @Column(name = "capital_loss_carryforward")
    private double capitalLossCarryforward;

    /**
     * Unused tuition / education credits carried forward from prior years (CRA Schedule 11).
     * Applied as a non-refundable credit against federal tax. Zero if none.
     */
    @Column(name = "tuition_carryforward")
    private double tuitionCarryforward;

    /**
     * Total income from the prior tax year (line 15000 of prior-year T1).
     * Used by the risk engine to detect income spikes.
     */
    @Column(name = "prior_year_income")
    private double priorYearIncome;

    @Embedded
    private ConfidenceScores confidenceScores;

    /** Required by JPA — not for use in application code. */
    protected TaxProfile() {}

    private TaxProfile(Builder builder) {
        this.profileId                = builder.profileId;
        this.taxYear                  = builder.taxYear;
        this.employmentIncome         = builder.employmentIncome;
        this.rrspContribution         = builder.rrspContribution;
        this.capitalGains             = builder.capitalGains;
        this.rrspContributionRoom     = builder.rrspContributionRoom;
        this.capitalLossCarryforward  = builder.capitalLossCarryforward;
        this.tuitionCarryforward      = builder.tuitionCarryforward;
        this.priorYearIncome          = builder.priorYearIncome;
        this.confidenceScores         = builder.confidenceScores;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String profileId()                      { return profileId; }
    public String taxYear()                        { return taxYear; }
    public double employmentIncome()               { return employmentIncome; }
    public double rrspContribution()               { return rrspContribution; }
    public double capitalGains()                   { return capitalGains; }
    public double rrspContributionRoom()           { return rrspContributionRoom; }
    public double capitalLossCarryforward()        { return capitalLossCarryforward; }
    public double tuitionCarryforward()            { return tuitionCarryforward; }
    public double priorYearIncome()                { return priorYearIncome; }
    public ConfidenceScores confidenceScores()     { return confidenceScores; }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String profileId;
        private String taxYear;
        private double employmentIncome;
        private double rrspContribution;
        private double capitalGains;
        private double rrspContributionRoom;
        private double capitalLossCarryforward;
        private double tuitionCarryforward;
        private double priorYearIncome;
        private ConfidenceScores confidenceScores = ConfidenceScores.defaultScores();

        public Builder profileId(String profileId)                             { this.profileId = profileId; return this; }
        public Builder taxYear(String taxYear)                                 { this.taxYear = taxYear; return this; }
        public Builder employmentIncome(double employmentIncome)               { this.employmentIncome = employmentIncome; return this; }
        public Builder rrspContribution(double rrspContribution)               { this.rrspContribution = rrspContribution; return this; }
        public Builder capitalGains(double capitalGains)                       { this.capitalGains = capitalGains; return this; }
        public Builder rrspContributionRoom(double room)                       { this.rrspContributionRoom = room; return this; }
        public Builder capitalLossCarryforward(double capitalLossCarryforward) { this.capitalLossCarryforward = capitalLossCarryforward; return this; }
        public Builder tuitionCarryforward(double tuitionCarryforward)         { this.tuitionCarryforward = tuitionCarryforward; return this; }
        public Builder priorYearIncome(double priorYearIncome)                 { this.priorYearIncome = priorYearIncome; return this; }
        public Builder confidenceScores(ConfidenceScores confidenceScores)     { this.confidenceScores = confidenceScores; return this; }

        public TaxProfile build() { return new TaxProfile(this); }
    }

    @Override
    public String toString() {
        return "TaxProfile{profileId='%s', taxYear='%s', employmentIncome=%.2f, rrspContribution=%.2f, capitalGains=%.2f}"
                .formatted(profileId, taxYear, employmentIncome, rrspContribution, capitalGains);
    }
}
