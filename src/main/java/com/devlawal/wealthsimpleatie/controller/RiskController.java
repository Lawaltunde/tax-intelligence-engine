package com.devlawal.wealthsimpleatie.controller;

import com.devlawal.wealthsimpleatie.dto.RiskAssessmentRequestDto;
import com.devlawal.wealthsimpleatie.dto.RiskResponseDto;
import com.devlawal.wealthsimpleatie.model.ConfidenceScores;
import com.devlawal.wealthsimpleatie.model.RiskAssessment;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.service.RiskAssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Triggers pre-filing risk assessment.
 * No business logic — delegates entirely to RiskAssessmentService.
 */
@RestController
@RequestMapping("/api/v1/risk")
public class RiskController {

    private final RiskAssessmentService riskAssessmentService;

    public RiskController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    /**
     * POST /api/v1/risk/assess
     *
     * Evaluates pre-filing reassessment risk for the supplied taxpayer profile.
     * Set hasDiscrepancy=true if a prior reconciliation call returned any results.
     */
    @PostMapping("/assess")
    public ResponseEntity<RiskResponseDto> assess(@RequestBody RiskAssessmentRequestDto request) {

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

        RiskAssessment assessment = riskAssessmentService.assess(profile, request.hasDiscrepancy());

        return ResponseEntity.ok(new RiskResponseDto(
                assessment.riskScore(),
                assessment.riskLevel(),
                assessment.drivers()));
    }
}
