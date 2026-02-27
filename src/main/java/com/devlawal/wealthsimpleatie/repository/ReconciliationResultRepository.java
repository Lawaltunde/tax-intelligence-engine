package com.devlawal.wealthsimpleatie.repository;

import com.devlawal.wealthsimpleatie.model.ReconciliationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link ReconciliationResult} persistence.
 * Backed by the reconciliation_results table.
 */
@Repository
public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, Long> {

    /** Returns all reconciliation results associated with a given tax profile. */
    List<ReconciliationResult> findByProfileId(String profileId);

    /** Returns only the results that require human review. */
    List<ReconciliationResult> findByProfileIdAndRequiresReviewTrue(String profileId);

    /** Returns ALL items requiring review across all profiles (for the global review queue). */
    List<ReconciliationResult> findByRequiresReviewTrue();
}
