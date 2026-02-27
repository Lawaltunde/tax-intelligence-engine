package com.devlawal.wealthsimpleatie.controller;

import com.devlawal.wealthsimpleatie.dto.ProvincialPolicyRequest;
import com.devlawal.wealthsimpleatie.dto.ProvincialPolicyResponse;
import com.devlawal.wealthsimpleatie.service.ProvincialPolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Provincial tax policy reasoning endpoint.
 *
 * POST /api/v1/provincial/analyse
 *   Accepts a taxpayer profile with province and returns:
 *   - Deterministic provincial tax payable (from bracket tables)
 *   - AI-identified applicable credits with estimated values
 *   - Net provincial tax after credits
 *   - Plain-language policy explanation
 *   - Strategic recommendations
 *
 * Example request:
 * {
 *   "profileId":        "profile-001",
 *   "province":         "ON",
 *   "employmentIncome": 95000,
 *   "rrspContribution": 18000,
 *   "capitalGains":     5000,
 *   "tuitionAmount":    8500,
 *   "hasDependents":    false,
 *   "isStudent":        true,
 *   "taxYear":          "2024"
 * }
 */
@RestController
@RequestMapping("/api/v1/provincial")
public class ProvincialPolicyController {

    private final ProvincialPolicyService provincialPolicyService;

    public ProvincialPolicyController(ProvincialPolicyService provincialPolicyService) {
        this.provincialPolicyService = provincialPolicyService;
    }

    @PostMapping("/analyse")
    public ResponseEntity<ProvincialPolicyResponse> analyse(
            @RequestBody ProvincialPolicyRequest request) {

        ProvincialPolicyResponse response = provincialPolicyService.analyse(request);
        return ResponseEntity.ok(response);
    }
}
