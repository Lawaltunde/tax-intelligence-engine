package com.devlawal.wealthsimpleatie.ai;

import com.devlawal.wealthsimpleatie.dto.ExtractedTaxFieldsDto;
import com.devlawal.wealthsimpleatie.exception.DocumentExtractionException;
import com.devlawal.wealthsimpleatie.util.JsonResponseValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * AI agent responsible for extracting structured tax fields from raw PDF text.
 *
 * Boundary:
 * ✅ LLM responsibility  — field extraction, confidence scoring
 * ❌ LLM responsibility  — numeric comparisons, tax calculations, risk scoring
 */
@Component
public class TaxSlipExtractionAgent {

    private static final Logger log = LoggerFactory.getLogger(TaxSlipExtractionAgent.class);

    private static final String EXTRACTION_PROMPT = """
            You are a Canadian tax document parser.
            Extract structured tax fields from the raw document text below.

            Return ONLY strictly valid JSON matching this exact schema. No explanations, no markdown.

            {
              "employmentIncome": <number or null>,
              "rrspContribution": <number or null>,
              "capitalGains": <number or null>,
              "confidence": {
                "employmentIncome": <0.0–1.0>,
                "rrspContribution": <0.0–1.0>,
                "capitalGains": <0.0–1.0>
              }
            }

            Rules:
            - Use null for any field not found in the document.
            - Set confidence to 0.0 for null fields.
            - Do not hallucinate values. Extract only what is explicitly present.

            Document text:
            ---
            %s
            ---
            """;

    private final ChatClient chatClient;
    private final JsonResponseValidator validator;

    public TaxSlipExtractionAgent(ChatClient chatClient, JsonResponseValidator validator) {
        this.chatClient = chatClient;
        this.validator  = validator;
    }

    /**
     * Sends the extracted PDF text to the LLM and parses the structured response.
     *
     * @param documentText plain text extracted from the uploaded PDF
     * @return structured tax fields with per-field confidence scores
     */
    public ExtractedTaxFieldsDto extract(String documentText) {
        if (documentText == null || documentText.isBlank()) {
            throw new DocumentExtractionException(
                    "Cannot extract tax fields: document text is null or blank.");
        }

        String prompt = EXTRACTION_PROMPT.formatted(documentText);

        log.debug("Sending tax extraction prompt ({} chars)", prompt.length());

        String rawResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // Log only size metadata — raw response may contain extracted financial values
        log.debug("AI extraction response received ({} chars)", rawResponse == null ? 0 : rawResponse.length());

        return validator.parse(rawResponse, ExtractedTaxFieldsDto.class);
    }
}
