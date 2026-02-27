package com.devlawal.wealthsimpleatie.service;

import com.devlawal.wealthsimpleatie.dto.ReviewItemDto;
import com.devlawal.wealthsimpleatie.model.ReconciliationResult;
import com.devlawal.wealthsimpleatie.repository.ReconciliationResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Aggregates all items that require human review across the system.
 *
 * Spec requirement:
 *  "Low-confidence extracted values and ambiguous deductions are surfaced to humans.
 *   Humans retain control over optimization tradeoffs and strategic contribution timing."
 *
 * Currently surfaces:
 *  1. Reconciliation discrepancies flagged requiresReview=true (MEDIUM + HIGH severity)
 *
 * Future extensions:
 *  2. TaxProfile fields with confidence score below atie.confidence.review-threshold
 *  3. Optimization scenarios where contribution room is ambiguous
 */
@Service
public class HumanReviewService {

    private static final Logger log = LoggerFactory.getLogger(HumanReviewService.class);

    private final ReconciliationResultRepository reconciliationResultRepository;

    public HumanReviewService(ReconciliationResultRepository reconciliationResultRepository) {
        this.reconciliationResultRepository = reconciliationResultRepository;
    }

    /**
     * Returns all pending review items for a specific profile.
     */
    public List<ReviewItemDto> getPendingReviewForProfile(String profileId) {
        List<ReconciliationResult> items =
                reconciliationResultRepository.findByProfileIdAndRequiresReviewTrue(profileId);
        log.info("Human review queue for profile={}: {} items", profileId, items.size());
        return items.stream().map(this::toReviewItem).toList();
    }

    /**
     * Returns ALL pending review items across all profiles (for compliance officers / admin).
     */
    public List<ReviewItemDto> getAllPendingReview() {
        List<ReconciliationResult> items =
                reconciliationResultRepository.findByRequiresReviewTrue();
        log.info("Global human review queue: {} items pending", items.size());
        return items.stream().map(this::toReviewItem).toList();
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    private ReviewItemDto toReviewItem(ReconciliationResult r) {
        return new ReviewItemDto(
                r.profileId(),
                "RECONCILIATION",
                r.field(),
                "Discrepancy of $%,.2f detected between uploaded slip ($%,.2f) and CRA ($%,.2f)"
                        .formatted(r.delta(), r.uploadedValue(), r.craValue()),
                r.severity(),
                r.aiExplanation(),
                r.createdAt()
        );
    }
}
