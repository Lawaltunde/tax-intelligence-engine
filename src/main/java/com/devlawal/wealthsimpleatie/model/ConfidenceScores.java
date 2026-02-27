package com.devlawal.wealthsimpleatie.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Per-field confidence scores assigned by the AI extraction agent.
 * Values range 0.0–1.0. Fields below the configured review threshold
 * are flagged for human escalation.
 *
 * Stored as embedded columns inside the tax_profiles table.
 */
@Embeddable
public final class ConfidenceScores {

    @Column(name = "confidence_employment_income")
    private double employmentIncome;

    @Column(name = "confidence_rrsp_contribution")
    private double rrspContribution;

    @Column(name = "confidence_capital_gains")
    private double capitalGains;

    /** Required by JPA. */
    protected ConfidenceScores() {}

    public ConfidenceScores(double employmentIncome, double rrspContribution, double capitalGains) {
        this.employmentIncome = clamp(employmentIncome);
        this.rrspContribution = clamp(rrspContribution);
        this.capitalGains     = clamp(capitalGains);
    }

    public double employmentIncome() { return employmentIncome; }
    public double rrspContribution() { return rrspContribution; }
    public double capitalGains()     { return capitalGains; }

    /** Returns a fully-confident placeholder used when CRA data (ground truth) is the source. */
    public static ConfidenceScores defaultScores() {
        return new ConfidenceScores(1.0, 1.0, 1.0);
    }

    private static double clamp(double v) {
        if (!Double.isFinite(v)) return 0.0;   // guard against NaN / ±Infinity from LLM output
        return Math.max(0.0, Math.min(1.0, v));
    }

    @Override
    public String toString() {
        return "ConfidenceScores{employmentIncome=%.2f, rrspContribution=%.2f, capitalGains=%.2f}"
                .formatted(employmentIncome, rrspContribution, capitalGains);
    }
}
