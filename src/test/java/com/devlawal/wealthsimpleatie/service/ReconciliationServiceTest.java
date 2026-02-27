package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.ai.ReconciliationExplanationAgent;
import com.devlawal.wealthsimpleatie.config.AtieProperties;
import com.devlawal.wealthsimpleatie.dto.CraImportDto;
import com.devlawal.wealthsimpleatie.model.DiscrepancySeverity;
import com.devlawal.wealthsimpleatie.model.ReconciliationResult;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.repository.ReconciliationResultRepository;
import com.devlawal.wealthsimpleatie.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReconciliationService")
class ReconciliationServiceTest {

    @Mock private ReconciliationExplanationAgent explanationAgent;
    @Mock private ReconciliationResultRepository reconciliationResultRepository;
    @Mock private AuditLogService auditLogService;

    private ReconciliationService reconciliationService;

    @BeforeEach
    void setUp() {
        AtieProperties properties = new AtieProperties(
                new AtieProperties.Reconciliation(100.0),
                new AtieProperties.Confidence(0.80),
                new AtieProperties.Risk(Map.of(
                        "income-mismatch", 20, "missing-slip", 15, "deduction-spike", 25))
        );
        reconciliationService = new ReconciliationService(
                explanationAgent, properties, reconciliationResultRepository, auditLogService);

        // lenient: stub is only invoked when a discrepancy is detected
        lenient().when(explanationAgent.explain(anyString(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn("Likely amended slip not yet processed by CRA.");
    }

    @Test
    @DisplayName("returns empty list when all values are within threshold")
    void noDiscrepancyWhenWithinThreshold() {
        TaxProfile uploaded = TaxProfile.builder()
                .profileId("test-profile-1")
                .employmentIncome(48_200.00)
                .rrspContribution(3_500.00)
                .capitalGains(1_200.00)
                .build();

        CraImportDto cra = CraImportDto.mock(48_200.00, 3_500.00, 1_200.00);

        List<ReconciliationResult> results = reconciliationService.reconcile(uploaded, cra);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("detects employment income discrepancy above threshold")
    void detectsIncomeMismatch() {
        TaxProfile uploaded = TaxProfile.builder()
                .profileId("test-profile-2")
                .employmentIncome(52_000.00)
                .rrspContribution(3_500.00)
                .capitalGains(1_200.00)
                .build();

        CraImportDto cra = CraImportDto.mock(48_200.00, 3_500.00, 1_200.00);

        List<ReconciliationResult> results = reconciliationService.reconcile(uploaded, cra);

        assertThat(results).hasSize(1);
        ReconciliationResult result = results.get(0);
        assertThat(result.field()).isEqualTo("employmentIncome");
        assertThat(result.uploadedValue()).isEqualTo(52_000.00);
        assertThat(result.craValue()).isEqualTo(48_200.00);
        assertThat(result.delta()).isEqualTo(3_800.00);
        assertThat(result.requiresReview()).isTrue();
    }

    @Test
    @DisplayName("classifies HIGH severity for large percentage discrepancy")
    void classifiesHighSeverityForLargeDelta() {
        TaxProfile uploaded = TaxProfile.builder()
                .profileId("test-profile-3")
                .employmentIncome(80_000.00)
                .rrspContribution(3_500.00)
                .capitalGains(0.00)
                .build();

        CraImportDto cra = CraImportDto.mock(48_200.00, 3_500.00, 0.00);

        List<ReconciliationResult> results = reconciliationService.reconcile(uploaded, cra);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).severity()).isEqualTo(DiscrepancySeverity.HIGH);
    }

    @Test
    @DisplayName("classifies LOW severity for small percentage discrepancy")
    void classifiesLowSeverityForSmallDelta() {
        // 2% difference — below 5% LOW threshold
        TaxProfile uploaded = TaxProfile.builder()
                .profileId("test-profile-4")
                .employmentIncome(49_200.00)
                .rrspContribution(3_500.00)
                .capitalGains(0.00)
                .build();

        CraImportDto cra = CraImportDto.mock(48_200.00, 3_500.00, 0.00);

        List<ReconciliationResult> results = reconciliationService.reconcile(uploaded, cra);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).severity()).isEqualTo(DiscrepancySeverity.LOW);
    }

    @Test
    @DisplayName("detects multiple field discrepancies simultaneously")
    void detectsMultipleFieldDiscrepancies() {
        TaxProfile uploaded = TaxProfile.builder()
                .profileId("test-profile-5")
                .employmentIncome(55_000.00)
                .rrspContribution(5_000.00)
                .capitalGains(2_500.00)
                .build();

        CraImportDto cra = CraImportDto.mock(48_200.00, 3_500.00, 1_200.00);

        List<ReconciliationResult> results = reconciliationService.reconcile(uploaded, cra);

        assertThat(results).hasSize(3);
    }
}
