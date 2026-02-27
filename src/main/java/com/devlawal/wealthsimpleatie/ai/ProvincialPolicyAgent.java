package com.devlawal.wealthsimpleatie.ai;

import com.devlawal.wealthsimpleatie.model.Province;
import com.devlawal.wealthsimpleatie.util.JsonResponseValidator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI agent for provincial tax policy reasoning.
 *
 * Boundary:
 * ✅ LLM responsibility  — identifying applicable provincial credits, explaining eligibility
 *                          rules in plain language, generating strategic recommendations
 * ❌ LLM responsibility  — computing tax amounts, applying bracket rates, numeric calculations
 *
 * The deterministic ProvincialTaxCalculator handles all numeric computations.
 * This agent adds the interpretive reasoning layer on top.
 */
@Component
public class ProvincialPolicyAgent {

    private static final Logger log = LoggerFactory.getLogger(ProvincialPolicyAgent.class);

    private static final String POLICY_PROMPT = """
            You are an expert Canadian provincial tax advisor specialising in %s tax law for the %s tax year.

            Taxpayer profile:
            - Employment income:   $%,.2f
            - RRSP contribution:   $%,.2f
            - Capital gains:       $%,.2f
            - Tuition amount:      $%,.2f
            - Has dependents:      %s
            - Is a student:        %s
            - Province:            %s

            Provincial tax computed (before credits): $%,.2f

            Your task:
            1. Identify ALL provincial tax credits and deductions this taxpayer may be eligible for.
            2. Estimate the total credit value in Canadian dollars where possible.
            3. Provide a plain-language explanation of the key provincial rules applied.
            4. List strategic recommendations to minimise provincial tax.

            Return ONLY a valid JSON object. No markdown, no commentary.

            {
              "eligibleCredits": ["<credit name and brief eligibility note>", ...],
              "estimatedCreditsTotal": <number>,
              "policyExplanation": "<2-3 sentence plain English explanation of provincial rules applied>",
              "recommendations": ["<actionable recommendation>", ...]
            }
            """;

    private final ChatClient chatClient;
    private final JsonResponseValidator validator;

    public ProvincialPolicyAgent(ChatClient chatClient, JsonResponseValidator validator) {
        this.chatClient = chatClient;
        this.validator  = validator;
    }

    /**
     * Analyses the taxpayer's provincial eligibility and returns AI-reasoned credits,
     * explanation, and strategic recommendations.
     */
    public ProvincialPolicyResponse analyse(
            Province province,
            double employmentIncome,
            double rrspContribution,
            double capitalGains,
            double tuitionAmount,
            boolean hasDependents,
            boolean isStudent,
            double provincialTaxPayable,
            String taxYear) {

        String prompt = POLICY_PROMPT.formatted(
                province.displayName(), taxYear,
                employmentIncome, rrspContribution, capitalGains, tuitionAmount,
                hasDependents ? "Yes" : "No",
                isStudent ? "Yes" : "No",
                province.displayName(),
                provincialTaxPayable);

        log.debug("Requesting provincial policy analysis for province={}, taxYear={}", province, taxYear);

        String rawResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        log.debug("Raw AI provincial response: {}", rawResponse);

        return validator.parse(rawResponse, ProvincialPolicyResponse.class);
    }

    // ── Structured response record ────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProvincialPolicyResponse(
            List<String> eligibleCredits,
            double estimatedCreditsTotal,
            String policyExplanation,
            List<String> recommendations
    ) {}
}
