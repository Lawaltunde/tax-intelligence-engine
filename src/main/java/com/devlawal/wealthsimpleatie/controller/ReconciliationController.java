package com.devlawal.wealthsimpleatie.controller;

import com.devlawal.wealthsimpleatie.dto.CraImportDto;
import com.devlawal.wealthsimpleatie.dto.ReconciliationRequestDto;
import com.devlawal.wealthsimpleatie.dto.ReconciliationResponseDto;
import com.devlawal.wealthsimpleatie.model.ConfidenceScores;
import com.devlawal.wealthsimpleatie.model.ReconciliationResult;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.service.CraImportService;
import com.devlawal.wealthsimpleatie.service.ReconciliationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Triggers reconciliation between a supplied TaxProfile and live CRA data.
 * No business logic — delegates entirely to ReconciliationService.
 */
@RestController
@RequestMapping("/api/v1/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final CraImportService craImportService;

    public ReconciliationController(ReconciliationService reconciliationService,
                                    CraImportService craImportService) {
        this.reconciliationService = reconciliationService;
        this.craImportService      = craImportService;
    }

    /**
     * POST /api/v1/reconciliation/run
     *
     * Compares the taxpayer's uploaded data against CRA-reported data and returns
     * a list of detected discrepancies with AI-generated explanations.
     * The SIN is in the request body (not the URL) to avoid logging sensitive data.
     */
    @PostMapping("/run")
    public ResponseEntity<List<ReconciliationResponseDto>> run(
            @Valid @RequestBody ReconciliationRequestDto request) {

        // Reconstruct the domain TaxProfile from the DTO
        TaxProfile profile = TaxProfile.builder()
                .profileId(request.profileId())
                .taxYear(request.taxYear())
                .employmentIncome(request.employmentIncome())
                .rrspContribution(request.rrspContribution())
                .capitalGains(request.capitalGains())
                .rrspContributionRoom(request.rrspContributionRoom())
                .capitalLossCarryforward(request.capitalLossCarryforward())
                .tuitionCarryforward(request.tuitionCarryforward())
                .priorYearIncome(request.priorYearIncome())
                .confidenceScores(request.confidenceScores() != null
                        ? request.confidenceScores()
                        : ConfidenceScores.defaultScores())
                .build();

        CraImportDto cra = craImportService.importForTaxpayer(request.sin(), request.taxYear());
        List<ReconciliationResult> results = reconciliationService.reconcile(profile, cra);

        List<ReconciliationResponseDto> response = results.stream()
                .map(r -> new ReconciliationResponseDto(
                        r.field(),
                        r.uploadedValue(),
                        r.craValue(),
                        r.delta(),
                        r.severity(),
                        r.requiresReview(),
                        r.aiExplanation()))
                .toList();

        return ResponseEntity.ok(response);
    }
}
