package com.devlawal.wealthsimpleatie.repository;

import com.devlawal.wealthsimpleatie.model.TaxProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link TaxProfile} persistence.
 * Provides CRUD and query-by-field operations backed by the tax_profiles table.
 */
@Repository
public interface TaxProfileRepository extends JpaRepository<TaxProfile, String> {

    /** Returns all profiles for a given tax year. */
    List<TaxProfile> findByTaxYear(String taxYear);
}
