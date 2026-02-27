package com.devlawal.wealthsimpleatie.model;

/**
 * Result of simulating an RRSP contribution optimization.
 * All monetary fields are in Canadian dollars.
 * The AI layer may attach a tradeoff explanation; all numeric values are deterministic.
 */
public final class OptimizationResult {

    private final double additionalContribution;
    private final double currentTax;
    private final double optimizedTax;
    private final double estimatedSavings;
    /** Capital loss carry-forward that was applied to reduce capital gains this year. */
    private final double capitalLossCarryforwardApplied;
    /** Tuition credit (at 15% federal rate) that was applied to reduce federal tax. */
    private final double tuitionCreditApplied;
    private final String aiTradeoffExplanation;

    private OptimizationResult(Builder b) {
        this.additionalContribution         = b.additionalContribution;
        this.currentTax                     = b.currentTax;
        this.optimizedTax                   = b.optimizedTax;
        this.estimatedSavings               = b.estimatedSavings;
        this.capitalLossCarryforwardApplied = b.capitalLossCarryforwardApplied;
        this.tuitionCreditApplied           = b.tuitionCreditApplied;
        this.aiTradeoffExplanation          = b.aiTradeoffExplanation;
    }

    public double additionalContribution()           { return additionalContribution; }
    public double currentTax()                       { return currentTax; }
    public double optimizedTax()                     { return optimizedTax; }
    public double estimatedSavings()                 { return estimatedSavings; }
    public double capitalLossCarryforwardApplied()   { return capitalLossCarryforwardApplied; }
    public double tuitionCreditApplied()             { return tuitionCreditApplied; }
    public String aiTradeoffExplanation()            { return aiTradeoffExplanation; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private double additionalContribution;
        private double currentTax;
        private double optimizedTax;
        private double estimatedSavings;
        private double capitalLossCarryforwardApplied;
        private double tuitionCreditApplied;
        private String aiTradeoffExplanation;

        public Builder additionalContribution(double v)           { this.additionalContribution = v; return this; }
        public Builder currentTax(double v)                       { this.currentTax = v; return this; }
        public Builder optimizedTax(double v)                     { this.optimizedTax = v; return this; }
        public Builder estimatedSavings(double v)                 { this.estimatedSavings = v; return this; }
        public Builder capitalLossCarryforwardApplied(double v)   { this.capitalLossCarryforwardApplied = v; return this; }
        public Builder tuitionCreditApplied(double v)             { this.tuitionCreditApplied = v; return this; }
        public Builder aiTradeoffExplanation(String e)            { this.aiTradeoffExplanation = e; return this; }

        public OptimizationResult build() { return new OptimizationResult(this); }
    }
}
