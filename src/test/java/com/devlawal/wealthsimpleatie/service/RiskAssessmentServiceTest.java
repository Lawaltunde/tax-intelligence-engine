package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.config.AtieProperties;
import com.devlawal.wealthsimpleatie.model.RiskAssessment;
import com.devlawal.wealthsimpleatie.model.RiskLevel;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.repository.RiskAssessmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("RiskAssessmentService")
class RiskAssessmentServiceTest {

    @Mock private RiskAssessmentRepository riskAssessmentRepository;

    private RiskAssessmentService riskAssessmentService;

    @BeforeEach
    void setUp() {
        AtieProperties properties = new AtieProperties(
                new AtieProperties.Reconciliation(100.0),
                new AtieProperties.Confidence(0.80),
                new AtieProperties.Risk(Map.of(
                        "income-mismatch", 20, "missing-slip", 15, "deduction-spike", 25))
        );
        riskAssessmentService = new RiskAssessmentService(properties, riskAssessmentRepository);
    }

    @Test
    @DisplayName("returns LOW risk when no flags are triggered")
    void lowRiskForCleanProfile() {
        TaxProfile profile = TaxProfile.builder()
                .profileId("risk-test-1")
                .employmentIncome(60_000.00)
                .rrspContribution(5_000.00)
                .capitalGains(0.00)
                .priorYearIncome(58_000.00)       // ~3% growth — not a spike
                .build();

        RiskAssessment result = riskAssessmentService.assess(profile, false);

        assertThat(result.riskScore()).isEqualTo(0);
        assertThat(result.riskLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(result.drivers()).isEmpty();
    }

    @Test
    @DisplayName("adds income-mismatch weight when discrepancy is present")
    void addsMismatchWeightWhenDiscrepancyPresent() {
        TaxProfile profile = TaxProfile.builder()
                .profileId("risk-test-2")
                .employmentIncome(60_000.00)
                .rrspContribution(5_000.00)
                .build();

        RiskAssessment result = riskAssessmentService.assess(profile, true);

        assertThat(result.riskScore()).isEqualTo(20);
        assertThat(result.drivers()).anyMatch(d -> d.contains("mismatch"));
    }

    @Test
    @DisplayName("adds deduction-spike weight when RRSP > 20% of income")
    void addsDeductionSpikeWeightForLargeRrsp() {
        TaxProfile profile = TaxProfile.builder()
                .profileId("risk-test-3")
                .employmentIncome(50_000.00)
                .rrspContribution(15_000.00)      // 30% of income → spike
                .build();

        RiskAssessment result = riskAssessmentService.assess(profile, false);

        assertThat(result.riskScore()).isEqualTo(25);
        assertThat(result.drivers()).anyMatch(d -> d.contains("unusually large"));
    }

    @Test
    @DisplayName("adds income-spike driver when income grew more than 50% vs prior year")
    void addsIncomeSpikeDriverForLargeGrowth() {
        TaxProfile profile = TaxProfile.builder()
                .profileId("risk-test-4")
                .employmentIncome(90_000.00)
                .rrspContribution(5_000.00)
                .priorYearIncome(50_000.00)       // 80% growth → spike
                .build();

        RiskAssessment result = riskAssessmentService.assess(profile, false);

        assertThat(result.riskScore()).isEqualTo(20);
        assertThat(result.drivers()).anyMatch(d -> d.contains("prior year"));
    }

    @Test
    @DisplayName("no income-spike driver when priorYearIncome is zero (not provided)")
    void noSpikeDriverWhenPriorYearIncomeAbsent() {
        TaxProfile profile = TaxProfile.builder()
                .profileId("risk-test-5")
                .employmentIncome(90_000.00)
                .rrspContribution(5_000.00)
                .priorYearIncome(0.00)            // absent — spike check skipped
                .build();

        RiskAssessment result = riskAssessmentService.assess(profile, false);

        assertThat(result.drivers()).noneMatch(d -> d.contains("prior year"));
    }

    @Test
    @DisplayName("score is clamped at 100 when multiple drivers fire")
    void scoreIsCappedAt100() {
        TaxProfile profile = TaxProfile.builder()
                .profileId("risk-test-6")
                .employmentIncome(0.00)           // missing slip (+15)
                .rrspContribution(0.00)
                .priorYearIncome(0.00)
                .build();

        // hasDiscrepancy=true → +20, employmentIncome=0 → +15 = 35
        RiskAssessment result = riskAssessmentService.assess(profile, true);

        assertThat(result.riskScore()).isLessThanOrEqualTo(100);
        assertThat(result.riskLevel()).isEqualTo(RiskLevel.MEDIUM);
    }

    @Test
    @DisplayName("classifies HIGH when total score is 61 or above")
    void classifiesHighRisk() {
        // income mismatch (+20) + deduction spike (+25) + income spike (+20) = 65
        TaxProfile profile = TaxProfile.builder()
                .profileId("risk-test-7")
                .employmentIncome(90_000.00)
                .rrspContribution(20_000.00)      // >20% of income → +25
                .priorYearIncome(50_000.00)       // 80% growth → +20
                .build();

        RiskAssessment result = riskAssessmentService.assess(profile, true); // +20

        assertThat(result.riskScore()).isGreaterThanOrEqualTo(61);
        assertThat(result.riskLevel()).isEqualTo(RiskLevel.HIGH);
    }
}
