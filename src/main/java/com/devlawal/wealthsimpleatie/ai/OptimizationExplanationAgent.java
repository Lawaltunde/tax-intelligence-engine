package com.devlawal.wealthsimpleatie.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.devlawal.wealthsimpleatie.exception.AiResponseParsingException;
import com.devlawal.wealthsimpleatie.util.JsonResponseValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * AI agent that generates human-readable RRSP optimization tradeoff explanations.
 *
 * Boundary:
 * ✅ LLM responsibility  — plain-English tradeoff explanation, liquidity considerations
 * ❌ LLM responsibility  — tax calculations, bracket comparisons, savings amounts
 */
@Component
public class OptimizationExplanationAgent {

    private static final Logger log = LoggerFactory.getLogger(OptimizationExplanationAgent.class);

    private static final String EXPLANATION_PROMPT = """
            You are a Canadian financial planning advisor.

            A taxpayer is considering an additional RRSP contribution with the following impact:
            - Additional contribution: $%,.2f
            - Current federal tax:     $%,.2f
            - Optimized federal tax:   $%,.2f
            - Estimated tax savings:   $%,.2f
            - Marginal rate:           %.1f%%

            Provide a single JSON object explaining the key tradeoffs in plain English.
            Consider: short-term cash flow impact, long-term RRSP growth, liquidity constraints.
            Return ONLY valid JSON. No markdown. No commentary.

            {
              "tradeoffExplanation": "<2–3 sentence plain English explanation>"
            }
            """;

    private final ChatClient chatClient;
    private final JsonResponseValidator validator;

    public OptimizationExplanationAgent(ChatClient chatClient, JsonResponseValidator validator) {
        this.chatClient = chatClient;
        this.validator  = validator;
    }

    /**
     * Generates a tradeoff explanation for the computed RRSP optimization scenario.
     */
    public String explain(double additionalContribution, double currentTax,
                          double optimizedTax, double savings, double marginalRate) {

        String prompt = EXPLANATION_PROMPT.formatted(
                additionalContribution, currentTax, optimizedTax, savings, marginalRate * 100);

        log.debug("Requesting optimization tradeoff explanation");

        String rawResponse = null;
        try {
            rawResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("Raw AI optimization explanation ({} chars)", rawResponse == null ? 0 : rawResponse.length());

            TradeoffResponse response = validator.parse(rawResponse, TradeoffResponse.class);
            return response.tradeoffExplanation() != null
                    ? response.tradeoffExplanation()
                    : "No explanation available.";

        } catch (AiResponseParsingException ex) {
            log.warn("Optimization explanation parse failed: {}", ex.getMessage());
            log.debug("Unparseable AI response: {}", rawResponse);
            return "No explanation available.";

        } catch (Exception ex) {
            log.error("Optimization explanation AI call failed: {}", ex.getMessage(), ex);
            return "No explanation available.";
        }
    }

    // ── Internal response record ─────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TradeoffResponse(String tradeoffExplanation) {}
}
