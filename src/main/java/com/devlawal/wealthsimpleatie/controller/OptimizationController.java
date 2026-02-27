package com.devlawal.wealthsimpleatie.controller;

import com.devlawal.wealthsimpleatie.dto.OptimizationResponseDto;
import com.devlawal.wealthsimpleatie.model.OptimizationResult;
import com.devlawal.wealthsimpleatie.model.TaxProfile;
import com.devlawal.wealthsimpleatie.service.OptimizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes RRSP contribution optimization simulation.
 * No business logic — delegates entirely to OptimizationService.
 */
@Validated
@RestController
@RequestMapping("/api/v1/optimization")
public class OptimizationController {

    private final OptimizationService optimizationService;

    public OptimizationController(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    /**
     * POST /api/v1/optimization/rrsp
     *
     * Simulates the tax impact of an additional RRSP contribution.
     *
     * @param additionalContrib  proposed additional RRSP contribution in CAD (must be ≥ 0)
     * @param profile            taxpayer's current TaxProfile (request body)
     */
    @PostMapping("/rrsp")
    public ResponseEntity<OptimizationResponseDto> simulateRrsp(
            @RequestParam @Min(0) double additionalContrib,
            @Valid @RequestBody TaxProfile profile) {

        OptimizationResult result = optimizationService.simulateRrspContribution(profile, additionalContrib);

        return ResponseEntity.ok(new OptimizationResponseDto(
                result.additionalContribution(),
                result.currentTax(),
                result.optimizedTax(),
                result.estimatedSavings(),
                result.capitalLossCarryforwardApplied(),
                result.tuitionCreditApplied(),
                result.aiTradeoffExplanation()));
    }
}
