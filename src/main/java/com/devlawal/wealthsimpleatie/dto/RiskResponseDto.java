package com.devlawal.wealthsimpleatie.dto;

import com.devlawal.wealthsimpleatie.model.RiskLevel;

import java.util.List;

/**
 * API response DTO for the pre-filing risk assessment.
 */
public record RiskResponseDto(
        int riskScore,
        RiskLevel riskLevel,
        List<String> drivers
) {}
