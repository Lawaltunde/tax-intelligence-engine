package com.devlawal.wealthsimpleatie.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Strongly-typed binding for all ATIE-specific configuration properties.
 * Avoids magic strings and @Value annotations throughout the codebase.
 */
@ConfigurationProperties(prefix = "atie")
public record AtieProperties(
        Reconciliation reconciliation,
        Confidence confidence,
        Risk risk
) {

    public record Reconciliation(double mismatchThreshold) {}

    public record Confidence(double reviewThreshold) {}

    public record Risk(Map<String, Integer> weights) {

        public int incomeWeight()    { return weights.getOrDefault("income-mismatch", 20); }
        public int missingSlipWeight() { return weights.getOrDefault("missing-slip", 15); }
        public int deductionSpikeWeight() { return weights.getOrDefault("deduction-spike", 25); }
    }
}
