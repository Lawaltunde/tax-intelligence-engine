package com.devlawal.wealthsimpleatie.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.devlawal.wealthsimpleatie.exception.AiResponseParsingException;
import com.devlawal.wealthsimpleatie.util.JsonResponseValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * AI agent responsible for generating human-readable reconciliation explanations.
 *
 * Boundary:
 * ✅ LLM responsibility  — natural-language explanation of discrepancy, hypothesis generation
 * ❌ LLM responsibility  — numeric delta computation, severity classification, risk scoring
 */
@Component
public class ReconciliationExplanationAgent {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationExplanationAgent.class);

    private static final String EXPLANATION_PROMPT = """
            You are a Canadian tax compliance expert.

            A discrepancy has been detected in the following tax field:
            - Field:           %s
            - Uploaded value:  $%,.2f (source: taxpayer's uploaded slip)
            - CRA value:       $%,.2f (source: CRA Auto-fill)
            - Delta:           $%,.2f

            Provide a single, concise JSON object with your explanation.
            Return ONLY valid JSON. No markdown. No commentary.

            {
              "explanation": "<one-sentence plain English explanation of the most likely cause>"
            }
            """;

    private final ChatClient chatClient;
    private final JsonResponseValidator validator;

    public ReconciliationExplanationAgent(ChatClient chatClient, JsonResponseValidator validator) {
        this.chatClient = chatClient;
        this.validator  = validator;
    }

    /**
     * Generates a plain-English explanation for a detected field discrepancy.
     *
     * @param field          the tax field name (e.g. "employmentIncome")
     * @param uploadedValue  value from the taxpayer's uploaded slip
     * @param craValue       value reported by CRA
     * @param delta          absolute difference
     * @return one-sentence human-readable explanation
     */
    public String explain(String field, double uploadedValue, double craValue, double delta) {
        String prompt = EXPLANATION_PROMPT.formatted(field, uploadedValue, craValue, delta);

        log.debug("Requesting reconciliation explanation for field '{}'", field);

        String rawResponse = null;
        try {
            rawResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("Raw AI reconciliation response: {}", rawResponse);

            ExplanationResponse response = validator.parse(rawResponse, ExplanationResponse.class);
            return response.explanation() != null ? response.explanation() : "No explanation available.";

        } catch (AiResponseParsingException ex) {
            // LLM returned malformed or non-JSON output — recoverable; reconciliation can still proceed.
            log.warn("AI explanation parse failed for field '{}': {}", field, ex.getMessage());
            log.debug("Unparseable AI response for field '{}': {}", field, rawResponse);
            return "No explanation available.";

        } catch (Exception ex) {
            // Network error, auth failure, rate limit, or other unexpected failure.
            // Log at ERROR so operators are alerted, but allow reconciliation to complete.
            log.error("AI explanation call failed for field '{}': {}", field, ex.getMessage(), ex);
            return "No explanation available.";
        }
    }

    // ── Internal response record ─────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ExplanationResponse(String explanation) {}
}
