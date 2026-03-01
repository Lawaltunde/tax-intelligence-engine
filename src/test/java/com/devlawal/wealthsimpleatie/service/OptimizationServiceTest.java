package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.ai.OptimizationExplanationAgent;
import com.devlawal.wealthsimpleatie.model.OptimizationResult;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OptimizationService")
class OptimizationServiceTest {

    @Mock private OptimizationExplanationAgent explanationAgent;
    @Mock private AuditLogService auditLogService;

    private OptimizationService optimizationService;

    @BeforeEach
    void setUp() {
        optimizationService = new OptimizationService(explanationAgent, auditLogService);
        when(explanationAgent.explain(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn("Contributing an additional $5,000 to your RRSP reduces your taxable income.");
    }

    @Test
    @DisplayName("computes positive savings for a valid additional RRSP contribution")
    void computesPositiveSavings() {
        // RRSP room = $14,400 (18% of $80k prior year), existing contribution = $3,000
        // → remaining room = $11,400 ≥ $5,000 requested, so full $5,000 is effective
        TaxProfile profile = TaxProfile.builder()
                .profileId("test-profile-001")
                .employmentIncome(80_000.00)
                .rrspContribution(3_000.00)
                .rrspContributionRoom(14_400.00)
                .capitalGains(0.00)
                .build();

        OptimizationResult result = optimizationService.simulateRrspContribution(profile, 5_000.00);

        assertThat(result.currentTax()).isGreaterThan(0);
        assertThat(result.optimizedTax()).isLessThan(result.currentTax());
        assertThat(result.estimatedSavings()).isGreaterThan(0);
        assertThat(result.additionalContribution()).isEqualTo(5_000.00);
    }

    @Test
    @DisplayName("returns zero savings for zero additional contribution")
    void zeroSavingsForZeroContribution() {
        TaxProfile profile = TaxProfile.builder()
                .profileId("test-profile-001")
                .employmentIncome(80_000.00)
                .rrspContribution(3_000.00)
                .capitalGains(0.00)
                .build();

        OptimizationResult result = optimizationService.simulateRrspContribution(profile, 0.00);

        assertThat(result.estimatedSavings()).isEqualTo(0.0);
        assertThat(result.currentTax()).isEqualTo(result.optimizedTax());
    }

    @Test
    @DisplayName("taxable income does not go below zero when contribution exceeds income")
    void taxableIncomeFloorAtZero() {
        TaxProfile profile = TaxProfile.builder()
                .profileId("test-profile-001")
                .employmentIncome(5_000.00)
                .rrspContribution(0.00)
                .capitalGains(0.00)
                .build();

        OptimizationResult result = optimizationService.simulateRrspContribution(profile, 100_000.00);

        assertThat(result.optimizedTax()).isEqualTo(0.0);
        assertThat(result.estimatedSavings()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    @DisplayName("optimization result contains AI tradeoff explanation")
    void resultContainsAiExplanation() {
        TaxProfile profile = TaxProfile.builder()
                .profileId("test-profile-001")
                .employmentIncome(75_000.00)
                .rrspContribution(2_000.00)
                .capitalGains(0.00)
                .build();

        OptimizationResult result = optimizationService.simulateRrspContribution(profile, 5_000.00);

        assertThat(result.aiTradeoffExplanation()).isNotBlank();
    }

    @Test
    @DisplayName("capital loss carryforward reduces taxable capital gains")
    void capitalLossCarryforwardReducesTax() {
        // Without carryforward: 10,000 capital gains * 50% = $5,000 taxable → more tax
        TaxProfile withoutCarryforward = TaxProfile.builder()
                .profileId("test-profile-001")
                .employmentIncome(70_000.00)
                .rrspContribution(0.00)
                .capitalGains(10_000.00)
                .capitalLossCarryforward(0.00)
                .build();

        // With full carryforward: capital gains are fully offset → no taxable capital gains
        TaxProfile withCarryforward = TaxProfile.builder()
                .profileId("test-profile-002")
                .employmentIncome(70_000.00)
                .rrspContribution(0.00)
                .capitalGains(10_000.00)
                .capitalLossCarryforward(10_000.00)
                .build();

        OptimizationResult without = optimizationService.simulateRrspContribution(withoutCarryforward, 0.00);
        OptimizationResult with    = optimizationService.simulateRrspContribution(withCarryforward, 0.00);

        assertThat(with.currentTax()).isLessThan(without.currentTax());
        assertThat(with.capitalLossCarryforwardApplied()).isEqualTo(10_000.00);
    }

    @Test
    @DisplayName("tuition carryforward applies 15% non-refundable credit against federal tax")
    void tuitionCarryforwardReducesTax() {
        // $10,000 tuition carryforward → $1,500 federal credit (10,000 * 15%)
        TaxProfile withTuition = TaxProfile.builder()
                .profileId("test-profile-001")
                .employmentIncome(60_000.00)
                .rrspContribution(0.00)
                .capitalGains(0.00)
                .tuitionCarryforward(10_000.00)
                .build();

        TaxProfile withoutTuition = TaxProfile.builder()
                .profileId("test-profile-002")
                .employmentIncome(60_000.00)
                .rrspContribution(0.00)
                .capitalGains(0.00)
                .tuitionCarryforward(0.00)
                .build();

        OptimizationResult with    = optimizationService.simulateRrspContribution(withTuition, 0.00);
        OptimizationResult without = optimizationService.simulateRrspContribution(withoutTuition, 0.00);

        assertThat(with.currentTax()).isLessThan(without.currentTax());
        assertThat(without.currentTax() - with.currentTax()).isEqualTo(1_500.00);
        assertThat(with.tuitionCreditApplied()).isEqualTo(1_500.00);
    }
}
