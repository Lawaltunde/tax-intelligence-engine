package com.devlawal.wealthsimpleatie.util;

import com.devlawal.wealthsimpleatie.exception.AiResponseParsingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Validates and sanitises raw LLM JSON responses before they are deserialized.
 *
 * Responsibilities:
 * - Strip markdown fences (```json ... ```) that some models include
 * - Assert the string is valid JSON
 * - Deserialize into the target type
 */
@Component
public class JsonResponseValidator {

    private final ObjectMapper objectMapper;

    public JsonResponseValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Cleans, validates, and deserializes an LLM JSON response.
     *
     * @param rawResponse raw string returned by the LLM
     * @param targetType  expected deserialization target
     * @param <T>         target type
     * @return deserialized object
     * @throws AiResponseParsingException if the response cannot be parsed
     */
    public <T> T parse(String rawResponse, Class<T> targetType) {
        String cleaned = stripMarkdownFences(rawResponse);
        assertValidJson(cleaned);

        try {
            return objectMapper.readValue(cleaned, targetType);
        } catch (JsonProcessingException ex) {
            throw new AiResponseParsingException(
                    "Failed to deserialize AI response into %s: %s".formatted(
                            targetType.getSimpleName(), ex.getMessage()), ex);
        }
    }

    // -----------------------------------------------------------------------

    private String stripMarkdownFences(String raw) {
        if (raw == null) return "";
        String trimmed = raw.strip();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence    = trimmed.lastIndexOf("```");
            if (firstNewline != -1 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).strip();
            }
        }
        return trimmed;
    }

    private void assertValidJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isObject()) {
                throw new AiResponseParsingException(
                        "AI response is valid JSON but is not a JSON object: " + json);
            }
        } catch (JsonProcessingException ex) {
            throw new AiResponseParsingException(
                    "AI response is not valid JSON: " + json, ex);
        }
    }
}
