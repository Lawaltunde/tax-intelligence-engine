package com.devlawal.wealthsimpleatie.repository;

import com.devlawal.wealthsimpleatie.model.RiskAssessment;
import com.devlawal.wealthsimpleatie.model.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link RiskAssessment} persistence.
 * Backed by the risk_assessments table.
 */
@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    /** Returns the most recent risk assessment for a given profile (if any). */
    Optional<RiskAssessment> findTopByProfileIdOrderByIdDesc(String profileId);

    /** Returns all high-risk assessments — useful for reporting / audit queries. */
    List<RiskAssessment> findByRiskLevel(RiskLevel riskLevel);
}
