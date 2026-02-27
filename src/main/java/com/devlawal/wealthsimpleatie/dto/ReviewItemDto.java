package com.devlawal.wealthsimpleatie.dto;

import com.devlawal.wealthsimpleatie.model.DiscrepancySeverity;

import java.time.Instant;

/**
 * A single item surfaced to the human review queue.
 *
 * @param profileId     the tax profile this item belongs to
 * @param itemType      "RECONCILIATION" or "EXTRACTION_LOW_CONFIDENCE"
 * @param field         the specific field requiring review
 * @param description   plain-language description of why review is needed
 * @param severity      severity level (for reconciliation items)
 * @param aiExplanation the AI-generated explanation (if any)
 * @param createdAt     when the item was created (from DB record)
 */
public record ReviewItemDto(
        String profileId,
        String itemType,
        String field,
        String description,
        DiscrepancySeverity severity,
        String aiExplanation,
        Instant createdAt
) {}
