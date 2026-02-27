package com.devlawal.wealthsimpleatie.controller;

import com.devlawal.wealthsimpleatie.dto.AuditLogDto;
import com.devlawal.wealthsimpleatie.dto.ReviewItemDto;
import com.devlawal.wealthsimpleatie.model.DecisionType;
import com.devlawal.wealthsimpleatie.service.AuditLogService;
import com.devlawal.wealthsimpleatie.service.HumanReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Human review and audit trail endpoints.
 *
 * GET /api/v1/review/pending
 *   Returns ALL reconciliation items requiring human review across all profiles.
 *
 * GET /api/v1/review/pending/{profileId}
 *   Returns pending review items for a specific tax profile.
 *
 * GET /api/v1/review/audit/{profileId}
 *   Returns the full AI decision audit trail for a profile (newest first).
 *
 * GET /api/v1/review/audit/{profileId}?type=RECONCILIATION
 *   Filters the audit trail by decision type.
 */
@RestController
@RequestMapping("/api/v1/review")
public class ReviewController {

    private final HumanReviewService humanReviewService;
    private final AuditLogService auditLogService;

    public ReviewController(HumanReviewService humanReviewService,
                            AuditLogService auditLogService) {
        this.humanReviewService = humanReviewService;
        this.auditLogService    = auditLogService;
    }

    /** All pending review items across every profile. */
    @GetMapping("/pending")
    public ResponseEntity<List<ReviewItemDto>> getAllPending() {
        return ResponseEntity.ok(humanReviewService.getAllPendingReview());
    }

    /** Pending review items for a specific profile. */
    @GetMapping("/pending/{profileId}")
    public ResponseEntity<List<ReviewItemDto>> getPendingForProfile(
            @PathVariable String profileId) {
        return ResponseEntity.ok(humanReviewService.getPendingReviewForProfile(profileId));
    }

    /** Full AI decision audit trail for a profile, optionally filtered by decision type. */
    @GetMapping("/audit/{profileId}")
    public ResponseEntity<List<AuditLogDto>> getAuditTrail(
            @PathVariable String profileId,
            @RequestParam(required = false) DecisionType type) {

        List<AuditLogDto> trail = (type != null
                ? auditLogService.getAuditTrail(profileId, type)
                : auditLogService.getAuditTrail(profileId))
                .stream()
                .map(AuditLogDto::from)
                .toList();

        return ResponseEntity.ok(trail);
    }
}
